/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.namespace;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class models dependencies between versions.
 * <p>
 * Based on an order of namespaces and an unordered collection of {@link NamespaceVersion} and its dependencies it may
 * provide a list of those versions via {@link #getItems(Collection, Collection)}.
 */
public class NamespaceVersionedModel<T extends INamespaceVersioned> {

  private static final Logger LOG = LoggerFactory.getLogger(NamespaceVersionedModel.class);

  // the items map contains for all known names an entry
  //   -  each entry contains at least an empty list
  //   -  each list is sorted ascending according to 'version'
  private final Map<String, List<T>> m_items;
  private final Map<T, Set<T>> m_dependencyModel;
  // used if ordering by dependencies not sufficient / stable
  private final Comparator<INamespaceVersioned> m_secondaryComparator;

  protected NamespaceVersionedModel(Builder<T> builder) {
    this(builder, new P_ByNameComparator(builder.getNames()));
  }

  protected NamespaceVersionedModel(Builder<T> builder, Comparator<INamespaceVersioned> secondaryComparator) {
    m_secondaryComparator = assertNotNull(secondaryComparator);
    m_items = initItems(builder);
    m_dependencyModel = initDependencyModel(builder);
    validateDependencyModel();
  }

  protected int compareVersion(NamespaceVersion o1, NamespaceVersion o2) {
    return NamespaceVersion.compareVersion(o1, o2);
  }

  protected Map<String, List<T>> initItems(Builder<T> builder) {
    Set<String> knownNames = new HashSet<>(builder.getNames());

    // collect
    Map<String, List<T>> items = builder.getItems().stream()
        .peek(i -> validateItem(i, knownNames))
        .collect(Collectors.groupingBy(i -> i.getVersion().getNamespace()));

    // sort
    Comparator<T> comparator = Comparator.comparing(T::getVersion, this::compareVersion);
    items.values().forEach(list -> list.sort(comparator));
    // add for all known names without a migration an empty list
    knownNames.removeAll(items.keySet());
    knownNames.forEach(n -> items.put(n, Collections.emptyList()));

    return items;
  }

  protected void validateItem(T item, Set<String> knownNames) {
    assertNotNull(item.getVersion(), "Version must be set - {}", item);
    assertTrue(knownNames.contains(item.getVersion().getNamespace()), "{} declares unknown name {}", item, item.getVersion().getNamespace());

    Set<String> seenNames = new HashSet<>();
    List<String> duplicateNames = item.getDependencies().stream()
        .map(NamespaceVersion::getNamespace).filter(n -> !seenNames.add(n)).collect(Collectors.toList());
    assertTrue(duplicateNames.isEmpty(), "{} has multiple dependencies with the same name", item);
    assertFalse(seenNames.stream().anyMatch(n -> !knownNames.contains(n)), "{} declares dependency to unknown name", item);
  }

  protected Map<T, Set<T>> initDependencyModel(Builder builder) {
    Map<T, Set<T>> collector = new HashMap<>();

    collectDirectDependencies(collector);

    Map<T, Set<T>> itemDependencies = new HashMap<>(collector.size());
    for (Entry<T, Set<T>> entry : collector.entrySet()) {
      itemDependencies.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }
    collectTransitiveDependencies(collector, itemDependencies);
    for (Entry<T, Set<T>> entry : collector.entrySet()) { // update itemDependencies from collector
      itemDependencies.get(entry.getKey()).addAll(entry.getValue());
    }
    collectTransitiveRootToPreviousDependencies(collector, itemDependencies);
    for (Entry<T, Set<T>> entry : collector.entrySet()) { // update itemDependencies from collector
      itemDependencies.get(entry.getKey()).addAll(entry.getValue());
    }
    collectPreviousItemTransitiveDependencies(collector, itemDependencies);
    return collector;
  }

  protected void collectDirectDependencies(Map<T, Set<T>> collector) {
    for (List<T> items : m_items.values()) {
      T previousItem = null;
      for (T item : items) {
        Set<T> dependencies = new HashSet<>();
        collector.put(item, dependencies);

        // add implicit dependency from last item within same name
        if (previousItem != null) {
          dependencies.add(previousItem);
        }
        previousItem = item;

        // add dependencies
        for (NamespaceVersion dependencyVersion : item.getDependencies()) {
          if (!m_items.getOrDefault(dependencyVersion.getNamespace(), Collections.emptyList()).isEmpty()) { // if there is no migration yet for a given name we can safely ignore dependency
            Optional<T> dependency = getItem(dependencyVersion);
            dependencies.add(dependency.orElseThrow(() -> new AssertionException("Unresolvable dependency {} for {} - change required version or add missing item", dependencyVersion, item)));
          }
        }
      }
    }
  }

  protected void collectTransitiveDependencies(Map<T, Set<T>> collector, Map<T, Set<T>> itemDependencies) {
    Set<T> visited = new HashSet<>();
    Deque<T> queue = new ArrayDeque<>();
    for (Entry<T, Set<T>> e : itemDependencies.entrySet()) {
      T item = e.getKey();
      Set<T> depsDirect = e.getValue();

      Set<T> dependencies = collector.get(item);

      // add transitive dependencies
      //   If item A depends on B and B depends on C then A depends on C, BUT only for names we have no dependency yet
      //   breath-first search where it is important that only the first visited dependency of a namespace is considered
      visited.clear();
      visited.addAll(depsDirect);
      queue.clear();
      queue.addAll(depsDirect);
      while (!queue.isEmpty()) {
        T i = queue.pop();

        for (T td : itemDependencies.get(i)) {
          if (!visited.add(td)) {
            continue;
          }

          NamespaceVersion v = td.getVersion();
          boolean hasLaterVersion = false;
          for (Iterator<T> it = dependencies.iterator(); it.hasNext();) {
            T dtd = it.next();
            NamespaceVersion nv = dtd.getVersion();
            if (!nv.namespaceEquals(v)) {
              continue;
            }
            if (NamespaceVersion.compareVersion(nv, v) > 0) {
              hasLaterVersion = true;
            }
            else {
              it.remove();
            }
          }
          if (!hasLaterVersion) {
            dependencies.add(td);
            queue.add(td);
          }
        }
      }
    }
  }

  protected void collectTransitiveRootToPreviousDependencies(Map<T, Set<T>> collector, Map<T, Set<T>> itemDependencies) {
    // for an item X, find for each item their dependency root, add on these roots a dependency to previous item of item X
    Set<T> visited = new HashSet<>();
    Set<T> dependencyRoots = new HashSet<>();
    for (Entry<String, List<T>> entry : m_items.entrySet()) {
      List<T> items = entry.getValue();
      for (T item : items) {
        // find dependency root
        dependencyRoots.clear();
        visited.clear();
        collectDependencyRoots(dependencyRoots, itemDependencies, item, visited);
        for (T dependencyRoot : dependencyRoots) {
          Optional<T> previous = getPrevious(item.getVersion());
          if (previous.isPresent() && !dependsOnRecursive(previous.get(), dependencyRoot, itemDependencies)) {
            collector.get(dependencyRoot).add(previous.get());
          }
        }
      }
    }
  }

  protected boolean collectDependencyRoots(Set<T> dependencyRoots, Map<T, Set<T>> itemDependencies, T item, Set<T> visited) {
    boolean added = false;
    for (T i : itemDependencies.get(item)) {
      boolean notSeen = visited.add(i);
      if (!item.getVersion().getNamespace().equals(i.getVersion().getNamespace())) {
        if (notSeen) {
          if (!collectDependencyRoots(dependencyRoots, itemDependencies, i, visited)) {
            dependencyRoots.add(i);
          }
        }
        added = true;
      }
    }
    return added;
  }

  protected boolean dependsOnRecursive(T item, T dependency, Map<T, Set<T>> itemDependencies) {
    Set<T> visited = new HashSet<>();
    ArrayDeque<T> work = new ArrayDeque<>();
    while (item != null) {
      visited.add(item);
      if (item.equals(dependency)) {
        return true;
      }
      itemDependencies.get(item).stream().filter(Predicate.not(visited::contains)).forEach(work::add);
      item = work.pollLast();
    }
    return false;
  }

  protected void collectPreviousItemTransitiveDependencies(Map<T, Set<T>> collector, Map<T, Set<T>> itemDependencies) {
    for (Entry<T, Set<T>> e : itemDependencies.entrySet()) {
      T item = e.getKey();
      Set<T> dependencies = collector.get(item);

      // add all items which depend on previous item with same name (except itself)
      Optional<T> previousItem = getPrevious(item.getVersion());
      if (previousItem.isEmpty()) {
        continue;
      }
      for (Entry<T, Set<T>> entry : itemDependencies.entrySet()) {
        if (entry.getValue().contains(previousItem.get()) && !item.equals(entry.getKey()) /* ignore itself */) {
          dependencies.add(entry.getKey());
        }
      }
    }
  }

  protected void validateDependencyModel() {
    List<NamespaceVersion> fromVersions = m_items.keySet().stream().map(namespace -> NamespaceVersion.of(namespace, "0")).collect(Collectors.toList());
    List<NamespaceVersion> toVersions = m_items.values().stream().map(CollectionUtility::lastElement).filter(Objects::nonNull).map(INamespaceVersioned::getVersion).collect(Collectors.toList());

    // call getItems on full version range to detect any dependency cycles
    VersionedItems<T> items = getItems(fromVersions, toVersions);
    assertTrue(items.getUnsatisfiedDependencies().isEmpty());
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loaded versioned items:\n{}\ndependency model:\n{}", items, dependencyModelToString(m_dependencyModel));
    }
  }

  protected List<T> getItemsInternal(String name) {
    return m_items.getOrDefault(name, Collections.emptyList());
  }

  protected Optional<T> findLastMatching(String name, Predicate<T> predicate) {
    List<T> items = getItemsInternal(name);
    Iterator<T> it = items.iterator();
    T result = null;
    T item = result;
    while (it.hasNext() && (item == null || predicate.test(item))) {
      result = item;
      item = it.next();
    }
    // last element may also match predicate
    if (!it.hasNext() && item != null && predicate.test(item)) {
      result = item;
    }
    return Optional.ofNullable(result);
  }

  protected List<T> getItems(NamespaceVersion fromVersion, NamespaceVersion toVersion) {
    assertTrue(toVersion == null || compareVersion(fromVersion, toVersion) < 0);
    List<T> items = getItemsInternal(fromVersion.getNamespace());
    List<T> result = new ArrayList<>();
    for (T item : items) {
      if (toVersion != null && compareVersion(item.getVersion(), toVersion) > 0) {
        break;
      }
      else if (compareVersion(item.getVersion(), fromVersion) > 0) {
        result.add(item);
      }
    }
    return result;
  }

  protected Set<T> getItemsUnordered(Collection<NamespaceVersion> fromVersions, Collection<NamespaceVersion> toVersions) {
    validateFromToVersions(fromVersions, toVersions);

    // if fromVersion did not (yet) exists, it is assumed no item is required; see also orderByDependencies
    Set<T> items = null; // lazy allocated - if versions valid should not allocate resources
    for (NamespaceVersion fromVersion : fromVersions) {
      NamespaceVersion toVersion = toVersions.stream().filter(fromVersion::namespaceEquals).findFirst().orElse(null);
      if (toVersion == null || compareVersion(fromVersion, toVersion) < 0) {
        if (items == null) {
          items = new HashSet<>();
        }
        items.addAll(getItems(fromVersion, toVersion));
      }
    }

    return items != null ? items : Collections.emptySet();
  }

  /**
   * Validates that namespace is unique per version.
   */
  protected void validateFromToVersions(Collection<NamespaceVersion> fromVersions, Collection<NamespaceVersion> toVersions) {
    List<String> duplicateNamespacesErrors = new ArrayList<>();

    Set<String> fromVersionNamespaces = new HashSet<>();
    for (NamespaceVersion version : fromVersions) {
      if (!fromVersionNamespaces.add(version.getNamespace())) {
        duplicateNamespacesErrors.add("Duplicate namespace " + version.getNamespace() + " (fromVersion)");
      }
    }

    Set<String> toVersionNamespaces = new HashSet<>();
    for (NamespaceVersion version : toVersions) {
      if (!toVersionNamespaces.add(version.getNamespace())) {
        duplicateNamespacesErrors.add("Duplicate namespace " + version.getNamespace() + " (toVersion)");
      }
    }

    if (!duplicateNamespacesErrors.isEmpty()) {
      throw new AssertionException("{}\nfromVersions: {}\ntoVersions: {}", StringUtility.join("\n", duplicateNamespacesErrors), fromVersions, toVersions);
    }
  }

  /**
   * This method retrieves all items. The items are ordered according to their dependencies.
   *
   * @return non-null {@link VersionedItems}
   */
  public VersionedItems<T> getItems() {
    Set<T> allItemsUnordered = m_items.values().stream().flatMap(List::stream).collect(Collectors.toSet());
    return orderByDependencies(sortByName(allItemsUnordered), null);
  }

  /**
   * This method retrieves all items which are in the given version range. The items are ordered according to their
   * dependencies. Even if the from- and to-versions are not equal, no item at all might be returned.
   * <p>
   * Any namespace which is contained only in {@code toVersions} is ignored. (It is assumed that in such a case nothing
   * has to be migrated (as not yet used)).
   * <p>
   * For any namespace in {@code fromVersions} which is missing in {@code toVersions} all items from this namespace with
   * a higher version is returned. (It is assumed that in such a case this namespace was deleted and the last migration
   * contains migration code for the old namespace.)
   * <p>
   * The versions in {@code toVersions} might be <em>incompatible</em>. In such a case the unsatisfiable items are also
   * returned.
   *
   * @param fromVersions
   *          not null without null elements
   * @param toVersions
   *          not null without null elements
   * @return non-null {@link VersionedItems}
   */
  public VersionedItems<T> getItems(Collection<NamespaceVersion> fromVersions, Collection<NamespaceVersion> toVersions) {
    return sort(getItemsUnordered(fromVersions, toVersions), fromVersions);
  }

  /**
   * @param name
   *          non-null valid version name
   * @return non-null modifiable list with all items for the given version name.
   */
  public List<T> getAllItems(String name) {
    return new ArrayList<>(getItemsInternal(name));
  }

  /**
   * @return item for given version
   */
  public Optional<T> getItem(NamespaceVersion version) {
    if (version == null) {
      return Optional.empty();
    }
    return findLastMatching(version.getNamespace(), i -> compareVersion(i.getVersion(), version) <= 0)
        .filter(i -> i.getVersion().equals(version)); // ensure exact match
  }

  /**
   * @return previous item for the given version
   */
  public Optional<T> getPrevious(NamespaceVersion version) {
    if (version == null) {
      return Optional.empty();
    }
    return findLastMatching(version.getNamespace(), i -> compareVersion(i.getVersion(), version) < 0);
  }

  protected Stream<T> getDependentItems(T item) {
    Set<T> dependentItems = m_dependencyModel.get(item);
    return dependentItems != null ? dependentItems.stream() : Stream.empty();
  }

  protected VersionedItems<T> sort(Set<T> items, Collection<NamespaceVersion> fromVersions) {
    if (items.isEmpty()) {
      return new VersionedItems<>();
    }
    // first we sort them according name ordering - this will give us a stable ordering even if dependencies are not sufficient for a stable order
    return orderByDependencies(sortByName(items), fromVersions);
  }

  protected List<T> sortByName(Collection<T> items) {
    return items.stream().sorted(m_secondaryComparator).collect(Collectors.toList());
  }

  protected VersionedItems<T> orderByDependencies(Collection<T> items, Collection<NamespaceVersion> fromVersions) {
    Set<T> unsatisfiedDependencies = new HashSet<>();
    Map<T, Set<T>> dependencyMap = new LinkedHashMap<>(); // preserve order
    for (T item : items) {
      dependencyMap.put(item, getDependentItems(item).filter(dep -> {
        if (items.contains(dep)) {
          return true;
        }

        if (fromVersions != null) {
          NamespaceVersion depVersion = dep.getVersion();
          Optional<NamespaceVersion> fromVersion = fromVersions.stream().filter(depVersion::namespaceEquals).findFirst();
          // if from version has no version for given name dependency is satisfied implicit; see also getItemsUnordered
          if (fromVersion.isPresent() && compareVersion(depVersion, fromVersion.get()) > 0) {
            unsatisfiedDependencies.add(dep);
          }
        }
        return false;
      }).collect(Collectors.toSet()));
    }

    List<T> result = new ArrayList<>();
    P_ItemDependencyIterator<T> it = new P_ItemDependencyIterator<>(dependencyMap);
    while (it.hasNext()) {
      result.add(it.next());
    }
    return new VersionedItems<>(result, unsatisfiedDependencies);
  }

  protected static <T extends INamespaceVersioned> String dependencyModelToString(Map<T, Set<T>> dependencyMap) {
    Comparator<T> itemComparator = Comparator.comparing(INamespaceVersioned::getVersion, Comparator.comparing(NamespaceVersion::getNamespace).thenComparing(NamespaceVersion::compareVersion));
    StringBuilder sb = new StringBuilder("{");
    if (!dependencyMap.isEmpty()) {
      List<T> keys = new ArrayList<>(dependencyMap.keySet());
      keys.sort(itemComparator);
      List<T> deps = new ArrayList<>();
      for (T key : keys) {
        sb.append(key.getVersion().unwrap()).append("=").append("[");
        deps.clear();
        deps.addAll(dependencyMap.get(key));
        if (!deps.isEmpty()) {
          deps.sort(itemComparator);
          for (T d : deps) {
            sb.append(d.getVersion().unwrap());
            sb.append(", ");
          }
          sb.setLength(sb.length() - 2);
        }
        sb.append("], ");
      }
      sb.setLength(sb.length() - 2);
    }
    return sb.append("}").toString();
  }

  public static class VersionedItems<T extends INamespaceVersioned> {

    private final List<T> m_items;
    private final Collection<T> m_unsatisfiedDependencies;

    public VersionedItems() {
      this(Collections.emptyList(), Collections.emptySet());
    }

    public VersionedItems(List<T> items, Collection<T> unsatisfiedDependencies) {
      m_items = assertNotNull(items);
      m_unsatisfiedDependencies = assertNotNull(unsatisfiedDependencies);
    }

    public List<T> getItems() {
      return m_items;
    }

    public boolean isValid() {
      return m_unsatisfiedDependencies.isEmpty();
    }

    public Collection<T> getUnsatisfiedDependencies() {
      return m_unsatisfiedDependencies;
    }

    @Override
    public String toString() {
      return "VersionedItems[items=[" + m_items.stream().map(INamespaceVersioned::getVersion).map(NamespaceVersion::unwrap).collect(Collectors.joining(", ")) +
          "], unsatisfiedDependencies=" + m_unsatisfiedDependencies.stream().map(INamespaceVersioned::getVersion).map(NamespaceVersion::unwrap).collect(Collectors.joining(", ")) + "]";
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends INamespaceVersioned> Builder<T> newBuilder() {
    return (Builder<T>) BEANS.get(Builder.class);
  }

  @Bean
  public static class Builder<T extends INamespaceVersioned> {

    private List<String> m_names;
    private Collection<T> m_items;

    public NamespaceVersionedModel<T> build() {
      return new NamespaceVersionedModel<>(this);
    }

    public List<String> getNames() {
      return m_names;
    }

    /**
     * All names used by version and dependency of and item ({@link NamespaceVersion}). The order of the names list
     * defines a secondary ordering of items in case dependencies are not specific enough.
     */
    public Builder<T> withNames(List<String> names) {
      m_names = names;
      return this;
    }

    public Collection<T> getItems() {
      return m_items;
    }

    /**
     * All known items.
     */
    public Builder<T> withItems(Collection<T> items) {
      m_items = items;
      return this;
    }
  }

  protected static class P_ByNameComparator implements Comparator<INamespaceVersioned>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Integer> m_ordering;

    public P_ByNameComparator(List<String> names) {
      m_ordering = createOrdering(names);
    }

    protected static Map<String, Integer> createOrdering(List<String> names) {
      Map<String, Integer> nameOrder = new HashMap<>();
      for (int i = 0; i < names.size(); i++) {
        nameOrder.put(names.get(i), i);
      }
      return nameOrder;
    }

    @Override
    public int compare(INamespaceVersioned o1, INamespaceVersioned o2) {
      // no name should be missing in ordering - no <null> handling
      Integer s1 = m_ordering.get(o1.getVersion().getNamespace());
      Integer s2 = m_ordering.get(o2.getVersion().getNamespace());
      return s1.compareTo(s2);
    }
  }

  protected static class P_ItemDependencyIterator<T extends INamespaceVersioned> implements Iterator<T> {

    private final Map<T, Set<T>> m_dependencyMap;
    private T m_next;

    public P_ItemDependencyIterator(Map<T, Set<T>> dependencyMap) {
      m_dependencyMap = dependencyMap;
      advance();
    }

    private void advance() {
      T next = findNext();
      m_dependencyMap.remove(next);
      for (Set<T> entry : m_dependencyMap.values()) {
        entry.remove(next);
      }
      m_next = next;
    }

    private T findNext() {
      for (Entry<T, Set<T>> entry : m_dependencyMap.entrySet()) {
        if (entry.getValue().isEmpty()) {
          return entry.getKey();
        }
      }
      if (!m_dependencyMap.isEmpty()) {
        fail("Unable to resolve all dependencies - cycle in the already reduced dependencies? {}", dependencyModelToString(m_dependencyMap));
      }
      return null;
    }

    @Override
    public boolean hasNext() {
      return m_next != null;
    }

    @Override
    public T next() {
      if (m_next == null) {
        throw new NoSuchElementException();
      }
      T next = m_next;
      advance();
      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}

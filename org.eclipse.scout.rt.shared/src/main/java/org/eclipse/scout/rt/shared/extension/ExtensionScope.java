/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * An extension scope helps to filter extensions, contributions and model move operations for a particular context. It
 * supports the following two strategies:
 * <ul>
 * <li><b>Sub scoping</b>: The set of potential matches is subsequently reduced by creating sub extension scopes
 * containing only those extensions which current path is matching the scopes defined so far. The caller is responsible
 * for iterating through the scopes. This strategy is used for top-down searches. See {@link #createSubScope(Class)}.
 * </li>
 * <li><b>Filtering</b>: The caller provides an {@link Iterator} that is going through the list of parent model objects.
 * The extension scope is responsible for iterating through the scopes. This strategy is used for bottom-up searches.
 * See {@link #filterScopeItems(Class, Iterator)}.</li>
 * </ul>
 * The root extension scope is called global scope and can be cached outside.
 */
public class ExtensionScope<T extends AbstractExtensionRegistryItem> {

  private static final ConcurrentMap<Class<?>, Set<Class<?>>> CLASS_HIERARCHIES = new ConcurrentHashMap<>(10_000);

  private final ExtensionScope<T> m_parentScope;
  private final Map<Class<?>, Set<ScopeItem>> m_scopeItems;
  private final Map<ClassIdentifier, List<T>> m_extensionItems;

  private final ConcurrentMap<Class<?>, ExtensionScope<T>> m_cachedSubScopesByModelClass = new ConcurrentHashMap<>();
  private final ConcurrentMap<Class<?>, Set<ScopeItem>> m_cachedScopeItemsByModelClass = new ConcurrentHashMap<>();

  /**
   * Creates a new global extension scope for the given extension items. Top-down strategy is considered to be used with
   * {@link #createSubScope(Class)} and {@link #getRegistryItems(Class)}, whereas a bottom-up strategy is considered to
   * be used with {@link #filterScopeItems(Class, Iterator)} and {@link #resolveRegistryItems(Set)}.
   * <p/>
   * <b>Important</b>: If the created instance is cached outside, then the extension items must be cloned outside.
   */
  public ExtensionScope(Map<ClassIdentifier, List<T>> extensionItems, boolean topDownStrategy) {
    m_extensionItems = extensionItems;
    m_scopeItems = createGlobalScope(extensionItems.keySet(), topDownStrategy);
    m_parentScope = null;
  }

  protected ExtensionScope(Map<Class<?>, Set<ScopeItem>> scopeItems, ExtensionScope<T> parentScope, Map<ClassIdentifier, List<T>> extensionItems) {
    m_scopeItems = scopeItems;
    m_parentScope = parentScope;
    m_extensionItems = extensionItems;
  }

  /**
   * Creates a new global scope for the given class identifiers.
   */
  protected Map<Class<?>, Set<ScopeItem>> createGlobalScope(Collection<ClassIdentifier> classIdentifiers, boolean topDownStrategy) {
    Map<Class<?>, Set<ScopeItem>> scopeItems = new HashMap<>(classIdentifiers.size());
    for (ClassIdentifier identifier : classIdentifiers) {
      ScopeItem item = new ScopeItem(identifier, topDownStrategy);
      scopeItems.computeIfAbsent(item.getCurrentSegment(), k -> new HashSet<>()).add(item);
    }

    return scopeItems;
  }

  /**
   * Returns all registry items that are valid within this scope.
   */
  public Set<T> getRegistryItems(Class<?> owner) {
    Set<ScopeItem> scopeItems = getScopeItems(owner);
    return resolveRegistryItems(scopeItems);
  }

  public ExtensionScope<T> getSubScope(Class<?> ownerType) {
    return m_cachedSubScopesByModelClass.computeIfAbsent(ownerType, this::createSubScope);
  }

  /**
   * Creates a new sub {@link ExtensionScope} for the given owner type. This method returns <code>this</code> if no
   * element is matching the given owner type and none of its (recursive) super classes or interfaces.
   */
  protected ExtensionScope<T> createSubScope(Class<?> ownerType) {
    if (ownerType == null) {
      throw new IllegalArgumentException("ownerType must not be null.");
    }

    // 1. collect scope items for the given owner type hierarchy
    Set<ScopeItem> items = new HashSet<>();
    ExtensionScope<T> curScope = this;
    while (curScope != null) {
      collectScopeItems(ownerType, items, curScope.m_scopeItems);
      curScope = curScope.m_parentScope;
    }

    if (CollectionUtility.isEmpty(items)) {
      // there are no items that are matching the given owner type
      return this;
    }

    // 2. create new sub scope
    Map<Class<?>, Set<ScopeItem>> subScopeItems = new HashMap<>(items.size());
    for (ScopeItem item : items) {
      collectSubScopeItems(item, subScopeItems);
    }

    if (subScopeItems.isEmpty()) {
      // happens if all items have already reached their last segment
      return this;
    }

    return new ExtensionScope<>(subScopeItems, this, m_extensionItems);
  }

  /**
   * Filters all scope items by the given model class. An optional iterator may be provided for filtering multi-segment
   * scope items.
   */
  public Set<ScopeItem> filterScopeItems(Class<?> modelClass, Iterator<?> parentModelObjectIterator) {
    // 1. get scope items by model class
    final Set<ScopeItem> scopeItems = getScopeItems(modelClass);
    if (CollectionUtility.isEmpty(scopeItems)) {
      return null;
    }

    // 2. filter matching items and create sub scope
    Set<ScopeItem> potentialScopeItems = new HashSet<>(scopeItems);
    Set<ScopeItem> filteredScopeItems = new HashSet<>();
    Map<Class<?>, Set<ScopeItem>> subScopeItems = new HashMap<>(potentialScopeItems.size());
    collectFilteredAndSubScopeItems(potentialScopeItems, filteredScopeItems, subScopeItems);

    // 3. apply additional parent model object filter if provided
    if (parentModelObjectIterator != null && CollectionUtility.hasElements(potentialScopeItems)) {
      while (parentModelObjectIterator.hasNext()) {
        Object parent = parentModelObjectIterator.next();
        if (parent == null) {
          continue;
        }
        potentialScopeItems.clear();
        collectScopeItems(parent.getClass(), potentialScopeItems, subScopeItems);
        collectFilteredAndSubScopeItems(potentialScopeItems, filteredScopeItems, subScopeItems);
      }
    }

    return filteredScopeItems;
  }

  protected void collectFilteredAndSubScopeItems(Set<ScopeItem> potentialScopeItems, Set<ScopeItem> filteredScopeItems, Map<Class<?>, Set<ScopeItem>> subScopeItems) {
    for (ScopeItem item : potentialScopeItems) {
      if (item.isLastSegment()) {
        filteredScopeItems.add(item);
      }
      else {
        collectSubScopeItems(item, subScopeItems);
      }
    }
  }

  protected void collectSubScopeItems(ScopeItem item, Map<Class<?>, Set<ScopeItem>> subScopeItems) {
    // 1. create a sub scope item
    ScopeItem subScopeItem = item.createSubScopeItem();
    if (subScopeItem == null) {
      // item has already reached its last segment
      return;
    }

    // 2. add sub scope item to item map by current segment class
    subScopeItems.computeIfAbsent(subScopeItem.getCurrentSegment(), k -> new HashSet<>()).add(subScopeItem);
  }

  protected Set<ScopeItem> getScopeItems(Class<?> owner) {
    return m_cachedScopeItemsByModelClass.computeIfAbsent(owner, o -> Set.copyOf(computeScopeItems(o)));
  }

  protected Set<ScopeItem> computeScopeItems(Class<?> owner) {
    Set<ScopeItem> collector = new HashSet<>();
    if (m_parentScope != null) {
      Set<ScopeItem> parentScopeItems = m_parentScope.getScopeItems(owner);
      collector.addAll(parentScopeItems);
    }
    collectScopeItems(owner, collector, m_scopeItems);
    return collector;
  }

  protected void collectScopeItems(Class<?> curClass, Set<ScopeItem> collector, Map<Class<?>, Set<ScopeItem>> scopeItems) {
    getClassHierarchy(curClass).stream()
        .map(scopeItems::get)
        .filter(Objects::nonNull)
        .forEach(collector::addAll);
  }

  /**
   * @return Returns the Set of all super classes and all implemented interfaces.
   */
  protected Set<Class<?>> getClassHierarchy(Class<?> c) {
    if (c == Object.class) {
      return Collections.emptySet();
    }

    // Do not use ConcurrentMap.computeIfAbsent because recursive calls are not supported by ConcurrentHashMap
    // (i.e. getClassHierarchy -> computeHierarchy -> getClassHierarchy)
    Set<Class<?>> h = CLASS_HIERARCHIES.get(c);
    if (h == null) {
      h = computeHierarchy(c);
      Set<Class<?>> hPrev = CLASS_HIERARCHIES.putIfAbsent(c, h);
      if (hPrev != null) {
        h = hPrev;
      }
    }
    return h;
  }

  protected Set<Class<?>> computeHierarchy(Class<?> c) {
    Set<Class<?>> result = new HashSet<>();
    result.add(c);

    final Class<?> superClass = c.getSuperclass();
    if (superClass != null && superClass != Object.class) {
      result.addAll(getClassHierarchy(superClass));
    }

    for (Class<?> i : c.getInterfaces()) {
      result.addAll(getClassHierarchy(i));
    }

    return Set.of(result.toArray(new Class<?>[0]));
  }

  /**
   * Returns an ordered set of resolved registry items for the given scope items (ordered by the resolved item's order).
   *
   * @see Order
   */
  public Set<T> resolveRegistryItems(Set<ScopeItem> scopeItems) {
    if (CollectionUtility.isEmpty(scopeItems)) {
      return Collections.emptySet();
    }

    return scopeItems.stream()
        .filter(ScopeItem::isLastSegment)
        .map(ScopeItem::getIdentifier)
        .map(m_extensionItems::get)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .sorted(Comparator.comparingLong(AbstractExtensionRegistryItem::getOrder))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}

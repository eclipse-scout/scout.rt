/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * An extension scope helps filtering extensions, contributions and model move operations for a particular context. It
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

  private final ExtensionScope<T> m_parentScope;
  private final Map<Class<?>, Set<ScopeItem>> m_scopeItems;
  private final Map<ClassIdentifier, List<T>> m_extensionItems;

  /**
   * Creates a new global extension scope for the given extension items. Top-down strategy is considered to be used with
   * {@link #createSubScope(Class)} and {@link #getRegistryItems(Class)}, whereas a bottom-up strategy is considered to
   * be used with {@link #filterScopeItems(Class, Iterator)} and {@link #resolveRegistryItems(Set)}.
   * <p/>
   * <b>Important</b>: If the created instance is cached outside, then the extension items must be cloned outside.
   *
   * @param extensionItems
   * @param topDownStrategy
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
      addCurrentItemSegment(scopeItems, item);
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

  /**
   * Creates a new sub {@link ExtensionScope} for the given owner type. This method returns <code>null</code> if no
   * element is matching the given owner type or one of its (recursive) super classes or interfaces.
   *
   * @param ownerType
   * @return
   */
  public ExtensionScope<T> createSubScope(Class<?> ownerType) {
    if (ownerType == null) {
      throw new IllegalArgumentException("ownerType must not be null.");
    }

    // 1. collect scope items for the given owner type hierarchy
    Set<ScopeItem> items = new HashSet<>();
    ExtensionScope<T> curScope = this;
    while (curScope != null) {
      collectScopeItemsRec(ownerType, items, curScope.m_scopeItems);
      curScope = curScope.m_parentScope;
    }

    if (CollectionUtility.isEmpty(items)) {
      // there are no items that are matching the given owner type
      return null;
    }

    // 2. create new sub scope
    Map<Class<?>, Set<ScopeItem>> subScopeItems = new HashMap<>(items.size());
    for (ScopeItem item : items) {
      collectSubScopeItems(item, subScopeItems);
    }

    if (subScopeItems.isEmpty()) {
      // happens if all items have already reached their last segment
      return null;
    }

    return new ExtensionScope<>(subScopeItems, this, m_extensionItems);
  }

  /**
   * Filters all scope items by the given model class. An optional iterator may be provided for filtering multi-segment
   * scope items.
   *
   * @param modelClass
   * @param parentModelObjectIterator
   * @return
   */
  public Set<ScopeItem> filterScopeItems(Class<?> modelClass, Iterator<?> parentModelObjectIterator) {
    // 1. get scope items by model class
    Set<ScopeItem> potentialScopeItems = getScopeItems(modelClass);
    if (CollectionUtility.isEmpty(potentialScopeItems)) {
      return null;
    }

    // 2. filter matching items and create sub scope
    Set<ScopeItem> filteredScopeItems = new HashSet<>();
    Map<Class<?>, Set<ScopeItem>> subScopeItems = new HashMap<>(potentialScopeItems.size());
    collectFilteredandSubScopItems(potentialScopeItems, filteredScopeItems, subScopeItems);

    // 3. apply additional parent model object filter if provided
    if (parentModelObjectIterator != null && CollectionUtility.hasElements(potentialScopeItems)) {
      while (parentModelObjectIterator.hasNext()) {
        Object parent = parentModelObjectIterator.next();
        if (parent == null) {
          continue;
        }
        potentialScopeItems.clear();
        collectScopeItemsRec(parent.getClass(), potentialScopeItems, subScopeItems);
        collectFilteredandSubScopItems(potentialScopeItems, filteredScopeItems, subScopeItems);
      }
    }

    return filteredScopeItems;
  }

  protected void collectFilteredandSubScopItems(Set<ScopeItem> potentialScopeItems, Set<ScopeItem> filteredScopeItems, Map<Class<?>, Set<ScopeItem>> subScopeItems) {
    for (ScopeItem item : potentialScopeItems) {
      if (item.isLastSegment()) {
        filteredScopeItems.add(item);
      }
      else {
        collectSubScopeItems(item, subScopeItems);
      }
    }
  }

  /**
   * @param item
   * @param subScopeItems
   */
  protected void collectSubScopeItems(ScopeItem item, Map<Class<?>, Set<ScopeItem>> subScopeItems) {
    // 1. create a sub scope item
    ScopeItem subScopeItem = item.createSubScopeItem();
    if (subScopeItem == null) {
      // item has already reached its last segment
      return;
    }

    // 2. add sub scope item to item map by current segment class
    addCurrentItemSegment(subScopeItems, subScopeItem);
  }

  /**
   * @param scopeItems
   * @param item
   */
  protected void addCurrentItemSegment(Map<Class<?>, Set<ScopeItem>> scopeItems, ScopeItem item) {
    Class<?> currentSegment = item.getCurrentSegment();
    Set<ScopeItem> subItemList = scopeItems.computeIfAbsent(currentSegment, k -> new HashSet<>());
    subItemList.add(item);
  }

  protected Set<ScopeItem> getScopeItems(Class<?> owner) {
    Set<ScopeItem> collector = new HashSet<>();
    if (m_parentScope != null) {
      Set<ScopeItem> parentScopeItems = m_parentScope.getScopeItems(owner);
      collector.addAll(parentScopeItems);
    }
    collectScopeItemsRec(owner, collector, m_scopeItems);
    return collector;
  }

  protected void collectScopeItemsRec(Class<?> curClass, Set<ScopeItem> collector, Map<Class<?>, Set<ScopeItem>> scopeItems) {
    if (Object.class.equals(curClass)) {
      return;
    }

    // add my extensions
    Set<ScopeItem> list = scopeItems.get(curClass);
    if (CollectionUtility.hasElements(list)) {
      collector.addAll(list);
    }

    // visit super class
    Class<?> superclass = curClass.getSuperclass();
    if (superclass != null) {
      collectScopeItemsRec(superclass, collector, scopeItems);
    }

    // visit interfaces
    for (Class<?> ifc : curClass.getInterfaces()) {
      collectScopeItemsRec(ifc, collector, scopeItems);
    }
  }

  /**
   * Returns a set of resolved registry items for the given scope items.
   *
   * @param scopeItems
   * @return
   */
  public Set<T> resolveRegistryItems(Set<ScopeItem> scopeItems) {
    if (CollectionUtility.isEmpty(scopeItems)) {
      return Collections.emptySet();
    }
    Set<T> collector = new TreeSet<>(new P_ExtensionRegistryItemComparator());
    for (ScopeItem scopeItem : scopeItems) {
      ClassIdentifier classIdentifier = scopeItem.getIdentifier();
      if (!scopeItem.isLastSegment()) {
        continue;
      }
      List<T> localExtensions = m_extensionItems.get(classIdentifier);
      if (CollectionUtility.hasElements(localExtensions)) {
        collector.addAll(localExtensions);
      }
    }
    return collector;
  }

  private static class P_ExtensionRegistryItemComparator implements Comparator<AbstractExtensionRegistryItem>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(AbstractExtensionRegistryItem o1, AbstractExtensionRegistryItem o2) {
      return Long.compare(o1.getOrder(), o2.getOrder());
    }
  }
}

/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Represents an index for which multiple elements can result in the very same index value.
 * <p>
 * Use this index if the element provides a multiple index values for this index.
 *
 * @since 5.2
 */
public abstract class AbstractMultiValuesIndex<INDEX, ELEMENT> implements IMultiValueIndex<INDEX, ELEMENT> {

  private final Map<INDEX, List<ELEMENT>> m_mapByIndex = new HashMap<>();
  private final Map<ELEMENT, Set<INDEX>> m_mapByElement = new LinkedHashMap<>(); // LinkedHashMap to preserve insertion-order.

  @Override
  public boolean addToIndex(final ELEMENT element) {
    if (m_mapByElement.containsKey(element)) {
      removeFromIndex(element);
    }

    final Set<INDEX> indexes = calculateIndexesFor(element);
    if (indexes == null || indexes.isEmpty()) {
      return false;
    }

    for (final INDEX index : indexes) {
      List<ELEMENT> elements = m_mapByIndex.computeIfAbsent(index, k -> new ArrayList<>());
      elements.add(element);
    }
    m_mapByElement.put(element, new HashSet<>(indexes));

    return true;
  }

  @Override
  public boolean removeFromIndex(final ELEMENT element) {
    final Set<INDEX> indexes = m_mapByElement.remove(element);
    if (indexes == null) {
      return false;
    }

    for (final INDEX index : indexes) {
      final List<ELEMENT> elements = m_mapByIndex.get(index);
      elements.remove(element);

      if (elements.isEmpty()) {
        m_mapByIndex.remove(index);
      }
    }

    return true;
  }

  @Override
  public Set<INDEX> indexValues() {
    return new HashSet<>(m_mapByIndex.keySet());
  }

  @Override
  public List<ELEMENT> values() {
    return new ArrayList<>(m_mapByElement.keySet()); // ordered as inserted because LinkedHashMap is used
  }

  @Override
  public void clear() {
    m_mapByIndex.clear();
    m_mapByElement.clear();
  }

  @Override
  public boolean contains(final ELEMENT element) {
    return m_mapByElement.containsKey(element);
  }

  @Override
  public Iterator<ELEMENT> iterator() {
    return values().iterator();
  }

  @Override
  public List<ELEMENT> get(final INDEX index) {
    return CollectionUtility.arrayList(m_mapByIndex.get(index));
  }

  /**
   * Method invoked to calculate the index values for the given element.
   *
   * @param element
   *          the element to calculate its index values.
   * @return the index values, or <code>null</code> or an empty {@link Set} to not add the element to this index.
   */
  protected abstract Set<INDEX> calculateIndexesFor(final ELEMENT element);
}

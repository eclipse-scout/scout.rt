/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an index that is unique among all elements, e.g. an element's primary key.
 *
 * @since 5.1
 */
public abstract class AbstractSingleValueIndex<INDEX, ELEMENT> implements ISingleValueIndex<INDEX, ELEMENT> {

  private final Map<INDEX, ELEMENT> m_mapByIndex = new HashMap<>();
  private final Map<ELEMENT, INDEX> m_mapByElement = new LinkedHashMap<>(); // LinkedHashMap to preserve insertion-order.

  @Override
  public boolean addToIndex(final ELEMENT element) {
    if (m_mapByElement.containsKey(element)) {
      removeFromIndex(element);
    }

    final INDEX index = calculateIndexFor(element);
    if (index == null) {
      return false;
    }

    m_mapByIndex.put(index, element);
    m_mapByElement.put(element, index);
    return true;
  }

  @Override
  public boolean removeFromIndex(final ELEMENT element) {
    final INDEX index = m_mapByElement.remove(element);
    if (index == null) {
      return false;
    }

    m_mapByIndex.remove(index);
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
  public ELEMENT get(final INDEX index) {
    return m_mapByIndex.get(index);
  }

  /**
   * Method invoked to calculate the index value for the given element.
   *
   * @param element
   *          the element to calculate its index value.
   * @return the index value, or <code>null</code> to not add to the index.
   */
  protected abstract INDEX calculateIndexFor(ELEMENT element);
}

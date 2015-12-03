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
 *
 * @since 5.1
 */
public abstract class AbstractMultiValueIndex<INDEX, ELEMENT> implements IIndex<INDEX, ELEMENT> {

  private final Map<INDEX, List<ELEMENT>> m_mapByIndex = new HashMap<>();
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

    List<ELEMENT> elements = m_mapByIndex.get(index);
    if (elements == null) {
      elements = new ArrayList<>();
      m_mapByIndex.put(index, elements);
    }
    elements.add(element);
    m_mapByElement.put(element, index);

    return true;
  }

  @Override
  public boolean removeFromIndex(final ELEMENT element) {
    final INDEX index = m_mapByElement.remove(element);
    if (index == null) {
      return false;
    }

    final List<ELEMENT> elements = m_mapByIndex.get(index);
    elements.remove(element);
    if (elements.isEmpty()) {
      m_mapByIndex.remove(index);
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

  /**
   * Returns the elements that correspond to the given index value in the order as inserted.
   *
   * @param index
   *          the index to look elements for.
   * @return elements ordered as inserted, or an empty {@link List} if no found.
   */
  public List<ELEMENT> get(final INDEX index) {
    return CollectionUtility.arrayList(m_mapByIndex.get(index));
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

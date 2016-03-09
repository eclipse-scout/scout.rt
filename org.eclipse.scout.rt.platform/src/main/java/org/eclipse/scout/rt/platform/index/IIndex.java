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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Represents an index with the functionality to index elements.
 *
 * @since 5.1
 * @see IndexedStore
 */
public interface IIndex<INDEX, ELEMENT> extends Iterable<ELEMENT> {

  /**
   * Adds the given element to this index. If already contained, calculated indices are first removed.
   *
   * @param element
   *          the element to be indexed.
   * @return <code>true</code> if being added to the index, <code>false</code> otherwise. Typically, an element is not
   *         added to the index if the calculated index value results in <code>null</code>.
   */
  boolean addToIndex(final ELEMENT element);

  /**
   * Removes the given element from this index.
   *
   * @param element
   *          the element to be removed from this index.
   * @return <code>true</code> if removed from this index, <code>false</code> if not contained in this index.
   */
  boolean removeFromIndex(final ELEMENT element);

  /**
   * @return the index values of the contained elements.
   */
  Set<INDEX> indexValues();

  /**
   * @return the elements contained in this index in the order as inserted.
   */
  List<ELEMENT> values();

  /**
   * @return <code>true</code> if the given element is contained in this index.
   */
  boolean contains(ELEMENT element);

  /**
   * Removes all elements from this index.
   */
  void clear();

  /**
   * Iterator to iterate over indexed elements.
   */
  @Override
  Iterator<ELEMENT> iterator();
}

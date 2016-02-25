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

import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Represents an index for which multiple elements can result in the very same index value.
 * <p>
 * Use this index if the element provides a single index value for this index.
 *
 * @since 5.1
 */
public abstract class AbstractMultiValueIndex<INDEX, ELEMENT> extends AbstractMultiValuesIndex<INDEX, ELEMENT> {

  @Override
  protected final Set<INDEX> calculateIndexesFor(final ELEMENT element) {
    final INDEX index = calculateIndexFor(element);
    if (index != null) {
      return CollectionUtility.hashSet(index);
    }
    else {
      return CollectionUtility.emptyHashSet();
    }
  }

  /**
   * Method invoked to calculate the index value for the given element.
   *
   * @param element
   *          the element to calculate its index value.
   * @return the index value, or <code>null</code> to not add the element to this index.
   */
  protected abstract INDEX calculateIndexFor(final ELEMENT element);
}

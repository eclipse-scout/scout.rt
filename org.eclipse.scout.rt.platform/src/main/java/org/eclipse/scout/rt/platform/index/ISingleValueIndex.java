/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.index;

/**
 * Represents an index that is unique among all elements, e.g. an element's primary key.
 *
 * @since 6.0
 */
public interface ISingleValueIndex<INDEX, ELEMENT> extends IIndex<INDEX, ELEMENT> {

  /**
   * Returns the element that corresponds to the given index value.
   *
   * @param index
   *          the index to look the element for.
   * @return element, or <code>null</code> if not found.
   */
  ELEMENT get(INDEX index);
}

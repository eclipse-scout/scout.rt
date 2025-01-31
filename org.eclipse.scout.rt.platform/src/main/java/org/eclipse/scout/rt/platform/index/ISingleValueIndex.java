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
   *     the index to look the element for.
   * @return element, or <code>null</code> if not found.
   */
  ELEMENT get(INDEX index);
}

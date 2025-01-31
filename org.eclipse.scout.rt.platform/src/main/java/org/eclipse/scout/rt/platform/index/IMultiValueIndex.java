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

import java.util.List;

/**
 * Represents an index for which multiple elements can result in the very same index value.
 *
 * @since 6.0
 */
public interface IMultiValueIndex<INDEX, ELEMENT> extends IIndex<INDEX, ELEMENT> {

  /**
   * Returns the elements that correspond to the given index value in the order as inserted.
   *
   * @param index
   *     the index to look elements for.
   * @return elements ordered as inserted, or an empty {@link List} if no found.
   */
  List<ELEMENT> get(INDEX index);
}

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
   *          the index to look elements for.
   * @return elements ordered as inserted, or an empty {@link List} if no found.
   */
  List<ELEMENT> get(INDEX index);
}

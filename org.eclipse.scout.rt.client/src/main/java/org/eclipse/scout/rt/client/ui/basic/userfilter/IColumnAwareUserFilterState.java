/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public interface IColumnAwareUserFilterState {
  /**
   * Replace the column in the filter with the new column. This method is used when the column configuration is changed.
   */
  void replaceColumn(IColumn<?> col);
}

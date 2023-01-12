/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public interface IColumnAwareUserFilterState {
  /**
   * Replace the column in the filter with the new column. This method is used when the column configuration is changed.
   */
  void replaceColumn(IColumn<?> col);
}

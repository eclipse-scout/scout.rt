/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.internal;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.holders.IHolder;

public class RowToHolderMapper<T> {
  private final ITableRow m_row;
  private final IColumn<T> m_col;
  private final IHolder<T> m_holder;

  public RowToHolderMapper(ITableRow row, IColumn<T> col, IHolder<T> holder) {
    m_row = row;
    m_col = col;
    m_holder = holder;
  }

  public void exportRowValue() {
    m_holder.setValue(m_col.getValue(m_row));
  }
}

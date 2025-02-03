/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.TriState;

/**
 * The filter accepts all active rows and in addition all checked rows
 */
class ActiveOrCheckedRowsFilter extends CheckedRowsFilter {
  private final IColumn<Boolean> m_activeCol;
  private final TriState m_filterValue;

  public ActiveOrCheckedRowsFilter(IColumn<Boolean> activeCol, TriState filterValue) {
    m_activeCol = activeCol;
    m_filterValue = filterValue;
  }

  @Override
  public boolean accept(ITableRow row) {
    Boolean active = m_activeCol.getValue(row);
    // active
    if (active != null) {
      Boolean b = m_filterValue.getBooleanValue();
      if (b != null && b.booleanValue() != active.booleanValue()) {
        // active mismatch, check checked
        return super.accept(row);
      }
    }
    return true;
  }
}

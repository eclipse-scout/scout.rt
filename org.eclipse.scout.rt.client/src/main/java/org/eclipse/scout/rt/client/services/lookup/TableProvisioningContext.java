/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.lookup;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class TableProvisioningContext implements IProvisioningContext {
  private final ITable m_table;
  private final ITableRow m_row;
  private final IColumn<?> m_column;

  public TableProvisioningContext(ITable table, ITableRow row, IColumn<?> column) {
    m_table = table;
    m_row = row;
    m_column = column;
  }

  public ITable getTable() {
    return m_table;
  }

  public ITableRow getRow() {
    return m_row;
  }

  public IColumn<?> getColumn() {
    return m_column;
  }
}

/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class TableEventFilterCondition {
  private static final long serialVersionUID = 1L;

  private int m_type;
  private List<? extends ITableRow> m_rows;
  private List<? extends IColumn<?>> m_columns;
  private boolean m_checkRows;
  private boolean m_checkColumns;

  public TableEventFilterCondition(int type) {
    m_type = type;
    m_rows = new ArrayList<ITableRow>();
    m_columns = new ArrayList<IColumn<?>>();
  }

  public int getType() {
    return m_type;
  }

  public List<ITableRow> getRows() {
    return CollectionUtility.arrayList(m_rows);
  }

  public void setRows(List<? extends ITableRow> rows) {
    m_rows = CollectionUtility.arrayList(rows);
    m_checkRows = true;
  }

  public List<? extends IColumn<?>> getColumns() {
    return m_columns;
  }

  public void setColumns(List<? extends IColumn<?>> columns) {
    m_columns = CollectionUtility.arrayList(columns);
    m_checkColumns = true;
  }

  public boolean checkRows() {
    return m_checkRows;
  }

  public boolean checkColumns() {
    return m_checkColumns;
  }
}

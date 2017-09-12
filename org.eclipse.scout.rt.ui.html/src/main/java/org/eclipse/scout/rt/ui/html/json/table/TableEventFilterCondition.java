/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class TableEventFilterCondition {

  private final int m_type;
  private List<ITableRow> m_rows;
  private List<ITableRow> m_checkedRows;
  private List<ITableRow> m_uncheckedRows;
  private List<IColumn<?>> m_columns;
  private IUserFilterState m_userFilter;

  private boolean m_checkRows;
  private boolean m_checkCheckedRows;
  private boolean m_checkColumns;
  private boolean m_checkUserFilter;

  /**
   * @param type
   *          event type (see {@link TableEvent})
   */
  public TableEventFilterCondition(int type) {
    m_type = type;
    m_rows = new ArrayList<>();
    m_columns = new ArrayList<>();
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

  public List<ITableRow> getCheckedRows() {
    return CollectionUtility.arrayList(m_checkedRows);
  }

  public List<ITableRow> getUncheckedRows() {
    return CollectionUtility.arrayList(m_uncheckedRows);
  }

  public void setCheckedRows(List<? extends ITableRow> checkedRows, List<? extends ITableRow> uncheckedRows) {
    m_checkedRows = CollectionUtility.arrayList(checkedRows);
    m_uncheckedRows = CollectionUtility.arrayList(uncheckedRows);
    m_checkCheckedRows = true;
  }

  public List<IColumn<?>> getColumns() {
    return m_columns;
  }

  public void setColumns(List<? extends IColumn<?>> columns) {
    m_columns = CollectionUtility.arrayList(columns);
    m_checkColumns = true;
  }

  public IUserFilterState getUserFilter() {
    return m_userFilter;
  }

  public void setUserFilter(IUserFilterState userFilter) {
    m_userFilter = userFilter;
    m_checkUserFilter = true;
  }

  public boolean checkRows() {
    return m_checkRows;
  }

  public boolean checkCheckedRows() {
    return m_checkCheckedRows;
  }

  public boolean checkColumns() {
    return m_checkColumns;
  }

  public boolean checkUserFilter() {
    return m_checkUserFilter;
  }
}

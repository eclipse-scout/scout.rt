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
package org.eclipse.scout.rt.client.ui.basic.table.columnfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Default implementation for {@link ITableColumnFilterManager}
 */
public class DefaultTableColumnFilterManager implements ITableColumnFilterManager, ITableRowFilter {
  private final ITable m_table;
  private final Map<IColumn, ITableColumnFilter> m_filterMap;
  private boolean m_enabled;

  public DefaultTableColumnFilterManager(ITable table) {
    m_table = table;
    m_filterMap = Collections.synchronizedMap(new HashMap<IColumn, ITableColumnFilter>());
    setEnabled(true);
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean enabled) {
    if (m_enabled != enabled) {
      m_enabled = enabled;
      m_table.removeRowFilter(this);
      if (m_enabled) {
        m_table.addRowFilter(this);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> ITableColumnFilter<T> getFilter(IColumn<T> col) {
    return m_filterMap.get(col);
  }

  public void reset() throws ProcessingException {
    try {
      m_table.setTableChanging(true);
      //
      for (IColumn<?> col : m_filterMap.keySet()) {
        m_table.getColumnSet().updateColumn(col);
      }
      m_filterMap.clear();
      m_table.applyRowFilters();
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  @SuppressWarnings("unchecked")
  public void showFilterForm(IColumn col) throws ProcessingException {
    ITableColumnFilter<?> filter = m_filterMap.get(col);
    if (filter == null) {
      if (col instanceof ISmartColumn<?>) {
        filter = new StringColumnFilter(col);
      }
      else if (String.class.isAssignableFrom(col.getDataType())) {
        filter = new StringColumnFilter(col);
      }
      else if (Boolean.class.isAssignableFrom(col.getDataType())) {
        filter = new BooleanColumnFilter(col);
      }
      else if (Comparable.class.isAssignableFrom(col.getDataType())) {
        filter = new ComparableColumnFilter(col);
      }
    }
    if (filter != null) {
      ColumnFilterForm f = new ColumnFilterForm();
      f.setDisplayHint(IForm.DISPLAY_HINT_POPUP_DIALOG);
      f.setModal(true);
      f.setColumnFilter(filter);
      f.startModify();
      f.waitFor();
      if (f.isFormStored()) {
        if (filter.isEmpty()) {
          m_filterMap.remove(col);
        }
        else {
          m_filterMap.put(col, filter);
        }
        m_table.getColumnSet().updateColumn(col);
        m_table.applyRowFilters();
      }
    }
  }

  public boolean accept(ITableRow row) {
    for (ITableColumnFilter f : m_filterMap.values()) {
      if (!f.accept(row)) return false;
    }
    return true;
  }

  public List<String> getDisplayTexts() {
    ArrayList<String> list = new ArrayList<String>();
    for (ITableColumnFilter<?> filter : m_filterMap.values()) {
      if (filter != null && !filter.isEmpty()) {
        list.add(ScoutTexts.get("Column") + " \"" + filter.getColumn().getHeaderCell().getText() + "\"");
      }
    }
    return list;
  }
}

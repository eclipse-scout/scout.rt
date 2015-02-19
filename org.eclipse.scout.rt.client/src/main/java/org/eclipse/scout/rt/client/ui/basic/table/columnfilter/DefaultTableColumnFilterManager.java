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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IContentAssistColumn;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Default implementation for {@link ITableColumnFilterManager}
 */
public class DefaultTableColumnFilterManager implements ITableColumnFilterManager, ITableRowFilter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultTableColumnFilterManager.class);

  private final ITable m_table;
  private final Map<IColumn, ITableColumnFilter> m_filterMap;
  private final EventListenerList m_listenerList;
  private boolean m_enabled;

  public DefaultTableColumnFilterManager(ITable table) {
    m_table = table;
    m_filterMap = Collections.synchronizedMap(new HashMap<IColumn, ITableColumnFilter>());
    m_listenerList = new EventListenerList();
    setEnabled(true);
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (m_enabled != enabled) {
      m_enabled = enabled;
      m_table.removeRowFilter(this);
      if (m_enabled) {
        m_table.addRowFilter(this);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> ITableColumnFilter<T> getFilter(IColumn<T> col) {
    return m_filterMap.get(col);
  }

  @Override
  public void reset() throws ProcessingException {
    try {
      m_table.setTableChanging(true);
      //
      for (IColumn col : m_filterMap.keySet()) {
        m_table.getColumnSet().updateColumn(col);
      }
      m_filterMap.clear();
      m_table.applyRowFilters();
      fireFiltersReset();
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void showFilterForm(IColumn col, boolean showAsPopupDialog) throws ProcessingException {
    ITableColumnFilter<?> filter = m_filterMap.get(col);
    boolean created = false;
    if (filter == null) {
      created = true;
      if (col instanceof IContentAssistColumn<?, ?>) {
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
      if (showAsPopupDialog) {
        f.setDisplayHint(IForm.DISPLAY_HINT_POPUP_DIALOG);
      }
      f.setModal(true);
      f.setColumnFilter(filter);
      f.startModify();
      f.waitFor();
      if (f.isFormStored()) {
        if (filter.isEmpty()) {
          m_filterMap.remove(col);
          if (!created) {
            fireFilterRemoved(col);
          }
        }
        else {
          m_filterMap.put(col, filter);
          if (created) {
            fireFilterAdded(col);
          }
          else {
            fireFilterChanged(col);
          }
        }
        m_table.getColumnSet().updateColumn(col);
        m_table.applyRowFilters();
      }
    }
  }

  @Override
  public boolean accept(ITableRow row) {
    for (ITableColumnFilter f : m_filterMap.values()) {
      if (!f.accept(row)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public List<String> getDisplayTexts() {
    ArrayList<String> list = new ArrayList<String>();
    for (ITableColumnFilter<?> filter : m_filterMap.values()) {
      if (filter != null && !filter.isEmpty()) {
        list.add(ScoutTexts.get("Column") + " \"" + filter.getColumn().getHeaderCell().getText() + "\"");
      }
    }
    return list;
  }

  @Override
  public Collection<ITableColumnFilter> getFilters() {
    return m_filterMap.values();
  }

  @Override
  public void refresh() {
    Collection<ITableColumnFilter> data = new ArrayList<ITableColumnFilter>();
    data.addAll(m_filterMap.values());
    m_filterMap.clear();
    for (ITableColumnFilter filter : data) {
      m_filterMap.put(filter.getColumn(), filter);
      m_table.getColumnSet().updateColumn(filter.getColumn());
    }
    m_table.applyRowFilters();
  }

  @Override
  @SuppressWarnings("unchecked")
  public byte[] getSerializedFilter(IColumn col) {
    ITableColumnFilter filter = m_filterMap.get(col);
    if (filter != null) {
      try {
        filter.setColumn(null);
        return SerializationUtility.createObjectSerializer().serialize(filter);
      }
      catch (Throwable t) {
        LOG.error("Failed storing filter data for " + t);
      }
      finally {
        filter.setColumn(col);
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setSerializedFilter(byte[] filterData, IColumn col) {
    try {
      if (col != null) {
        ITableColumnFilter filter = SerializationUtility.createObjectSerializer().deserialize(filterData, ITableColumnFilter.class);
        filter.setColumn(col);
        this.m_filterMap.put(col, filter);
      }
      m_table.applyRowFilters();
    }
    catch (Throwable t) {
      LOG.error("Failed reading filter data: " + t);
    }
  }

  @Override
  public void addListener(TableColumnFilterListener listener) {
    m_listenerList.add(TableColumnFilterListener.class, listener);
  }

  @Override
  public void removeListener(TableColumnFilterListener listener) {
    m_listenerList.remove(TableColumnFilterListener.class, listener);
  }

  private void fireFilterAdded(IColumn<?> column) throws ProcessingException {
    fireFilterEvent(new TableColumnFilterEvent(m_table, TableColumnFilterEvent.TYPE_FILTER_ADDED, column));
  }

  private void fireFilterChanged(IColumn<?> column) throws ProcessingException {
    fireFilterEvent(new TableColumnFilterEvent(m_table, TableColumnFilterEvent.TYPE_FILTER_CHANGED, column));
  }

  private void fireFilterRemoved(IColumn<?> column) throws ProcessingException {
    fireFilterEvent(new TableColumnFilterEvent(m_table, TableColumnFilterEvent.TYPE_FILTER_REMOVED, column));
  }

  private void fireFiltersReset() {
    try {
      fireFilterEvent(new TableColumnFilterEvent(m_table, TableColumnFilterEvent.TYPE_FILTERS_RESET));
    }
    catch (ProcessingException e) {
      LOG.warn(null, e);
    }
  }

  private void fireFilterEvent(TableColumnFilterEvent e) throws ProcessingException {
    TableColumnFilterListener[] listeners = m_listenerList.getListeners(TableColumnFilterListener.class);
    if (listeners != null && listeners.length > 0) {
      ProcessingException pe = null;
      for (TableColumnFilterListener listener : listeners) {
        try {
          listener.tableColumnFilterChanged(e);
        }
        catch (ProcessingException ex) {
          if (pe == null) {
            pe = ex;
          }
        }
        catch (Throwable t) {
          if (pe == null) {
            pe = new ProcessingException("Unexpected", t);
          }
        }
      }
      if (pe != null) {
        throw pe;
      }
    }
  }
}

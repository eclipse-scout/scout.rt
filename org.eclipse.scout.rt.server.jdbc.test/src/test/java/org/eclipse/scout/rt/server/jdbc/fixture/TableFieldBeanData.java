/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.fixture;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * Table generated as {@link AbstractTableFieldBeanData} (new with Luna)
 */
public class TableFieldBeanData extends AbstractTableFieldBeanData {

  private static final long serialVersionUID = 1L;

  public TableFieldBeanData() {
  }

  @Override
  public TableFieldBeanDataRowData addRow() {
    return (TableFieldBeanDataRowData) super.addRow();
  }

  @Override
  public TableFieldBeanDataRowData addRow(int rowState) {
    return (TableFieldBeanDataRowData) super.addRow(rowState);
  }

  @Override
  public TableFieldBeanDataRowData createRow() {
    return new TableFieldBeanDataRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return TableFieldBeanDataRowData.class;
  }

  @Override
  public TableFieldBeanDataRowData[] getRows() {
    return (TableFieldBeanDataRowData[]) super.getRows();
  }

  @Override
  public TableFieldBeanDataRowData rowAt(int index) {
    return (TableFieldBeanDataRowData) super.rowAt(index);
  }

  public void setRows(TableFieldBeanDataRowData[] rows) {
    super.setRows(rows);
  }

  public static class TableFieldBeanDataRowData extends AbstractTableRowData {

    private static final long serialVersionUID = 1L;
    public static final String active = "active";
    public static final String state = "state";
    public static final String name = "name";
    private Boolean m_active;
    private Integer m_state;
    private String m_name;

    public TableFieldBeanDataRowData() {
    }

    public Boolean getActive() {
      return m_active;
    }

    public void setActive(Boolean active) {
      m_active = active;
    }

    public Integer getState() {
      return m_state;
    }

    public void setState(Integer state) {
      m_state = state;
    }

    public String getName() {
      return m_name;
    }

    public void setName(String name) {
      m_name = name;
    }
  }
}

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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.annotations.ColumnData;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.beans.IPropertyFilter;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * Default implementation of {@link ITableRowDataMapper}.
 * 
 * @since 3.8.2
 */
public class TableRowDataMapper implements ITableRowDataMapper {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableRowDataMapper.class);

  private final Map<IColumn, FastPropertyDescriptor> m_propertyDescriptorByColumn = new HashMap<IColumn, FastPropertyDescriptor>();
  private final ColumnSet m_columnSet;
  private final Set<IColumn<?>> m_ignoredColumns;

  public TableRowDataMapper(Class<? extends AbstractTableRowData> rowType, ColumnSet columnSet) throws ProcessingException {
    if (rowType == null) {
      throw new IllegalArgumentException("rowType must not be null");
    }
    if (columnSet == null) {
      throw new IllegalArgumentException("columnSet must not be null");
    }
    m_columnSet = columnSet;
    try {
      FastPropertyDescriptor[] props = BeanUtility.getFastPropertyDescriptors(rowType, AbstractTableRowData.class, createPropertyFilter());
      for (FastPropertyDescriptor rowDataPropertyDesc : props) {
        IColumn column = findColumn(columnSet, rowDataPropertyDesc);
        if (column != null) {
          m_propertyDescriptorByColumn.put(column, rowDataPropertyDesc);
        }
        else {
          LOG.warn("No column found for property [" + rowDataPropertyDesc.getBeanClass().getName() + "#" + rowDataPropertyDesc.getName() + "]");
        }
      }
    }
    catch (IntrospectionException e) {
      throw new ProcessingException("Could not determine property descriptors", e);
    }
    // compute ignored columns
    Set<IColumn<?>> ignoredColumns = null;
    for (IColumn<?> col : columnSet.getColumns()) {
      if (isColumnIgnored(col)) {
        if (ignoredColumns == null) {
          ignoredColumns = new HashSet<IColumn<?>>();
        }
        ignoredColumns.add(col);
      }
    }
    if (ignoredColumns != null) {
      m_ignoredColumns = ignoredColumns;
    }
    else {
      m_ignoredColumns = Collections.emptySet();
    }
  }

  /**
   * @return Returns an {@link IPropertyFilter} used to filter those properties, that are mapped to table columns.
   */
  protected IPropertyFilter createPropertyFilter() {
    return new TableRowDataPropertyFilter();
  }

  /**
   * Returns the {@link IColumn} that corresponds to the given property description.
   * 
   * @param columnset
   * @param rowDataPropertyDesc
   */
  protected IColumn findColumn(ColumnSet columnset, FastPropertyDescriptor rowDataPropertyDesc) {
    String columnId = capitalize(rowDataPropertyDesc.getName());
    return columnset.getColumnById(columnId);
  }

  /**
   * @return Returns <code>true</code> if values of the given column are not imported from and exported to a
   *         {@link AbstractTableFieldBeanData}.
   * @since 3.10.0-M5
   */
  protected boolean isColumnIgnored(IColumn<?> column) {
    Class<?> c = column.getClass();
    ColumnData a = null;
    while (((a = c.getAnnotation(ColumnData.class)) == null || a.value() == SdkColumnCommand.IGNORE) && c.isAnnotationPresent(Replace.class)) {
      c = c.getSuperclass();
    }
    return a != null && a.value() == SdkColumnCommand.IGNORE;
  }

  /**
   * @return Returns the given string with the first character in upper case.
   */
  private String capitalize(String s) {
    if (s == null || s.length() == 0) {
      return null;
    }
    if (s.length() == 1) {
      return s.toUpperCase();
    }
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void importTableRowData(ITableRow row, AbstractTableRowData rowData) throws ProcessingException {
    for (IColumn column : m_columnSet.getColumns()) {
      if (m_ignoredColumns.contains(column)) {
        continue;
      }
      Object value = null;
      FastPropertyDescriptor propertyDesc = m_propertyDescriptorByColumn.get(column);
      if (propertyDesc != null) {
        try {
          value = propertyDesc.getReadMethod().invoke(rowData);
        }
        catch (Throwable t) {
          LOG.warn("Error reading row data property for column [" + column.getClass().getName() + "]", t);
        }
      }
      else {
        value = rowData.getCustomColumnValue(column.getColumnId());
      }
      column.setValue(row, value);
    }
    row.setStatus(rowData.getRowState());
  }

  @Override
  public void exportTableRowData(ITableRow row, AbstractTableRowData rowData) throws ProcessingException {
    for (IColumn column : m_columnSet.getColumns()) {
      if (m_ignoredColumns.contains(column)) {
        continue;
      }
      Object value = column.getValue(row);
      FastPropertyDescriptor propertyDesc = m_propertyDescriptorByColumn.get(column);
      if (propertyDesc != null) {
        try {
          propertyDesc.getWriteMethod().invoke(rowData, value);
        }
        catch (Throwable t) {
          LOG.warn("Error writing row data property for column [" + column.getClass().getName() + "]", t);
        }
      }
      else {
        rowData.setCustomColumnValue(column.getColumnId(), value);
      }
    }
    rowData.setRowState(row.getStatus());
  }

  @Override
  public boolean acceptExport(ITableRow row) throws ProcessingException {
    return true;
  }

  @Override
  public boolean acceptImport(AbstractTableRowData rowData) throws ProcessingException {
    return true;
  }

  public static class TableRowDataPropertyFilter implements IPropertyFilter {
    @Override
    public boolean accept(FastPropertyDescriptor descriptor) {
      Class<?> propertyType = descriptor.getPropertyType();
      if (propertyType == null) {
        return false;
      }
      if (descriptor.getReadMethod() == null) {
        return false;
      }
      if (descriptor.getWriteMethod() == null) {
        return false;
      }
      if ("rowState".equals(descriptor.getName())) {
        return false;
      }
      if ("customColumnValues".equals(descriptor.getName())) {
        return false;
      }
      return true;
    }
  }
}

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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.beans.IPropertyFilter;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * Maps table rows form an {@link ITable} to a {@link AbstractTableRowData} and vice versa. This implementation uses
 * reflection for getting all properties of an {@link AbstractTableRowData} and maps them by name to
 * {@link IColumn#getColumnId()}. If there is no property for a particular {@link IColumn}, its value is stored to and
 * read from the custom column value map (i.e. {@link AbstractTableRowData#setCustomColumnValue(String, Object)} and
 * {@link AbstractTableRowData#getCustomColumnValue(String)}, respectively).
 * 
 * @since 3.8.2
 */
public class TableRowDataMapper {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableRowDataMapper.class);

  private final Map<IColumn, FastPropertyDescriptor> m_propertyDescriptorByColumn = new HashMap<IColumn, FastPropertyDescriptor>();
  private final ColumnSet m_columnSet;

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

  /**
   * Writes the data form the given {@link AbstractTableRowData} to the given {@link ITableRow}.
   * 
   * @param row
   * @param rowData
   */
  @SuppressWarnings("unchecked")
  public void importTableRowData(ITableRow row, AbstractTableRowData rowData) throws ProcessingException {
    for (IColumn column : m_columnSet.getColumns()) {
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

  /**
   * Writes the data from the given {@link ITableRow} to the given {@link AbstractTableRowData}.
   * 
   * @param row
   * @param rowData
   */
  public void exportTableRowData(ITableRow row, AbstractTableRowData rowData) throws ProcessingException {
    for (IColumn column : m_columnSet.getColumns()) {
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

  /**
   * Override to exclude certain rows from being exported.
   * <p>
   * As default every row is accepted.
   */
  public boolean acceptExport(ITableRow row) throws ProcessingException {
    return true;
  }

  /**
   * Override to exclude certain rows from being imported.
   * <p>
   * As default every row is accepted.
   */
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
      if ((!propertyType.isPrimitive()) && (!propertyType.isInterface()) && !Serializable.class.isAssignableFrom(propertyType)) {
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

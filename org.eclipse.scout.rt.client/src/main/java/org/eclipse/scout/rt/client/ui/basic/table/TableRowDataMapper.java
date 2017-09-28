/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.ColumnData;
import org.eclipse.scout.rt.client.dto.ColumnData.SdkColumnCommand;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.reflect.IPropertyFilter;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.TableRowDataPropertyFilter;
import org.eclipse.scout.rt.shared.extension.IInternalExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ITableRowDataMapper}.
 *
 * @since 3.8.2
 */
public class TableRowDataMapper implements ITableRowDataMapper {
  private static final Logger LOG = LoggerFactory.getLogger(TableRowDataMapper.class);

  private final Map<IColumn, FastPropertyDescriptor> m_propertyDescriptorByColumn;
  private final ColumnSet m_columnSet;
  private final Set<IColumn<?>> m_ignoredColumns;

  public TableRowDataMapper(Class<? extends AbstractTableRowData> rowType, ColumnSet columnSet) {
    Assertions.assertNotNull(rowType);
    Assertions.assertNotNull(columnSet);

    m_columnSet = columnSet;
    IPropertyFilter filter = createPropertyFilter();
    List<FastPropertyDescriptor> props = new LinkedList<>();
    Collections.addAll(props, BeanUtility.getFastPropertyDescriptors(rowType, AbstractTableRowData.class, filter));
    Set<Class<?>> contributions = BEANS.get(IInternalExtensionRegistry.class).getContributionsFor(rowType);
    for (Class<?> contribution : contributions) {
      Collections.addAll(props, BeanUtility.getFastPropertyDescriptors(contribution, Object.class, filter));
    }

    m_propertyDescriptorByColumn = new HashMap<>(props.size());
    for (FastPropertyDescriptor rowDataPropertyDesc : props) {
      IColumn column = findColumn(columnSet, rowDataPropertyDesc);
      if (column != null) {
        m_propertyDescriptorByColumn.put(column, rowDataPropertyDesc);
      }
      else {
        LOG.warn("No column found for property [{}#{}]", rowDataPropertyDesc.getBeanClass().getName(), rowDataPropertyDesc.getName());
      }
    }
    // compute ignored columns
    Set<IColumn<?>> ignoredColumns = null;
    for (IColumn<?> col : columnSet.getColumns()) {
      if (isColumnIgnored(col)) {
        if (ignoredColumns == null) {
          ignoredColumns = new HashSet<>();
        }
        ignoredColumns.add(col);
      }
    }
    if (ignoredColumns != null) {
      m_ignoredColumns = ignoredColumns;
    }
    else {
      m_ignoredColumns = CollectionUtility.hashSet();
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
    if (s == null || s.isEmpty()) {
      return null;
    }
    if (s.length() == 1) {
      return s.toUpperCase();
    }
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  @Override
  public void importTableRowData(ITableRow row, AbstractTableRowData rowData) {
    for (IColumn column : m_columnSet.getColumns()) {
      if (m_ignoredColumns.contains(column)) {
        continue;
      }
      Object value = getValue(column, rowData);
      column.importValue(row, value);
    }
    row.setStatus(rowData.getRowState());
    importCustomValues(row, rowData);
  }

  public void importCustomValues(ITableRow row, AbstractTableRowData rowData) {
    if (rowData.getCustomValues() == null) {
      return;
    }
    Map<String, Object> customValuesCopy = new HashMap<>(rowData.getCustomValues());
    for (IColumn col : m_columnSet.getColumns()) {
      customValuesCopy.remove(col.getColumnId());
    }
    for (Entry<String, Object> entry : customValuesCopy.entrySet()) {
      row.setCustomValue(entry.getKey(), entry.getValue());
    }
  }

  private Object getValue(IColumn<?> column, AbstractTableRowData rowData) {
    Object value = null;
    FastPropertyDescriptor propertyDesc = m_propertyDescriptorByColumn.get(column);
    if (propertyDesc != null) {
      try {
        Method columnReadMethod = propertyDesc.getReadMethod();
        Object dto = getDataContainer(rowData, columnReadMethod.getDeclaringClass());
        value = columnReadMethod.invoke(dto);
      }
      catch (Exception e) {
        LOG.warn("Error reading row data property for column [{}]", column.getClass().getName(), e);
      }
    }
    else {
      value = rowData.getCustomValue(column.getColumnId());
    }
    return value;
  }

  @Override
  public void exportTableRowData(ITableRow row, AbstractTableRowData rowData) {
    for (IColumn column : m_columnSet.getColumns()) {
      if (m_ignoredColumns.contains(column)) {
        continue;
      }
      Object value = column.getValue(row);
      FastPropertyDescriptor propertyDesc = m_propertyDescriptorByColumn.get(column);
      if (propertyDesc != null) {
        try {
          Method columnWriteMethod = propertyDesc.getWriteMethod();
          Object dto = getDataContainer(rowData, columnWriteMethod.getDeclaringClass());
          columnWriteMethod.invoke(dto, value);
        }
        catch (Exception t) {
          LOG.warn("Error writing row data property for column [{}]", column.getClass().getName(), t);
        }
      }
      else {
        rowData.setCustomValue(column.getColumnId(), value);
      }
    }
    rowData.setRowState(row.getStatus());

    exportCustomValues(row, rowData);
  }

  public void exportCustomValues(ITableRow row, AbstractTableRowData rowData) {
    Set<Entry<String, Object>> entries = row.getCustomValues().entrySet();
    for (Entry<String, Object> entry : entries) {
      rowData.setCustomValue(entry.getKey(), entry.getValue());
    }
  }

  protected Object getDataContainer(AbstractTableRowData rowData, Class<?> dataOwnerClass) {
    if (dataOwnerClass.isAssignableFrom(rowData.getClass())) {
      return rowData;
    }
    return rowData.getContribution(dataOwnerClass);
  }

  @Override
  public boolean acceptExport(ITableRow row) {
    return true;
  }

  @Override
  public boolean acceptImport(AbstractTableRowData rowData) {
    return true;
  }
}

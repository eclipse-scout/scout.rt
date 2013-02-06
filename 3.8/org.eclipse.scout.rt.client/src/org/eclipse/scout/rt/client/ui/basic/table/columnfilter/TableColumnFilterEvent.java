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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * @since 3.8.0
 */
public class TableColumnFilterEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  public static final int TYPE_FILTER_ADDED = 10;
  public static final int TYPE_FILTER_CHANGED = 20;
  public static final int TYPE_FILTER_REMOVED = 30;
  public static final int TYPE_FILTERS_RESET = 40;

  private final int m_type;
  private final IColumn<?> m_column;

  /**
   * @param source
   */
  public TableColumnFilterEvent(ITable table, int type) {
    this(table, type, null);
  }

  public TableColumnFilterEvent(ITable table, int type, IColumn<?> column) {
    super(table);
    m_type = type;
    m_column = column;
  }

  public ITable getTable() {
    return (ITable) getSource();
  }

  public ITableColumnFilterManager getColumnFilterManager() {
    if (getTable() == null) {
      return null;
    }
    return getTable().getColumnFilterManager();
  }

  public int getType() {
    return m_type;
  }

  public IColumn<?> getColumn() {
    return m_column;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("DialogEvent[");
    // decode type
    try {
      Field[] f = getClass().getDeclaredFields();
      for (int i = 0; i < f.length; i++) {
        if (Modifier.isPublic(f[i].getModifiers()) && Modifier.isStatic(f[i].getModifiers()) && f[i].getName().startsWith("TYPE_")) {
          if (((Number) f[i].get(null)).intValue() == m_type) {
            sb.append(f[i].getName());
            break;
          }
        }
      }
    }
    catch (Throwable t) {
      sb.append("#" + m_type);
    }
    // table
    if (getTable() != null) {
      sb.append(" " + getTable().getClass());
    }
    sb.append("]");
    return sb.toString();
  }
}

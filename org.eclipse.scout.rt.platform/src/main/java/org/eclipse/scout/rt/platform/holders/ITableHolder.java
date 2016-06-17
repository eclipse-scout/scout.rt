/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.holders;

/**
 * @since 3.0
 */

/**
 * Holder for table data value access of columns is done by Bean introspection column methods must have a rowIndex
 * argument, see example comment below
 */
public interface ITableHolder {
  /**
   * same number as
   *
   * @see org.eclipse.scout.rt.platform.holders.ITableBeanRowHolder
   */
  int STATUS_NON_CHANGED = 0;
  /**
   * same number as
   *
   * @see org.eclipse.scout.rt.platform.holders.ITableBeanRowHolder
   */
  int STATUS_INSERTED = 1;
  /**
   * same number as
   *
   * @see org.eclipse.scout.rt.platform.holders.ITableBeanRowHolder
   */
  int STATUS_UPDATED = 2;
  /**
   * same number as
   *
   * @see org.eclipse.scout.rt.platform.holders.ITableBeanRowHolder
   */
  int STATUS_DELETED = 3;

  int/* newIndex */ addRow();

  void ensureSize(int size);

  int getRowCount();

  int getColumnCount();

  int getRowState(int rowIndex);

  void setRowState(int rowIndex, int state);

  /**
   * The implementation will delegate this getter to one of the column getters
   */
  Object getValueAt(int row, int column);

  /**
   * The implementation will delegate this setter to one of the column setters
   *
   * @throws IllegalArgumentException
   */
  void setValueAt(int row, int column, Object value);

  /**
   * Sample of column property of type String
   */
  /*
   * String getCityColumn(int rowIndex){ ... } setCityColumn(int rowIndex,String
   * value){ ... }
   */

}

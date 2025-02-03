/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.holders;

/**
 * Holder for bean based table. {@link ITableBeanRowHolder} are used for storing the table's contents.
 *
 * @since 3.10.0-M3
 */
public interface ITableBeanHolder {

  /**
   * @return Returns the number of available table row data.
   */
  int getRowCount();

  /**
   * @return Returns all rows.
   */
  ITableBeanRowHolder[] getRows();

  /**
   * @return Creates, adds and returns a new {@link ITableBeanRowHolder}. Its row state is initialized with
   * {@link ITableBeanRowHolder#STATUS_NON_CHANGED} and its type is the one returned by {@link #getRowType()}.
   */
  ITableBeanRowHolder addRow();

  /**
   * @return Returns the type of the rows managed by this
   * {@link org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData}.
   */
  Class<? extends ITableBeanRowHolder> getRowType();

  /**
   * Removes the row at the given index.
   *
   * @param index
   */
  void removeRow(int index);
}

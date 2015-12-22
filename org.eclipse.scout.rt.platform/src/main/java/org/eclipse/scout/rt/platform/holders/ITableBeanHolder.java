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
   *         {@link ITableBeanRowHolder#STATUS_NON_CHANGED} and its type is the one returned by {@link #getRowType()}.
   */
  ITableBeanRowHolder addRow();

  /**
   * @return Returns the type of the rows managed by this
   *         {@link org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData}.
   */
  Class<? extends ITableBeanRowHolder> getRowType();

  /**
   * Removes the row at the given index.
   *
   * @param index
   */
  void removeRow(int index);
}

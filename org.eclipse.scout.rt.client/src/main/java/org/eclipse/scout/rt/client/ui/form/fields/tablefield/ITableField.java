/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.status.IStatus;

public interface ITableField<T extends ITable> extends IFormField {

  /**
   * {@link ITable}
   */
  String PROP_TABLE = "table";

  T getTable();

  /**
   * Install a (new) table into the table field.
   *
   * @param externallyManaged
   *          true means init, dispose, load and store are not handled by the table field
   */
  void setTable(T newTable, boolean externallyManaged);

  /**
   * Convenience function for <code>getTable().getTableStatus()</code> (returns <code>null</code> when table is
   * <code>null</code>).
   */
  IStatus getTableStatus();

  /**
   * Convenience function for <code>getTable().setTableStatus(status)</code> (does nothing when table is
   * <code>null</code>).
   */
  void setTableStatus(IStatus tableStatus);

  /**
   * Convenience function for <code>getTable().isTableStatusVisible()</code> (returns <code>false</code> when table is
   * <code>null</code>).
   */
  boolean isTableStatusVisible();

  /**
   * Convenience function for <code>getTable().setTableStatusVisible(b)</code> (does nothing when table is
   * <code>null</code>).
   */
  void setTableStatusVisible(boolean tableStatusVisible);

  void doSave();

  /**
   * Reload data due to - master value change - sort by data source
   */
  void reloadTableData();
}

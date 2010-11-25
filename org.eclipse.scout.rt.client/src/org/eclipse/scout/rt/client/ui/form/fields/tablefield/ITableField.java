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
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public interface ITableField<T extends ITable> extends IFormField {

  /**
   * {@link ITable}
   */
  String PROP_TABLE = "table";
  /**
   * {@link String}
   */
  String PROP_TABLE_STATUS = "tableStatus";
  /**
   * {@link Boolean}
   */
  String PROP_TABLE_STATUS_VISIBLE = "tableStatusVisible";

  T getTable();

  /**
   * Install a (new) table into the table field.
   * 
   * @param externallyManaged
   *          true means init, dispose, load and store are not handled by the
   *          table field
   */
  void setTable(T newTable, boolean externallyManaged);

  /**
   * get the status of the table
   */
  String getTableStatus();

  /**
   * set a status on the table
   * <p>
   * this is normally displayed in a status bar on the bottom of the table gui
   */
  void setTableStatus(String s);

  /**
   * @return true if the table status is visible, false of not
   */
  boolean isTableStatusVisible();

  void setTableStatusVisible(boolean b);

  /**
   * Calls {@link #execUpdateStatus()} that by default builds a status text and calls {@link #setTableStatus(String)}.<br>
   */
  void updateTableStatus();

  void doSave() throws ProcessingException;

  /**
   * Reload data due to - master value change - sort by data source
   */
  void reloadTableData() throws ProcessingException;
}

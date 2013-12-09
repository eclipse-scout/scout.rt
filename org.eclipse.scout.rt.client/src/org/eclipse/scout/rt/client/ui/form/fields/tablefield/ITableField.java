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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public interface ITableField<T extends ITable> extends IFormField {

  /**
   * {@link ITable}
   */
  String PROP_TABLE = "table";
  /**
   * {@link IProcessingStatus}
   */
  String PROP_TABLE_SELECTION_STATUS = "tableSelectionStatus";
  /**
   * {@link IProcessingStatus}
   */
  String PROP_TABLE_POPULATE_STATUS = "tablePopulateStatus";
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
   * @return the selected row count, total row count and the sum of all numeric columns
   */
  String createDefaultTableStatus();

  /**
   * Convenience for {@link #getTableSelectionStatus()} and only reading the message text
   */
  String getTableStatus();

  /**
   * Convenience for {@link #setTableSelectionStatus(IProcessingStatus)} with a string. Creates a status with
   * {@link IStatus#INFO}
   */
  void setTableStatus(String s);

  /**
   * @return the status of the table data row count and selection
   */
  IProcessingStatus getTableSelectionStatus();

  /**
   * set the selection status on the table
   * <p>
   * this is normally displayed in a status bar on the bottom of the table gui
   * <p>
   * Use {@link AbstractTableField#execUpdateTableStatus()} to set the (new) status. This is called by
   * {@link #updateTableStatus()} whenever relevant changes happened.
   * <p>
   * see also {@link #createDefaultTableStatus()}
   * <p>
   * see {@link #setTableSelectionStatus(IProcessingStatus)}
   */
  void setTableSelectionStatus(IProcessingStatus status);

  /**
   * @return the data fetching>/loading status, warnings and other general messages related with data currently loaded
   *         into this table
   */
  IProcessingStatus getTablePopulateStatus();

  /**
   * set the data loading status on the table
   * <p>
   * this is normally displayed in a status bar on the bottom of the table gui
   * <p>
   * Use {@link AbstractTableField#execUpdateTableStatus()} to set the (new) status. This is called by
   * {@link #updateTableStatus()} whenever relevant changes happened.
   * <p>
   * see also {@link #createDefaultTableStatus()}
   * <p>
   * see {@link #setTableSelectionStatus(IProcessingStatus)}
   * <p>
   * see {@link #setTablePopulateStatus(IProcessingStatus)}
   */
  void setTablePopulateStatus(IProcessingStatus status);

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

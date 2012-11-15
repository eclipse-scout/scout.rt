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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;

/**
 * Note: TableRows are equal-consistent by their rowIndex
 */
public interface ITableRow {
  /**
   * same number as
   * 
   * @see AbstractTableFieldData
   */
  int STATUS_NON_CHANGED = 0;
  /**
   * same number as
   * 
   * @see AbstractTableFieldData
   */
  int STATUS_INSERTED = 1;
  /**
   * same number as
   * 
   * @see AbstractTableFieldData
   */
  int STATUS_UPDATED = 2;
  /**
   * same number as
   * 
   * @see AbstractTableFieldData
   */
  int STATUS_DELETED = 3;

  ITable getTable();

  int getRowIndex();

  boolean isEnabled();

  void setEnabled(boolean b);

  boolean isSelected();

  boolean isChecked();

  void setChecked(boolean b);

  boolean isFilterAccepted();

  int getCellCount();

  ICell getCell(IColumn column);

  ICell getCell(int columnIndex);

  void setCell(IColumn column, ICell cell) throws ProcessingException;

  void setCell(int columnIndex, ICell cell) throws ProcessingException;

  Cell getCellForUpdate(IColumn column);

  Cell getCellForUpdate(int columnIndex);

  Object getCellValue(int columnIndex);

  /**
   * Warning: this method is not validating the new value against the
   * corresponding table column use {@link IColumn#setValue(ITableRow, Object)} instead
   * 
   * @return true if value was in fact changed
   */
  boolean setCellValue(int columnIndex, Object value) throws ProcessingException;

  /**
   * Warning: this method is not validating the new value against the
   * corresponding table column use {@link IColumn#setValue(ITableRow, Object)} instead
   * 
   * @return true if value was in fact changed
   */
  boolean setCellValues(Object[] values) throws ProcessingException;

  Object[] getKeyValues();

  int getStatus();

  void setStatus(int rowStatus);

  boolean isStatusInserted();

  void setStatusInserted();

  boolean isStatusUpdated();

  void setStatusUpdated();

  boolean isStatusDeleted();

  void setStatusDeleted();

  boolean isStatusNonchanged();

  void setStatusNonchanged();

  /**
   * Convenience method for forced table row change
   */
  void touch() throws ProcessingException;

  /**
   * Convenience method for getTable().deleteRow(this);
   */
  void delete() throws ProcessingException;

  /**
   * Convenience method for setting background on all cells of this row
   */
  void setBackgroundColor(String c);

  /**
   * Convenience method for setting foreground on all cells of this row
   */
  void setForegroundColor(String c);

  /**
   * Convenience method for setting font on all cells of this row
   */
  void setFont(FontSpec f);

  /**
   * Convenience method for setting tooltipText on all cells of this row
   */
  void setTooltipText(String s);

  String getIconId();

  void setIconId(String id);

  boolean isRowPropertiesChanged();

  void setRowPropertiesChanged(boolean b);

  boolean isRowChanging();

  /**
   * if this is the top level caller calling with b=false, then calls {@link ITable#updateRow(ITableRow)}
   */
  void setRowChanging(boolean b);

  /**
   * move this row to the top this method has no effect if sorting is activated
   * on the table
   */
  void moveToTop();

  /**
   * move this row to the bottom this method has no effect if sorting is
   * activated on the table
   */
  void moveToBottom();

  /**
   * move this row one up this method has no effect if sorting is activated on
   * the table
   */
  void moveUp();

  /**
   * move this row one down this method has no effect if sorting is activated on
   * the table
   */
  void moveDown();
}

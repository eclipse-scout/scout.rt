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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.holders.ITableBeanRowHolder;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

/**
 * Note: TableRows are equal-consistent by their rowIndex
 */
public interface ITableRow {

  /**
   * @see {@link ITableBeanRowHolder#STATUS_NON_CHANGED}
   */
  int STATUS_NON_CHANGED = ITableBeanRowHolder.STATUS_NON_CHANGED;

  /**
   * @see {@link ITableBeanRowHolder#STATUS_INSERTED}
   */
  int STATUS_INSERTED = ITableBeanRowHolder.STATUS_INSERTED;

  /**
   * @see {@link ITableBeanRowHolder#STATUS_UPDATED}
   */
  int STATUS_UPDATED = ITableBeanRowHolder.STATUS_UPDATED;

  /**
   * @see {@link ITableBeanRowHolder#STATUS_DELETED}
   */
  int STATUS_DELETED = ITableBeanRowHolder.STATUS_DELETED;

  ITable getTable();

  int getRowIndex();

  boolean isEnabled();

  void setEnabled(boolean b);

  boolean isSelected();

  boolean isChecked();

  void setChecked(boolean b);

  boolean isFilterAccepted();

  /**
   * Indicates whether {@link #isFilterAccepted()} returns false because the row has been filtered by the user.
   *
   * @return true if @link IUserFilter is the only filter not accepting the row.
   */
  boolean isRejectedByUser();

  int getCellCount();

  ICell getCell(IColumn column);

  ICell getCell(int columnIndex);

  void setCell(IColumn column, ICell cell);

  void setCell(int columnIndex, ICell cell);

  Cell getCellForUpdate(IColumn column);

  Cell getCellForUpdate(int columnIndex);

  Object getCellValue(int columnIndex);

  Object getCustomValue(String id);

  Map<String, Object> getCustomValues();

  void setCustomValue(String id, Object value);

  /**
   * Warning: this method is not validating the new value against the corresponding table column use
   * {@link IColumn#setValue(ITableRow, Object)} instead
   *
   * @return true if value was in fact changed
   */
  boolean setCellValue(int columnIndex, Object value);

  /**
   * Warning: this method is not validating the new value against the corresponding table column use
   * {@link IColumn#setValue(ITableRow, Object)} instead
   *
   * @return true if value was in fact changed
   */
  boolean setCellValues(List<?> values);

  List<Object> getKeyValues();

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
  void touch();

  /**
   * Convenience method for getTable().deleteRow(this);
   */
  void delete();

  /**
   * Convenience method for setting background on all cells of this row
   */
  void setBackgroundColor(String c);

  /**
   * Convenience method for setting foreground on all cells of this row
   */
  void setForegroundColor(String c);

  /**
   * Convenience method for setting css class on all cells of this row
   */
  void setCssClass(String cssClass);

  String getCssClass();

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

  /**
   * @return set of column indexes that have been updated on the current table row (only valid during "rowChanging", by
   *         default only on InternalTableRow). Return value is never <code>null</code>.
   */
  Set<Integer> getUpdatedColumnIndexes();

  /**
   * Manually set result for {@link #getUpdatedColumnIndexes()}. May have no effect when
   * {@link #getUpdatedColumnIndexes()} is overridden by subclass.
   */
  void setUpdatedColumnIndexes(Set<Integer> updatedColumnIndexes);

  /**
   * @return set of column indexes that have changed the value of the given changeBit. (only valid during "rowChanging",
   *         by default only on InternalTableRow). Return value is never <code>null</code>.
   */
  Set<Integer> getUpdatedColumnIndexes(int changedBit);

  boolean isRowChanging();

  void setRowChanging(boolean b);

  /**
   * move this row to the top this method has no effect if sorting is activated on the table
   */
  void moveToTop();

  /**
   * move this row to the bottom this method has no effect if sorting is activated on the table
   */
  void moveToBottom();

  /**
   * move this row one up this method has no effect if sorting is activated on the table
   */
  void moveUp();

  /**
   * move this row one down this method has no effect if sorting is activated on the table
   */
  void moveDown();
}

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

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Columns are defined as inner classes for every inner column class there is a generated getXYColumn method directly on
 * the table
 */
public interface ITableUIFacade {

  boolean isUIProcessing();

  /**
   * A mouse single click on a row or (for checkable tables) the space key triggers this action
   */
  void fireRowClickFromUI(ITableRow row, MouseButton mouseButton);

  /**
   * A rows checked or unchecked
   */
  void setCheckedRowsFromUI(List<? extends ITableRow> rows, boolean checked);

  /**
   * A mouse double click on a row triggers this action
   */
  void fireRowActionFromUI(ITableRow row);

  void fireColumnMovedFromUI(IColumn<?> c, int toViewIndex);

  /**
   * @param visibleColumns
   */
  void fireVisibleColumnsChangedFromUI(Collection<IColumn<?>> visibleColumns);

  void setColumnWidthFromUI(IColumn<?> c, int newWidth);

  /**
   * @param column
   * @param multiSort
   *          True: Multiple sort columns are supported, which means the given column is added to the list of sort
   *          columns, if not added yet.<br>
   *          False: The selected column is set as the (new) primary sort column.<br>
   * @param ascending
   *          Specifies the new sort direction
   */
  void fireHeaderSortFromUI(IColumn<?> column, boolean multiSort, boolean ascending);

  void fireHeaderGroupFromUI(IColumn<?> c, boolean multiGroup, boolean ascending);

  void fireAggregationFunctionChanged(INumberColumn<?> c, String function);

  void setSelectedRowsFromUI(List<? extends ITableRow> rows);

  /**
   * Drag selected rows
   */
  TransferObject fireRowsDragRequestFromUI();

  /**
   * To copy selected rows into clipboard
   */
  TransferObject fireRowsCopyRequestFromUI();

  void fireRowDropActionFromUI(ITableRow row, TransferObject dropData);

  /**
   * Set the column that represents the last ui (mouse click) column context
   */
  void setContextColumnFromUI(IColumn<?> col);

  /**
   * @return the editing field or group box or null if no editing is available right now
   *         <p>
   *         The ui should call {@link #completeCellEditFromUI()} or {@link #cancelCellEditFromUI()} at some time.
   */
  IFormField prepareCellEditFromUI(ITableRow row, IColumn<?> col);

  void completeCellEditFromUI();

  void cancelCellEditFromUI();

  boolean fireKeyTypedFromUI(String keyStrokeText, char keyChar);

  void fireAppLinkActionFromUI(String ref);

  void fireTableReloadFromUI();

  void fireTableResetFromUI();

  void fireSortColumnRemovedFromUI(IColumn<?> column);

  void fireGroupColumnRemovedFromUI(IColumn<?> column);

  void fireOrganizeColumnAddFromUI(IColumn<?> column);

  void fireOrganizeColumnRemoveFromUI(IColumn<?> column);

  void fireOrganizeColumnModifyFromUI(IColumn<?> column);

  void fireFilterAddedFromUI(IUserFilterState filter);

  void fireFilterRemovedFromUI(IUserFilterState filter);

  void setFilteredRowsFromUI(List<? extends ITableRow> rows);

  void removeFilteredRowsFromUI();

  void setColumnBackgroundEffect(INumberColumn<?> column, String mode);
}

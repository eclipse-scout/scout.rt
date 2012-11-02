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

import java.net.URL;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Columns are defined as inner classes for every inner column class there is a
 * generated getXYColumn method directly on the table
 */
public interface ITableUIFacade {

  boolean isUIProcessing();

  /**
   * A mouse single click on a row or (for checkable tables) the space key triggers this action
   */
  void fireRowClickFromUI(ITableRow row);

  /**
   * A mouse double click on a row triggers this action
   */
  void fireRowActionFromUI(ITableRow row);

  /**
   * Popup on selected row(s)
   */
  IMenu[] fireRowPopupFromUI();

  /**
   * Popup on empty space
   */
  IMenu[] fireEmptySpacePopupFromUI();

  IMenu[] fireHeaderPopupFromUI();

  void fireColumnMovedFromUI(IColumn<?> c, int toViewIndex);

  /**
   * @param visibleColumns
   */
  void fireVisibleColumnsChangedFromUI(IColumn<?>[] visibleColumns);

  void setColumnWidthFromUI(IColumn<?> c, int newWidth);

  /**
   * @param column
   * @param multiSort
   *          true = multiple sort columns are supported, every event toggles
   *          the current column between the states ON-ASCENDING (add to tail of
   *          sort columns), ON-DESCENDING. False = the selected column is set
   *          as the (new) primary sort column, if already set it is toggled
   *          between ascending and descending
   */
  void fireHeaderSortFromUI(IColumn<?> column, boolean multiSort);

  void setSelectedRowsFromUI(ITableRow[] rows);

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

  void fireHyperlinkActionFromUI(ITableRow row, IColumn<?> column, URL url);
}

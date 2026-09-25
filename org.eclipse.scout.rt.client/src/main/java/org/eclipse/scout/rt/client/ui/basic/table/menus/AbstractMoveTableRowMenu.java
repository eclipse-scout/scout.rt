/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.menus;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;

@ClassId("361b242b-0798-4832-9045-68b1e0cf097f")
public abstract class AbstractMoveTableRowMenu extends AbstractMenu {

  protected ITable getTable() {
    return getParentOfType(ITable.class);
  }

  @Override
  protected byte getConfiguredHorizontalAlignment() {
    return HORIZONTAL_ALIGNMENT_RIGHT;
  }

  @Override
  protected String getConfiguredIconId() {
    return getConfiguredMoveUp() ? AbstractIcons.AngleUp : AbstractIcons.AngleDown;
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return AbstractAction.combineKeyStrokes(IKeyStroke.CONTROL, getConfiguredMoveUp() ? IKeyStroke.UP : IKeyStroke.DOWN);
  }

  @Override
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(TableMenuType.EmptySpace);
  }

  /**
   * @return true for move up, false for move down; default true
   */
  protected abstract boolean getConfiguredMoveUp();

  @Override
  protected void execInitAction() {
    updateEnabled();
    // We install our own "owner value changed" listener, because the real one is not fired for EmptySpace menus
    getTable().addTableListener(e -> updateEnabled(),
        TableEvent.TYPE_ROWS_SELECTED, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED,
        TableEvent.TYPE_ROWS_DELETED, TableEvent.TYPE_ROW_ORDER_CHANGED, TableEvent.TYPE_ROW_FILTER_CHANGED);
  }

  protected void updateEnabled() {
    ITable table = getTable();
    boolean canMove = table.getSelectedRowCount() > 0 && table.getSelectedRows().stream()
        .map(table::getFilteredRowIndex)
        .noneMatch(rowIndex -> rowIndex == (getConfiguredMoveUp() ? 0 : table.getFilteredRowCount() - 1));
    setEnabled(canMove);
  }

  @Override
  protected void execAction() { // only called if enabled
    List<ITableRow> selectedRows = getTable().getSelectedRows();
    if (!getConfiguredMoveUp()) {
      Collections.reverse(selectedRows); // live reverse is alright as
    }
    selectedRows.forEach(this::execMoveRow);
  }

  protected void execMoveRow(ITableRow row) {
    ITable table = getTable();
    int selectedFilteredRowIndex = table.getFilteredRowIndex(row);
    BiConsumer<ITableRow, ITableRow> moveConsumer = getConfiguredMoveUp() ? table::moveRowBefore : table::moveRowAfter;
    moveConsumer.accept(row, table.getFilteredRow(selectedFilteredRowIndex + (getConfiguredMoveUp() ? -1 : 1)));
  }
}

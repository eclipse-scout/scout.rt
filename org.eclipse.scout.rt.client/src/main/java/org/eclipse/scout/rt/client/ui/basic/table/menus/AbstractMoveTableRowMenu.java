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
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;

@ClassId("361b242b-0798-4832-9045-68b1e0cf097f")
public abstract class AbstractMoveTableRowMenu extends AbstractMenu {

  private static final Integer[] UPDATE_ENABLED_EVENT_TYPES = {
      TableEvent.TYPE_ROWS_SELECTED, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED,
      TableEvent.TYPE_ROWS_DELETED, TableEvent.TYPE_ROW_ORDER_CHANGED, TableEvent.TYPE_ROW_FILTER_CHANGED, TableEvent.TYPE_COLUMN_HEADERS_UPDATED};

  private ITable m_table;
  private TableListener m_updateEnabledListener;

  /**
   * @return table of this menu, available after {@link #execInitAction()}
   */
  protected ITable getTable() {
    return m_table;
  }

  @Override
  protected byte getConfiguredHorizontalAlignment() {
    return HORIZONTAL_ALIGNMENT_RIGHT;
  }

  @Override
  protected String getConfiguredIconId() {
    return isMoveUp() ? AbstractIcons.AngleUp : AbstractIcons.AngleDown;
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return AbstractAction.combineKeyStrokes(IKeyStroke.CONTROL, isMoveUp() ? IKeyStroke.UP : IKeyStroke.DOWN);
  }

  @Override
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(TableMenuType.EmptySpace);
  }

  /**
   * @return true for move up, false for move down
   */
  protected abstract boolean isMoveUp();

  @Override
  protected void execInitAction() {
    uninstallUpdateEnabledListener(); // in case of a re-init
    m_table = getParentOfType(ITable.class);
    // We install our own "owner value changed" listener, because the real one is not fired for EmptySpace menus
    m_updateEnabledListener = e -> updateEnabled();
    m_table.addTableListener(m_updateEnabledListener, UPDATE_ENABLED_EVENT_TYPES);
    updateEnabled();
  }

  @Override
  protected void execDispose() {
    uninstallUpdateEnabledListener();
  }

  protected void uninstallUpdateEnabledListener() {
    if (m_table != null) {
      m_table.removeTableListener(m_updateEnabledListener, UPDATE_ENABLED_EVENT_TYPES);
      m_table = null;
      m_updateEnabledListener = null;
    }
  }

  protected void updateEnabled() {
    boolean sorted = m_table.isSortEnabled() && m_table.getColumnSet().getSortColumnCount() > 0; // manual order has no meaning in a sorted table
    boolean canMove = !sorted && m_table.getSelectedRowCount() > 0 && m_table.getSelectedRows().stream()
        .map(m_table::getFilteredRowIndex)
        .noneMatch(rowIndex -> rowIndex == (isMoveUp() ? 0 : m_table.getFilteredRowCount() - 1));
    setEnabled(canMove);
  }

  @Override
  protected void execAction() { // only called if enabled
    List<ITableRow> selectedRows = m_table.getSelectedRows();
    if (!isMoveUp()) {
      Collections.reverse(selectedRows); // reverse in place is fine: getSelectedRows() returns a copy; move the last row first when moving down
    }
    try {
      m_table.setTableChanging(true); // buffer events of all moves
      selectedRows.forEach(this::execMoveRow);
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  protected void execMoveRow(ITableRow row) {
    int selectedFilteredRowIndex = m_table.getFilteredRowIndex(row);
    BiConsumer<ITableRow, ITableRow> moveConsumer = isMoveUp() ? m_table::moveRowBefore : m_table::moveRowAfter;
    moveConsumer.accept(row, m_table.getFilteredRow(selectedFilteredRowIndex + (isMoveUp() ? -1 : 1)));
  }
}

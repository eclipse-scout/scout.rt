/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * AI Disclosure: This file was partially AI-generated.
 * The AI-generated portions are made available under CC0-1.0
 * and not subject to the project's licence.
 *
 * SPDX-License-Identifier: EPL-2.0 and CC0-1.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.menus;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractMoveTableRowUpMenu} and {@link AbstractMoveTableRowDownMenu}.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class MoveTableRowMenuTest {

  @Test
  public void testEnabledState() {
    P_Table table = createTable(false);
    assertEnabled(table, false, false); // no selection

    select(table, 0);
    assertEnabled(table, false, true); // first row

    select(table, 2);
    assertEnabled(table, true, true); // middle row

    select(table, 4);
    assertEnabled(table, true, false); // last row

    select(table, 0, 2);
    assertEnabled(table, false, true); // multi selection including first row

    select(table, 2, 4);
    assertEnabled(table, true, false); // multi selection including last row

    select(table, 1, 3);
    assertEnabled(table, true, true);

    table.selectRows(List.of(), false);
    assertEnabled(table, false, false);
  }

  @Test
  public void testMoveUp() {
    P_Table table = createTable(false);
    select(table, 2);
    table.getMenuByClass(P_Table.MoveUpMenu.class).doAction();
    assertOrder(table, 0, 2, 1, 3, 4);
    assertSelection(table, 2);
  }

  @Test
  public void testMoveDown() {
    P_Table table = createTable(false);
    select(table, 2);
    table.getMenuByClass(P_Table.MoveDownMenu.class).doAction();
    assertOrder(table, 0, 1, 3, 2, 4);
    assertSelection(table, 2);
  }

  @Test
  public void testMoveFirstRowUpIsNoop() {
    P_Table table = createTable(false);
    select(table, 0);
    table.getMenuByClass(P_Table.MoveUpMenu.class).doAction(); // disabled
    assertOrder(table, 0, 1, 2, 3, 4);
  }

  @Test
  public void testMoveLastRowDownIsNoop() {
    P_Table table = createTable(false);
    select(table, 4);
    table.getMenuByClass(P_Table.MoveDownMenu.class).doAction(); // disabled
    assertOrder(table, 0, 1, 2, 3, 4);
  }

  /**
   * Non-contiguous selection: each selected row moves by one, the relative order and the selection are kept.
   */
  @Test
  public void testMoveUpMultipleNonContiguous() {
    P_Table table = createTable(false);
    select(table, 1, 3);
    table.getMenuByClass(P_Table.MoveUpMenu.class).doAction();
    assertOrder(table, 1, 0, 3, 2, 4);
    assertSelection(table, 1, 3);
  }

  @Test
  public void testMoveDownMultipleNonContiguous() {
    P_Table table = createTable(false);
    select(table, 1, 3);
    table.getMenuByClass(P_Table.MoveDownMenu.class).doAction();
    assertOrder(table, 0, 2, 1, 4, 3);
    assertSelection(table, 1, 3);
  }

  /**
   * Contiguous selection moves as a block (rows must not overtake each other).
   */
  @Test
  public void testMoveDownMultipleContiguous() {
    P_Table table = createTable(false);
    select(table, 1, 2);
    table.getMenuByClass(P_Table.MoveDownMenu.class).doAction();
    assertOrder(table, 0, 3, 1, 2, 4);
  }

  @Test
  public void testMoveUpMultipleContiguous() {
    P_Table table = createTable(false);
    select(table, 2, 3);
    table.getMenuByClass(P_Table.MoveUpMenu.class).doAction();
    assertOrder(table, 0, 2, 3, 1, 4);
  }

  /**
   * Rows hidden by a row filter are skipped: the selected row moves before the previous visible row.
   */
  @Test
  public void testMoveUpSkipsFilteredRow() {
    P_Table table = createTable(false);
    ITableRow hiddenRow = table.getRow(1);
    table.addRowFilter(row -> row != hiddenRow);
    select(table, 2);
    assertEnabled(table, true, true);

    table.getMenuByClass(P_Table.MoveUpMenu.class).doAction();
    assertOrder(table, 2, 0, 1, 3, 4);
  }

  /**
   * First visible row (but not first row) cannot be moved up.
   */
  @Test
  public void testFirstVisibleRowCannotMoveUp() {
    P_Table table = createTable(false);
    ITableRow hiddenRow = table.getRow(0);
    table.addRowFilter(row -> row != hiddenRow);
    select(table, 1);
    assertEnabled(table, false, true);
  }

  /**
   * A manual order has no meaning in a sorted table: menus are disabled.
   */
  @Test
  public void testSortedTableDisablesMenus() {
    P_Table table = createTable(true);
    assertTrue(table.isSortEnabled());
    assertTrue(table.getColumnSet().getSortColumnCount() > 0);

    select(table, 2);
    assertEnabled(table, false, false);
  }

  /**
   * Sort columns are irrelevant if sorting is disabled.
   */
  @Test
  public void testSortColumnWithSortDisabled() {
    P_Table table = new P_Table(true);
    table.setSortEnabledConfig(false);
    table.init();
    addRows(table);

    select(table, 2);
    assertEnabled(table, true, true);
  }

  /**
   * Sort check disabled ({@link AbstractMoveTableRowMenu#isTableSorted()} overridden): menus are enabled in a sorted table and
   * rows can be moved.
   */
  @Test
  public void testSortCheckDisabled() {
    P_Table table = new P_Table(true);
    table.setSortCheckEnabled(false);
    table.init();
    addRows(table);
    assertTrue(table.getColumnSet().getSortColumnCount() > 0);

    select(table, 2);
    assertEnabled(table, true, true);

    table.getMenuByClass(P_Table.MoveUpMenu.class).doAction();
    assertOrder(table, 0, 2, 1, 3, 4);
  }

  /**
   * All moves of one action are buffered: a single row order changed event.
   */
  @Test
  public void testSingleRowOrderChangedEventPerAction() {
    P_Table table = createTable(false);
    select(table, 1, 3);
    List<TableEvent> orderChangedEvents = new ArrayList<>();
    table.addTableListener(orderChangedEvents::add, TableEvent.TYPE_ROW_ORDER_CHANGED);

    table.getMenuByClass(P_Table.MoveDownMenu.class).doAction();
    assertEquals(1, orderChangedEvents.size());
  }

  @Test
  public void testEnabledStateUpdatedAfterMove() {
    P_Table table = createTable(false);
    select(table, 1);
    assertEnabled(table, true, true);

    table.getMenuByClass(P_Table.MoveUpMenu.class).doAction(); // row 1 is now the first row
    assertEnabled(table, false, true);
  }

  @Test
  public void testEnabledStateUpdatedAfterRowsDeleted() {
    P_Table table = createTable(false);
    select(table, 3);
    assertEnabled(table, true, true);

    table.deleteRow(table.getRow(4)); // row 3 is now the last row
    assertEnabled(table, true, false);
  }

  // ---------------------------------------------------------------------------------------------------------------

  protected P_Table createTable(boolean sorted) {
    P_Table table = new P_Table(sorted);
    table.init();
    addRows(table);
    return table;
  }

  protected void addRows(P_Table table) {
    for (int i = 0; i < 5; i++) {
      table.addRowByArray(new Object[]{i});
    }
  }

  protected void select(P_Table table, Integer... values) {
    List<ITableRow> rows = new ArrayList<>();
    for (Integer value : values) {
      rows.add(table.getValueColumn().findRow(value));
    }
    table.selectRows(rows, false);
  }

  protected void assertOrder(P_Table table, Integer... expectedValues) {
    assertEquals(List.of(expectedValues), table.getValueColumn().getValues());
  }

  protected void assertSelection(P_Table table, Integer... expectedValues) {
    assertEquals(List.of(expectedValues), table.getValueColumn().getSelectedValues());
  }

  protected void assertEnabled(P_Table table, boolean upEnabled, boolean downEnabled) {
    assertEquals("move up enabled", upEnabled, table.getMenuByClass(P_Table.MoveUpMenu.class).isEnabled());
    assertEquals("move down enabled", downEnabled, table.getMenuByClass(P_Table.MoveDownMenu.class).isEnabled());
  }

  public static class P_Table extends AbstractTable {

    private final boolean m_sorted;
    private boolean m_sortEnabledConfig = true;
    private boolean m_sortCheckEnabled = true;

    public P_Table(boolean sorted) {
      super(false);
      m_sorted = sorted;
      callInitializer();
    }

    public void setSortCheckEnabled(boolean sortCheckEnabled) {
      m_sortCheckEnabled = sortCheckEnabled;
    }

    public void setSortEnabledConfig(boolean sortEnabled) {
      m_sortEnabledConfig = sortEnabled;
      setSortEnabled(sortEnabled);
    }

    @Override
    protected boolean getConfiguredSortEnabled() {
      return m_sortEnabledConfig;
    }

    @Override
    protected boolean getConfiguredMultiSelect() {
      return true;
    }

    public ValueColumn getValueColumn() {
      return getColumnSet().getColumnByClass(ValueColumn.class);
    }

    @Order(10)
    public class ValueColumn extends AbstractIntegerColumn {

      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }

      @Override
      protected int getConfiguredSortIndex() {
        return m_sorted ? 0 : -1;
      }
    }

    @Order(10)
    public class MoveUpMenu extends AbstractMoveTableRowUpMenu {

      @Override
      protected boolean isTableSorted() {
        return m_sortCheckEnabled && super.isTableSorted();
      }
    }

    @Order(20)
    public class MoveDownMenu extends AbstractMoveTableRowDownMenu {

      @Override
      protected boolean isTableSorted() {
        return m_sortCheckEnabled && super.isTableSorted();
      }
    }
  }
}

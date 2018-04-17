/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class HierarchicalTableTest {

  private void expectRowOrder(Integer[] primaryKeys, List<ITableRow> rows) {
    assertArrayEquals(primaryKeys, rows.stream()
        .map(row -> (Integer) row.getCellValue(0))
        .collect(Collectors.toList())
        .toArray(new Integer[primaryKeys.length]));
  }

  @Test
  public void testCorrectRowOrderAfterAddRows() throws Exception {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{3, 1}));
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, null}));
    table.addRows(rows);
    expectRowOrder(new Integer[]{1, 3, 2}, table.getRows());
  }

  /**
   * Table expect always to have all parent rows
   */
  @Test(expected = IllegalArgumentException.class)
  public void testAddRowsWithInvalidRowList() throws Exception {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, null}));
    rows.add(table.createRow(new Object[]{3, 4}));
    table.addRows(rows);
  }

  /**
   * Table expect always to have all parent rows
   */
  @Test(expected = IllegalArgumentException.class)
  public void testAddRowWithUnresolvedParentRow() throws Exception {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, null}));
    table.addRows(rows);

    table.addRow(table.createRow(new Object[]{3, 4}));
  }

  /**
   * Table does a minimal sort to ensure parent child order.
   */
  @Test
  public void testAddRow() throws Exception {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, null}));
    table.addRows(rows);

    table.addRow(table.createRow(new Object[]{3, 1}));
    expectRowOrder(new Integer[]{1, 3, 2}, table.getRows());
  }

  /**
   * Table removes cascading removes all child rows
   */
  @Test
  public void testRemoveParentRow() throws Exception {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, null}));
    rows.add(table.createRow(new Object[]{3, 1}));
    table.replaceRows(rows);
    rows = table.getRows();
    assertEquals(rows.get(0), table.findParentRow(rows.get(2)));

    // remove parent row and expect cascading deletion of child row
    table.deleteRow(rows.get(0));
    rows = table.getRows();
    assertEquals(1, rows.size());
    expectRowOrder(new Integer[]{2}, rows);
  }

  /**
   * <pre>
   * 1
   * |--2
   * |--3
   * 4
   * |--5
   *    |--6
   * </pre>
   */
  @Test
  public void testExpandCollapseAll() {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, 1}));
    rows.add(table.createRow(new Object[]{3, 1}));
    rows.add(table.createRow(new Object[]{4, null}));
    rows.add(table.createRow(new Object[]{5, 4}));
    rows.add(table.createRow(new Object[]{6, 5}));
    table.replaceRows(rows);
    rows = table.getRows();

    table.collapseAll(null);

    expectRowOrder(new Integer[]{},
        table.getRows().stream()
            .filter(row -> row.isExpanded())
            .collect(Collectors.toList()));

    table.expandAll(null);
    expectRowOrder(new Integer[]{1, 2, 3, 4, 5, 6},
        table.getRows().stream()
            .filter(row -> row.isExpanded())
            .collect(Collectors.toList()));
  }

  /**
   * <pre>
   * 1
   * |--2
   * |--3
   * 4
   * |--5
   *    |--6
   * </pre>
   */
  @Test
  public void testExpandCollapseRow() throws Exception {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, 1}));
    rows.add(table.createRow(new Object[]{3, 1}));
    rows.add(table.createRow(new Object[]{4, null}));
    rows.add(table.createRow(new Object[]{5, 4}));
    rows.add(table.createRow(new Object[]{6, 5}));
    table.addRows(rows);
    table.expandAll(null);
    rows = table.getRows();

    table.setRowExpanded(rows.get(3), false);
    expectRowOrder(new Integer[]{1, 2, 3, 5, 6},
        table.getRows().stream()
            .filter(row -> row.isExpanded())
            .collect(Collectors.toList()));

    table.setRowExpanded(rows.get(3), true);
    expectRowOrder(new Integer[]{1, 2, 3, 4, 5, 6},
        table.getRows().stream()
            .filter(row -> row.isExpanded())
            .collect(Collectors.toList()));

    table.setRowExpanded(rows.get(4), false);
    expectRowOrder(new Integer[]{1, 2, 3, 4, 6},
        table.getRows().stream()
            .filter(row -> row.isExpanded())
            .collect(Collectors.toList()));

    table.setRowExpanded(rows.get(3), false);
    expectRowOrder(new Integer[]{1, 2, 3, 6},
        table.getRows().stream()
            .filter(row -> row.isExpanded())
            .collect(Collectors.toList()));

    table.setRowExpanded(rows.get(4), true);
    expectRowOrder(new Integer[]{1, 2, 3, 5, 6},
        table.getRows().stream()
            .filter(row -> row.isExpanded())
            .collect(Collectors.toList()));

    table.setRowExpanded(rows.get(3), true);
    expectRowOrder(new Integer[]{1, 2, 3, 4, 5, 6},
        table.getRows().stream()
            .filter(row -> row.isExpanded())
            .collect(Collectors.toList()));
  }

  public static class P_SinglePrimaryKeyColumnTable extends AbstractTable {

    public ParentKeyColumn getParentKeyColumn() {
      return getColumnSet().getColumnByClass(ParentKeyColumn.class);
    }

    public PrimaryKeyColumn getPrimaryKeyColumn() {
      return getColumnSet().getColumnByClass(PrimaryKeyColumn.class);
    }

    @Order(100)
    @ClassId("d6a4727f-9e31-4b72-bc19-dfa614193d28")
    public class PrimaryKeyColumn extends AbstractIntegerColumn {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }
    }

    @Order(200)
    @ClassId("034c2e4a-b9fe-47a3-87c3-b53ee08297ad")
    public class ParentKeyColumn extends AbstractIntegerColumn {
      @Override
      protected boolean getConfiguredParentKey() {
        return true;
      }
    }
  }
}

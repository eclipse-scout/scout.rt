/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=395185">Bug 395185 -Table: Rows contained at TableEvent are in
 * random order when fired as batch</a>
 * <p>
 * If table rows are inserted while tableChanging is active, a batch event will be fired at the end. This batch event
 * should contain the table rows in the same order as they were inserted.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableRowInsertOrderTest {

  @Test
  public void testEventRowOrderAfterInsert() throws Exception {
    P_Table table = new P_Table();
    table.initTable();

    P_TableListener tableListener = new P_TableListener();
    table.addTableListener(tableListener);

    table.setTableChanging(true);
    try {
      for (int i = 0; i < 10; i++) {
        table.addRowByArray(new Object[]{i, "Item" + i});
      }
    }
    finally {
      table.setTableChanging(false);
    }

    assertTrue(CollectionUtility.equalsCollection(table.getRows(), tableListener.getInsertedRows()));
    //No order_change_event expected
    assertTrue(tableListener.getOrderedRows() == null);
  }

  @Test
  public void testEventRowOrderAfterInsertWithSort() throws Exception {
    P_Table table = new P_Table();
    table.getFirstColumn().setInitialSortIndex(0);
    table.getFirstColumn().setInitialSortIndex(1);
    table.getFirstColumn().setInitialSortAscending(false);
    table.initTable();

    P_TableListener tableListener = new P_TableListener();
    table.addTableListener(tableListener);

    for (int i = 0; i < 10; i++) {
      table.setTableChanging(true);
      try {
        table.addRowByArray(new Object[]{i, "Item" + i});
      }
      finally {
        table.setTableChanging(false);
      }
    }

    assertNotSame(table.getRows().get(0), tableListener.getInsertedRows().get(0));
    assertSame(table.getRows().get(table.getRows().size() - 1), tableListener.getInsertedRows().get(0));
    assertTrue(CollectionUtility.equalsCollection(table.getRows(), tableListener.getInsertedRows(), false));
    assertTrue(CollectionUtility.equalsCollection(table.getRows(), tableListener.getOrderedRows()));
  }

  private static class P_TableListener extends TableAdapter {
    private final List<ITableRow> m_insertedRows = new ArrayList<>();
    private List<ITableRow> m_orderedRows;

    @Override
    public void tableChanged(TableEvent e) {
      if (e.getType() == TableEvent.TYPE_ROWS_INSERTED) {
        m_insertedRows.addAll(e.getRows());
      }
      else if (e.getType() == TableEvent.TYPE_ROW_ORDER_CHANGED) {
        m_orderedRows = e.getRows();
      }
    }

    public List<ITableRow> getInsertedRows() {
      return m_insertedRows;
    }

    public List<ITableRow> getOrderedRows() {
      return m_orderedRows;
    }
  }

  private static class P_Table extends AbstractTable {

    public FirstColumn getFirstColumn() {
      return getColumnSet().getColumnByClass(FirstColumn.class);
    }

    @SuppressWarnings("unused")
    public SecondColumn getSecondColumn() {
      return getColumnSet().getColumnByClass(SecondColumn.class);
    }

    @Order(10)
    public class FirstColumn extends AbstractIntegerColumn {
    }

    @Order(20)
    public class SecondColumn extends AbstractStringColumn {
    }
  }
}

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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableEventOrderTest {

  @Test
  public void testRowsInsertedAfterColumnStructureChanged() throws Exception {
    P_Table table = new P_Table();
    table.initTable();

    P_TableListener tableListener = new P_TableListener();
    table.addTableListener(tableListener);

    table.setTableChanging(true);
    try {
      // Sends TYPE_COLUMN_STRUCTURE_CHANGED
      table.resetColumnConfiguration();

      // Sends TYPE_ROWS_INSERTED
      for (int i = 0; i < 10; i++) {
        table.addRowByArray(new Object[]{i, "Item" + i});
      }
    }
    finally {
      table.setTableChanging(false);
    }

    List<Integer> eventTypes = new ArrayList<Integer>();
    for (TableEvent event : tableListener.getBatch()) {
      eventTypes.add(event.getType());
    }

    // The event order is essential for the gui (e.g. if the gui holds a column map which needs to be refreshed if columns change)
    assertTrue(eventTypes.indexOf(TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED) < eventTypes.indexOf(TableEvent.TYPE_ROWS_INSERTED));
  }

  private static class P_TableListener extends TableAdapter {
    private List<? extends TableEvent> m_batch;

    @Override
    public void tableChangedBatch(List<? extends TableEvent> batch) {
      m_batch = batch;
    }

    public List<? extends TableEvent> getBatch() {
      return m_batch;
    }
  }

  private static class P_Table extends AbstractTable {

    @Order(10)
    public class FirstColumn extends AbstractIntegerColumn {
    }

    @Order(20)
    public class SecondColumn extends AbstractStringColumn {
    }
  }

}

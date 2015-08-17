/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that updating other columns is possible by adding a change listener.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractRowDataColumnTest {

  @Test
  public void testTablesObservedBeforeInsertionEvent() throws Exception {
    TestTable table = new TestTable();
    table.initTable();
    ITableRow row = table.createRow();
    table.getRowDataColumn().setValue(row, "newValue");
    row = table.addRow(row);

    assertEquals("newValue", table.getRowDataColumn().getDisplayText(table.getRow(0)));
    assertEquals("newValue", table.getRowDataColumn().getValue(0));
    assertEquals("newValue", table.getStringTestColumn().getValue(0));
    assertEquals("newValue", table.getStringTestColumn().getDisplayText(table.getRow(0)));
  }

  public class TestTable extends AbstractTable {

    public StringTestColumn getStringTestColumn() {
      return getColumnSet().getColumnByClass(StringTestColumn.class);
    }

    public RowDataColumn getRowDataColumn() {
      return getColumnSet().getColumnByClass(RowDataColumn.class);
    }

    @Order(10.0)
    public class StringTestColumn extends AbstractStringColumn {
    }

    @Order(20.0)
    public class RowDataColumn extends AbstractRowDataColumn<String> {

      @Override
      protected void updateTableColumns(ITableRow r, String newValue) throws ProcessingException {
        getStringTestColumn().setValue(r, newValue);
      }
    }
  }
}

abstract class AbstractRowDataColumn<T> extends AbstractColumn<T> {

  private TableListener m_updateTableRowListener;

  @Override
  public void initColumn() throws ProcessingException {
    super.initColumn();
    if (getTable() != null) {
      if (m_updateTableRowListener != null) {
        getTable().removeTableListener(m_updateTableRowListener);
      }
      m_updateTableRowListener = new P_UpdateTableRowListener(this);
      getTable().addTableListener(m_updateTableRowListener);
    }
  }

  @Override
  public void disposeColumn() throws ProcessingException {
    super.disposeColumn();
    if (getTable() != null) {
      if (m_updateTableRowListener != null) {
        getTable().removeTableListener(m_updateTableRowListener);
      }
    }
    m_updateTableRowListener = null;
  }

  /**
   * Updates all other columns based on this column's value.
   */
  protected abstract void updateTableColumns(ITableRow r, T newValue) throws ProcessingException;

  protected class P_UpdateTableRowListener extends TableAdapter {

    private final IColumn<?> m_column;

    public P_UpdateTableRowListener(IColumn<?> column) {
      m_column = column;
    }

    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROWS_INSERTED:
        case TableEvent.TYPE_ROWS_UPDATED: {
          try {
            getTable().setTableChanging(true);
            for (ITableRow row : e.getRows()) {
              // Trigger "updateTableColumn" when a row was inserted, or the value of this column is changed.
              // Do _not_ trigger the method when other columns change their values (this might lead to loops).
              if (e.getType() == TableEvent.TYPE_ROWS_INSERTED || e.getUpdatedColumns(row).contains(m_column)) {
                updateTableColumns(row, getValue(row));
              }
            }
          }
          catch (ProcessingException ex) {
            BEANS.get(ExceptionHandler.class).handle(ex);
          }
          finally {
            getTable().setTableChanging(false);
          }
          break;
        }
      }
    }
  }

}

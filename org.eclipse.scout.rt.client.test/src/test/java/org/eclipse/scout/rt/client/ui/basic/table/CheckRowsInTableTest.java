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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractTable}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class CheckRowsInTableTest {

  @Test
  public void testSetCheckedByRow() throws Exception {
    P_Table table = createTable(true);
    ITableRow row = table.getRow(0);
    table.checkRow(row, true);
    assertTrue(row.isChecked());
  }

  @Test
  public void testSetUncheckedByRow() throws Exception {
    P_Table table = createTable(true);
    ITableRow row = table.getRow(0);
    table.checkRow(row, false);
    assertTrue(!row.isChecked());
  }

  @Test
  public void testCheckAllWithDisabledRowSingleSelect() throws Exception {
    P_Table table = createTable(false);
    ITableRow disabledRow = table.getRow(0);
    disabledRow.setEnabled(false);
    table.checkAllRows();
    assertTrue(table.getCheckedRows().size() == 1);
  }

  @Test
  public void testSetCheckedByIndex() throws Exception {
    P_Table table = createTable(true);
    ITableRow row = table.getRow(0);
    table.checkRow(row.getRowIndex(), true);
    assertTrue(row.isChecked());
  }

  public void testSetUncheckedById() throws Exception {
    P_Table table = createTable(true);
    ITableRow row = table.getRow(0);
    table.checkRow(row.getRowIndex(), false);
    assertTrue(!row.isChecked());
  }

  @Test
  public void testCheckAll() throws Exception {
    P_Table table = createTable(true);
    table.checkAllRows();
    for (ITableRow row : table.getRows()) {
      assertTrue(row.isChecked());
    }
  }

  @Test
  public void testCheckAllOnSingleCheckable() throws Exception {
    P_Table table = createTable(false);
    table.checkAllRows();
    assertEquals(table.getCheckedRows().size(), 1);
  }

  @Test
  public void testUncheckAll() throws Exception {
    P_Table table = createTable(true);
    for (ITableRow row : table.getRows()) {
      row.setChecked(true);
    }
    table.uncheckAllRows();
    for (ITableRow row : table.getRows()) {
      assertTrue(!row.isChecked());
    }
  }

  @Test
  public void testSetAllRowsCheckedOverUIFacade() throws Exception {
    P_Table table = createTable(true);

    ITableUIFacade facade = table.getUIFacade();

    //check all rows
    facade.setCheckedRowsFromUI(table.getRows(), true);

    for (ITableRow row : table.getRows()) {
      assertTrue(row.isChecked());
    }
  }

  @Test
  public void testSetAllRowsUncheckedOverUIFacade() throws Exception {
    P_Table table = createTable(true);
    ITableUIFacade facade = table.getUIFacade();
    for (ITableRow row : table.getRows()) {
      row.setChecked(true);
    }
    facade.setCheckedRowsFromUI(table.getRows(), false);
    for (ITableRow row : table.getRows()) {
      assertTrue(!row.isChecked());
    }
  }

  @Test
  public void testSetAllRowsCheckedOverUIFacadeOnSingleCheckable() throws Exception {
    P_Table table = createTable(false);
    table.getUIFacade().setCheckedRowsFromUI(table.getRows(), true);
    assertEquals(table.getCheckedRows().size(), 1);
  }

  private P_Table createTable(boolean isMulticheck) throws ProcessingException {
    P_Table table = new P_Table();
    table.initTable();
    table.setMultiCheck(isMulticheck);
    table.addRowsByMatrix(new Object[][]{new Object[]{10, "Lorem"}, new Object[]{11, "Ipsum"}, new Object[]{12, "Bla"}}, ITableRow.STATUS_NON_CHANGED);

    return table;
  }

  public static class P_Table extends AbstractTable {

    public FirstColumn getFirstColumn() {
      return getColumnSet().getColumnByClass(FirstColumn.class);
    }

    public SecondColumn getSecondColumn() {
      return getColumnSet().getColumnByClass(SecondColumn.class);
    }

    public ThirdColumn getThridColumn() {
      return getColumnSet().getColumnByClass(ThirdColumn.class);
    }

    @Order(10)
    public class FirstColumn extends AbstractIntegerColumn {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }
    }

    @Order(20)
    public class SecondColumn extends AbstractStringColumn {
    }

    @Order(30)
    public class ThirdColumn extends AbstractIntegerColumn {

      @Override
      protected int getConfiguredSortIndex() {
        return 20;
      }

      @Override
      protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
        return true;
      }
    }
  }
}

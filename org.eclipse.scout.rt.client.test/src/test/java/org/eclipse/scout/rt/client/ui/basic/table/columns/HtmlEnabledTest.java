/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractColumn#setHtmlEnabled(boolean)}.
 */
@RunWith(PlatformTestRunner.class)
public class HtmlEnabledTest {
  private TestTable m_testTable;

  @Before
  public void before() {
    m_testTable = new TestTable();
    Object[] rowData = {"", "", ""};
    m_testTable.addRowByArray(rowData);
  }

  /**
   * Tests that the initial configuration is as configured and propagated to the cell.
   */
  @Test
  public void testHtmlEnabled() {
    assertTrue(m_testTable.getTest1Column().isHtmlEnabled());
    assertFalse(m_testTable.getTest2Column().isHtmlEnabled());
    assertFalse(m_testTable.getTest3Column().isHtmlEnabled());
    assertTrue(m_testTable.getRow(0).getCell(m_testTable.getTest1Column()).isHtmlEnabled());
    assertFalse(m_testTable.getRow(0).getCell(m_testTable.getTest2Column()).isHtmlEnabled());
    assertTrue(m_testTable.getRow(0).getCell(m_testTable.getTest3Column()).isHtmlEnabled());
  }

  public class TestTable extends AbstractTable {

    public Test1Column getTest1Column() {
      return getColumnSet().getColumnByClass(Test1Column.class);
    }

    public Test2Column getTest2Column() {
      return getColumnSet().getColumnByClass(Test2Column.class);
    }

    public Test3Column getTest3Column() {
      return getColumnSet().getColumnByClass(Test3Column.class);
    }

    @Override
    protected void execInitTable() {
    }

    @Order(10)
    public class Test1Column extends AbstractColumn<Object> {

      @Override
      protected boolean getConfiguredHtmlEnabled() {
        return true;
      }
    }

    @Order(20)
    public class Test2Column extends AbstractColumn<Object> {
    }

    @Order(30)
    public class Test3Column extends AbstractColumn<Object> {

      @Override
      protected void execDecorateCell(Cell cell, ITableRow row) {
        cell.setHtmlEnabled(true);
      }
    }
  }
}

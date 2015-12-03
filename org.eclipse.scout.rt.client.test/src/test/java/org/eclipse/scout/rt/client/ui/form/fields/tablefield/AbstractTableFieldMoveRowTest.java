/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class AbstractTableFieldMoveRowTest extends AbstractTableField<AbstractTableFieldMoveRowTest.Table> {
  private static final String LOREM = "Lorem";
  private static final String IPSUM = "Ipsum";
  private static final String DOLOR = "Dolor";

  private void assertRows(String... expected) {
    String[] actual = getTable().getTestColumn().getValues().toArray(new String[0]);
    assertArrayEquals(expected, actual);
  }

  @Before
  public void setUp() {
    String[] texts = new String[]{
        LOREM, IPSUM, DOLOR
    };
    for (String text : texts) {
      ITableRow row = getTable().createRow();
      getTable().getTestColumn().setValue(row, text);
      getTable().addRow(row);
    }
  }

  @After
  public void tearDown() {
    getTable().discardAllRows();
  }

  /**
   * <ol>
   * <li>select first row</li>
   * <li>move rows around using {@link AbstractTable#moveRow(int, int)}</li>
   * <li>verify selected row again</li>
   * </ol>
   */
  @Test
  public void testMoveRow() {
    getTable().selectFirstRow();
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRow(1, 1);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRow(0, 1);
    assertRows(IPSUM, LOREM, DOLOR);
    getTable().moveRow(0, 1);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRow(3, 0);
    assertRows(DOLOR, LOREM, IPSUM);
    getTable().moveRow(0, 2);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRow(Integer.MAX_VALUE, Integer.MIN_VALUE);
    assertRows(DOLOR, LOREM, IPSUM);
    getTable().moveRow(Integer.MIN_VALUE, Integer.MAX_VALUE);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRow(Integer.MIN_VALUE, Integer.MIN_VALUE);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRow(Integer.MAX_VALUE, Integer.MAX_VALUE);
    assertRows(LOREM, IPSUM, DOLOR);
    assertEquals(LOREM, getTable().getTestColumn().getValue(getTable().getSelectedRow()));
  }

  /**
   * <ol>
   * <li>select first row</li>
   * <li>move rows around using {@link AbstractTable#moveRowBefore(ITableRow, ITableRow)}</li>
   * <li>verify selected row again</li>
   * </ol>
   */
  @Test
  public void testMoveRowBefore() {
    getTable().selectFirstRow();
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowBefore(null, null);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowBefore(getTable().getRow(0), null);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowBefore(null, getTable().getRow(0));
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowBefore(getTable().getRow(0), getTable().getRow(2));
    assertRows(IPSUM, LOREM, DOLOR);
    getTable().moveRowBefore(getTable().getRow(2), getTable().getRow(0));
    assertRows(DOLOR, IPSUM, LOREM);
    getTable().moveRowBefore(getTable().getRow(1), getTable().getRow(1));
    assertRows(DOLOR, IPSUM, LOREM);
    getTable().moveRowBefore(getTable().getRow(1), getTable().getRow(0));
    assertRows(IPSUM, DOLOR, LOREM);
    getTable().moveRowBefore(getTable().getRow(0), getTable().getRow(1));
    assertRows(IPSUM, DOLOR, LOREM);
    assertEquals(LOREM, getTable().getTestColumn().getValue(getTable().getSelectedRow()));
  }

  /**
   * <ol>
   * <li>select first row</li>
   * <li>move rows around using {@link AbstractTable#moveRowAfter(ITableRow, ITableRow)}</li>
   * <li>verify selected row again</li>
   * </ol>
   */
  @Test
  public void testMoveRowAfter() {
    getTable().selectFirstRow();
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowAfter(null, null);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowAfter(getTable().getRow(0), null);
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowAfter(null, getTable().getRow(0));
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowAfter(getTable().getRow(0), getTable().getRow(2));
    assertRows(IPSUM, DOLOR, LOREM);
    getTable().moveRowAfter(getTable().getRow(2), getTable().getRow(0));
    assertRows(IPSUM, LOREM, DOLOR);
    getTable().moveRowAfter(getTable().getRow(1), getTable().getRow(1));
    assertRows(IPSUM, LOREM, DOLOR);
    getTable().moveRowAfter(getTable().getRow(1), getTable().getRow(0));
    assertRows(IPSUM, LOREM, DOLOR);
    getTable().moveRowAfter(getTable().getRow(0), getTable().getRow(1));
    assertRows(LOREM, IPSUM, DOLOR);
    getTable().moveRowAfter(getTable().getRow(0), getTable().getRow(2));
    assertRows(IPSUM, DOLOR, LOREM);
    assertEquals(LOREM, getTable().getTestColumn().getValue(getTable().getSelectedRow()));
  }

  /*
   * table
   */
  public class Table extends AbstractTable {

    public TestColumn getTestColumn() {
      return getColumnSet().getColumnByClass(TestColumn.class);
    }

    @Order(10)
    public class TestColumn extends AbstractStringColumn {

    }
  }
}

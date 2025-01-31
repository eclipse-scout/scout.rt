/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class AbstractTableFieldTest extends AbstractTableField<AbstractTableFieldTest.Table> {
  private static final String[] LOREM_IPSUM = new String[]{
      "Lorem ipsum dolor sit amet,",
      "consetetur sadipscing elitr,",
      "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,",
      "sed diam voluptua.",
      "At vero eos et accusam et justo duo dolores et ea rebum.",
      "Stet clita kasd gubergren,",
      "no sea takimata sanctus est Lorem ipsum dolor sit amet.",
      "Lorem ipsum dolor sit amet,",
      "consetetur sadipscing elitr,",
      "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,",
      "sed diam voluptua.",
      "At vero eos et accusam et justo duo dolores et ea rebum.",
      "Stet clita kasd gubergren,",
      "no sea takimata sanctus est Lorem ipsum dolor sit amet."
  };

  private static final BigDecimal FAR_BELOW_ZERO = new BigDecimal("-999999999999999999999999999999999999999999999999999999999999");

  @Before
  public void setUp() {
    for (int i = 0; i < 10; i++) {
      ITableRow row = getTable().createRow();
      getTable().getIntegerColumn().setValue(row, i);
      getTable().getLongColumn().setValue(row, Long.valueOf(i * 2));
      getTable().getString1Column().setValue(row, i + ". row");
      getTable().getString2Column().setValue(row, getTextFor(i, " "));
      getTable().getString3Column().setValue(row, getTextFor(i, " "));
      getTable().getString4Column().setValue(row, getTextFor(i, "\n"));
      getTable().getBigDecimalColumn().setValue(row, FAR_BELOW_ZERO.add(BigDecimal.valueOf(1.11).multiply(BigDecimal.valueOf(i))));
      getTable().addRow(row);

      getTable().selectAllRows();
    }
  }

  @Test
  public void testSetTableStatusToTable() {
    IStatus status = new Status("Hello hello", IStatus.ERROR);
    setTableStatus(status);
    Assert.assertSame(status, getTable().getTableStatus());
  }

  private String getTextFor(int size, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < size; j++) {
      sb.append(LOREM_IPSUM[j % LOREM_IPSUM.length]);
      sb.append(separator);
    }
    return sb.toString();
  }

  @Test
  public void testIsEmpty() {
    // completely filled row
    assertFalse(isEmptyRow(getTable().getRow(0)));

    // new empty row
    ITableRow row = getTable().createRow();
    assertTrue(isEmptyRow(row));

    getTable().getIntegerColumn().setValue(row, 100);
    assertFalse(isEmptyRow(row));
    assertTrue(isEmptyRow(row, CollectionUtility.hashSet(getTable().getIntegerColumn().getColumnIndex())));

    getTable().getString1Column().setValue(row, "foo");
    assertFalse(isEmptyRow(row));
    assertTrue(isEmptyRow(row, CollectionUtility.hashSet(getTable().getIntegerColumn().getColumnIndex(), getTable().getString1Column().getColumnIndex())));

    getTable().getIntegerColumn().setValue(row, null);
    getTable().getString1Column().setValue(row, null);
    assertTrue(isEmptyRow(row));
  }

  /**
   * table
   */
  public class Table extends AbstractTable {

    @Override
    protected boolean getConfiguredMultilineText() {
      return true;
    }

    public IntegerColumn getIntegerColumn() {
      return getColumnSet().getColumnByClass(IntegerColumn.class);
    }

    public LongColumn getLongColumn() {
      return getColumnSet().getColumnByClass(LongColumn.class);
    }

    public String1Column getString1Column() {
      return getColumnSet().getColumnByClass(String1Column.class);
    }

    public String2Column getString2Column() {
      return getColumnSet().getColumnByClass(String2Column.class);
    }

    public String3Column getString3Column() {
      return getColumnSet().getColumnByClass(String3Column.class);
    }

    public String4Column getString4Column() {
      return getColumnSet().getColumnByClass(String4Column.class);
    }

    public BigDecimalColumn getBigDecimalColumn() {
      return getColumnSet().getColumnByClass(BigDecimalColumn.class);
    }

    @Order(1)
    public class IntegerColumn extends AbstractIntegerColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected String getConfiguredHeaderText() {
        return "Integer";
      }

      @Override
      protected int getConfiguredWidth() {
        return 70;
      }
    }

    @Order(5)
    public class LongColumn extends AbstractLongColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected String getConfiguredHeaderText() {
        return "Long";
      }

      @Override
      protected int getConfiguredWidth() {
        return 70;
      }
    }

    @Order(10)
    public class String1Column extends AbstractStringColumn {

      @Override
      protected String getConfiguredHeaderText() {
        return "String1";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected int getConfiguredWidth() {
        return 100;
      }
    }

    @Order(20)
    public class String2Column extends AbstractStringColumn {

      @Override
      protected String getConfiguredHeaderText() {
        return "String2 (TextWrap false)";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected boolean getConfiguredTextWrap() {
        return false;
      }

      @Override
      protected int getConfiguredWidth() {
        return 200;
      }
    }

    @Order(30)
    public class String3Column extends AbstractStringColumn {

      @Override
      protected String getConfiguredHeaderText() {
        return "String3 (TextWrap true)";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected boolean getConfiguredTextWrap() {
        return true;
      }

      @Override
      protected int getConfiguredWidth() {
        return 200;
      }
    }

    @Order(30)
    public class String4Column extends AbstractStringColumn {

      @Override
      protected String getConfiguredHeaderText() {
        return "String4 (TextWrap false, multiline text)";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected boolean getConfiguredTextWrap() {
        return false;
      }

      @Override
      protected int getConfiguredWidth() {
        return 200;
      }
    }

    @Order(40)
    public class BigDecimalColumn extends AbstractBigDecimalColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "BigDecimal";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected int getConfiguredMaxFractionDigits() {
        return 25;
      }

      @Override
      protected int getConfiguredFractionDigits() {
        return 25;
      }

      @Override
      protected int getConfiguredWidth() {
        return 160;
      }
    }
  }
}

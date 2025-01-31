/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.junit.Test;

/**
 * Tickets: <br>
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=452283 <br>
 */
public class TableValidationTest {

  /*
   * Workaround for bugs 396848 & 408741
   * Currently, we set the error status and value directly on the cell before calling the decorator.
   * A cleaner way is to fire a table update event like in {@link AbstractTable#fireRowsUpdated(List<ITableRow> rows)}
   * to propagate the new error status and value.
   */

  //only validation with internal table row
  //why?
  @Test
  public void testValidateWithInternalTableRow() {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    table.getC1Column().setValue(row, "key");
    table.addRow(row);
  }

  /**
   * A test table with two editable columns: Mandatory and Non-mandatory column
   */
  public class TestTable extends AbstractTable {

    public TestTable() {
      setEnabled(true);
    }

    public C1 getC1Column() {
      return getColumnSet().getColumnByClass(C1.class);
    }

    @Order(10)
    public class C1 extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }
    }
  }
}

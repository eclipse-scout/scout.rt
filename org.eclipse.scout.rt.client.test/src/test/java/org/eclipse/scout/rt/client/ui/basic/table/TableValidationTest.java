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
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tickets: <br>
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=452283 <br>
 */
public class TableValidationTest {

  /**
   * Compares performance for editable vs. non-editable table cells.
   */
  //TODO jgu
  @Ignore
  @Test
  public void testLoadingPerformance() throws ProcessingException {
    TestTable t = new TestTable();
    Object[][] testRows = new Object[10000][1];
    for (int i = 0; i < 10000; i++) {
      testRows[i][0] = "a" + i;
    }

    TuningUtility.startTimer();
    t.addRowsByMatrix(testRows);
    TuningUtility.stopTimer("loading rows editable");
    TuningUtility.finishAll();

    TestTable t2 = new TestTable();
    t2.getC1Column().setEditable(false);

    TuningUtility.startTimer();
    t2.addRowsByMatrix(testRows);
    TuningUtility.stopTimer("loading rows not editable");
    TuningUtility.finishAll();
  }

  /*
   * Workaround for bugs 396848 & 408741
   * Currently, we set the error status and value directly on the cell before calling the decorator.
   * A cleaner way is to fire a table update event like in {@link AbstractTable#fireRowsUpdated(List<ITableRow> rows)}
   * to propagate the new error status and value.
   */

  //only validation with internal table row
  //why?
  @Test
  public void testValidateWithInternalTableRow() throws ProcessingException {
    TestTable table = new TestTable();
    ITableRow row = table.createRow();
    table.getC1Column().setValue(row, "key");
    table.addRow(row);
  }

  /**
   * A test table with two editable columns: Mandatory and Non-mandatory column
   */
  @Order(10.0)
  public class TestTable extends AbstractTable {

    public TestTable() {
      setEnabled(true);
    }

    public C1 getC1Column() {
      return getColumnSet().getColumnByClass(C1.class);
    }

    @Order(10.0)
    public class C1 extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

    }

  }

  //TODO JGU
  //CSS CLASS soll das aufs feld gemapped werden?

}

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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.junit.Test;

/**
 * PerformanceTest for {@link AbstractTable}. <br>
 * Should run successfully on slow infrastructure.
 */
public class TableLoadingPerfTest {
  int TEST_RUN_COUNT = 10;

  /**
   * Tests that adding rows to a table is fast.<b> Executes tests multiple times to avoid temporary problems with
   * infrastructure.
   */
  @Test
  public void testLoadingMatrix() {
    Object[][] testRows = createRows(5000);
    TestTable testTable = new TestTable();
    Long[] durations = new Long[TEST_RUN_COUNT];
    for (int i = 0; i < TEST_RUN_COUNT; i++) {
      long start = System.nanoTime();
      //actual table load
      testTable.addRowsByMatrix(testRows);

      long stop = System.nanoTime();
      durations[i] = TimeUnit.NANOSECONDS.toMillis(stop - start);
    }
    //should be ok on a slow machine
    assertMean(durations, 1000);
  }

  private void assertMean(Long[] durations, int expectedMean) {
    Arrays.sort(durations);
    DescriptiveStatistics stats = new DescriptiveStatistics();
    for (int i = 1; i < durations.length - 1; i++) {
      stats.addValue(durations[i]);
    }
    double avgDuration = stats.getMean();
    assertTrue(String.format("Expected Mean<100 Mean:%s Variance:%s", avgDuration, stats.getVariance()), avgDuration < expectedMean);
  }

  private Object[][] createRows(int count) {
    Object[][] testRows = new Object[count][1];
    for (int i = 0; i < count; i++) {
      testRows[i][0] = "a" + i;
    }
    return testRows;
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

    @Order(10.0)
    public class C1 extends AbstractStringColumn {

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected boolean getConfiguredMandatory() {
        return true;
      }

      @Override
      protected String getConfiguredCssClass() {
        return "testClass";
      }

    }

  }

}

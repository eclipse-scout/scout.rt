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
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;

public final class TestMemoryUsage {
  private final Cell[] mv;

  private TestMemoryUsage(int rows, int cols) {
    mv = new Cell[rows * cols];
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        Cell v = new Cell();
        String s = "Hello World " + r + "," + c;
        v.setText(s);
        v.setValue(new Long(r * cols + c));
        mv[r * cols + c] = v;
      }
    }
  }

  public static void main(String[] args) {
    System.gc();
    long max0 = Runtime.getRuntime().maxMemory();
    long total0 = Runtime.getRuntime().totalMemory();
    long free0 = Runtime.getRuntime().freeMemory();

    // consumeMemory
    int rows = 100000;
    int cols = 10;
    int cells = rows * cols;
    @SuppressWarnings("unused")
    TestMemoryUsage t = new TestMemoryUsage(rows, cols);

    System.gc();
    long max1 = Runtime.getRuntime().maxMemory();
    long total1 = Runtime.getRuntime().totalMemory();
    long free1 = Runtime.getRuntime().freeMemory();
    System.out.println("DELTA   max/total/consumed=" + (max1 - max0) + "/" + (total1 - total0) + "/" + (free0 - free1));
    System.out.println("DELTA/N max/total/consumed=" + (max1 - max0) / cells + "/" + (total1 - total0) / cells + "/" + (free0 - free1) / cells);
    System.out.println("DELTA   total+consumed=" + ((max1 - max0) + (total1 - total0) + (free0 - free1)));
    long d = ((max1 - max0) + (total1 - total0) + (free0 - free1)) / cells;
    System.out.println("DELTA/N total+consumed=43+" + (d - 43));
    System.exit(0);
  }
}

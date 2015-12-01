/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.io.Serializable;

public class CellRange implements Serializable {
  private static final long serialVersionUID = 1L;

  private int m_row1, m_col1, m_row2, m_col2;

  public CellRange(String cells) {
    parse(cells);
  }

  public CellRange(int row, int col) {
    rangeCheck(row, col);
    m_row1 = row;
    m_col1 = col;
    m_row2 = row;
    m_col2 = col;
  }

  public CellRange(int row1, int col1, int row2, int col2) {
    rangeCheck(row1, col1);
    rangeCheck(row2, col2);
    m_row1 = row1;
    m_col1 = col1;
    m_row2 = row2;
    m_col2 = col2;
    rangeOrder();
  }

  public boolean isRowsOnly() {
    return m_col1 < 0 && m_col2 < 0;
  }

  public boolean isColumnsOnly() {
    return m_row1 < 0 && m_row2 < 0;
  }

  public int getFirstRow() {
    return m_row1;
  }

  public int getLastRow() {
    return m_row2;
  }

  public int getFirstColumn() {
    return m_col1;
  }

  public int getLastColumn() {
    return m_col2;
  }

  public int getColumnCount() {
    if (isColumnsOnly()) {
      return -1;
    }
    else {
      return m_col2 - m_col1 + 1;
    }
  }

  public int getRowCount() {
    if (isRowsOnly()) {
      return -1;
    }
    else {
      return m_row2 - m_row1 + 1;
    }
  }

  public CellRange normalize(int rowCount, int colCount) {
    boolean rowOnly = isRowsOnly();
    boolean colOnly = isColumnsOnly();
    if (rowOnly || colOnly) {
      CellRange x = new CellRange(m_row1, m_col1, m_row2, m_col2);
      if (rowOnly) {
        x.m_col1 = 1;
        x.m_col2 = colCount;
      }
      if (colOnly) {
        x.m_row1 = 1;
        x.m_row2 = rowCount;
      }
      return x;
    }
    else {
      return this;
    }
  }

  public String toRangeString() {
    return format();
  }

  @Override
  public String toString() {
    return format();
  }

  public String format() {
    if (m_row1 == m_row2 && m_col1 == m_col2) {
      return formatItem(m_row1, m_col1);
    }
    else {
      return formatItem(m_row1, m_col1) + ":" + formatItem(m_row2, m_col2);
    }
  }

  public static String toRangeString(int row, int col) {
    return new CellRange(row, col).format();
  }

  public static String toRangeString(int row1, int col1, int row2, int col2) {
    return new CellRange(row1, col1, row2, col2).format();
  }

  public static CellRange toRange(String s) {
    return new CellRange(s);
  }

  private String formatItem(int row, int col) {
    StringBuilder sb = new StringBuilder();
    while (col >= 0) {
      int n = col % 26;
      if (n == 0 && col >= 26) {
        n = 26;
      }
      if (n > 0) {
        char ch = (char) ('A' + n - 1);
        sb.insert(0, ch);
      }
      if (col == 0) {
        col = -1;
      }
      else {
        col = (col - n) / 26;
      }
    }
    if (row > 0) {
      sb.append(row);
    }
    return sb.toString();
  }

  public void parse(String s) {// A4:C5
    int p;
    if ((p = s.indexOf(':')) >= 0) {
      int[] a = parseItem(s.substring(0, p));
      m_row1 = a[0];
      m_col1 = a[1];
      a = parseItem(s.substring(p + 1));
      m_row2 = a[0];
      m_col2 = a[1];
    }
    else {
      int[] a = parseItem(s);
      m_row1 = a[0];
      m_col1 = a[1];
      m_row2 = m_row1;
      m_col2 = m_col1;
    }
    rangeOrder();
  }

  private int[] parseItem(String s) {// A4
    s = s.toUpperCase();
    char ch;
    int[] a = new int[2];
    int i = 0;
    for (; i < s.length(); i++) {
      ch = s.charAt(i);
      if (ch >= 'A' && ch <= 'Z') {
        a[1] = a[1] * 26 + ((ch - 'A' + 1));
      }
      else {
        break;
      }
    }
    if (a[1] == 0) {
      a[1] = -1;
    }
    //
    if (i < s.length()) {
      a[0] = Integer.parseInt(s.substring(i));
    }
    if (a[0] == 0) {
      a[0] = -1;
    }
    return a;
  }

  private void rangeCheck(int row, int col) {
    if (row == 0 || row < -1 || col == 0 || col < -1) {
      throw new IllegalArgumentException("value must be at least 1 or -1 for wildcards: " + row + "," + col);
    }
  }

  private void rangeOrder() {
    if (m_row2 < m_row1) {
      int t = m_row2;
      m_row2 = m_row1;
      m_row1 = t;
    }
    if (m_col2 < m_col1) {
      int t = m_col2;
      m_col2 = m_col1;
      m_col1 = t;
    }
  }

}

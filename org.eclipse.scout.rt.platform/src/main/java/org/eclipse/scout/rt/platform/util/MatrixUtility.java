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
package org.eclipse.scout.rt.platform.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.scout.rt.platform.nls.NlsLocale;

public final class MatrixUtility {

  private MatrixUtility() {
  }

  /**
   * If data is a single value: returns a matrix with 1 row and 2 columns [0..n/2][0..1] and the value is at [0][0] If
   * data is an array: transforms a list with n values into a matrix with n/2 rows and 2 columns [0..n/2][0..1] always 2
   * values are used to create one matrix row If data is a matrix: transforms a matrix with n values into a matrix with
   * n/2 rows and 2 columns [0..n/2][0..1] always 2 values are used to create one matrix traversal is row-wise
   */
  public static Object[][] toTupels(Object data) {
    if (data == null) {
      return new Object[0][0];
    }
    if (data.getClass().isArray()) {
      if (data.getClass().getComponentType().isArray()) {
        // matrix
        if (Array.getLength(data) == 0) {
          return new Object[0][0];
        }
        int rows = Array.getLength(data);
        int cols = Array.getLength(Array.get(data, 0));
        int count = rows * cols;
        Object[][] matrix = new Object[count / 2][2];
        for (int i = 0; i < matrix.length; i++) {
          int r = (i * 2) / cols;
          int c = (i * 2) % cols;
          matrix[i][0] = Array.get(Array.get(data, r), c);
          r = (i * 2 + 1) / cols;
          c = (i * 2 + 1) % cols;
          matrix[i][1] = Array.get(Array.get(data, r), c);
        }
        return matrix;
      }
      else {
        // array
        if (Array.getLength(data) == 0) {
          return new Object[0][0];
        }
        int n = Array.getLength(data);
        Object[][] matrix = new Object[n / 2][2];
        for (int i = 0; i < matrix.length; i++) {
          matrix[i][0] = Array.get(data, i * 2);
          matrix[i][1] = Array.get(data, i * 2 + 1);
        }
        return matrix;
      }
    }
    else {
      // single value
      Object[][] matrix = new Object[1][2];
      matrix[0][1] = data;
      return matrix;
    }
  }

  /**
   * union all lists into a matrix every list is interpreted as a matrix row
   */
  @SafeVarargs
  public static Object[][] union(Collection<Object>... collections) {
    int rowCount = 0;
    int colCount = 0;
    for (Collection<Object> list : collections) {
      rowCount++;
      if (list != null) {
        colCount = Math.max(colCount, list.size());
      }
    }
    Object[][] matrix = new Object[rowCount][colCount];
    int r = 0;
    for (Collection<Object> list : collections) {
      if (list != null) {
        Object[] a = list.toArray();
        System.arraycopy(a, 0, matrix[r], 0, Math.min(colCount, a.length));
      }
      r++;
    }
    return matrix;
  }

  /**
   * union all matrixes into a single matrix where the number of rows is the sum of rows of all matrixes every list is
   * interpreted as a matrix row in sql: UNION ALL
   */
  public static Object[][] union(Object... matrixes) {
    int rowCount = 0;
    int colCount = 0;
    for (Object m : matrixes) {
      if (m != null) {
        int r = Array.getLength(m);
        rowCount += r;
        if (r > 0) {
          int c = Array.getLength(Array.get(m, 0));
          colCount = Math.max(colCount, c);
        }
      }
    }
    Object[][] matrix = new Object[rowCount][colCount];
    int r = 0;
    for (Object m : matrixes) {
      if (m != null) {
        int mRows = Array.getLength(m);
        if (mRows > 0) {
          for (int i = 0; i < mRows; i++) {
            Object rowArray = Array.get(m, i);
            int mCols = Array.getLength(rowArray);
            for (int c = 0; c < colCount && c < mCols; c++) {
              matrix[r][c] = Array.get(rowArray, c);
            }
            r++;
          }
        }
      }
    }
    return matrix;
  }

  public static Object[][] toMatrix(Object... values) {
    if (values == null) {
      return new Object[0][0];
    }
    Object[][] matrix = new Object[1][values.length];
    matrix[0] = values;
    return matrix;
  }

  @SuppressWarnings("pmd:AvoidArrayLoops")
  public static Object[][] toMatrix(Object[]... rows) {
    if (rows == null) {
      return new Object[0][0];
    }
    Object[][][] matrices = new Object[rows.length][1][];
    for (int i = 0; i < rows.length; i++) {
      matrices[i][0] = rows[i];
    }
    return union((Object[]) matrices);
  }

  @SafeVarargs
  public static Object[][] toMatrix(Collection<Object>... rows) {
    return union(rows);
  }

  public static String toRange(int row, int col) {
    return CellRange.toRangeString(row, col);
  }

  public static String toRange(int row1, int col1, int row2, int col2) {
    return CellRange.toRangeString(row1, col1, row2, col2);
  }

  public static int rowCount(String range) {
    return new CellRange(range).getRowCount();
  }

  public static int columnCount(String range) {
    return new CellRange(range).getColumnCount();
  }

  public static int firstRow(String range) {
    return new CellRange(range).getFirstRow();
  }

  public static int lastRow(String range) {
    return new CellRange(range).getLastRow();
  }

  public static int firstColumn(String range) {
    return new CellRange(range).getFirstColumn();
  }

  public static int lastColumn(String range) {
    return new CellRange(range).getLastColumn();
  }

  @SuppressWarnings("unchecked")
  public static <T> T getColumn(Object matrix, int columnIndex, Class<T> arrayType) {
    int rowCount = Array.getLength(matrix);
    int colCount = 0;
    if (rowCount > 0) {
      colCount = Array.getLength(Array.get(matrix, 0));
    }
    T column = (T) Array.newInstance(arrayType.getComponentType(), rowCount);
    if (rowCount > 0 && colCount > columnIndex) {
      for (int r = 0; r < rowCount; r++) {
        Array.set(column, r, TypeCastUtility.castValue(Array.get(Array.get(matrix, r), columnIndex), arrayType.getComponentType()));
      }
    }
    return column;
  }

  /**
   * Sorting a 2d Object array providing {@link ArrayComparator.ColumnComparator ArrayComparator.ColumnComparators}
   *
   * @param data
   * @param columnComparators
   */
  public static void sortWithComparators(Object[][] data, ArrayComparator.ColumnComparator... columnComparators) {
    if (data != null && data.length >= 2 && columnComparators != null && columnComparators.length > 0) {
      Arrays.sort(data, new ArrayComparator(columnComparators));
    }
  }

  /**
   * Delegates to {@link #sort(Locale, Object[][], int...)} using {@link NlsLocale#get()}
   * <p>
   * <b>It is recommended to use {@link #sort(Locale, Object[][], int...)} with the specific {@link Locale}</b>
   *
   * @param data
   * @param sortColumns
   */
  public static void sort(Object[][] data, int... sortColumns) {
    sort(NlsLocale.get(), data, sortColumns);
  }

  /**
   * Sorting a 2d Object array using {@link ArrayComparator}
   *
   * @param locale
   * @param data
   * @param sortColumns
   *          0-bases column indices
   */
  public static void sort(Locale locale, Object[][] data, int... sortColumns) {
    if (data != null && data.length >= 2 && sortColumns != null && sortColumns.length > 0) {
      Arrays.sort(data, new ArrayComparator(locale, sortColumns));
    }
  }

  public static Object getColumn(Object matrix, int columnIndex) {
    int rowCount = Array.getLength(matrix);
    int colCount = 0;
    if (rowCount > 0) {
      colCount = Array.getLength(Array.get(matrix, 0));
    }
    Object column = Array.newInstance(matrix.getClass().getComponentType().getComponentType(), rowCount);
    if (rowCount > 0 && colCount > columnIndex) {
      for (int r = 0; r < rowCount; r++) {
        Array.set(column, r, Array.get(Array.get(matrix, r), columnIndex));
      }
    }
    return column;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] getColumn(T[][] matrix, int columnIndex) {
    int rowCount = Array.getLength(matrix);
    int colCount = 0;
    if (rowCount > 0) {
      colCount = Array.getLength(Array.get(matrix, 0));
    }
    T[] column = (T[]) Array.newInstance(matrix.getClass().getComponentType().getComponentType(), rowCount);
    if (rowCount > 0 && colCount > columnIndex) {
      for (int r = 0; r < rowCount; r++) {
        column[r] = (T) Array.get(Array.get(matrix, r), columnIndex);
      }
    }
    return column;
  }

  /**
   * lookup a row by its key (keyIndex=0) and return the value at valueIndex in the same row
   */
  public static Object lookup(Object matrix, Object lookupKey, int valueIndex) {
    int keyIndex = 0;
    int rowCount = Array.getLength(matrix);
    int colCount = 0;
    if (rowCount > 0) {
      colCount = Array.getLength(Array.get(matrix, 0));
    }
    if (rowCount > 0 && colCount > valueIndex && colCount > keyIndex) {
      for (int r = 0; r < rowCount; r++) {
        Object row = Array.get(matrix, r);
        Object key = Array.get(row, keyIndex);
        if (key == lookupKey || (key != null && key.equals(lookupKey))) {
          return Array.get(row, valueIndex);
        }
      }
    }
    return null;
  }

}

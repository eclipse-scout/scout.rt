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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ILongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.html.HTMLUtility;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TableUtility {
  private static final Logger LOG = LoggerFactory.getLogger(TableUtility.class);

  private TableUtility() {
  }

  /**
   * synchronous resolving of lookup values to text
   * <p>
   * Note that remote lookup calls are evaluated one by one, no batch processing.
   */
  public static <T> void resolveLookupCall(Map<ILookupCall<T>, List<? extends ILookupRow<T>>> lookupCache, ITableRow row, ISmartColumn<T> col, boolean multilineText) {
    try {
      ILookupCall<T> call = col.prepareLookupCall(row);
      if (call != null) {
        List<? extends ILookupRow<T>> result = null;
        boolean verifiedQuality = verifyLookupCallBeanQuality(call);
        //optimize local calls by caching the results
        if (verifiedQuality) {
          result = lookupCache.get(call);
        }
        if (result == null) {
          result = call.getDataByKey();
          if (verifiedQuality) {
            lookupCache.put(call, result);
          }
        }
        applyLookupResult(row, col, result, multilineText);
      }
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  /**
   * In order to use caching of results on lookup calls, it is crucial that the javabean concepts are valid, especially
   * hashCode and equals (when the subclass has additional member fields).
   * <p>
   * Scout tries to help developers to find problems related to this issue and write a warning in development mode on
   * all local lookup call subclasses that do not overwrite hashCode and equals.
   */
  public static boolean verifyLookupCallBeanQuality(ILookupCall<?> call) {
    Class<?> clazz = call.getClass();
    if (LocalLookupCall.class == clazz) {
      return true;
    }
    if (LookupCall.class == clazz) {
      return true;
    }
    if (LocalLookupCall.class.isAssignableFrom(clazz)) {
      Class<?> tmp = clazz;
      while (tmp != LocalLookupCall.class) {
        if (ConfigurationUtility.isMethodOverwrite(LocalLookupCall.class, "equals", new Class[]{Object.class}, tmp)) {
          return true;
        }
        Field[] fields = tmp.getDeclaredFields();
        if (fields != null && fields.length > 0) {
          for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
              LOG.warn("" + clazz + " subclasses LocalLookupCall with additional member " + fields[i].getName() + " and should therefore override the 'equals' and 'hashCode' methods");
              return false;
            }
          }
        }
        //next
        tmp = tmp.getSuperclass();
      }
      return true;
    }
    if (LookupCall.class.isAssignableFrom(clazz)) {
      Class<?> tmp = clazz;
      while (tmp != LookupCall.class) {
        if (ConfigurationUtility.isMethodOverwrite(LookupCall.class, "equals", new Class[]{Object.class}, tmp)) {
          return true;
        }
        Field[] fields = tmp.getDeclaredFields();
        if (fields != null && fields.length > 0) {
          for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
              LOG.warn("" + clazz + " subclasses LookupCall with additional member " + fields[i].getName() + " and should therefore override the 'equals' and 'hashCode' methods");
              return false;
            }
          }
        }
        //next
        tmp = tmp.getSuperclass();
      }
      return true;
    }
    return false;
  }

  public static <T> void applyLookupResult(ITableRow row, IColumn<T> col, List<? extends ILookupRow<T>> result, boolean multilineText) {
    // disable row changed trigger on row
    try {
      row.setRowChanging(true);
      //
      Cell cell = (Cell) row.getCell(col.getColumnIndex());
      if (result.size() == 1) {
        cell.setText(result.get(0).getText());
      }
      else if (result.size() > 1) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < result.size(); i++) {
          if (i > 0) {
            if (multilineText) {
              buf.append("\n");
            }
            else {
              buf.append(", ");
            }
          }
          buf.append(result.get(i).getText());
        }
        cell.setText(buf.toString());
      }
      else {
        cell.setText("");
      }
    }
    finally {
      row.setRowPropertiesChanged(false);
      row.setRowChanging(false);
    }
  }

  /**
   * @return matrix[rowCount][columnCount] with cell as CSV cells
   *         <p>
   *         The returned csv will be:
   *         <ul>
   *         <li>Optional row: column names (Strings)</li>
   *         <li>Optional row: column types (Classes)</li>
   *         <li>Optional row: column formats (Strings)</li>
   *         <li>Data row (Objects)</li>
   *         <li>Data row (Objects)</li>
   *         <li>...</li>
   *         </ul>
   *         <p>
   *         valid exported csv types include:
   *         <ul>
   *         <li>java.lang.String</li>
   *         <li>java.lang.Long</li>
   *         <li>java.lang.Integer</li>
   *         <li>java.lang.Float</li>
   *         <li>java.lang.Double</li>
   *         <li>java.lang.Boolean</li>
   *         <li>java.util.Date</li>
   *         </ul>
   */
  public static Object[][] exportRowsAsCSV(List<? extends ITableRow> rows, List<? extends IColumn> columns, boolean includeLineForColumnNames, boolean includeLineForColumnTypes, boolean includeLineForColumnFormats) {
    int nr = rows.size();
    Object[][] a = new Object[nr + (includeLineForColumnNames ? 1 : 0) + (includeLineForColumnTypes ? 1 : 0) + (includeLineForColumnFormats ? 1 : 0)][columns.size()];
    for (int c = 0; c < columns.size(); c++) {
      IColumn<?> col = columns.get(c);
      Class<?> type;
      boolean byValue;
      String format;
      if (col instanceof IDateColumn) {
        if (((IDateColumn) col).isHasTime()) {
          type = Timestamp.class;
          byValue = true;
          format = ((IDateColumn) col).getFormat();
        }
        else {
          type = Date.class;
          byValue = true;
          format = ((IDateColumn) col).getFormat();
        }
      }
      else if (col instanceof IIntegerColumn) {
        type = Integer.class;
        byValue = true;
        format = ((IIntegerColumn) col).getFormat().toPattern();
      }
      else if (col instanceof ILongColumn) {
        type = Long.class;
        byValue = true;
        format = ((ILongColumn) col).getFormat().toPattern();
      }
      else if (col instanceof IBigDecimalColumn) {
        type = Double.class;
        byValue = true;
        format = ((IBigDecimalColumn) col).getFormat().toPattern();
      }
      else if (col instanceof ISmartColumn<?>) {
        type = String.class;
        byValue = false;
        format = null;
      }
      else if (col instanceof IBooleanColumn) {
        type = Boolean.class;
        byValue = false;
        format = null;
      }
      else {
        type = String.class;
        byValue = false;
        format = null;
      }
      //
      int csvRowIndex = 0;
      if (includeLineForColumnNames) {
        a[csvRowIndex][c] = columns.get(c).getHeaderCell().getText();
        csvRowIndex++;
      }
      if (includeLineForColumnTypes) {
        a[csvRowIndex][c] = type;
        csvRowIndex++;
      }
      if (includeLineForColumnFormats) {
        a[csvRowIndex][c] = format;
        csvRowIndex++;
      }
      for (int r = 0; r < nr; r++) {
        if (byValue) {
          if (type == Timestamp.class) {
            a[csvRowIndex][c] = TypeCastUtility.castValue(columns.get(c).getValue(rows.get(r)), Timestamp.class);
          }
          else {
            a[csvRowIndex][c] = columns.get(c).getValue(rows.get(r));
          }
        }
        else {
          String text = columns.get(c).getDisplayText(rows.get(r));
          //special intercept for boolean
          if (type == Boolean.class) {
            Boolean b = TypeCastUtility.castValue(columns.get(c).getValue(rows.get(r)), Boolean.class);
            if (b != null && b.booleanValue()) {
              // only use X if no display text is set
              if (!StringUtility.hasText(text)) {
                text = "X";
              }
            }
          }
          //special intercept for html
          if (type == String.class) {
            if (text != null && text.startsWith("<html")) {
              text = HTMLUtility.getPlainText(text);
            }
          }
          a[csvRowIndex][c] = text;
        }
        csvRowIndex++;
      }
    }
    return a;
  }

  public interface ITableCellEditorFilter {
    boolean accept(ITableRow row, IColumn<?> col);
  }

  /**
   * based on row and column, find the next visible cell and start editing
   *
   * @param table
   * @param row
   * @param col
   * @param forward
   * @param filter
   *          (optional) is used to further narrow which cells are editable. This filter is checked after the check to
   *          {@link ITable#isCellEditable(ITableRow, IColumn)}
   */
  public static void editNextTableCell(ITable table, ITableRow currentRow, IColumn<?> currentCol, boolean forward, ITableCellEditorFilter filter) {
    if (table == null) {
      return;
    }
    int rowCount = table.getFilteredRowCount();
    int colCount = table.getVisibleColumnCount();
    if (rowCount <= 0 || colCount <= 0) {
      return;
    }
    currentRow = table.resolveRow(currentRow);
    currentCol = table.getColumnSet().resolveColumn(currentCol);
    if (currentRow == null || currentCol == null) {
      return;
    }
    int row = table.getFilteredRowIndex(currentRow);
    int modelCol = currentCol.getColumnIndex();
    int[] visibleIndexes = table.getColumnSet().getVisibleColumnIndexes();
    int col = -1;
    for (int i = 0; i < visibleIndexes.length; i++) {
      if (visibleIndexes[i] == modelCol) {
        col = i;
        break;
      }
    }
    if (row < 0 || col < 0) {
      return;
    }
    int a = rowCount * colCount;
    while (a > 1) {
      a--;
      if (forward) {
        col++;
        if (col >= colCount) {
          row++;
          col = 0;
        }
        if (row >= rowCount) {
          row = 0;
        }
      }
      else {
        col--;
        if (col < 0) {
          row--;
          col = colCount - 1;
        }
        if (row < 0) {
          row = rowCount - 1;
        }
      }
      ITableRow tr = table.getFilteredRow(row);
      IColumn<?> tc = table.getColumnSet().getVisibleColumn(col);
      if (tr != null && tc != null && table.isCellEditable(tr, tc) && (filter == null || filter.accept(tr, tc))) {
        table.requestFocusInCell(tc, tr);
        return;
      }
    }
  }

}

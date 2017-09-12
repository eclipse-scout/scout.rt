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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractTableRowBuilder<T> {

  public ITableRow createTableRow(ILookupRow<T> dataRow) {
    ITableRow tableRow = createEmptyTableRow();
    tableRow.setEnabled(dataRow.isEnabled());
    Cell cell = tableRow.getCellForUpdate(1);
    if (dataRow.getTooltipText() != null) {
      cell.setTooltipText(dataRow.getTooltipText());
    }
    if (dataRow.getIconId() != null) {
      cell.setIconId(dataRow.getIconId());
    }
    if (dataRow.getBackgroundColor() != null) {
      cell.setBackgroundColor(dataRow.getBackgroundColor());
    }
    if (dataRow.getForegroundColor() != null) {
      cell.setForegroundColor(dataRow.getForegroundColor());
    }
    if (dataRow.getFont() != null) {
      cell.setFont(dataRow.getFont());
    }
    tableRow.setStatusNonchanged();
    return tableRow;
  }

  public ITableRow createRow() {
    return createEmptyTableRow();
  }

  public ITableRow createRow(Object rowValues) {
    if (!rowValues.getClass().isArray()) {
      throw new IllegalArgumentException("argument must be an array value []");
    }
    ITableRow row = createEmptyTableRow();
    for (int c = 0, nc = Array.getLength(rowValues); c < nc; c++) {
      row.setCellValue(c, Array.get(rowValues, c));
    }
    return row;
  }

  public List<ITableRow> createRowsByArray(Object dataArray) {
    return createRowsByArray(dataArray, ITableRow.STATUS_INSERTED);
  }

  public List<ITableRow> createRowsByArray(Object dataArray, int rowStatus) {
    if (dataArray == null) {
      dataArray = new Object[0];
    }
    if (dataArray instanceof Collection) {
      dataArray = ((Collection) dataArray).toArray();
    }
    if (!dataArray.getClass().isArray()) {
      throw new IllegalArgumentException("argument must be a matrix value [][]");
    }

    int rowCount = Array.getLength(dataArray);
    List<ITableRow> rows = new ArrayList<>(rowCount);
    for (int r = 0; r < rowCount; r++) {
      rows.add(createRow(new Object[]{Array.get(dataArray, r)}));
    }
    return rows;
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as a
   * <code>new AtomicReference&lt;Object&gt;</code>(Object[][]) so that the further processing can set the content of
   * the holder to null while processing.
   */
  public List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference) {
    return createRowsByMatrix(dataMatrixOrReference, ITableRow.STATUS_INSERTED);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as a
   * <code>new AtomicReference&lt;Object&gt;</code>(Object[][]) so that the further processing can set the content of
   * the holder to null while processing.
   */
  public List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference, int rowStatus) {
    Object dataMatrix;
    boolean isRef;
    if (dataMatrixOrReference instanceof AtomicReference<?>) {
      isRef = true;
      dataMatrix = ((AtomicReference<?>) dataMatrixOrReference).get();
    }
    else {
      isRef = false;
      dataMatrix = dataMatrixOrReference;
    }
    if (dataMatrix == null) {
      dataMatrix = new Object[0][0];
    }
    if (!dataMatrix.getClass().isArray()) {
      throw new IllegalArgumentException("argument must be a matrix value [][]");
    }
    //
    int rowCount = Array.getLength(dataMatrix);
    List<ITableRow> rows = new ArrayList<>(rowCount);
    if (isRef) {
      Object[] refData = new Object[rowCount];
      for (int r = 0; r < rowCount; r++) {
        refData[r] = Array.get(dataMatrix, r);
      }
      //clear the atomic reference
      dataMatrix = null;
      ((AtomicReference<?>) dataMatrixOrReference).set(null);
      dataMatrix = null;
      for (int r = 0; r < rowCount; r++) {
        ITableRow row = createRow(refData[r]);
        row.setStatus(rowStatus);
        rows.add(row);
        //clear the row immediately to help gc
        refData[r] = null;
      }
    }
    else {
      for (int r = 0; r < rowCount; r++) {
        ITableRow row = createRow(Array.get(dataMatrix, r));
        row.setStatus(rowStatus);
        rows.add(row);
      }
    }
    return rows;
  }

  public List<ITableRow> createRowsByCodes(Collection<? extends ICode<?>> codes) {
    List<ITableRow> result = new ArrayList<>(codes.size());
    for (ICode<?> code : codes) {
      ITableRow row = createRow(new Object[]{code.getId(), code.getText()});
      row.setIconId(code.getIconId());
      row.setBackgroundColor(code.getBackgroundColor());
      row.setForegroundColor(code.getForegroundColor());
      row.setFont(code.getFont());
      result.add(row);
    }
    return result;
  }

  protected abstract ITableRow createEmptyTableRow();

}

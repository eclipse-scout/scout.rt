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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public abstract class AbstractTableRowBuilder {

  public ITableRow createTableRow(LookupRow dataRow) throws ProcessingException {
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

  public ITableRow createRow() throws ProcessingException {
    return createEmptyTableRow();
  }

  public ITableRow createRow(Object rowValues) throws ProcessingException {
    if (!rowValues.getClass().isArray()) {
      throw new IllegalArgumentException("argument must be an array value []");
    }
    ITableRow row = createEmptyTableRow();
    for (int c = 0, nc = Array.getLength(rowValues); c < nc; c++) {
      row.setCellValue(c, Array.get(rowValues, c));
    }
    return row;
  }

  public ITableRow[] createRowsByArray(Object dataArray) throws ProcessingException {
    return createRowsByArray(dataArray, ITableRow.STATUS_INSERTED);
  }

  public ITableRow[] createRowsByArray(Object dataArray, int rowStatus) throws ProcessingException {
    if (dataArray == null) {
      dataArray = new Object[0];
    }
    if (dataArray instanceof Collection) {
      dataArray = ((Collection) dataArray).toArray();
    }
    if (!dataArray.getClass().isArray()) {
      throw new IllegalArgumentException("argument must be a matrix value [][]");
    }
    ITableRow[] rows = new ITableRow[Array.getLength(dataArray)];
    for (int r = 0; r < rows.length; r++) {
      rows[r] = createRow(new Object[]{Array.get(dataArray, r)});
    }
    return rows;
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new
   * AtomicReference<Object>(Object[][])
   * so that the further processing can set the content of the holder to null while processing.
   */
  public ITableRow[] createRowsByMatrix(Object dataMatrixOrReference) throws ProcessingException {
    return createRowsByMatrix(dataMatrixOrReference, ITableRow.STATUS_INSERTED);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new
   * AtomicReference<Object>(Object[][])
   * so that the further processing can set the content of the holder to null while processing.
   */
  public ITableRow[] createRowsByMatrix(Object dataMatrixOrReference, int rowStatus) throws ProcessingException {
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
    ITableRow[] rows = new ITableRow[Array.getLength(dataMatrix)];
    if (isRef) {
      Object[] refData = new Object[rows.length];
      for (int r = 0; r < rows.length; r++) {
        refData[r] = Array.get(dataMatrix, r);
      }
      //clear the atomic reference
      dataMatrix = null;
      ((AtomicReference<?>) dataMatrixOrReference).set(null);
      dataMatrix = null;
      for (int r = 0; r < rows.length; r++) {
        rows[r] = createRow(refData[r]);
        //clear the row immediately to help gc
        refData[r] = null;
        rows[r].setStatus(rowStatus);
      }
    }
    else {
      for (int r = 0; r < rows.length; r++) {
        rows[r] = createRow(Array.get(dataMatrix, r));
        rows[r].setStatus(rowStatus);
      }
    }
    return rows;
  }

  public ITableRow[] createRowsByCodes(ICode[] codes) throws ProcessingException {
    ITableRow[] rows = new ITableRow[codes.length];
    for (int i = 0; i < rows.length; i++) {
      rows[i] = createRow(new Object[]{codes[i].getId(), codes[i].getText()});
      rows[i].setIconId(codes[i].getIconId());
      rows[i].setBackgroundColor(codes[i].getBackgroundColor());
      rows[i].setForegroundColor(codes[i].getForegroundColor());
      rows[i].setFont(codes[i].getFont());
    }
    return rows;
  }

  protected abstract ITableRow createEmptyTableRow();

}

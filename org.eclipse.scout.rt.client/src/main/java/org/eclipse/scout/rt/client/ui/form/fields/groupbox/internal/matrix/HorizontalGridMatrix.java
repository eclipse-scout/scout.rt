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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.matrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.matrix.MatrixCursor.Orientation;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
public class HorizontalGridMatrix implements IGridMatrix {

  private final int m_columnCount;
  private int m_rowCount;
  private final MatrixCursor m_cursor;
  private final Map<MatrixIndex, Cell> m_assignedCells = new HashMap<>();

  public HorizontalGridMatrix(int columnCount) {
    m_columnCount = columnCount;
    m_cursor = new MatrixCursor(0, 0, columnCount, Integer.MAX_VALUE, Orientation.Horizontal);
  }

  public int getColumnCount() {
    return m_columnCount;
  }

  public int getRowCount() {
    return m_rowCount;
  }

  @Override
  public boolean computeGridData(List<IFormField> fields) {
    for (IFormField f : fields) {
      GridData hints = GridDataBuilder.createFromHints(f, m_columnCount);
      GridData gridData = new GridData(hints);
      gridData.w = Math.min(hints.w, m_columnCount);
      add(f, hints, gridData);
      f.setGridDataInternal(gridData);
    }
    return true;
  }

  private void add(IFormField field, GridData hints, GridData data) {
    nextFree(data.w, data.h);
    MatrixIndex currentIndex = m_cursor.currentIndex();
    if (data.w <= (m_columnCount - currentIndex.x)) {
      data.x = currentIndex.x;
      data.y = currentIndex.y;
      // add field
      for (int xx = currentIndex.x; xx < currentIndex.x + data.w; xx++) {
        for (int yy = currentIndex.y; yy < currentIndex.y + data.h; yy++) {
          m_assignedCells.put(new MatrixIndex(xx, yy), new Cell(field, data));
        }
      }
      m_rowCount = currentIndex.y + data.h;
    }
    else {
      // add dummy cell
      m_assignedCells.put(m_cursor.currentIndex(), new Cell());
      add(field, hints, data);
    }
  }

  private void nextFree(int w, int h) {
    m_cursor.increment();
    MatrixIndex currentIndex = m_cursor.currentIndex();
    if (!isAllCellFree(currentIndex.x, currentIndex.y, w, h)) {
      m_assignedCells.put(currentIndex, new Cell());
      nextFree(w, h);
    }
  }

  private boolean isAllCellFree(int x, int y, int w, int h) {
    if (x + w > getColumnCount()) {
      return false;
    }
    for (int xi = x; xi < x + w; xi++) {
      for (int yi = y; yi < y + h; yi++) {
        if (m_assignedCells.get(new MatrixIndex(xi, yi)) != null) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("----Horizontal Grid Matrix [columnCount=" + getColumnCount() + ",rowCount=" + getRowCount() + "]--------------\n");
    MatrixCursor tempCursor = new MatrixCursor(0, 0, getColumnCount(), getRowCount(), Orientation.Horizontal);
    while (tempCursor.increment()) {
      Cell cell = m_assignedCells.get(tempCursor.currentIndex());
      builder.append("cell[").append(tempCursor.currentIndex().x).append(", ").append(tempCursor.currentIndex().y).append("] ");
      if (cell == null) {
        builder.append("NULL");
      }
      else if (cell.field == null) {
        builder.append("PlaceHolder");
      }
      else {
        builder.append(cell.field);
      }
      builder.append("\n");
    }
    return builder.toString();
  }
}

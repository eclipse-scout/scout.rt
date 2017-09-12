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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.AbstractGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.matrix.MatrixCursor.Orientation;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
public class VerticalGridMatrix implements IGridMatrix {

  private MatrixCursor m_cursor;

  private final Map<IFormField, GridData> m_fieldGridDatas = new HashMap<>();
  private final Map<MatrixIndex, Cell> m_cells = new HashMap<>();
  private final Map<IFormField, Integer> m_formFieldIndexes = new HashMap<>();

  public VerticalGridMatrix(int columnCount, int rowCount) {
    this(0, 0, columnCount, rowCount);
  }

  public VerticalGridMatrix(int x, int y, int columnCount, int rowCount) {
    m_cursor = new MatrixCursor(x, y, columnCount, rowCount, Orientation.Vertical);
  }

  public void resetAll(int columnCount, int rowCount) {
    m_fieldGridDatas.clear();
    m_cells.clear();
    m_formFieldIndexes.clear();
    m_cursor = new MatrixCursor(m_cursor.startX, m_cursor.startY, columnCount, rowCount, Orientation.Vertical);
  }

  @Override
  public boolean computeGridData(List<IFormField> fields) {
    int i = 0;
    for (IFormField field : fields) {
      m_formFieldIndexes.put(field, i++);
      GridData gridData = AbstractGroupBoxBodyGrid.getGridDataFromHints(field, m_cursor.columnCount);
      if (add(field, gridData)) {
        m_fieldGridDatas.put(field, gridData);
      }
      else {
        return false;
      }
    }
    return true;
  }

  public GridData getGridData(IFormField field) {
    return m_fieldGridDatas.get(field);
  }

  private boolean add(IFormField field, GridData data) {
    MatrixIndex currentIndex = m_cursor.currentIndex();
    if (data.w > 1) {
      // try to reorganize fields above
      int x = currentIndex.x;
      int y = currentIndex.y;
      // try to move left if the right border of the field is outside the column range
      while (x + data.w > m_cursor.startX + m_cursor.columnCount) {
        // shift left and bottom
        x--;
        y = m_cursor.rowCount - 1;
      }
      reorganizeGridAbove(x, y, data.w);
    }
    if (!nextFree(data.w, data.h)) {
      return false;
    }
    currentIndex = m_cursor.currentIndex();
    data.x = currentIndex.x;
    data.y = currentIndex.y;
    // add field
    for (int xx = currentIndex.x; xx < currentIndex.x + data.w; xx++) {
      for (int yy = currentIndex.y; yy < currentIndex.y + data.h; yy++) {
        m_cells.put(new MatrixIndex(xx, yy), new Cell(field, data));
      }
    }
    return true;

  }

  private void reorganizeGridAbove(int x, int y, int w) {
    Set<IFormField> fieldsToReorganize = new HashSet<>();
    Map<MatrixIndex, Cell> occupiedCells = new HashMap<>();
    Bounds reorgBounds = new Bounds(x, 0, w, y + 1);

    int minY = y;
    int usedCells = 0;
    boolean continueLoop = true;
    for (int yi = y; yi >= 0 && continueLoop; yi--) {
      for (int xi = x; xi < x + w && continueLoop; xi++) {
        MatrixIndex matrixIndex = new MatrixIndex(xi, yi);
        Cell cell = m_cells.get(matrixIndex);
        if (cell != null && !cell.isEmpty()) {
          GridData gd = cell.fieldGridData;

          if (horizontalMatchesOrOverlaps(reorgBounds, gd)) {
            continueLoop = false;
          }
          else if (horizontalOverlapsOnSide(reorgBounds, gd)) {
            // freeze the cells for reorganization
            occupiedCells.put(matrixIndex, cell);
            usedCells++;
            minY = Math.min(matrixIndex.y, minY);
          }
          // includes
          else {
            // add field to reorganization
            m_cells.remove(matrixIndex);
            fieldsToReorganize.add(cell.field);
            usedCells++;
            minY = Math.min(matrixIndex.y, minY);
          }
        }
      }
    }
    if (fieldsToReorganize.isEmpty()) {
      return;
    }
    // sort fields
    List<IFormField> sortedFieldsToReorganize = new ArrayList<>(fieldsToReorganize);
    sortedFieldsToReorganize.sort((o1, o2) -> {
      Integer i1 = m_formFieldIndexes.get(o1);
      Integer i2 = m_formFieldIndexes.get(o2);
      return i1.compareTo(i2);
    });
    reorgBounds.y = minY;

    VerticalGridMatrix reorgMatrix = new VerticalGridMatrix(reorgBounds.x, reorgBounds.y, reorgBounds.w, (usedCells + reorgBounds.w - 1) / reorgBounds.w);
    reorgMatrix.addCells(occupiedCells);
    while (!reorgMatrix.computeGridData(sortedFieldsToReorganize)) {
      reorgMatrix.resetAll(reorgMatrix.getColumnCount(), reorgMatrix.getRowCount() + 1);
    }
    m_cursor.reset();
    m_cells.putAll(reorgMatrix.getCells());
    m_fieldGridDatas.putAll(reorgMatrix.getFieldGridDatas());

  }

  private boolean nextFree(int w, int h) {
    if (!m_cursor.increment()) {
      return false;
    }
    MatrixIndex currentIndex = m_cursor.currentIndex();
    if (!isAllCellFree(currentIndex.x, currentIndex.y, w, h)) {
      m_cells.computeIfAbsent(currentIndex, k -> new Cell());
      return nextFree(w, h);
    }
    return true;
  }

  private boolean isAllCellFree(int x, int y, int w, int h) {
    if (x + w > m_cursor.startX + m_cursor.columnCount
        || y + h > m_cursor.startY + m_cursor.rowCount) {
      return false;
    }
    for (int xi = x; xi < x + w; xi++) {
      for (int yi = y; yi < y + h; yi++) {
        if (m_cells.get(new MatrixIndex(xi, yi)) != null) {
          return false;
        }
      }
    }
    return true;
  }

  protected Map<MatrixIndex, Cell> getCells() {
    return m_cells;
  }

  public Map<IFormField, GridData> getFieldGridDatas() {
    return m_fieldGridDatas;
  }

  protected void addCells(Map<MatrixIndex, Cell> cells) {
    m_cells.putAll(cells);
  }

  public int getColumnCount() {
    return m_cursor.columnCount;
  }

  public int getRowCount() {
    return m_cursor.rowCount;
  }

  protected static boolean horizontalMatchesOrOverlaps(Bounds bounds, GridData gd) {
    return bounds.x >= gd.x && bounds.x + bounds.w <= gd.x + gd.w;
  }

  protected static boolean horizontalOverlapsOnSide(Bounds bounds, GridData gd) {
    return bounds.x > gd.x || bounds.x + bounds.w < gd.x + gd.w;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("----Vertical Grid Matrix [columnCount=" + getColumnCount() + ",rowCount=" + getRowCount() + "]--------------\n");
    MatrixCursor tempCursor = new MatrixCursor(0, 0, getColumnCount(), getRowCount(), Orientation.Vertical);
    while (tempCursor.increment()) {
      Cell cell = m_cells.get(tempCursor.currentIndex());
      builder.append("cell[").append(tempCursor.currentIndex().x).append(", ").append(tempCursor.currentIndex().y).append("] ");
      if (cell == null) {
        builder.append("NULL");
      }
      else if (cell.field == null) {
        builder.append("PlaceHolder");
      }
      else {
        builder.append(cell.field.getClass().getSimpleName()).append("/").append(cell.field.getLabel()).append("\t").append(cell.field.getGridData());
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  @SuppressWarnings({"squid:S00116", "squid:ClassVariableVisibilityCheck"})
  protected static final class Bounds {
    public int x, y, w, h;

    public Bounds(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }
  }
}

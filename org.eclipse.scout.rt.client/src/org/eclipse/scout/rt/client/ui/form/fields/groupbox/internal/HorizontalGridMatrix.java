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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
public class HorizontalGridMatrix {

  private final int m_columnCount;
  private int m_rowCount;
  private MatrixPosition m_nextFree = new MatrixPosition(0, 0);
  private final Map<MatrixPosition, Cell> m_assignedCells = new HashMap<HorizontalGridMatrix.MatrixPosition, HorizontalGridMatrix.Cell>();

  public HorizontalGridMatrix(int columnCount) {
    m_columnCount = columnCount;
  }

  public int getColumnCount() {
    return m_columnCount;
  }

  public int getRowCount() {
    return m_rowCount;
  }

  public GridData add(IFormField field) {
    GridData hints = GridDataBuilder.createFromHints(field, m_columnCount);
    GridData gridData = new GridData(hints);
    gridData.w = Math.min(hints.w, m_columnCount);
    add(field, hints, gridData);
    return gridData;
  }

  private void add(IFormField field, GridData hints, GridData data) {
    if (data.w <= (m_columnCount - m_nextFree.x)) {
      data.x = m_nextFree.x;
      data.y = m_nextFree.y;
      // add field
      for (int xx = m_nextFree.x; xx < m_nextFree.x + data.w; xx++) {
        for (int yy = m_nextFree.y; yy < m_nextFree.y + data.h; yy++) {
          m_assignedCells.put(new MatrixPosition(xx, yy), new Cell(field));
        }
      }
      nextFree();

    }
    else {
      // add dummy cell
      m_assignedCells.put(m_nextFree.copy(), new Cell());
      nextFree();
      add(field, hints, data);
    }
  }

  private void nextFree() {
    m_rowCount = Math.max(m_rowCount, m_nextFree.y + 1);
    m_nextFree.x += 1;
    if (m_nextFree.x >= m_columnCount) {
      m_nextFree.x = 0;
      m_nextFree.y += 1;
    }
    if (m_assignedCells.get(m_nextFree) != null) {
      nextFree();
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("-------------- GridMatrix ----------------\n");
    MatrixPosition index = new MatrixPosition(0, 0);
    Cell cell = m_assignedCells.get(index);
    while (cell != null) {
      builder.append("cell[").append(index.x).append(", ").append(index.y).append("] ");
      if (cell.field == null) {
        builder.append("PlaceHolder");
      }
      else {
        builder.append(cell.field);
      }
      builder.append("\n");
      index.x++;
      if (index.x >= m_columnCount) {
        index.x = 0;
        index.y++;
      }
      cell = m_assignedCells.get(index);
    }
    return builder.toString();
  }

  public static class Cell {
    IFormField field;

    public Cell() {
      this(null);
    }

    public Cell(IFormField field) {
      this.field = field;
    }

  }

  private static class MatrixPosition {
    public int x, y;

    public MatrixPosition(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public MatrixPosition copy() {
      return new MatrixPosition(x, y);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + x;
      result = prime * result + y;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      MatrixPosition other = (MatrixPosition) obj;
      if (x != other.x) {
        return false;
      }
      if (y != other.y) {
        return false;
      }
      return true;
    }
  }

}

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

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
@SuppressWarnings({"squid:S00116", "squid:ClassVariableVisibilityCheck"})
public class MatrixCursor {
  public enum Orientation {
    Horizontal,
    Vertical
  }

  public final int startX;
  public final int startY;
  public final int columnCount;
  public final int rowCount;
  private final MatrixIndex m_currentIndex = new MatrixIndex(-1, -1);
  private final Orientation m_orientation;

  public MatrixCursor(int x, int y, int columnCount, int rowCount, Orientation orientation) {
    startX = x;
    startY = y;
    this.columnCount = columnCount;
    this.rowCount = rowCount;
    m_orientation = orientation;
  }

  public void reset() {
    m_currentIndex.x = -1;
    m_currentIndex.y = -1;
  }

  public Orientation getOrientation() {
    return m_orientation;
  }

  public boolean increment() {
    if (m_currentIndex.x < 0 || m_currentIndex.y < 0) {
      // initial
      m_currentIndex.x = startX;
      m_currentIndex.y = startY;
    }
    else if (getOrientation() == Orientation.Horizontal) {
      m_currentIndex.x++;
      if (m_currentIndex.x >= startX + columnCount) {
        m_currentIndex.x = startX;
        m_currentIndex.y++;
      }
    }
    else {
      // vertical
      m_currentIndex.y++;
      if (m_currentIndex.y >= startY + rowCount) {
        m_currentIndex.y = startY;
        m_currentIndex.x++;
      }
    }
    if (m_currentIndex.x >= startX + columnCount || m_currentIndex.y >= startY + rowCount) {
      return false;
    }
    return true;
  }

  public boolean decrement() {
    if (m_currentIndex.x < 0 || m_currentIndex.y < 0) {
      return false;
    }
    else if (m_currentIndex.x >= startX + columnCount || m_currentIndex.y >= startY + rowCount) {
      m_currentIndex.x = startX + columnCount - 1;
      m_currentIndex.y = startY + rowCount - 1;
    }
    else if (getOrientation() == Orientation.Horizontal) {
      m_currentIndex.x--;
      if (m_currentIndex.x < startX) {
        m_currentIndex.x = startX + columnCount - 1;
        m_currentIndex.y--;
      }
    }
    else {
      // vertical
      m_currentIndex.y--;
      if (m_currentIndex.y < startY) {
        m_currentIndex.y = startY + rowCount - 1;
        m_currentIndex.x--;
      }
    }
    if (m_currentIndex.x < startX || m_currentIndex.y < startY) {
      return false;
    }
    return true;
  }

  public MatrixIndex currentIndex() {
    return new MatrixIndex(m_currentIndex);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("MatrixCursor [");
    builder.append("orientation=").append(m_orientation);
    builder.append(", startX=").append(startX);
    builder.append(", startY=").append(startY);
    builder.append(", columnCount=").append(columnCount);
    builder.append(", rowCount=").append(rowCount);
    builder.append(", currentIndex=").append(m_currentIndex);
    builder.append("]");
    return builder.toString();
  }
}

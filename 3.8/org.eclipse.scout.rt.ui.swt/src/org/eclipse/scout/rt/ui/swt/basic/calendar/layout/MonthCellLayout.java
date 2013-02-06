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
package org.eclipse.scout.rt.ui.swt.basic.calendar.layout;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.basic.calendar.widgets.SwtCalendar;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Layouting calendar month cells.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class MonthCellLayout extends Layout {
  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtCalendar.class);

  private int numColumns = 1;
  private int numLines = 1;

  public int getNumColumns() {
    return numColumns;
  }

  public void setNumColumns(int numColumns) {
    this.numColumns = numColumns;
  }

  public int getNumLines() {
    return numLines;
  }

  public void setNumLines(int numLines) {
    this.numLines = numLines;
  }

  @Override
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    // return a dummy size
    return new Point(0, 0);
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    // get children (items)
    Control[] children = composite.getChildren();

    // clipRect of parent composite
    Rectangle clipRect = composite.getClientArea();

    //logger.debug("size: " + clipRect.width + " w, " + clipRect.height + " h.");

    float cellWidth = ((float) clipRect.width) / numColumns;
    float cellHeight = ((float) clipRect.height) / numLines;

    // init pos array
    int[][] ary = new int[numColumns][numLines];
    for (int v = 0; v < numLines; v++) {
      for (int h = 0; h < numColumns; h++) {
        ary[h][v] = -1;
      }
    }

    // 1st pass
    int colIndex = 0;
    int lineIndex = 0;
    for (int i = 0; i < children.length;) {

      // cell still free?
      if (ary[colIndex][lineIndex] == -1) {
        Control child = children[i];
        MonthCellData dat = (MonthCellData) child.getLayoutData();

        int hSpan = Math.max(1, Math.min(dat.getHorizontalSpan(), numColumns - colIndex));
        int vSpan = Math.max(1, Math.min(dat.getVerticalSpan(), numLines - lineIndex));

        for (int v = 0; v < vSpan; v++) {
          for (int h = 0; h < hSpan; h++) {
            // assign cell i
            ary[colIndex + h][lineIndex + v] = i;
          }
        }
        // next elem
        i++;
      }

      colIndex = (colIndex + 1) % numColumns;
      if (colIndex % numColumns == 0) {
        lineIndex++;
      }
    }

    int lastId = -1;
    for (int v = 0; v < numLines; v++) {
      for (int h = 0; h < numColumns; h++) {
        int index = ary[h][v];

        // new cell?
        if (index > lastId) {

          Control child = children[index];
          MonthCellData dat = (MonthCellData) child.getLayoutData();

          int x = Math.round(h * cellWidth);
          int y = Math.round(v * cellHeight);
          int width = Math.round(dat.getHorizontalSpan() * cellWidth);
          int height = Math.round(dat.getVerticalSpan() * cellHeight);

          // inset not (yet) done

          Rectangle bounds = new Rectangle(x, y, width, height);

          child.setBounds(bounds);

          lastId = index;
        }
      }
    }
  }
}

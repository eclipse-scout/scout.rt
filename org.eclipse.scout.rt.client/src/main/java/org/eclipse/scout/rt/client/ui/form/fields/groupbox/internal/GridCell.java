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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import java.io.PrintStream;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;

/**
 * Grid Cell as container helper for dynamic layouting in scout
 */
public class GridCell {
  private IFormField m_field;
  private int m_gridColumnCount;
  private GridCell m_up, m_down, m_right;

  protected GridData data;

  public GridCell(IFormField f, int gridColumnCount) {
    m_field = f;
    m_gridColumnCount = gridColumnCount;
    if (f != null) {
      GridData hints = GridDataBuilder.createFromHints(f, gridColumnCount);
      data = new GridData(hints);
    }
  }

  public IFormField getField() {
    return m_field;
  }

  // down, then right
  protected void calculateGridLayout(int gridX, int gridY) {
    if (data.w >= m_gridColumnCount || gridX >= m_gridColumnCount) {
      gridX = 0;
    }
    data.x = gridX;
    data.y = gridY;
    //
    if (m_field != null) {
      m_field.setGridDataInternal(new GridData(data));
    }
    //
    if (m_down != null) {
      m_down.calculateGridLayout(gridX, gridY + data.h);
    }
    if (m_right != null) {
      m_right.calculateGridLayout(gridX + data.w, gridY);
    }
  }

  protected GridCell getSplitCell(int before, int maxWeight, int colCount) {
    int after = maxWeight - before - data.h * data.w;
    // split-off item with ancestor and descendant
    if ((colCount - 1) * before >= (data.h + after)) {
      return this;
    }
    // split-off item with ancestor and no descendant
    if (before > 0 && after == 0) {
      return this;
    }
    // not the split-off item
    if (m_down != null) {
      GridCell cell = m_down.getSplitCell(before + data.h, maxWeight, colCount);
      if (cell != null) {
        return cell;
      }
    }
    if (m_right != null) {
      GridCell cell = m_right.getSplitCell(before, maxWeight, colCount);
      if (cell != null) {
        return cell;
      }
    }
    return null;
  }

  /**
   * used for splitting of items the weight is the maximum y (getMaxY()) but weighted with the column count. so an item
   * of 4 rows and 2 columns adds a weight of 4*2
   */
  protected int getMaxWeight() {
    int wDown = m_down != null ? m_down.getMaxWeight() : 0;
    int wRight = m_right != null ? m_right.getMaxWeight() : 0;
    return Math.max(data.h * Math.max(data.w, 1) + wDown, wRight);
  }

  protected int getMaxY() {
    int mdown = m_down != null ? m_down.getMaxY() : 0;
    int mright = m_right != null ? m_right.getMaxY() : 0;
    return Math.max(data.h + mdown, mright);
  }

  protected int getMaxCols() {
    int mdown = m_down != null ? m_down.getMaxCols() : 0;
    int mright = m_right != null ? m_right.getMaxCols() : 0;
    return Math.max(mdown, data.w + mright);
  }

  protected void addBottomCell(GridCell c) {// down or right
    int mdown = m_down != null ? m_down.getMaxY() : 0;
    int mright = m_right != null ? m_right.getMaxY() : 0;
    if (m_down != null && data.h + mdown >= mright) {
      m_down.addBottomCellImpl(c);
    }
    else if (m_right != null && mright > data.h) {
      m_right.addBottomCellImpl(c);
    }
    else {
      this.m_down = c;
      c.m_up = this;
    }
  }

  private void addBottomCellImpl(GridCell c) {// down only
    if (m_down != null) {
      m_down.addBottomCellImpl(c);
    }
    else {
      this.m_down = c;
      c.m_up = this;
    }
  }

  protected GridCell getUp() {
    return m_up;
  }

  protected void setDown(GridCell down) {
    m_down = down;
  }

  protected void setRight(GridCell right) {
    m_right = right;
  }

  protected boolean isProcessButton() {
    if (m_field instanceof IButton) {
      return ((IButton) m_field).isProcessButton();
    }
    else {
      return false;
    }
  }

  protected boolean isSystemProcessButton() {
    if (isProcessButton()) {
      return ((IButton) m_field).getSystemType() != IButton.SYSTEM_TYPE_NONE;
    }
    else {
      return false;
    }
  }

  void printCell(PrintStream out, String pref) {
    out.println(pref + "field: " + getField());
    if (m_up != null) {
      out.println(pref + "-up: " + m_up.getField());
//      m_up.printCell(out, pref + "  ");
    }
    if (m_right != null) {
      out.println(pref + "-right: " + m_right.getField());
      m_right.printCell(out, pref + "  ");
    }
    if (m_down != null) {

      out.println(pref + "-down: " + m_down.getField());
      m_down.printCell(out, pref + "  ");
    }

  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getClass().getSimpleName());
    buf.append("[");
    buf.append(data);
    buf.append(" field=" + (m_field != null ? m_field.getFieldId() : null));
    buf.append(" class=" + (m_field != null ? m_field.getClass() : null));
    buf.append("]");
    return buf.toString();
  }

}// end class

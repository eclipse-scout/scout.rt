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

import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Layouting within a day or week cell (i.e. item placement)
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class WeekItemLayout extends Layout {

  private static final int MIN_WIDTH = 3;
  private static final int MIN_HEIGHT = 4;
  private int m_startHour = CalendarConstants.DAY_TIMELINE_START_TIME;
  private int m_endHour = CalendarConstants.DAY_TIMELINE_END_TIME;
  private boolean m_useOverflowCells = true;

  @Override
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    // return a dummy size
    return new Point(0, 0);
  }

  @Override
  /**
   * Does the layout of the children controls, which are in this case calendar
   * item within a week or day cell.
   */
  protected void layout(Composite composite, boolean flushCache) {
    // get children (items)
    Control[] children = composite.getChildren();

    Rectangle clipRect = composite.getClientArea();

    for (Control child : children) {
      // get WeekItemData for that item
      WeekItemData dat = (WeekItemData) child.getLayoutData();

      // possible header offset
      int yOffset = Math.max(0, dat.getOffsetCellHeader() - 1);
      // timeless height: 24 pixels x the max nb of timeless items but at most the third of the cell height
      int timelessHeight = Math.min(24 * dat.getTimelessMaxCount(), 33 * clipRect.height / 100);
      // hTimeless: timelessHeight - 1 but at least 0
      int hTimeless = Math.max(0, timelessHeight - 1);
      int yTimed = yOffset + hTimeless + 1;
      int hTimed = clipRect.height - yOffset - hTimeless;

      Rectangle r = new Rectangle(0, 0, 0, 0);
      long intervalMillis = (m_endHour /*+ 1*/- m_startHour) * CalendarConstants.HOUR_MILLIS;

      int timelessItemHeight = 0;
      int countTimeless = dat.getTimelessCount();
      if (countTimeless > 0) {
        // timelessItemHeight: timeless part divided by the nb of timeless items, but at least 5
        timelessItemHeight = Math.max(5, hTimeless / countTimeless);
      }
      // TODO: is this correct
      int absX = 0;
      int absY = 0;

      // get clipping rect of parent cell
      int w = clipRect.width;

      // timed
      if (dat.getItem().isTimed()) {
        r.x = (int) (dat.getItem().getX0() * w);
        r.width = (int) (dat.getItem().getX1() * w) - r.x;

        // check that there is a minimum width
        r.width = r.width < MIN_WIDTH ? MIN_WIDTH : r.width;

        r.y = yTimed + (int) (dat.getItem().getFromRelative() * hTimed / intervalMillis);
        r.height = yTimed + (int) (dat.getItem().getToRelative() * hTimed / intervalMillis) - r.y;

        // check min height
        r.height = r.height < MIN_HEIGHT ? MIN_HEIGHT : r.height;
      }
      // timeless
      else {
        r.x = 0;
        r.width = w;
        r.y = yOffset + (dat.getTimelessIndex() * timelessItemHeight);
        r.height = r.y < hTimeless + yOffset ? timelessItemHeight : 0;

      } // end if timeless

      // insets and top/bottom gap
      r.x = r.x + 1;
      r.width = Math.max(0, r.width - 2);
      r.y += 1;
      r.height = Math.max(0, r.height - 2);

      //store dimensions in ABSOLUT (+x0,+y0) coordinates (TODO: doesn't work here)
      child.setBounds(r.x + absX, r.y + absY, r.width, r.height);

    } // end for loop

  }

  /**
   * @param startHour
   * @param endHour
   * @param useOverflowCells
   */
  public void setWorkingHours(int startHour, int endHour, boolean useOverflowCells) {
    m_startHour = startHour;
    m_endHour = endHour;
    m_useOverflowCells = useOverflowCells;
  }

}

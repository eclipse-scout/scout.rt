/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.calendar.widgets;

import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarConstants;
import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarItemContainer;
import org.eclipse.scout.rt.ui.swt.basic.calendar.DisplayMode;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.scout.rt.ui.swt.basic.calendar.layout.WeekItemData;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Represents a calendar item within a week or day cell.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class WeekCalendarItem extends AbstractCalendarItem {

  /**
   * if this item is timeless, this field contains it index
   * within the other timeless items of the same cell
   */
  private int m_timelessIndex = -1;

  public WeekCalendarItem(AbstractCell parent, int style, CalendarItemContainer item) {
    super(parent, style, item);
  }

  /** prerequisite: max timeless count should already have been done */
  public void setLayoutData() {

    // We want a correct numbering of timeless item. Global counter stored
    // inside WeekCell (only used if timeless).
    int timelessIndex = 0;
    if (!getItem().isTimed()) {
      timelessIndex = ((WeekCell) getCell()).getNextTimelessCounter();
    }

    WeekItemData wid = new WeekItemData();
    wid.setItem(getItem());
    wid.setTimelessCount(getCell().getCountTimelessItems());
    wid.setTimelessMaxCount(getCell().getCalendar().getCentralPanel().getTimelessMaxCount());
    wid.setTimelessIndex(timelessIndex);
    wid.setOffsetCellHeader(getCell().getCalendar().getDisplayMode() == DisplayMode.DAY ? 0 : CalendarConstants.OFFSET_CELL_HEADER_Y);
    setLayoutData(wid);
  }

  @Override
  protected void createControls() {
  }

  @Override
  protected void setLayout() {
  }

  @Override
  protected void hookListeners() {
    super.hookListeners();
  }

  protected void drawLabel(PaintEvent e) {
    Rectangle bounds = getBounds();

    // label
    // drawn within this composite, ref point is composite
    Rectangle rlabel = new Rectangle(0, 0, bounds.width, bounds.height);
    //
    getItem().setLabeled(true);/*r.height>=fm.getAscent() && r.width>=SWITCH_ITEM_WIDTH*/
    FontMetrics fm = e.gc.getFontMetrics();
    if (getItem().isLabeled()) {
      String s = getCell().getCalendar().getModel().getLabel(getItem().getItem(), getCell().getDate().getTime());
      int centery = Math.max(0, (rlabel.height - fm.getAscent()) / 2 - 3);
      e.gc.drawString(s, rlabel.x + 3, rlabel.y + centery);
    }
  }

  @Override
  public void paintControl(PaintEvent e) {

    // background color
    Color color = new Color(SwtColors.getStandardDisplay(), getItem().getColor().getRed(), getItem().getColor().getGreen(), getItem().getColor().getBlue());
    if (getItem().getItem().equals(getCell().getCalendar().getSelectedItem())) {
      color = SwtColors.getInstance().getDarker(color);
    }
    setBackground(color);

    drawLabel(e);
  }
}

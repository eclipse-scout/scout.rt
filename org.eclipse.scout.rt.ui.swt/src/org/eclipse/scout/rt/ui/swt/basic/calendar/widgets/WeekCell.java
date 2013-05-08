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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.client.ui.basic.calendar.DateTimeFormatFactory;
import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarConstants;
import org.eclipse.scout.rt.ui.swt.basic.calendar.DisplayMode;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.scout.rt.ui.swt.basic.calendar.layout.WeekItemLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * Cell within a week or day calendar.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class WeekCell extends AbstractCell {

  /** real y offset in use (depends on m_drawHeader) */
  private int m_realOffsetY;

  /** should draw header? */
  private boolean m_drawHeader;

  /** used if this item is timeless, index within other timeless items */
  private int m_timelessCounter = 0;

  public WeekCell(Composite parent, int style, SwtCalendar calendar, Date cellDate, boolean isFirstColumn, boolean isCurrentMonth) {
    super(parent, style);

    setCalendar(calendar);
    setDate(Calendar.getInstance());
    getDate().setTime(cellDate);
    setFirstColumn(isFirstColumn);
    m_drawHeader = getCalendar().getDisplayMode() != DisplayMode.DAY;
    setSelectedInternal(false);
    setCurrentPeriod(isCurrentMonth);

    // what is the real y offset used?
    m_realOffsetY = m_drawHeader ? CalendarConstants.OFFSET_CELL_HEADER_Y : 0;

    createControls();
    setVisualState();

    /**
     * no add calendar items here, as we need first to compute some
     * values (e.g. maxTimelessCount) that depends on all cells, so wait
     */

    hookListeners();

  }

  protected void createControls() {

    // calc vertical span for this cell
    int vertSpan = 2; // per default 2
    int day = getDate().get(Calendar.DAY_OF_WEEK);
    int firstDay = getCalendar().getFirstDayOfWeek();
    int weekEndDay1 = (firstDay - 1 + 5) % 7 + 1;
    int weekEndDay2 = (firstDay - 1 + 6) % 7 + 1;
    if (getCalendar().getCondensedMode()
        && getCalendar().getDisplayMode() == DisplayMode.WEEK
        && (day == weekEndDay1 || day == weekEndDay2)) {
      vertSpan = 1;
    }

    GridData gd;
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    gd.verticalSpan = vertSpan;
    this.setLayoutData(gd);

    // create new grid layout
    WeekItemLayout layout = new WeekItemLayout();
    layout.setWorkingHours(getCalendar().getStartHour(), getCalendar().getEndHour(), getCalendar().getUseOverflowCells());
    this.setLayout(layout);
  }

  @Override
  protected void setVisualState() {
    if (m_drawHeader) {
      super.setVisualState();
    }
  }

  @Override
  protected void drawTimeline(PaintEvent e) {

    Rectangle clipRect = getBounds();
    int timelessMaxCount = getCalendar().getCentralPanel().getTimelessMaxCount();

    // timeless height: 24 pixels x the max nb of timeless items but at most the third of the cell height
    int timelessHeight = Math.min(24 * timelessMaxCount, 33 * clipRect.height / 100);
    // hTimeless: timelessHeight - 1 but at least 0
    int hTimeless = Math.max(0, timelessHeight - 1);
    int yTimed = m_realOffsetY + hTimeless + 1;

    Rectangle bounds = getBounds();
    int slots = (getCalendar().getEndHour() - getCalendar().getStartHour());
    double deltaY = Double.valueOf((bounds.height - yTimed) / (slots * 1.0));

    // set foreground color
    e.gc.setForeground(BORDER_UNSELECTED_COLOR);

    // draw noon rect
    int x1 = 1;
    int y1 = (int) Math.round(deltaY * (12 - getCalendar().getStartHour())) + yTimed;
    int x2 = bounds.width - 3;
    int y2 = (int) Math.round(deltaY);
    Rectangle noon = new Rectangle(x1, y1, x2, y2);
    if (getCalendar().getMarkNoonHour() || (!isCurrentPeriod() && getCalendar().getMarkOutOfMonthDays())) {
      e.gc.setBackground(SwtColors.getInstance().getLightgray());
    }
    else {
      e.gc.setBackground(SwtColors.getInstance().getWhite());
    }
    e.gc.fillRectangle(noon);
    e.gc.setBackground(SwtColors.getInstance().getWhite());

    int time = getCalendar().getStartHour(); // we have 1 slot before start time
    for (int i = 0; i < slots; i++) { // we go one slot after end time
      int y = (int) Math.round(deltaY * i) + yTimed;
      e.gc.drawLine(0, y, bounds.width - 1, y);

      time++;
    }
  }

  /** This method for WeekCell needs to be public */
  @Override
  public void addCalendarItems() {
    // so just call the protected method from the base class
    super.addCalendarItems();
  }

  /** set layout data for all items */
  public void setItemsLayoutData() {
    if (getWidgetItems() != null) {
      for (AbstractCalendarItem item : getWidgetItems()) {
        WeekCalendarItem it = (WeekCalendarItem) item;
        it.setLayoutData();
      }
    }
  }

  @Override
  protected void drawLabels(PaintEvent e) {
    if (m_drawHeader) {
      if (isFirstColumn()) {
        drawWeekLabel(e);
      }

      drawDayLabel(e);
    }
  }

  public int getNextTimelessCounter() {
    return m_timelessCounter++;
  }

  public void resetTimelessCounter() {
    m_timelessCounter = 0;
  }

  @Override
  protected void disposeCalendarItems() {
    super.disposeCalendarItems();

    // clear timeless counter
    resetTimelessCounter();
  }

  @Override
  public String toString() {
    DateFormat weekDayFmt = new SimpleDateFormat("EEEEE", Locale.getDefault());
    DateFormat dateFmt = new DateTimeFormatFactory().getDayMonthYear(DateFormat.LONG);
    return "WeekCell {" + weekDayFmt.format(getDate().getTime()) + " " + dateFmt.format(getDate().getTime()) + "}";
  }

}

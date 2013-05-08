package org.eclipse.scout.rt.ui.svg.calendar.builder;

import java.util.Calendar;

import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.svg.calendar.comp.IComponentElementFactory;
import org.eclipse.scout.rt.ui.svg.calendar.comp.TimeLineComponentElementFactory;
import org.w3c.dom.Element;

public class CalendarWorkWeekBuilder extends AbstractCalendarDocumentBuilder {
  public CalendarWorkWeekBuilder() {
    super("resources/WorkWeekCalendar.svg");
  }

  @Override
  protected int getNumWeekdays() {
    return 5;
  }

  @Override
  protected IComponentElementFactory getComponentElementFactory() {
    return new TimeLineComponentElementFactory(getStartHour(), getEndHour());
  }

  @Override
  protected int getNumWeeks() {
    return 1;
  }

  @Override
  protected int getDisplayMode() {
    return ICalendar.DISPLAY_MODE_WORKWEEK;
  }

  @Override
  protected String getRangeTitle(Calendar cal) {
    return getMonthLabel(cal.get(Calendar.MONTH)) + " " + cal.get(Calendar.YEAR) + " - " + ScoutTexts.get("Week") + " " + cal.get(Calendar.WEEK_OF_YEAR);
  }

  @Override
  protected boolean hasTimeLine() {
    return true;
  }

  @Override
  protected String getDayTitle(Calendar cal) {
    return "" + cal.get(Calendar.DAY_OF_MONTH);
  }

  @Override
  protected void truncateToRange(Calendar cal) {
    cal.add(Calendar.DAY_OF_MONTH, -getNumOfDaysInWeekBefore(cal));
  }

  @Override
  protected int getSmallNextField() {
    return Calendar.WEEK_OF_YEAR;
  }

  @Override
  protected int getBigNextField() {
    return Calendar.MONTH;
  }

  @Override
  protected double getGridTop() {
    return 64.508;
  }

  @Override
  protected void resizeDayBoxes(double height) {
    for (int i = 0; i < getNumWeekdays(); i++) {
      Element el = getSVGDocument().getElementById("b" + i + "0");
      el.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, "" + height);
    }
  }
}

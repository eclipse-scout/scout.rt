package org.eclipse.scout.rt.ui.svg.calendar.builder;

import java.util.Calendar;

import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.svg.calendar.comp.IComponentElementFactory;
import org.eclipse.scout.rt.ui.svg.calendar.comp.TimeLineTextComponentElementFactory;
import org.w3c.dom.Element;

public class CalendarDayBuilder extends AbstractCalendarDocumentBuilder {
  public CalendarDayBuilder() {
    super("resources/DayCalendar.svg");
  }

  @Override
  protected int getNumWeekdays() {
    return 1;
  }

  @Override
  protected IComponentElementFactory getComponentElementFactory() {
    return new TimeLineTextComponentElementFactory(getStartHour(), getEndHour());
  }

  @Override
  protected String getRangeTitle(Calendar cal) {
    return getWeekDayLabelLong(cal.get(Calendar.DAY_OF_WEEK)) + " " + cal.get(Calendar.DAY_OF_MONTH) + ". " +
        getMonthLabel(cal.get(Calendar.MONTH)) +
        " " + cal.get(Calendar.YEAR) + " - " + ScoutTexts.get("Week") + " " + cal.get(Calendar.WEEK_OF_YEAR);
  }

  @Override
  protected int getDisplayMode() {
    return ICalendar.DISPLAY_MODE_DAY;
  }

  @Override
  protected int getNumWeeks() {
    return 1;
  }

  @Override
  protected boolean hasTimeLine() {
    return true;
  }

  @Override
  protected String getDayTitle(Calendar cal) {
    return null;
  }

  @Override
  protected void truncateToRange(Calendar cal) {
  }

  @Override
  protected int getSmallNextField() {
    return Calendar.DAY_OF_MONTH;
  }

  @Override
  protected int getBigNextField() {
    return Calendar.WEEK_OF_YEAR;
  }

  @Override
  protected double getGridTop() {
    return 30.672;
  }

  @Override
  protected void resizeDayBoxes(double height) {
    Element el = getSVGDocument().getElementById("b00");
    el.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, "" + height);
  }
}

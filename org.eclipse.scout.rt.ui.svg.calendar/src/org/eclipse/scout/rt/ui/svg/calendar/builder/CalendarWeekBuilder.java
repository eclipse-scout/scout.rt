package org.eclipse.scout.rt.ui.svg.calendar.builder;

import java.util.Calendar;

import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.svg.calendar.comp.IComponentElementFactory;
import org.eclipse.scout.rt.ui.svg.calendar.comp.TimeLineComponentElementFactory;

public class CalendarWeekBuilder extends AbstractCalendarDocumentBuilder {
  public CalendarWeekBuilder() {
    super("resources/WeekCalendar.svg");
  }

  @Override
  protected int getNumWeekdays() {
    return NUM_DAYS_IN_WEEK;
  }

  @Override
  protected IComponentElementFactory getComponentElementFactory() {
    return new TimeLineComponentElementFactory();
  }

  @Override
  protected int getNumWeeks() {
    return 1;
  }

  @Override
  protected int getDisplayMode() {
    return ICalendar.DISPLAY_MODE_WEEK;
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
}

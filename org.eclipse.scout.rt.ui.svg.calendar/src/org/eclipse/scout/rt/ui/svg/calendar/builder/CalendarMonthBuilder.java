package org.eclipse.scout.rt.ui.svg.calendar.builder;

import java.util.Calendar;

import org.eclipse.scout.rt.ui.svg.calendar.comp.IComponentElementFactory;
import org.eclipse.scout.rt.ui.svg.calendar.comp.LinearFillComponentElementFactory;

public class CalendarMonthBuilder extends AbstractCalendarDocumentBuilder {

  public CalendarMonthBuilder() {
    super("resources/MonthCalendar.svg");
  }

  @Override
  protected int getNumWeekdays() {
    return NUM_DAYS_IN_WEEK;
  }

  @Override
  protected int getNumWeeks() {
    return 6;
  }

  @Override
  protected IComponentElementFactory getComponentElementFactory() {
    return new LinearFillComponentElementFactory();
  }

  @Override
  protected String getDayTitle(Calendar cal) {
    return "" + cal.get(Calendar.DAY_OF_MONTH);
  }

  @Override
  protected boolean hasTimeLine() {
    return false;
  }

  @Override
  protected void truncateToRange(Calendar cal) {
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.add(Calendar.DAY_OF_MONTH, -getNumOfDaysInWeekBefore(cal));
  }

  @Override
  protected int getSmallNextField() {
    return Calendar.MONTH;
  }

  @Override
  protected String getRangeTitle(Calendar cal) {
    return getMonthLabel(cal.get(Calendar.MONTH)) + " " + cal.get(Calendar.YEAR);
  }

  @Override
  protected int getBigNextField() {
    return Calendar.YEAR;
  }
}

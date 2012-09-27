package org.eclipse.scout.rt.ui.svg.calendar.builder;

import java.util.Calendar;

import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.svg.calendar.comp.IComponentElementFactory;
import org.eclipse.scout.rt.ui.svg.calendar.comp.TimeLineTextComponentElementFactory;

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
    return new TimeLineTextComponentElementFactory();
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
}

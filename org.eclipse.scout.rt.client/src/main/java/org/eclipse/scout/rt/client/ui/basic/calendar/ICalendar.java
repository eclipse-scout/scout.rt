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
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.root.ICalendarContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

public interface ICalendar extends IPropertyObserver, IContextMenuOwner {

  /**
   * type {@link Set<CalendarComponent>}
   */
  String PROP_COMPONENTS = "components";
  /**
   * type {@link CalendarComponent}
   */
  String PROP_SELECTED_COMPONENT = "selectedComponent";
  /**
   * type int (DISPLAY_MODE_DAY,DISPLAY_MODE_WEEK,DISPLAY_MODE_MONTH, DISPLAY_MODE_WORKWEEK)
   */
  String PROP_DISPLAY_MODE = "displayMode";
  /**
   * type boolean
   */
  String PROP_DISPLAY_CONDENSED = "displayCondensed";
  /**
   * type String
   */
  String PROP_TITLE = "title";
  /**
   * type {@link Date}[2]
   */
  String PROP_VIEW_RANGE = "viewRange";
  /**
   * type {@link Date}
   */
  String PROP_SELECTED_DATE = "selectedDate";
  /**
   * type {@link Boolean}
   */
  String PROP_LOAD_IN_PROGRESS = "loadInProgress";
  /**
   * type int
   */
  String PROP_START_HOUR = "startHour";
  /**
   * type int
   */
  String PROP_END_HOUR = "endHour";
  /**
   * type {@link Boolean}
   */
  String PROP_USE_OVERFLOW_CELLS = "useOverflowCells";
  /**
   * type {@link Boolean}
   */
  String PROP_SHOW_DISPLAY_MODE_SELECTION = "showDisplayModeSelection";

  /**
   * type {@link Boolean}
   */
  String PROP_MARK_NOON_HOUR = "markNoonHour";

  /**
   * type {@link Boolean}
   */
  String PROP_MARK_OUT_OF_MONTH_DAYS = "markOutOfMonthDays";

  /**
   * {@link Object}
   * <p>
   * Container of this calendar, {@link ICalendarField}
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   *
   * @since 3.8.1
   */
  String PROP_CONTAINER = "container";

  /**
   * @since 4.0.0 {@link IContextMenu}
   */
  String PROP_CONTEXT_MENU = "contextMenus";

  void initCalendar();

  void disposeCalendar();

  String getTitle();

  void setTitle(String s);

  int getDisplayMode();

  void setDisplayMode(int mode);

  boolean isDisplayCondensed();

  void setDisplayCondensed(boolean condensed);

  /**
   * @return a Date tupel [begin, end]
   */
  Range<Date> getViewRange();

  void setViewRange(Date viewDateStart, Date viewDateEnd);

  void setViewRange(Range<Date> dateRange);

  Date getSelectedDate();

  void setSelectedDate(Date d);

  CalendarComponent getSelectedComponent();

  void setSelectedComponent(CalendarComponent comp);

  /**
   * @return selected item if it is of the requested type
   */
  <T extends ICalendarItem> T getSelectedItem(Class<T> c);

  boolean isLoadInProgress();

  void setLoadInProgress(boolean b);

  /**
   * @return all calendar components sorted by {@link CalendarComponent#getFromDate()}
   */
  Set<? extends CalendarComponent> getComponents();

  DateTimeFormatFactory getDateTimeFormatFactory();

  /**
   * reload all calendar items
   */
  void reloadCalendarItems();

  /*
   * modification observer
   */
  void addCalendarListener(CalendarListener listener);

  void removeCalendarListener(CalendarListener listener);

  /**
   * when performing a batch mutation use this marker like
   *
   * <pre>
   * try{
   *   setCalendarChanging(true);
   *   ...modify data, etc.
   * }
   * finally{
   *   setCalendarChanging(false);
   * }
   * </pre>
   */
  void setCalendarChanging(boolean b);

  boolean isCalendarChanging();

  /**
   * Configures the starting hour of the calendar. Only visible when the calendar is in day, week or work-week mode.
   * Together with getEndHour and getOverflowCells is defines the timeline of a day.
   * <p>
   * Default {@code 6}.
   *
   * @see getEndHour
   * @see getUseOverflowCells
   */
  int getStartHour();

  void setStartHour(int hour);

  /**
   * The starting hour of the calendar. Only visible when the calendar is in day, week or work-week mode. Together with
   * getStartHour and getUseOverflowCells is defines the timeline of a day.
   * <p>
   * Default {@code 19}.
   *
   * @see getStartHour
   * @see getUseOverflowCells
   */
  int getEndHour();

  void setEndHour(int hour);

  /**
   * Defines the label of the first and last cell of the calendar. Only visible when the calendar is in day, week or
   * work-week mode. Together with getConfiguredStartHour and getConfiguredEndHour it defines the timeline of a day. If
   * true the timeline displays "sooner" and "later" instead of the first and last defined hour.
   * <p>
   * Appointments that are outside the defined hours of the calender are still shown in the first and last cell.
   * <p>
   * Default {@code true}.
   *
   * @see getEndHour
   * @see getStartHour
   */
  boolean getUseOverflowCells();

  void setUseOverflowCells(boolean useOverflowCells);

  /**
   * Specifies whether the display mode options (day, week, workweek or month) at the bottom of the calendar are visible
   * or not. If hidden, set the mode in code with setDisplayMode(ICalendar.DISPLAY_MODE_WEEK);
   * <p>
   * Default {@code true}.
   */
  boolean getShowDisplayModeSelection();

  void setShowDisplayModeSelection(boolean showDisplayModeSelection);

  /**
   * Defines whether or not the noon hour is painted with a darker color. The actually implementation is UI rendering
   * specific.
   */
  boolean getMarkNoonHour();

  void setMarkNoonHour(boolean markNoonHour);

  /**
   * Defines whether or not days that are outside the current month are painted with a darker background color.
   */
  boolean getMarkOutOfMonthDays();

  void setMarkOutOfMonthDays(boolean markOutOfMonthDays);

  /*
   * UI interface
   */

  /**
   * {@link Object}
   * <p>
   * Container of this calendar, {@link ICalendarField}
   * <p>
   * {@link IListBox} https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   *
   * @since 3.8.1
   */
  Object getContainer();

  /*
   * UI interface
   */
  ICalendarUIFacade getUIFacade();

  /**
   * @return An unmodifiable list of all {@link ICalendarItemProvider}s defined for this calendar.
   */
  List<ICalendarItemProvider> getCalendarItemProviders();

  @Override
  ICalendarContextMenu getContextMenu();
}

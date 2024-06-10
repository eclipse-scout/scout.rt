/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.root.ICalendarContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarDescriptor;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

public interface ICalendar extends IWidget, IContextMenuOwner {

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
   * type {@link List<ICalendarDescriptor>}
   */
  String PROP_CALENDAR_DESCRIPTORS = "calendarDescriptors";
  /**
   * type {@link ICalendarDescriptor}
   */
  String PROP_SELECTED_CALENDAR_DESCRIPTOR = "selectedCalendarDescriptor";
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
   * type {@link Date}[2]
   */
  String PROP_SELECTED_RANGE = "selectedRange";
  /**
   * type {@link Boolean}
   */
  String PROP_LOAD_IN_PROGRESS = "loadInProgress";
  /**
   * type int
   */
  String PROP_START_HOUR = "startHour";
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
  String PROP_RANGE_SELECTION_ALLOWED = "rangeSelectionAllowed";

  /**
   * @since 4.0.0 {@link IContextMenu}
   */
  String PROP_CONTEXT_MENU = "contextMenus";

  String PROP_MENU_INJECTION_TARGET = "menuInjectionTarget";

  String PROP_SHOW_CALENDAR_SIDEBAR = "showCalendarSidebar";

  String PROP_SHOW_CALENDARS_PANEL = "showCalendarsPanel";

  String PROP_SHOW_LIST_PANEL = "showYearPanel";

  String getTitle();

  void setTitle(String s);

  int getDisplayMode();

  void setDisplayMode(int mode);

  boolean isDisplayCondensed();

  void setDisplayCondensed(boolean condensed);

  List<ICalendarDescriptor> getCalendarDescriptors();

  void setCalendarDescriptors(List<ICalendarDescriptor> calendars);

  ICalendarDescriptor getSelectedCalendarDescriptor();

  void setCalendarVisibility(String calendarId, boolean visible);

  /**
   * @return a Date tupel [begin, end]
   */
  Range<Date> getViewRange();

  void setViewRange(Date viewDateStart, Date viewDateEnd);

  void setViewRange(Range<Date> dateRange);

  Date getSelectedDate();

  void setSelectedDate(Date d);

  /**
   * @return a Date tupel [begin, end]
   */
  Range<Date> getSelectedRange();

  void setSelectedRange(Date selectedRangeStart, Date selectedRangeEnd);

  void setSelectedRange(Range<Date> dateRange);

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

  void reloadCalendarItems(String calendarId);

  /**
   * Model Observer
   */
  IFastListenerList<CalendarListener> calendarListeners();

  default void addCalendarListener(CalendarListener listener) {
    calendarListeners().add(listener);
  }

  default void removeCalendarListener(CalendarListener listener) {
    calendarListeners().remove(listener);
  }

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
   * Together with getOverflowCells is defines the timeline of a day.
   * <p>
   * Default {@code 6}.
   *
   * @see #getUseOverflowCells
   */
  int getStartHour();

  void setStartHour(int hour);

  /**
   * Defines the label of the first and last cell of the calendar. Only visible when the calendar is in day, week or
   * work-week mode. Together with getConfiguredStartHour and getConfiguredEndHour it defines the timeline of a day. If
   * true the timeline displays "sooner" and "later" instead of the first and last defined hour.
   * <p>
   * Appointments that are outside the defined hours of the calender are still shown in the first and last cell.
   * <p>
   * Default {@code true}.
   *
   * @see #getStartHour
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
   * Specifies whether a range in the calendar can be selected.
   * <p>
   * Default {@code false}.
   */
  boolean getRangeSelectionAllowed();

  void setRangeSelectionAllowed(boolean rangeSelectionAllowed);

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

  IGroupBox getMenuInjectionTarget();

  /**
   * Specifies a target {@link IGroupBox} where the calendar will inject all menus contained in
   * {@link #getContextMenu()}. The menus will be injected within the ui layer and will be removed as soon as the
   * injection target changes. As soon as the menus within the contextmenu change, the injected menus will be removed
   * and the new menu structure will be injected.
   */
  void setMenuInjectionTarget(IGroupBox target);

  boolean getShowCalendarSidebar();

  void setShowCalendarSidebar(boolean showYearPanel);

  boolean getShowCalendarsPanel();

  void setShowCalendarsPanel(boolean showCalendarsPanel);

  boolean getShowListPanel();

  void setShowListPanel(boolean showListPanel);
}

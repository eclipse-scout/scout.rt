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

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

public interface ICalendar extends IPropertyObserver {

  // never change final constants (properties files might have references)
  int DISPLAY_MODE_DAY = 1;
  int DISPLAY_MODE_WEEK = 2;
  int DISPLAY_MODE_MONTH = 3;
  int DISPLAY_MODE_WORKWEEK = 4;

  /**
   * type {@link CalendarComponent}[]
   */
  String PROP_COMPONENTS = "components";
  /**
   * type {@link CalendarComponent}
   */
  String PROP_SELECTED_COMPONENT = "selectedComponent";
  /**
   * type int (DISPLAY_MODE_DAY,DISPLAY_MODE_WEEK,DISPLAY_MODE_MONTH,
   * DISPLAY_MODE_WORKWEEK)
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
   * {@link Object}
   * <p>
   * Container of this calendar, {@link ICalendarField}
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   * 
   * @since 3.8.1
   */
  String PROP_CONTAINER = "container";

  void initCalendar() throws ProcessingException;

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
  Date[] getViewRange();

  void setViewRange(Date viewDateStart, Date viewDateEnd);

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
  CalendarComponent[] getComponents();

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
}

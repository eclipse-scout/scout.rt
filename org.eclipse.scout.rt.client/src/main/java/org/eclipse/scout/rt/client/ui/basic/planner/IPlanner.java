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
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.Date;
import java.util.List;

import org.eclipse.scout.commons.Range;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.IPlannerContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.Resource;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfieldold.IPlannerFieldOld;

/**
 * The activity map is a specialized model which contains a set of {@link Activity}s that are grouped by resource.
 *
 * @since 5.1
 */
public interface IPlanner<RI, AI> extends IPropertyObserver, IContextMenuOwner {

  /**
   * type {@link Date}[2]
   */
  String PROP_VIEW_RANGE = "viewRange";

  /**
   * {@link Integer}
   */
  String PROP_WORK_DAY_COUNT = "workDayCount";
  /**
   * {@link Boolean}
   */
  String PROP_WORK_DAYS_ONLY = "workDaysOnly";
  /**
   * {@link Date}
   */
  String PROP_FIRST_HOUR_OF_DAY = "firstHourOfDay";
  /**
   * {@link Date}
   */
  String PROP_LAST_HOUR_OF_DAY = "lastHourOfDay";
  /**
   * {@link Long}
   */
  String PROP_INTRADAY_INTERVAL = "intradayInterval";
  /**
   * {@link #DISPLAY_MODE_INTRADAY},{@link #DISPLAY_MODE_DAY}, {@link #DISPLAY_MODE_WEEK}, {@link #DISPLAY_MODE_MONTH},
   * {@link #DISPLAY_MODE_WORKWEEK}
   */
  String PROP_DISPLAY_MODE = "displayMode";
  /**
   * {@link Date}
   */
  String PROP_SELECTED_BEGIN_TIME = "selectedBeginTime";
  /**
   * {@link Date}
   */
  String PROP_SELECTED_END_TIME = "selectedEndTime";
  /**
   * {@link Long}[]
   */
  String PROP_SELECTED_RESOURCES = "selectedResources";
  /**
   * {@link Activity}
   */
  String PROP_SELECTED_ACTIVITY_CELL = "selectedActivityCell";
  /**
   * {@link Boolean}
   */
  String PROP_DRAW_SECTIONS = "drawSections";
  /**
   * {@link Object} Container of this map, {@link IPlannerFieldOld} https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   */
  String PROP_CONTAINER = "container";
  String PROP_CONTEXT_MENU = "contextMenus";

  //FIXME CGU same as for calendar, merge?
  int DISPLAY_MODE_INTRADAY = 0;
  int DISPLAY_MODE_DAY = 1;
  int DISPLAY_MODE_WEEK = 2;
  int DISPLAY_MODE_MONTH = 3;
  int DISPLAY_MODE_WORKWEEK = 4;
  int DISPLAY_MODE_CALENDAR_WEEK = 5;
  int DISPLAY_MODE_YEAR = 6;

  void initPlanner() throws ProcessingException;

  void disposePlanner();

  void addPlannerListener(PlannerListener listener);

  void removePlannerListener(PlannerListener listener);

  boolean isPlannerChanging();

  /**
   * when performing a batch mutation use this marker like
   *
   * <pre>
   * try{
   *   setPlannerChanging(true);
   *   ...modify data...
   * }
   * finally{
   *   setPlannerChanging(false);
   * }
   * </pre>
   */
  void setPlannerChanging(boolean b);

  /**
   * @return a Date tupel [begin, end]
   */
  Range<Date> getViewRange();

  void setViewRange(Date viewDateStart, Date viewDateEnd);

  void setViewRange(Range<Date> dateRange);

  int getWorkDayCount();

  void setWorkDayCount(int n);

  boolean isWorkDaysOnly();

  void setWorkDaysOnly(boolean b);

  /**
   * @return the first hour of a day<br>
   *         When a working day starts at 08:00 and ends at 17:00, this value is
   *         8.
   */
  int getFirstHourOfDay();

  /**
   * see {@link #getFirstHourOfDay()}
   */
  void setFirstHourOfDay(int i);

  /**
   * @return the last hour of a day<br>
   *         When a working day starts at 08:00 and ends at 17:00, this value is
   *         16 since the last hour starts at 16:00 and ends at 16:59.
   */
  int getLastHourOfDay();

  /**
   * see {@link #getLastHourOfDay()}
   */
  void setLastHourOfDay(int i);

  /**
   * {@link #DISPLAY_MODE_INTRADAY},{@link #DISPLAY_MODE_DAY}, {@link #DISPLAY_MODE_WEEK}, {@link #DISPLAY_MODE_MONTH},
   * {@link #DISPLAY_MODE_WORKWEEK}
   */
  int getDisplayMode();

  /**
   * {@link #DISPLAY_MODE_INTRADAY},{@link #DISPLAY_MODE_DAY}, {@link #DISPLAY_MODE_WEEK}, {@link #DISPLAY_MODE_MONTH},
   * {@link #DISPLAY_MODE_WORKWEEK}
   */
  void setDisplayMode(int mode);

  /**
   * milliseconds
   */
  long getIntradayInterval();

  void setIntradayInterval(long millis);

  void setIntradayIntervalInMinutes(long minutes);

  long getMinimumActivityDuration();

  void setMinimumActivityDuration(long minDuration);

  void setMinimumActivityDurationInMinutes(long min);

  Date getSelectedBeginTime();

  Date getSelectedEndTime();

  void setSelectedTime(Date beginTime, Date endTime);

  void decorateActivityCell(Activity<RI, AI> p);

  void replaceResources(List<Resource<RI>> resources);

  void deleteResources(List<Resource<RI>> resources);

  void deleteAllResources();

  void addResources(List<Resource<RI>> resources);

  List<Resource<RI>> getResources();

//  ActivityCell<RI, AI> resolveActivityCell(ActivityCell<RI, AI> cell);
//
//  List<ActivityCell<RI, AI>> resolveActivityCells(List<? extends ActivityCell<RI, AI>> cells);
//
//  List<ActivityCell<RI, AI>> getActivityCells(RI resource);
//
//  List<ActivityCell<RI, AI>> getActivityCells(List<Resource<RI>> resources);
//
//  List<ActivityCell<RI, AI>> getAllActivityCells();

//  void addActivityCells(List<? extends ActivityCell<RI, AI>> cells);
//
//  void updateActivityCells(List<? extends ActivityCell<RI, AI>> cells);
//
//  void updateActivityCellsById(List<Resource<RI>> resources);
//
//  void removeActivityCells(List<? extends ActivityCell<RI, AI>> cells);
//
//  void removeActivityCellsById(List<Resource<RI>> resources);
//
//  void removeAllActivityCells();

  Activity<RI, AI> getSelectedActivityCell();

  void setSelectedActivityCell(Activity<RI, AI> cell);

  boolean isSelectedActivityCell(Activity<RI, AI> cell);

//  void setResources(List<Resource<RI>> resources);

  /**
   * selected resources in arbitrary order
   */
  List<? extends Resource<RI>> getSelectedResources();

  List<RI> getSelectedResourceIds();

  void setSelectedResources(List<? extends Resource<RI>> resources);

  void isSelectedResource(Resource<RI> resource);

  void deselectResources(List<? extends Resource> resources);

  void deselectAllResources();

  /**
   * Indicates whether the selected sections in the activity
   * map should be visualized (by a rectangle with red and
   * green borders).
   *
   * @return true if the activity map draws these sections,
   *         false if not.
   */
  boolean isDrawSections();

  /**
   * Sets whether the selected sections in the activity
   * map should be visualized (by a rectangle with red and
   * green borders).
   *
   * @param drawSections
   *          true if the activity map should draw these sections,
   *          false if not.
   */
  void setDrawSections(boolean drawSections);

  /**
   * {@link Object}
   * <p>
   * Container of this map, {@link IPlannerFieldOld}
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   *
   * @since 3.8.1
   */
  Object getContainer();

  /**
   * @param menus
   */
  void setMenus(List<? extends IMenu> menus);

  /**
   * @param menu
   */
  void addMenu(IMenu menu);

  @Override
  IPlannerContextMenu getContextMenu();

  AbstractEventBuffer<PlannerEvent> createEventBuffer();

  IPlannerUIFacade getUIFacade();

}

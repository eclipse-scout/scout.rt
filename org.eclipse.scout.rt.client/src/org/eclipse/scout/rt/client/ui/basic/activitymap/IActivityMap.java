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
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import java.util.Date;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;

/**
 * The activity map is a specialized model which contains a set of {@link ActivityCell}s that are grouped by resourceId.
 */
public interface IActivityMap<RI, AI> extends IPropertyObserver {

  /**
   * {@link java.util.Date}[] truncated to day using {@link com.bsiag.DateUtility#truncDate(Date)}
   */
  String PROP_DAYS = "days";
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
   * {@link #PLANNING_MODE_TIME},{@link #PLANNING_MODE_DAY}, {@link #PLANNING_MODE_WEEK}
   */
  String PROP_PLANNING_MODE = "planningMode";
  /**
   * {@link Long}[]
   */
  String PROP_RESOURCE_IDS = "resourceIds";
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
  String PROP_SELECTED_RESOURCE_IDS = "selectedResourceIds";
  /**
   * {@link ActivityCell}
   */
  String PROP_SELECTED_ACTIVITY_CELL = "selectedActivityCell";
  /**
   * {@link TimeScale}
   */
  String PROP_TIME_SCALE = "timeScale";
  /**
   * {@link Boolean}
   */
  String PROP_DRAW_SECTIONS = "drawSections";
  /**
   * {@link Object} Container of this map, {@link IPlannerField} https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   * 
   * @since 3.8.1
   */
  String PROP_CONTAINER = "container";

  int PLANNING_MODE_INTRADAY = 0;
  int PLANNING_MODE_DAY = 1;
  int PLANNING_MODE_WEEK = 2;

  void initActivityMap() throws ProcessingException;

  void disposeActivityMap();

  void addActivityMapListener(ActivityMapListener listener);

  void removeActivityMapListener(ActivityMapListener listener);

  boolean isActivityMapChanging();

  /**
   * when performing a batch mutation use this marker like
   * 
   * <pre>
   * try{
   *   setActivityMapChanging(true);
   *   ...modify data...
   * }
   * finally{
   *   setActivityMapChanging(false);
   * }
   * </pre>
   */
  void setActivityMapChanging(boolean b);

  /**
   * Create a time scalewith {@link MajorTimeColumn}s and {@link MinorTimeColumn}s
   * 
   * @see {@link #getPlanningMode()} and {@link #getDays()}
   */
  void createTimeScale();

  /**
   * ui model can use this objects to build the columns of the map
   */
  TimeScale getTimeScale();

  void setTimeScale(TimeScale scale);

  /**
   * All the days which are display in the activity map.<br>
   * Ordered ascending
   */
  Date[] getDays();

  void setDays(Date[] days);

  void addDay(Date day);

  void removeDay(Date day);

  void setDay(Date day);

  /**
   * @return begin time of activity map
   *         <p>
   *         Intraday: This is the first hour of the first day<br>
   *         Day: This is the first day at 00:00<br>
   *         Week: This is the first day at 00:00 of the first week
   */
  Date getBeginTime();

  /**
   * @return end time of activity map
   *         <p>
   *         Intraday: This is the last hour +1 of the first day<br>
   *         Day: This is the day after the last day at 00:00<br>
   *         Week: This is the monday of the week after the last week at 00:00
   */
  Date getEndTime();

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
   * {@link #PLANNING_MODE_INTRADAY},{@link #PLANNING_MODE_DAY}, {@link #PLANNING_MODE_WEEK}
   */
  int getPlanningMode();

  /**
   * {@link #PLANNING_MODE_INTRADAY},{@link #PLANNING_MODE_DAY}, {@link #PLANNING_MODE_WEEK}
   */
  void setPlanningMode(int mode);

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

  void decorateActivityCell(ActivityCell<RI, AI> p);

  /**
   * collect all selected days from starthour to endhour and intersect with
   * earliest and latest time range
   */
  MultiTimeRange calculateSelectedTimeRanges(Date earliestBeginTime, Date latestEndTime);

  /**
   * Create a planned activity that includes one of the selected persons.
   * 
   * @param singleMatch
   *          true=plan an activity for only one of the selected resource.
   *          false=plan an activity for all of the selected resources
   * @param chooseRandom
   *          only used in combination with singleMatch=true. true=a random
   *          person is chosen, false=the first matching resource is chosen
   * @param earliestBeginTime
   *          consider only matches that start after this time; {@link System#currentTimeMillis()} is used when null is
   *          passed
   * @param latestEndTime
   *          consider only matches that end before this time; {@link System#currentTimeMillis()}+10 years is used when
   *          null is
   *          passed
   * @param preferredDuration
   *          (in milliseconds) the preferred duration of the planned activity;
   *          30 minutes is used when 0 is passed
   */
  void planActivityForSelectedResources(boolean singleMatch, boolean chooseRandom, Date earliestBeginTime, Date latestEndTime, long preferredDuration);

  ActivityCell<RI, AI> resolveActivityCell(ActivityCell<RI, AI> cell);

  ActivityCell<RI, AI>[] resolveActivityCells(ActivityCell<RI, AI>[] cells);

  ActivityCell<RI, AI>[] getActivityCells(RI resourceId);

  ActivityCell<RI, AI>[] getActivityCells(RI[] resourceIds);

  ActivityCell<RI, AI>[] getAllActivityCells();

  void addActivityCells(ActivityCell<RI, AI>[] cells);

  void updateActivityCells(ActivityCell<RI, AI>[] cells);

  void updateActivityCells(RI[] resourceIds);

  void removeActivityCells(ActivityCell<RI, AI>[] cells);

  void removeActivityCells(RI[] resourceIds);

  void removeAllActivityCells();

  ActivityCell<RI, AI> getSelectedActivityCell();

  void setSelectedActivityCell(ActivityCell<RI, AI> cell);

  boolean isSelectedActivityCell(ActivityCell<RI, AI> cell);

  /**
   * available resource ids in the same order as the resource table
   */
  RI[] getResourceIds();

  void setResourceIds(RI[] resourceIds);

  /**
   * selected resource ids in arbitrary order
   */
  RI[] getSelectedResourceIds();

  void setSelectedResourceIds(RI[] resourceIds);

  void isSelectedResourceId(RI resourceId);

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
   * Container of this map, {@link IPlannerField}
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   * 
   * @since 3.8.1
   */
  Object getContainer();

  IMenu[] getMenus();

  IActivityMapUIFacade getUIFacade();

}

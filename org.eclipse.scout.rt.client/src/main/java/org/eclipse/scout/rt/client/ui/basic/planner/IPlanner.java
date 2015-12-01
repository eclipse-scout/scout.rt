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
import java.util.Set;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.IPlannerContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.util.Range;

/**
 * The activity map is a specialized model which contains a set of {@link Activity}s that are grouped by resource.
 *
 * @since 5.1
 */
public interface IPlanner<RESOURCE_ID, ACTIVITY_ID> extends IPropertyObserver, IContextMenuOwner {

  /**
   * type {@link Date}[2]
   */
  String PROP_VIEW_RANGE = "viewRange";

  String PROP_SELECTION_RANGE = "selectionRange";

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
   * {@link String}
   */
  String PROP_LABEL = "label";

  /**
   * {@link Boolean}
   */
  String PROP_HEADER_VISIBLE = "headerVisible";

  /**
   * {@link #INTRADAY},{@link #DAY}, {@link #WEEK}, {@link #MONTH},
   * {@link #WORK_WEEK}
   */
  String PROP_DISPLAY_MODE = "displayMode";

  /**
   * {@link Set}
   */
  String PROP_AVAILABLE_DISPLAY_MODES = "availableDisplayModes";

  /**
   * {@link #SELECTION_MODE_NONE}, {@link #SELECTION_MODE_ACTIVITY}, {@link #SELECTION_MODE_SINGLE_RANGE},
   * {@link #SELECTION_MODE_MULTI_RANGE}
   */
  String PROP_SELECTION_MODE = "selectionMode";

  /**
   * {@link Activity}
   */
  String PROP_SELECTED_ACTIVITY = "selectedActivity";
  /**
   * {@link Object} Container of this planner, {@link IPlannerField}
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388227
   */
  String PROP_CONTAINER = "container";
  String PROP_CONTEXT_MENU = "contextMenus";

  int SELECTION_MODE_NONE = 0;
  int SELECTION_MODE_ACTIVITY = 1;
  int SELECTION_MODE_SINGLE_RANGE = 2;
  int SELECTION_MODE_MULTI_RANGE = 3;

  void initPlanner();

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

  String getLabel();

  void setLabel(String label);

  Range<Date> getViewRange();

  void setViewRange(Date viewDateStart, Date viewDateEnd);

  void setViewRange(Range<Date> dateRange);

  /**
   * @return the first hour of a day<br>
   *         When a working day starts at 08:00 and ends at 17:00, this value is 8.
   */
  int getFirstHourOfDay();

  /**
   * see {@link #getFirstHourOfDay()}
   */
  void setFirstHourOfDay(int i);

  /**
   * @return the last hour of a day<br>
   *         When a working day starts at 08:00 and ends at 17:00, this value is 16 since the last hour starts at 16:00
   *         and ends at 16:59.
   */
  int getLastHourOfDay();

  /**
   * see {@link #getLastHourOfDay()}
   */
  void setLastHourOfDay(int i);

  /**
   * {@link #INTRADAY},{@link #DAY}, {@link #WEEK}, {@link #MONTH},
   * {@link #WORK_WEEK}
   */
  int getDisplayMode();

  /**
   * {@link #INTRADAY},{@link #DAY}, {@link #WEEK}, {@link #MONTH},
   * {@link #WORK_WEEK}
   */
  void setDisplayMode(int mode);

  Set<Integer> getAvailableDisplayModes();

  void setAvailableDisplayModes(Set<Integer> displayModes);

  boolean isHeaderVisible();

  void setHeaderVisible(boolean visible);

  int getSelectionMode();

  void setSelectionMode(int mode);

  /**
   * milliseconds
   */
  long getIntradayInterval();

  void setIntradayInterval(long millis);

  void setIntradayIntervalInMinutes(long minutes);

  long getMinimumActivityDuration();

  void setMinimumActivityDuration(long minDuration);

  void setMinimumActivityDurationInMinutes(long min);

  Range<Date> getSelectionRange();

  void setSelectionRange(Date beginDate, Date endDate);

  void setSelectionRange(Range<Date> selectionRange);

  Date getSelectedBeginTime();

  Date getSelectedEndTime();

  void decorateResource(Resource<RESOURCE_ID> resource);

  void replaceResources(List<Resource<RESOURCE_ID>> resources);

  void deleteResource(Resource<RESOURCE_ID> resource);

  void deleteResources(List<Resource<RESOURCE_ID>> resources);

  void deleteAllResources();

  void addResources(List<Resource<RESOURCE_ID>> resources);

  void addResource(Resource<RESOURCE_ID> resource);

  List<Resource<RESOURCE_ID>> getResources();

  void decorateActivity(Activity<RESOURCE_ID, ACTIVITY_ID> p);

  Activity<RESOURCE_ID, ACTIVITY_ID> getSelectedActivity();

  void setSelectedActivityCell(Activity<RESOURCE_ID, ACTIVITY_ID> cell);

  boolean isSelectedActivityCell(Activity<RESOURCE_ID, ACTIVITY_ID> cell);

  /**
   * First selected resource
   */
  Resource<RESOURCE_ID> getSelectedResource();

  /**
   * selected resources in arbitrary order
   */
  List<? extends Resource<RESOURCE_ID>> getSelectedResources();

  List<RESOURCE_ID> getSelectedResourceIds();

  void setSelectedResources(List<? extends Resource<RESOURCE_ID>> resources);

  void isSelectedResource(Resource<RESOURCE_ID> resource);

  void selectResource(Resource<RESOURCE_ID> resource);

  void selectResources(List<? extends Resource<RESOURCE_ID>> resources);

  boolean deselectResource(Resource<RESOURCE_ID> resources);

  boolean deselectResources(List<? extends Resource<RESOURCE_ID>> resources);

  void deselectAllResources();

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

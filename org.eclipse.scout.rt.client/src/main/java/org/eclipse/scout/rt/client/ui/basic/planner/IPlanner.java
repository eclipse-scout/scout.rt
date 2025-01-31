/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.IPlannerContextMenu;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

/**
 * The planner contains a list of {@link Resource}s that are associated with 0..n {@link Activity}s.
 *
 * @since 5.1
 */
public interface IPlanner<RESOURCE_ID, ACTIVITY_ID> extends IWidget, IContextMenuOwner {

  /**
   * type {@link Date}[2]
   */
  String PROP_VIEW_RANGE = "viewRange";

  String PROP_SELECTION_RANGE = "selectionRange";

  /**
   * {@link String}
   */
  String PROP_LABEL = "label";

  /**
   * {@link Boolean}
   */
  String PROP_HEADER_VISIBLE = "headerVisible";

  /**
   * {@link Boolean}
   */
  String PROP_RANGE_SELECTABLE = "rangeSelectable";

  /**
   * {@link Boolean}
   */
  String PROP_MULTI_SELECT = "multiSelect";

  /**
   * Values from {@link IPlannerDisplayMode}.
   */
  String PROP_DISPLAY_MODE = "displayMode";

  /**
   * {@link DisplayModeOptions}
   */
  String PROP_DISPLAY_MODE_OPTIONS = "displayModeOptions";

  /**
   * {@link Set}
   */
  String PROP_AVAILABLE_DISPLAY_MODES = "availableDisplayModes";

  /**
   * {@link Boolean}
   */
  String PROP_ACTIVITY_SELECTABLE = "activitySelectable";

  /**
   * {@link Activity}
   */
  String PROP_SELECTED_ACTIVITY = "selectedActivity";
  String PROP_CONTEXT_MENU = "contextMenus";

  IFastListenerList<PlannerListener> plannerListeners();

  default void addPlannerListener(PlannerListener listener) {
    plannerListeners().add(listener);
  }

  default void removePlannerListener(PlannerListener listener) {
    plannerListeners().remove(listener);
  }

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
   * @return value from {@link IPlannerDisplayMode}
   */
  int getDisplayMode();

  /**
   * @param mode
   *     value from {@link IPlannerDisplayMode}
   */
  void setDisplayMode(int mode);

  Set<Integer> getAvailableDisplayModes();

  void setAvailableDisplayModes(Set<Integer> displayModes);

  Map<Integer, DisplayModeOptions> getDisplayModeOptions();

  void setDisplayModeOptions(Map<Integer, DisplayModeOptions> displayModeOptions);

  void setDisplayModeOption(int displayMode, DisplayModeOptions displayModeOption);

  boolean isHeaderVisible();

  void setHeaderVisible(boolean visible);

  boolean isMultiSelect();

  void setMultiSelect(boolean multiSelect);

  boolean isRangeSelectable();

  void setRangeSelectable(boolean rangeSelectable);

  boolean isActivitySelectable();

  void setActivitySelectable(boolean selectable);

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

  void setSelectedActivity(Activity<RESOURCE_ID, ACTIVITY_ID> activity);

  boolean isSelectedActivity(Activity<RESOURCE_ID, ACTIVITY_ID> activity);

  /**
   * First selected resource
   */
  Resource<RESOURCE_ID> getSelectedResource();

  /**
   * selected resources in arbitrary order
   */
  List<? extends Resource<RESOURCE_ID>> getSelectedResources();

  List<RESOURCE_ID> getSelectedResourceIds();

  boolean isSelectedResource(Resource<RESOURCE_ID> resource);

  void selectResource(Resource<RESOURCE_ID> resource);

  void selectResources(List<? extends Resource<RESOURCE_ID>> resources);

  boolean deselectResource(Resource<RESOURCE_ID> resources);

  boolean deselectResources(List<? extends Resource<RESOURCE_ID>> resources);

  void deselectAllResources();

  void setMenus(List<? extends IMenu> menus);

  void addMenu(IMenu menu);

  @Override
  IPlannerContextMenu getContextMenu();

  AbstractEventBuffer<PlannerEvent> createEventBuffer();

  IPlannerUIFacade getUIFacade();
}

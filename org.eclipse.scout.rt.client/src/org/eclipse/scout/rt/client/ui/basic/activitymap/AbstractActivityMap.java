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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractActivityMap<RI, AI> extends AbstractPropertyObserver implements IActivityMap<RI, AI> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractActivityMap.class);

  private boolean m_initialized;
  private EventListenerList m_listenerList;
  private IActivityMapUIFacade m_activityMapUIFacade;
  private long m_minimumActivityDuration;// millis
  private HashMap<RI/* resourceId */, List<ActivityCell<RI, AI>>> m_resourceIdToActivities;
  private HashMap<CompositeObject/* resourceId,activityId */, ActivityCell<RI, AI>> m_activities;
  private HashSet<RI/* resourceId */> m_selectedResourceIds;
  private int m_tableChanging;
  private ArrayList<ActivityMapEvent> m_eventBuffer = new ArrayList<ActivityMapEvent>();
  private IMenu[] m_menus;
  private IActivityCellObserver<RI, AI> m_cellObserver;
  private boolean m_timeScaleValid;

  private Class<RI> m_resourceIdClass;
  private Class<AI> m_activityIdClass;

  public AbstractActivityMap() {
    this(true);
  }

  public AbstractActivityMap(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      initConfig();
      m_initialized = true;
    }
  }

  @SuppressWarnings("unchecked")
  private Class<RI> getResourceIdClass() {
    if (m_resourceIdClass == null) {
      m_resourceIdClass = TypeCastUtility.getGenericsParameterClass(getClass(), IActivityMap.class, 0);
    }
    return m_resourceIdClass;
  }

  @SuppressWarnings("unchecked")
  private Class<AI> getActivityIdClass() {
    if (m_activityIdClass == null) {
      m_activityIdClass = TypeCastUtility.getGenericsParameterClass(getClass(), IActivityMap.class, 1);
    }
    return m_activityIdClass;
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  @ConfigPropertyValue("5")
  protected int getConfiguredWorkDayCount() {
    return 5;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredWorkDaysOnly() {
    return false;
  }

  @ConfigProperty(ConfigProperty.HOUR_OF_DAY)
  @Order(110)
  @ConfigPropertyValue("8")
  protected int getConfiguredFirstHourOfDay() {
    return 8;
  }

  @ConfigProperty(ConfigProperty.HOUR_OF_DAY)
  @Order(130)
  @ConfigPropertyValue("16")
  protected int getConfiguredLastHourOfDay() {
    return 16;
  }

  @ConfigProperty(ConfigProperty.DURATION_MINUTES)
  @Order(120)
  @ConfigPropertyValue("1800000L")
  protected long getConfiguredIntradayInterval() {
    return 1800000L;
  }

  @ConfigProperty(ConfigProperty.DURATION_MINUTES)
  @Order(125)
  @ConfigPropertyValue("1800000L")
  protected long getConfiguredMinimumActivityDuration() {
    return 1800000L;
  }

  /**
   * Indicates whether this ActivityMap should draw the
   * red and green bordered rectangle sections
   * around the area selected by the mouse.
   * 
   * @return true if the colored sections should be displayed,
   *         false if not. Default is true.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredDrawSections() {
    return true;
  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<IMenu>[] foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  /**
   * @param activityCell
   *          may be null
   */
  @ConfigOperation
  @Order(60)
  protected void execDecorateActivityCell(ActivityCell<RI, AI> cell) throws ProcessingException {
  }

  /**
   * @param activityCell
   *          may be null
   */
  @ConfigOperation
  @Order(70)
  protected void execActivityCellSelected(ActivityCell<RI, AI> cell) throws ProcessingException {
  }

  /**
   * @param activityCell
   *          may be null
   */
  @ConfigOperation
  @Order(75)
  protected void execCellAction(RI resourceId, MinorTimeColumn column, ActivityCell<RI, AI> activityCell) throws ProcessingException {
  }

  @ConfigOperation
  @Order(80)
  protected void execInitActivityMap() throws ProcessingException {
  }

  @ConfigOperation
  @Order(90)
  protected void execDisposeActivityMap() throws ProcessingException {
  }

  /**
   * Create a time scale containing {@link MajorTimeColumn}s and {@link MinorTimeColumn}s
   * <p>
   * By default this method switches on {@link #getPlanningMode()} and uses {@link #getDays()} and
   * {@link #execDecorateMajorTimeColumn(MajorTimeColumn)} and
   * {@link #execDecorateMinorTimeColumn(MajorTimeColumn, MinorTimeColumn)} to create these columns
   */
  @ConfigOperation
  @Order(100)
  protected TimeScale execCreateTimeScale() throws ProcessingException {
    return new TimeScaleBuilder(this).build();
  }

  /**
   * set the label of a {@link MajorTimeColumn}
   */
  @ConfigOperation
  @Order(110)
  protected void execDecorateMajorTimeColumn(TimeScale scale, MajorTimeColumn columns) throws ProcessingException {
  }

  /**
   * set the label of a {@link MinorTimeColumn}
   */
  @ConfigOperation
  @Order(120)
  protected void execDecorateMinorTimeColumn(TimeScale scale, MajorTimeColumn majorColumn, MinorTimeColumn minorColumn) throws ProcessingException {
  }

  protected void initConfig() {
    m_listenerList = new EventListenerList();
    m_activityMapUIFacade = createUIFacade();
    m_resourceIdToActivities = new HashMap<RI, List<ActivityCell<RI, AI>>>();
    m_activities = new HashMap<CompositeObject, ActivityCell<RI, AI>>();
    m_cellObserver = new P_ActivityCellObserver();
    m_selectedResourceIds = new HashSet<RI>();
    //
    setWorkDayCount(getConfiguredWorkDayCount());
    setWorkDaysOnly(getConfiguredWorkDaysOnly());
    setFirstHourOfDay(getConfiguredFirstHourOfDay());
    setIntradayInterval(getConfiguredIntradayInterval());
    setMinimumActivityDuration(getConfiguredMinimumActivityDuration());
    setLastHourOfDay(getConfiguredLastHourOfDay());
    setDrawSections(getConfiguredDrawSections());
    // menus
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    Class<? extends IMenu>[] ma = getConfiguredMenus();
    for (int i = 0; i < ma.length; i++) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, ma[i]);
        menuList.add(menu);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contributing menus.", e);
    }
    m_menus = menuList.toArray(new IMenu[0]);
    // local property observer
    addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(PROP_DAYS)) {
          invalidateTimeScale();
          if (!isActivityMapChanging()) {
            validateTimeScale();
          }
        }
        else if (e.getPropertyName().equals(PROP_FIRST_HOUR_OF_DAY)) {
          invalidateTimeScale();
          if (!isActivityMapChanging()) {
            validateTimeScale();
          }
        }
        else if (e.getPropertyName().equals(PROP_LAST_HOUR_OF_DAY)) {
          invalidateTimeScale();
          if (!isActivityMapChanging()) {
            validateTimeScale();
          }
        }
        else if (e.getPropertyName().equals(PROP_INTRADAY_INTERVAL)) {
          invalidateTimeScale();
          if (!isActivityMapChanging()) {
            validateTimeScale();
          }
        }
        else if (e.getPropertyName().equals(PROP_PLANNING_MODE)) {
          invalidateTimeScale();
          if (!isActivityMapChanging()) {
            validateTimeScale();
          }
        }
        else if (e.getPropertyName().equals(PROP_SELECTED_ACTIVITY_CELL)) {
          @SuppressWarnings("unchecked")
          ActivityCell<RI, AI> cell = (ActivityCell<RI, AI>) e.getNewValue();
          if (cell != null) {
            try {
              execActivityCellSelected(cell);
            }
            catch (ProcessingException t) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(t);
            }
            catch (Throwable t) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
            }
          }
        }
      }
    });
    createTimeScale();
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus
   * 
   * @param menuList
   *          live and mutable list of configured menus
   */
  protected void injectMenusInternal(List<IMenu> menuList) {
  }

  protected IActivityMapUIFacade createUIFacade() {
    return new P_ActivityMapUIFacade();
  }

  /*
   * Runtime
   */

  @Override
  public final void initActivityMap() throws ProcessingException {
    initActivityMapInternal();
    execInitActivityMap();
  }

  protected void initActivityMapInternal() throws ProcessingException {
  }

  @Override
  public final void disposeActivityMap() {
    disposeActivityMapInternal();
    try {
      execDisposeActivityMap();
    }
    catch (Throwable t) {
      LOG.warn(getClass().getName(), t);
    }
  }

  protected void disposeActivityMapInternal() {
  }

  private void invalidateTimeScale() {
    m_timeScaleValid = false;
  }

  private void validateTimeScale() {
    if (!m_timeScaleValid) {
      m_timeScaleValid = true;
      createTimeScale();
    }
  }

  @Override
  public void createTimeScale() {
    try {
      TimeScale scale = execCreateTimeScale();
      setTimeScale(scale);
    }
    catch (ProcessingException t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(t);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
  }

  @Override
  public TimeScale getTimeScale() {
    TimeScale scale = (TimeScale) propertySupport.getProperty(PROP_TIME_SCALE);
    if (scale == null) {
      scale = new TimeScale();
    }
    return scale;
  }

  @Override
  public void setTimeScale(TimeScale scale) {
    if (scale == null) {
      scale = new TimeScale();
    }
    propertySupport.setProperty(PROP_TIME_SCALE, scale);
  }

  /*
   * Model Observer
   */
  @Override
  public void addActivityMapListener(ActivityMapListener listener) {
    m_listenerList.add(ActivityMapListener.class, listener);
  }

  @Override
  public void removeActivityMapListener(ActivityMapListener listener) {
    m_listenerList.remove(ActivityMapListener.class, listener);
  }

  @Override
  public ActivityCell<RI, AI> resolveActivityCell(ActivityCell<RI, AI> cell) {
    if (cell == null) {
      return cell;
    }
    return m_activities.get(new CompositeObject(cell.getResourceId(), cell.getActivityId()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public ActivityCell<RI, AI>[] resolveActivityCells(ActivityCell<RI, AI>[] cells) {
    if (cells == null) {
      cells = new ActivityCell[0];
    }
    int mismatchCount = 0;
    for (int i = 0; i < cells.length; i++) {
      if (resolveActivityCell(cells[i]) != cells[i]) {
        LOG.warn("could not resolve " + cells[i]);
        mismatchCount++;
      }
    }
    if (mismatchCount > 0) {
      ActivityCell<RI, AI>[] resolvedCells = new ActivityCell[cells.length - mismatchCount];
      int index = 0;
      for (int i = 0; i < cells.length; i++) {
        if (resolveActivityCell(cells[i]) == cells[i]) {
          resolvedCells[index] = cells[i];
          index++;
        }
      }
      cells = resolvedCells;
    }
    return cells;
  }

  @Override
  public ActivityCell<RI, AI>[] getActivityCells(RI resourceId) {
    @SuppressWarnings("unchecked")
    RI[] array = (RI[]) Array.newInstance(getResourceIdClass(), 1);
    array[0] = resourceId;
    return getActivityCells(array);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ActivityCell<RI, AI>[] getActivityCells(RI[] resourceIds) {
    ArrayList<ActivityCell<RI, AI>> all = new ArrayList<ActivityCell<RI, AI>>();
    for (RI resourceId : resourceIds) {
      List<ActivityCell<RI, AI>> list = m_resourceIdToActivities.get(resourceId);
      if (list != null) {
        all.addAll(list);
      }
    }
    return all.toArray(new ActivityCell[all.size()]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ActivityCell<RI, AI>[] getAllActivityCells() {
    return m_activities.values().toArray(new ActivityCell[m_activities.size()]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void addActivityCells(ActivityCell<RI, AI>[] cells) {
    ArrayList<ActivityCell<RI, AI>> addedCells = new ArrayList<ActivityCell<RI, AI>>();
    for (ActivityCell<RI, AI> cell : cells) {
      CompositeObject key = new CompositeObject(cell.getResourceId(), cell.getActivityId());
      if (!m_activities.containsKey(key)) {
        m_activities.put(key, cell);
        List<ActivityCell<RI, AI>> list = m_resourceIdToActivities.get(cell.getResourceId());
        if (list == null) {
          list = new ArrayList<ActivityCell<RI, AI>>();
          m_resourceIdToActivities.put(cell.getResourceId(), list);
        }
        list.add(cell);
        addedCells.add(cell);
        decorateActivityCell(cell);
        cell.setObserver(m_cellObserver);
      }
    }
    if (addedCells.size() > 0) {
      fireActivitiesInserted(addedCells.toArray(new ActivityCell[addedCells.size()]));
    }
  }

  @Override
  public void updateActivityCells(ActivityCell<RI, AI>[] cells) {
    cells = resolveActivityCells(cells);
    updateActivityCellsInternal(cells);
  }

  @Override
  public void updateActivityCells(RI[] resourceIds) {
    updateActivityCellsInternal(getActivityCells(resourceIds));
  }

  // resolved cells
  private void updateActivityCellsInternal(ActivityCell<RI, AI>[] cells) {
    for (ActivityCell<RI, AI> cell : cells) {
      decorateActivityCell(cell);
    }
    fireActivitiesUpdated(cells);
  }

  @Override
  public void removeActivityCells(ActivityCell<RI, AI>[] cells) {
    cells = resolveActivityCells(cells);
    removeActivityCellsInternal(cells);
  }

  @Override
  public void removeActivityCells(RI[] resourceIds) {
    removeActivityCellsInternal(getActivityCells(resourceIds));
  }

  // cells are resolved
  private void removeActivityCellsInternal(ActivityCell<RI, AI>[] cells) {
    if (cells.length > 0) {
      for (ActivityCell<RI, AI> cell : cells) {
        cell.setObserver(null);
        m_activities.remove(new CompositeObject(cell.getResourceId(), cell.getActivityId()));
        List<ActivityCell<RI, AI>> list = m_resourceIdToActivities.get(cell.getResourceId());
        if (list != null) {
          list.remove(cell);
        }
      }
      fireActivitiesDeleted(cells);
    }
  }

  @Override
  public void removeAllActivityCells() {
    ActivityCell<RI, AI>[] a = getAllActivityCells();
    if (a.length > 0) {
      for (ActivityCell<RI, AI> cell : a) {
        cell.setObserver(null);
      }
      m_activities.clear();
      m_resourceIdToActivities.clear();
      fireAllActivitiesDeleted(a);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public ActivityCell<RI, AI> getSelectedActivityCell() {
    return (ActivityCell<RI, AI>) propertySupport.getProperty(PROP_SELECTED_ACTIVITY_CELL);
  }

  @Override
  public void setSelectedActivityCell(ActivityCell<RI, AI> cell) {
    cell = resolveActivityCell(cell);
    propertySupport.setProperty(PROP_SELECTED_ACTIVITY_CELL, cell);
  }

  @Override
  public boolean isSelectedActivityCell(ActivityCell<RI, AI> cell) {
    return getSelectedActivityCell() == cell;
  }

  @SuppressWarnings("unchecked")
  @Override
  public RI[] getSelectedResourceIds() {
    RI[] a = (RI[]) propertySupport.getProperty(PROP_SELECTED_RESOURCE_IDS);
    if (a == null) {
      a = (RI[]) Array.newInstance(getResourceIdClass(), 0);
    }
    return a;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setSelectedResourceIds(RI[] resourceIds) {
    RI[] internalResourceIds;
    if (resourceIds == null) {
      internalResourceIds = (RI[]) Array.newInstance(getResourceIdClass(), 0);
    }
    else {
      // Guarantee the type of the array. This is necessary for raw type clients which might still pass Object[]
      internalResourceIds = (RI[]) Array.newInstance(getResourceIdClass(), resourceIds.length);
      System.arraycopy(resourceIds, 0, internalResourceIds, 0, resourceIds.length);
    }

    m_selectedResourceIds.clear();
    m_selectedResourceIds.addAll(Arrays.asList(internalResourceIds));
    propertySupport.setProperty(PROP_SELECTED_RESOURCE_IDS, internalResourceIds);
  }

  @SuppressWarnings("unchecked")
  @Override
  public RI[] getResourceIds() {
    RI[] resourceIds = (RI[]) propertySupport.getProperty(PROP_RESOURCE_IDS);
    if (resourceIds == null) {
      resourceIds = (RI[]) Array.newInstance(getResourceIdClass(), 0);
    }
    return resourceIds;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setResourceIds(RI[] resourceIds) {
    if (resourceIds == null) {
      resourceIds = (RI[]) Array.newInstance(getResourceIdClass(), 0);
    }
    // delete activities of resourceIds that no Objecter exist
    HashSet<RI> eliminatedResourceIdSet = new HashSet<RI>();
    eliminatedResourceIdSet.addAll(Arrays.asList(getResourceIds()));
    eliminatedResourceIdSet.removeAll(Arrays.asList(resourceIds));
    try {
      setActivityMapChanging(true);
      //
      propertySupport.setProperty(PROP_RESOURCE_IDS, resourceIds);
      removeActivityCells(eliminatedResourceIdSet.toArray((RI[]) Array.newInstance(getResourceIdClass(), eliminatedResourceIdSet.size())));
      updateActivityCellsInternal(getAllActivityCells());
    }
    finally {
      setActivityMapChanging(false);
    }
  }

  @Override
  public void isSelectedResourceId(RI resourceId) {
    m_selectedResourceIds.contains(resourceId);
  }

  @Override
  public void setDrawSections(boolean drawSections) {
    propertySupport.setProperty(PROP_DRAW_SECTIONS, drawSections);
  }

  @Override
  public boolean isDrawSections() {
    return propertySupport.getPropertyBool(PROP_DRAW_SECTIONS);
  }

  @Override
  public IMenu[] getMenus() {
    return m_menus;
  }

  private IMenu[] fireEditActivityPopup(ActivityCell<RI, AI> cell) {
    if (cell != null) {
      ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_EDIT_ACTIVITY_POPUP, cell);
      // single observer for declared menus
      addEditActivityPopupMenus(e);
      fireActivityMapEventInternal(e);
      return e.getPopupMenus();
    }
    else {
      return new IMenu[0];
    }
  }

  private void addEditActivityPopupMenus(ActivityMapEvent e) {
    IMenu[] a = getMenus();
    for (int i = 0; i < a.length; i++) {
      IMenu validMenu = null;
      // pass 1
      if (a[i].isSingleSelectionAction()) {
        validMenu = a[i];
      }
      // pass 2
      if (validMenu != null) {
        validMenu.prepareAction();
        if (validMenu.isVisible()) {
          e.addPopupMenu(validMenu);
        }
      }
    }
  }

  private IMenu[] fireNewActivityPopup() {
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_NEW_ACTIVITY_POPUP);
    // single observer for declared menus
    addNewActivityPopupMenus(e);
    fireActivityMapEventInternal(e);
    return e.getPopupMenus();
  }

  private void addNewActivityPopupMenus(ActivityMapEvent e) {
    IMenu[] a = getMenus();
    for (int i = 0; i < a.length; i++) {
      IMenu validMenu = null;
      // pass 1
      if (a[i].isSingleSelectionAction()) {
        // ignore
      }
      else if (a[i].isMultiSelectionAction()) {
        // ignore
      }
      else {
        validMenu = a[i];
      }
      // pass 2
      if (validMenu != null) {
        validMenu.prepareAction();
        if (validMenu.isVisible()) {
          e.addPopupMenu(validMenu);
        }
      }
    }
  }

  private void fireCellAction(RI resourceId, MinorTimeColumn column, ActivityCell<RI, AI> activityCell) {
    // single observer
    try {
      execCellAction(resourceId, column, activityCell);
    }
    catch (ProcessingException t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(t);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
    ActivityMapEvent e = new ActivityMapEvent<RI>(this, ActivityMapEvent.TYPE_CELL_ACTION, resourceId, column, activityCell);
    fireActivityMapEventInternal(e);
  }

  private void fireActivitiesInserted(ActivityCell<RI, AI>[] a) {
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_ACTIVITIES_INSERTED, a);
    fireActivityMapEventInternal(e);
  }

  private void fireActivitiesUpdated(ActivityCell<RI, AI>[] a) {
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_ACTIVITIES_UPDATED, a);
    fireActivityMapEventInternal(e);
  }

  private void fireActivitiesDeleted(ActivityCell<RI, AI>[] a) {
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_ACTIVITIES_DELETED, a);
    fireActivityMapEventInternal(e);
  }

  private void fireAllActivitiesDeleted(ActivityCell<RI, AI>[] a) {
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_ALL_ACTIVITIES_DELETED, a);
    fireActivityMapEventInternal(e);
  }

  // main handler
  private void fireActivityMapEventInternal(ActivityMapEvent e) {
    if (isActivityMapChanging()) {
      // buffer the event for later batch firing
      m_eventBuffer.add(e);
    }
    else {
      EventListener[] listeners = m_listenerList.getListeners(ActivityMapListener.class);
      if (listeners != null && listeners.length > 0) {
        for (int i = 0; i < listeners.length; i++) {
          ((ActivityMapListener) listeners[i]).activityMapChanged(e);
        }
      }
    }
  }

  // batch handler
  private void fireActivityMapEventBatchInternal(ActivityMapEvent[] batch) {
    if (isActivityMapChanging()) {
      LOG.error("Illegal State: firing an event batch while table is changing");
    }
    else {
      if (batch.length > 0) {
        EventListener[] listeners = m_listenerList.getListeners(ActivityMapListener.class);
        if (listeners != null && listeners.length > 0) {
          for (int i = 0; i < listeners.length; i++) {
            for (ActivityMapEvent e : batch) {
              ((ActivityMapListener) listeners[i]).activityMapChanged(e);
            }
          }
        }
      }
    }
  }

  private void processChangeBuffer() {
    /*
     * 3. fire events changes are finished now, fire all buffered events
     * coalesce all events of same type and sort according to their type,
     * eventually merge
     */
    ArrayList<ActivityMapEvent> list = m_eventBuffer;
    m_eventBuffer = new ArrayList<ActivityMapEvent>();
    if (list.size() > 0) {
      HashMap<Integer, List<ActivityMapEvent>> coalesceMap = new HashMap<Integer, List<ActivityMapEvent>>();
      for (ActivityMapEvent e : list) {
        List<ActivityMapEvent> subList = coalesceMap.get(e.getType());
        if (subList == null) {
          subList = new ArrayList<ActivityMapEvent>();
          coalesceMap.put(e.getType(), subList);
        }
        subList.add(e);
      }
      TreeMap<Integer, ActivityMapEvent> sortedCoalescedMap = new TreeMap<Integer, ActivityMapEvent>();
      for (Map.Entry<Integer, List<ActivityMapEvent>> entry : coalesceMap.entrySet()) {
        int type = entry.getKey();
        List<ActivityMapEvent> subList = entry.getValue();
        int lastIndex = subList.size() - 1;
        switch (type) {
          case ActivityMapEvent.TYPE_ALL_ACTIVITIES_DELETED: {
            sortedCoalescedMap.put(10, subList.get(lastIndex));// use last
            break;
          }
          case ActivityMapEvent.TYPE_ACTIVITIES_INSERTED: {
            sortedCoalescedMap.put(20, coalesceActivityMapEvents(subList));// merge
            break;
          }
          case ActivityMapEvent.TYPE_ACTIVITIES_DELETED: {
            sortedCoalescedMap.put(30, coalesceActivityMapEvents(subList));// merge
            break;
          }
          case ActivityMapEvent.TYPE_ACTIVITIES_UPDATED: {
            sortedCoalescedMap.put(40, coalesceActivityMapEvents(subList));// merge
            break;
          }
          case ActivityMapEvent.TYPE_CELL_ACTION: {
            sortedCoalescedMap.put(50, subList.get(lastIndex));// use last
            break;
          }
          case ActivityMapEvent.TYPE_EDIT_ACTIVITY_POPUP: {
            sortedCoalescedMap.put(70, subList.get(lastIndex));// use last
            break;
          }
          case ActivityMapEvent.TYPE_NEW_ACTIVITY_POPUP: {
            sortedCoalescedMap.put(80, subList.get(lastIndex));// use last
            break;
          }
          default: {
            sortedCoalescedMap.put(-type, subList.get(lastIndex));// use last
          }
        }
      }
      fireActivityMapEventBatchInternal(sortedCoalescedMap.values().toArray(new ActivityMapEvent[sortedCoalescedMap.size()]));
    }
  }

  private ActivityMapEvent coalesceActivityMapEvents(List<ActivityMapEvent> list) {
    if (list.size() == 0) {
      return null;
    }
    else if (list.size() == 1) {
      return list.get(0);
    }
    else {
      ActivityMapEvent last = list.get(list.size() - 1);
      ActivityMapEvent ce = new ActivityMapEvent(last.getActivityMap(), last.getType());
      //
      ce.addPopupMenus(last.getPopupMenus());
      //
      HashSet<ActivityCell> coalesceList = new HashSet<ActivityCell>();
      for (ActivityMapEvent t : list) {
        if (t.getActivityCount() > 0) {
          coalesceList.addAll(Arrays.asList(t.getActivities()));
        }
      }
      ce.setActivities(coalesceList.toArray(new ActivityCell[coalesceList.size()]));
      //
      return ce;
    }
  }

  @Override
  public boolean isActivityMapChanging() {
    return m_tableChanging > 0;
  }

  @Override
  public void setActivityMapChanging(boolean b) {
    // use a stack counter because setActivityMapChanging might be called in
    // nested loops
    if (b) {
      m_tableChanging++;
      if (m_tableChanging == 1) {
        // 0 --> 1
        propertySupport.setPropertiesChanging(true);
      }
    }
    else {
      if (m_tableChanging > 0) {
        m_tableChanging--;
        if (m_tableChanging == 0) {
          // 1 --> 0
          try {
            validateTimeScale();
            processChangeBuffer();
          }
          finally {
            propertySupport.setPropertiesChanging(false);
          }
        }
      }
    }
  }

  @Override
  public Date getBeginTime() {
    Calendar cal = Calendar.getInstance();
    Date[] a = getDays();
    if (a.length > 0) {
      cal.setTime(a[0]);
    }
    else {
      cal.setTime(DateUtility.truncDate(new Date()));
    }
    switch (getPlanningMode()) {
      case PLANNING_MODE_INTRADAY: {
        cal.set(Calendar.HOUR_OF_DAY, getFirstHourOfDay());
        break;
      }
      case PLANNING_MODE_DAY: {
        break;
      }
      case PLANNING_MODE_WEEK: {
        break;
      }
    }
    return cal.getTime();
  }

  @Override
  public Date getEndTime() {
    Calendar cal = Calendar.getInstance();
    Date[] a = getDays();
    if (a.length > 0) {
      cal.setTime(a[a.length - 1]);
    }
    else {
      cal.setTime(DateUtility.truncDate(new Date()));
    }
    switch (getPlanningMode()) {
      case PLANNING_MODE_INTRADAY: {
        cal.set(Calendar.HOUR_OF_DAY, getLastHourOfDay());
        break;
      }
      case PLANNING_MODE_DAY: {
        cal.add(Calendar.DATE, 1);
        break;
      }
      case PLANNING_MODE_WEEK: {
        cal.add(Calendar.DATE, 7);
        break;
      }
    }
    return cal.getTime();
  }

  @Override
  public void addDay(Date day) {
    day = DateUtility.truncDate(day);
    if (day != null) {
      TreeSet<Date> set = new TreeSet<Date>();
      set.addAll(Arrays.asList(getDays()));
      set.add(day);
      setDaysInternal(set);
    }
  }

  @Override
  public void removeDay(Date day) {
    day = DateUtility.truncDate(day);
    if (day != null) {
      TreeSet<Date> set = new TreeSet<Date>();
      set.addAll(Arrays.asList(getDays()));
      set.remove(day);
      setDaysInternal(set);
    }
  }

  @Override
  public void setDay(Date day) {
    day = DateUtility.truncDate(day);
    TreeSet<Date> set = new TreeSet<Date>();
    if (day != null) {
      set.add(day);
    }
    setDaysInternal(set);
  }

  @Override
  public void setDays(Date[] days) {
    TreeSet<Date> set = new TreeSet<Date>();
    for (Date d : days) {
      set.add(DateUtility.truncDate(d));
    }
    setDaysInternal(set);
  }

  private void setDaysInternal(TreeSet<Date> set) {
    propertySupport.setProperty(PROP_DAYS, set.toArray(new Date[set.size()]));
  }

  @Override
  public Date[] getDays() {
    Date[] a = (Date[]) propertySupport.getProperty(PROP_DAYS);
    if (a == null) {
      a = new Date[0];
    }
    return a;
  }

  @Override
  public int getWorkDayCount() {
    return propertySupport.getPropertyInt(PROP_WORK_DAY_COUNT);
  }

  @Override
  public void setWorkDayCount(int n) {
    if (n < 1 || n > 6) {
      return;// ignore it
    }
    propertySupport.setPropertyInt(PROP_WORK_DAY_COUNT, n);
  }

  @Override
  public boolean isWorkDaysOnly() {
    return propertySupport.getPropertyBool(PROP_WORK_DAYS_ONLY);
  }

  @Override
  public void setWorkDaysOnly(boolean b) {
    propertySupport.setPropertyBool(PROP_WORK_DAYS_ONLY, b);
  }

  @Override
  public int getFirstHourOfDay() {
    return propertySupport.getPropertyInt(PROP_FIRST_HOUR_OF_DAY);
  }

  @Override
  public void setFirstHourOfDay(int i) {
    propertySupport.setPropertyInt(PROP_FIRST_HOUR_OF_DAY, i);
  }

  @Override
  public int getLastHourOfDay() {
    return propertySupport.getPropertyInt(PROP_LAST_HOUR_OF_DAY);
  }

  @Override
  public void setLastHourOfDay(int i) {
    propertySupport.setPropertyInt(PROP_LAST_HOUR_OF_DAY, i);
  }

  @Override
  public long getIntradayInterval() {
    return propertySupport.getPropertyInt(PROP_INTRADAY_INTERVAL);
  }

  @Override
  public void setIntradayInterval(long millis) {
    if (millis < 15L * 60000L || millis > 24L * 3600000L) {
      throw new IllegalArgumentException("intradayIntervalMinutes must be between 15 minutes and 24 hours");
    }
    propertySupport.setPropertyLong(PROP_INTRADAY_INTERVAL, millis);
  }

  @Override
  public void setIntradayIntervalInMinutes(long min) {
    setIntradayInterval(min * 60000L);
  }

  @Override
  public long getMinimumActivityDuration() {
    return m_minimumActivityDuration;
  }

  @Override
  public void setMinimumActivityDuration(long minDuration) {
    m_minimumActivityDuration = minDuration;
  }

  @Override
  public void setMinimumActivityDurationInMinutes(long min) {
    setMinimumActivityDuration(min * 60000L);
  }

  @Override
  public int getPlanningMode() {
    return propertySupport.getPropertyInt(PROP_PLANNING_MODE);
  }

  @Override
  public void setPlanningMode(int mode) {
    propertySupport.setPropertyInt(PROP_PLANNING_MODE, mode);
  }

  @Override
  public Date getSelectedBeginTime() {
    return (Date) propertySupport.getProperty(PROP_SELECTED_BEGIN_TIME);
  }

  @Override
  public Date getSelectedEndTime() {
    return (Date) propertySupport.getProperty(PROP_SELECTED_END_TIME);
  }

  @Override
  public void setSelectedTime(Date beginTime, Date endTime) {
    try {
      setActivityMapChanging(true);
      //
      propertySupport.setProperty(PROP_SELECTED_BEGIN_TIME, beginTime);
      propertySupport.setProperty(PROP_SELECTED_END_TIME, endTime);
    }
    finally {
      setActivityMapChanging(false);
    }
  }

  @Override
  public Object getContainer() {
    return propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls an
   * {@link IActivityMap}
   */
  public void setContainerInternal(Object container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  @Override
  public void decorateActivityCell(ActivityCell<RI, AI> cell) {
    try {
      cell.setObserver(null);
      //
      decorateActivityCellInternal(cell);
      execDecorateActivityCell(cell);
    }
    catch (ProcessingException t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(t);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
    finally {
      cell.setObserver(m_cellObserver);
    }
  }

  protected void decorateActivityCellInternal(ActivityCell<RI, AI> p) {
    String from = getTimeScale().getDateFormat().format(p.getBeginTime());
    String to = from;
    if (p.getEndTime() != null) {
      to = getTimeScale().getDateFormat().format(p.getEndTime());
    }
    if (from.equals(to)) {
      p.setTooltipText(from);
    }
    else {
      p.setTooltipText(from + " - " + to);
    }
  }

  @Override
  public MultiTimeRange calculateSelectedTimeRanges(Date earliestBeginTime, Date latestEndTime) {
    MultiTimeRange multiRange = new MultiTimeRange();
    Calendar cal = Calendar.getInstance();
    for (Date d : getDays()) {
      cal.setTime(d);
      cal.set(Calendar.HOUR_OF_DAY, getFirstHourOfDay());
      Date from = cal.getTime();
      cal.set(Calendar.HOUR_OF_DAY, getLastHourOfDay());
      cal.add(Calendar.HOUR_OF_DAY, 1);
      Date to = cal.getTime();
      multiRange.add(from, to);
    }
    // remove too early and too late timeranges
    Date min = multiRange.getBeginDate();
    if (min != null && min.before(earliestBeginTime)) {
      multiRange.remove(min, earliestBeginTime);
    }
    Date max = multiRange.getEndDate();
    if (max != null && max.after(latestEndTime)) {
      multiRange.remove(latestEndTime, max);
    }
    return multiRange;
  }

  @Override
  public void planActivityForSelectedResources(boolean singleMatch, boolean chooseRandom, Date earliestBeginTime, Date latestEndTime, long preferredDuration) {
    earliestBeginTime = DateUtility.toUtilDate(earliestBeginTime);
    latestEndTime = DateUtility.toUtilDate(latestEndTime);
    if (earliestBeginTime == null) {
      earliestBeginTime = new Date();
    }
    if (latestEndTime == null) {
      // at most 10 years in future
      latestEndTime = new Date(System.currentTimeMillis() + 10L * 365L * 24L * 3600L * 1000L);
    }
    if (preferredDuration <= 0) {
      preferredDuration = 30L * 60L * 1000L;
    }
    //
    setSelectedTime(null, null);
    if (singleMatch) {
      planActivityForSelectedResourcesSingleInternal(chooseRandom, earliestBeginTime, latestEndTime, preferredDuration);
    }
    else {
      planActivityForSelectedResourcesMultiInternal(earliestBeginTime, latestEndTime, preferredDuration);
    }
  }

  /**
   * step through all resources and collect best activities with a scoring
   * algorithm<br>
   * build a sorted map<br>
   * the key is either (startDate-now) in minutes [if duration is ok] otherwise
   * the key is 1000000-duration in minutes [if duration is at least 15 minutes]
   * <p>
   * the value is the plannedActivity for that resource
   */
  private void planActivityForSelectedResourcesSingleInternal(boolean chooseRandom, Date earliestBeginTime, Date latestEndTime, long preferredDuration) {
    MultiTimeRange multiTimeRange = calculateSelectedTimeRanges(earliestBeginTime, latestEndTime);
    if (multiTimeRange.isEmpty()) {
      return;
    }
    TreeMap<CompositeLong, ActivityCell<Integer, Integer>> sortMap = new TreeMap<CompositeLong, ActivityCell<Integer, Integer>>();
    Random rnd = new Random();
    int resourceIndex = 0;
    for (RI resourceId : getSelectedResourceIds()) {
      MultiTimeRange localTimeRanges = (MultiTimeRange) multiTimeRange.clone();
      for (ActivityCell<RI, AI> a : getActivityCells(resourceId)) {
        localTimeRanges.remove(a.getBeginTime(), a.getEndTime());
      }
      // now only available time ranges for that resource are left
      for (TimeRange tr : localTimeRanges.getTimeRanges()) {
        long durationMillis = tr.getDurationMillis();
        long sortNo = chooseRandom ? rnd.nextLong() : resourceIndex;
        if (durationMillis >= preferredDuration) {
          ActivityCell<Integer, Integer> a = new ActivityCell<Integer, Integer>(0, 0);
          a.setBeginTime(tr.getFrom());
          a.setEndTime(new Date(tr.getFrom().getTime() + preferredDuration));
          sortMap.put(new CompositeLong(0, a.getBeginTime().getTime(), sortNo), a);
        }
        else if (durationMillis >= 15L * 60L * 1000L) {// at least 15 minutes
          ActivityCell<Integer, Integer> a = new ActivityCell<Integer, Integer>(0, 0);
          a.setBeginTime(tr.getFrom());
          a.setEndTime(new Date(tr.getFrom().getTime() + durationMillis));
          sortMap.put(new CompositeLong(1, -durationMillis, sortNo), a);
        }
      }
      resourceIndex++;
    }
    // the top entry of the sort map is the one with best score (lowest number)
    if (!sortMap.isEmpty()) {
      ActivityCell<Integer, Integer> a = sortMap.get(sortMap.firstKey());
      setSelectedTime(a.getBeginTime(), a.getEndTime());
    }
  }

  /**
   * step through all resources and collect best activities with a scoring
   * algorithm<br>
   * build a sorted map<br>
   * the key is either (startDate-now) in minutes [if duration is ok] otherwise
   * the key is 1000000-duration in minutes [if duration is at least 15 minutes]
   * <p>
   * the value is the plannedActivity for that resource
   */
  private void planActivityForSelectedResourcesMultiInternal(Date earliestBeginTime, Date latestEndTime, long preferredDuration) {
    MultiTimeRange multiTimeRange = calculateSelectedTimeRanges(earliestBeginTime, latestEndTime);
    if (multiTimeRange.isEmpty()) {
      return;
    }
    TreeMap<CompositeLong, ActivityCell<Integer, Integer>> sortMap = new TreeMap<CompositeLong, ActivityCell<Integer, Integer>>();
    for (RI resourceId : getSelectedResourceIds()) {
      for (ActivityCell<RI, AI> a : getActivityCells(resourceId)) {
        multiTimeRange.remove(a.getBeginTime(), a.getEndTime());
      }
    }
    // now only available time ranges for that resource are left
    for (TimeRange tr : multiTimeRange.getTimeRanges()) {
      long durationMillis = tr.getDurationMillis();
      if (durationMillis >= preferredDuration) {
        ActivityCell<Integer, Integer> a = new ActivityCell<Integer, Integer>(0, 0);
        a.setBeginTime(tr.getFrom());
        a.setEndTime(new Date(tr.getFrom().getTime() + preferredDuration));
        sortMap.put(new CompositeLong(0, a.getBeginTime().getTime()), a);
      }
      else if (durationMillis >= 15L * 60L * 1000L) {// at least 15 minutes
        ActivityCell<Integer, Integer> a = new ActivityCell<Integer, Integer>(0, 0);
        a.setBeginTime(tr.getFrom());
        a.setEndTime(new Date(tr.getFrom().getTime() + durationMillis));
        sortMap.put(new CompositeLong(1, -durationMillis), a);
      }
    }
    // the top entry of the sort map is the one with best score (lowest number)
    if (!sortMap.isEmpty()) {
      ActivityCell<Integer, Integer> a = sortMap.get(sortMap.firstKey());
      setSelectedTime(a.getBeginTime(), a.getEndTime());
    }
  }

  @Override
  public IActivityMapUIFacade getUIFacade() {
    return m_activityMapUIFacade;
  }

  /**
   * Private planned activity cell observer
   */
  private class P_ActivityCellObserver implements IActivityCellObserver<RI, AI> {

    @SuppressWarnings("unchecked")
    @Override
    public void cellChanged(ActivityCell<RI, AI> cell, int bitPos) {
      fireActivitiesUpdated(new ActivityCell[]{cell});
    }
  }

  /**
   * Specialized ui facade
   */
  private class P_ActivityMapUIFacade implements IActivityMapUIFacade<RI, AI> {

    public void setSelectedDayListFromUI(Date[] days) {
      setDays(days);
    }

    @Override
    public void setSelectionFromUI(RI[] resourceIds, double[] normalizedRange) {
      try {
        setActivityMapChanging(true);
        //
        double cellCenter = (1.0 / getTimeScale().getMinorTimeColumns().length / 2.0);
        setSelectedResourceIds(resourceIds);
        Date beginTime = normalizedRange != null ? getTimeScale().getMinorTimeColumn(normalizedRange[0] + cellCenter).getBeginTime() : null;
        Date endTime = normalizedRange != null ? getTimeScale().getMinorTimeColumn(normalizedRange[1] - cellCenter).getEndTime() : null;
        setSelectedTime(beginTime, endTime);
      }
      finally {
        setActivityMapChanging(false);
      }
    }

    @Override
    public IMenu[] fireEditActivityPopupFromUI() {
      return fireEditActivityPopup(getSelectedActivityCell());
    }

    @Override
    public IMenu[] fireNewActivityPopupFromUI() {
      return fireNewActivityPopup();
    }

    @Override
    public void setDaysFromUI(Date[] days) {
      setDays(days);
    }

    @Override
    public void setSelectedActivityCellFromUI(ActivityCell<RI, AI> cell) {
      setSelectedActivityCell(cell);
    }

    @Override
    public void fireCellActionFromUI(RI resourceId, double[] normalizedRange, ActivityCell<RI, AI> activityCell) {
      if (activityCell != null) {
        setSelectedActivityCell(activityCell);
      }
      MinorTimeColumn column = getTimeScale().getMinorTimeColumn((normalizedRange[0] + normalizedRange[1]) / 2);
      fireCellAction(resourceId, column, activityCell);
    }
  }
}

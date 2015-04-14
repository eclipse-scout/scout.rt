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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapActivityCellSelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapCellActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapCreateTimeScaleChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapDecorateActivityCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapDecorateMajorTimeColumnChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapDecorateMinorTimeColumnChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapDisposeActivityMapChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.ActivityMapChains.ActivityMapInitActivityMapChain;
import org.eclipse.scout.rt.client.extension.ui.basic.activitymap.IActivityMapExtension;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IActivityMapContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.ActivityMapContextMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

public abstract class AbstractActivityMap<RI, AI> extends AbstractPropertyObserver implements IActivityMap<RI, AI>, IContributionOwner, IExtensibleObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractActivityMap.class);

  private boolean m_initialized;
  private EventListenerList m_listenerList;
  private IActivityMapUIFacade m_activityMapUIFacade;
  private long m_minimumActivityDuration;// millis
  private Map<RI/* resourceId */, List<ActivityCell<RI, AI>>> m_resourceIdToActivities;
  private Map<CompositeObject/* resourceId,activityId */, ActivityCell<RI, AI>> m_activities;
  private Set<RI/* resourceId */> m_selectedResourceIds;
  private int m_tableChanging;
  private List<ActivityMapEvent> m_eventBuffer = new ArrayList<ActivityMapEvent>();
  private IActivityCellObserver<RI, AI> m_cellObserver;
  private boolean m_timeScaleValid;
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractActivityMap<RI, AI>, IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> m_objectExtensions;

  public AbstractActivityMap() {
    this(true);
  }

  public AbstractActivityMap(boolean callInitializer) {
    m_objectExtensions = new ObjectExtensions<AbstractActivityMap<RI, AI>, IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>>(this);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  protected void callInitializer() {
    if (!m_initialized) {
      interceptInitConfig();
      m_initialized = true;
    }
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredWorkDayCount() {
    return 5;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredWorkDaysOnly() {
    return false;
  }

  /**
   * Configures the first hour of the day. A value of <code>8</code> will be considered as 8 a.m. - 9 a.m. , so that the
   * first entry can start at 8 a.m. The very first possible value is <code>0</code> which is considered as 12 a.m.
   * (beginning of the day)
   *
   * @return
   */
  @ConfigProperty(ConfigProperty.HOUR_OF_DAY)
  @Order(110)
  protected int getConfiguredFirstHourOfDay() {
    return 8;
  }

  /**
   * Configures the last hour of the day. A value of <code>16</code> will be considered as 4 p.m. - 5 p.m. , so that the
   * last possible entry can last to 5 p.m. The very last possible value is <code>23</code> which is considered as 11
   * p.m. - 12 a.m. (midnight)
   */
  @ConfigProperty(ConfigProperty.HOUR_OF_DAY)
  @Order(130)
  protected int getConfiguredLastHourOfDay() {
    return 16;
  }

  @ConfigProperty(ConfigProperty.DURATION_MINUTES)
  @Order(120)
  protected long getConfiguredIntradayInterval() {
    return 1800000L;
  }

  @ConfigProperty(ConfigProperty.DURATION_MINUTES)
  @Order(125)
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
  protected boolean getConfiguredDrawSections() {
    return true;
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
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

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
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
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
        menus.addOrdered(menu);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + menuClazz.getName() + "'.", e));
      }
    }
    m_contributionHolder = new ContributionComposite(this);
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    menus.addAllOrdered(contributedMenus);

    try {
      injectMenusInternal(menus);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contributing menus.", e);
    }
    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    IActivityMapContextMenu contextMenu = new ActivityMapContextMenu(this, menus.getOrderedList());
    setContextMenu(contextMenu);

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
              interceptActivityCellSelected(cell);
            }
            catch (ProcessingException t) {
              BEANS.get(ExceptionHandler.class).handle(t);
            }
            catch (Throwable t) {
              BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
            }
          }
        }
      }
    });
    createTimeScale();
  }

  @Override
  public final List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> createLocalExtension() {
    return new LocalActivityMapExtension<RI, AI, AbstractActivityMap<RI, AI>>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
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
    interceptInitActivityMap();
    // init actions
    ActionUtility.initActions(getMenus());
  }

  protected void initActivityMapInternal() throws ProcessingException {
  }

  @Override
  public final void disposeActivityMap() {
    disposeActivityMapInternal();
    try {
      interceptDisposeActivityMap();
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
      TimeScale scale = interceptCreateTimeScale();
      setTimeScale(scale);
    }
    catch (ProcessingException t) {
      BEANS.get(ExceptionHandler.class).handle(t);
    }
    catch (Throwable t) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
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

  @Override
  public List<ActivityCell<RI, AI>> resolveActivityCells(List<? extends ActivityCell<RI, AI>> cells) {
    if (cells == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<ActivityCell<RI, AI>> result = new ArrayList<ActivityCell<RI, AI>>(cells.size());
    for (ActivityCell<RI, AI> cell : cells) {
      if (resolveActivityCell(cell) == cell) {
        result.add(cell);
      }
    }
    return result;
  }

  @Override
  public List<ActivityCell<RI, AI>> getActivityCells(RI resourceId) {
    ArrayList<RI> resourceList = new ArrayList<RI>();
    resourceList.add(resourceId);
    return getActivityCells(resourceList);
  }

  @Override
  public List<ActivityCell<RI, AI>> getActivityCells(List<? extends RI> resourceIds) {
    List<ActivityCell<RI, AI>> all = new ArrayList<ActivityCell<RI, AI>>();
    for (RI resourceId : resourceIds) {
      List<ActivityCell<RI, AI>> list = m_resourceIdToActivities.get(resourceId);
      if (list != null) {
        all.addAll(list);
      }
    }
    return all;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ActivityCell<RI, AI>> getAllActivityCells() {
    return new ArrayList(m_activities.values());
  }

  @Override
  public void addActivityCells(List<? extends ActivityCell<RI, AI>> cells) {
    List<ActivityCell<RI, AI>> addedCells = new ArrayList<ActivityCell<RI, AI>>();
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
      fireActivitiesInserted(addedCells);
    }
  }

  @Override
  public void updateActivityCells(List<? extends ActivityCell<RI, AI>> cells) {
    cells = resolveActivityCells(cells);
    updateActivityCellsInternal(cells);
  }

  @Override
  public void updateActivityCellsById(List<? extends RI> resourceIds) {
    updateActivityCellsInternal(getActivityCells(resourceIds));
  }

  // resolved cells
  private void updateActivityCellsInternal(List<? extends ActivityCell<RI, AI>> cells) {
    for (ActivityCell<RI, AI> cell : cells) {
      decorateActivityCell(cell);
    }
    fireActivitiesUpdated(cells);
  }

  @Override
  public void removeActivityCells(List<? extends ActivityCell<RI, AI>> cells) {
    cells = resolveActivityCells(cells);
    removeActivityCellsInternal(cells);
  }

  @Override
  public void removeActivityCellsById(List<? extends RI> resourceIds) {
    removeActivityCellsInternal(getActivityCells(resourceIds));
  }

  // cells are resolved
  private void removeActivityCellsInternal(List<? extends ActivityCell<RI, AI>> cells) {
    if (CollectionUtility.hasElements(cells)) {
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
    List<ActivityCell<RI, AI>> a = getAllActivityCells();
    if (CollectionUtility.hasElements(a)) {
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
  public List<RI> getSelectedResourceIds() {
    List<RI> a = (List<RI>) propertySupport.getProperty(PROP_SELECTED_RESOURCE_IDS);
    if (a == null) {
      a = CollectionUtility.emptyArrayList();
    }
    return a;
  }

  @Override
  public void setSelectedResourceIds(List<? extends RI> resourceIds) {
    List<RI> internalResourceIds = CollectionUtility.arrayList(resourceIds);
    m_selectedResourceIds.clear();
    m_selectedResourceIds.addAll(internalResourceIds);
    propertySupport.setProperty(PROP_SELECTED_RESOURCE_IDS, internalResourceIds);

    // check whether current selected activity cell needs to be updated
    if (CollectionUtility.size(internalResourceIds) != 1) {
      // at most one activity cell might be selected
      setSelectedActivityCell(null);
      return;
    }

    ActivityCell<RI, AI> selectedCell = getSelectedActivityCell();
    if (selectedCell == null) {
      // nothing selected
      return;
    }

    RI resourceId = CollectionUtility.firstElement(resourceIds);
    if (CompareUtility.notEquals(resourceId, selectedCell.getResourceId())) {
      // selected cell does not belong to selected resources
      setSelectedActivityCell(null);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<RI> getResourceIds() {
    List<RI> resourceIds = (List<RI>) propertySupport.getProperty(PROP_RESOURCE_IDS);
    if (resourceIds == null) {
      return CollectionUtility.emptyArrayList();
    }
    return CollectionUtility.arrayList(resourceIds);
  }

  @Override
  public void setResourceIds(List<? extends RI> resourceIds) {
    if (resourceIds == null) {
      resourceIds = CollectionUtility.emptyArrayList();
    }
    // delete activities of resourceIds that no Object exists
    HashSet<RI> eliminatedResourceIdSet = new HashSet<RI>();
    eliminatedResourceIdSet.addAll(getResourceIds());
    eliminatedResourceIdSet.removeAll(resourceIds);
    try {
      setActivityMapChanging(true);
      //
      propertySupport.setProperty(PROP_RESOURCE_IDS, resourceIds);
      removeActivityCellsById(new ArrayList<RI>(eliminatedResourceIdSet));
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
  public void setMenus(List<? extends IMenu> menus) {
    getContextMenu().setChildActions(menus);
  }

  @Override
  public void addMenu(IMenu menu) {
    List<IMenu> menus = getMenus();
    menus.add(menu);
    setMenus(menus);
  }

  protected void setContextMenu(IActivityMapContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public IActivityMapContextMenu getContextMenu() {
    return (IActivityMapContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  private void fireCellAction(RI resourceId, MinorTimeColumn column, ActivityCell<RI, AI> activityCell) {
    // single observer
    try {
      interceptCellAction(resourceId, column, activityCell);
    }
    catch (ProcessingException t) {
      BEANS.get(ExceptionHandler.class).handle(t);
    }
    catch (Throwable t) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
    }
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_CELL_ACTION, resourceId, column, activityCell);
    fireActivityMapEventInternal(e);
  }

  private void fireActivitiesInserted(List<? extends ActivityCell<RI, AI>> a) {
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_ACTIVITIES_INSERTED, a);
    fireActivityMapEventInternal(e);
  }

  private void fireActivitiesUpdated(List<? extends ActivityCell<RI, AI>> a) {
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_ACTIVITIES_UPDATED, a);
    fireActivityMapEventInternal(e);
  }

  private void fireActivitiesDeleted(List<? extends ActivityCell<RI, AI>> a) {
    ActivityMapEvent e = new ActivityMapEvent(this, ActivityMapEvent.TYPE_ACTIVITIES_DELETED, a);
    fireActivityMapEventInternal(e);
  }

  private void fireAllActivitiesDeleted(List<? extends ActivityCell<RI, AI>> a) {
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
    List<ActivityMapEvent> list = m_eventBuffer;
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
          coalesceList.addAll(t.getActivities());
        }
      }
      ce.setActivities(new ArrayList<ActivityCell>(coalesceList));
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

      // update selected activity cell based on selected time range.
      // this is a work around for enforcing an owner value change event on context menus. Actually the empty selection event
      // should be handled correctly in the GUI.
      List<RI> selectedResourceIds = getSelectedResourceIds();
      if (selectedResourceIds.size() == 1) {
        RI resourceId = CollectionUtility.firstElement(selectedResourceIds);
        List<ActivityCell<RI, AI>> activityCells = getActivityCells(resourceId);
        for (ActivityCell<RI, AI> cell : activityCells) {

          if (CompareUtility.equals(cell.getBeginTime(), beginTime) &&
              (CompareUtility.equals(cell.getEndTime(), endTime)
                  // see TimeScaleBuilder, end time is sometimes actual end time minus 1ms
                  || (cell != null
                  && cell.getEndTime() != null
                  && endTime != null
                  && cell.getEndTime().getTime() == endTime.getTime() + 1))) {
            setSelectedActivityCell(cell);
            return;
          }
        }
      }
      setSelectedActivityCell(null);
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
      interceptDecorateActivityCell(cell);
    }
    catch (ProcessingException t) {
      BEANS.get(ExceptionHandler.class).handle(t);
    }
    catch (Throwable t) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
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

    @Override
    public void cellChanged(ActivityCell<RI, AI> cell, int bitPos) {
      ArrayList<ActivityCell<RI, AI>> cellList = new ArrayList<ActivityCell<RI, AI>>(1);
      cellList.add(cell);
      fireActivitiesUpdated(cellList);
    }
  }

  /**
   * Specialized ui facade
   */
  private class P_ActivityMapUIFacade implements IActivityMapUIFacade<RI, AI> {

    @Override
    public void setSelectionFromUI(List<? extends RI> resourceIds, double[] normalizedRange) {
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

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalActivityMapExtension<RI, AI, OWNER extends AbstractActivityMap<RI, AI>> extends AbstractExtension<OWNER> implements IActivityMapExtension<RI, AI, OWNER> {

    public LocalActivityMapExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execDecorateMinorTimeColumn(ActivityMapDecorateMinorTimeColumnChain<RI, AI> chain, TimeScale scale, MajorTimeColumn majorColumn, MinorTimeColumn minorColumn) throws ProcessingException {
      getOwner().execDecorateMinorTimeColumn(scale, majorColumn, minorColumn);
    }

    @Override
    public void execActivityCellSelected(ActivityMapActivityCellSelectedChain<RI, AI> chain, ActivityCell<RI, AI> cell) throws ProcessingException {
      getOwner().execActivityCellSelected(cell);
    }

    @Override
    public void execDisposeActivityMap(ActivityMapDisposeActivityMapChain<RI, AI> chain) throws ProcessingException {
      getOwner().execDisposeActivityMap();
    }

    @Override
    public TimeScale execCreateTimeScale(ActivityMapCreateTimeScaleChain<RI, AI> chain) throws ProcessingException {
      return getOwner().execCreateTimeScale();
    }

    @Override
    public void execDecorateActivityCell(ActivityMapDecorateActivityCellChain<RI, AI> chain, ActivityCell<RI, AI> cell) throws ProcessingException {
      getOwner().execDecorateActivityCell(cell);
    }

    @Override
    public void execInitActivityMap(ActivityMapInitActivityMapChain<RI, AI> chain) throws ProcessingException {
      getOwner().execInitActivityMap();
    }

    @Override
    public void execCellAction(ActivityMapCellActionChain<RI, AI> chain, RI resourceId, MinorTimeColumn column, ActivityCell<RI, AI> activityCell) throws ProcessingException {
      getOwner().execCellAction(resourceId, column, activityCell);
    }

    @Override
    public void execDecorateMajorTimeColumn(ActivityMapDecorateMajorTimeColumnChain<RI, AI> chain, TimeScale scale, MajorTimeColumn columns) throws ProcessingException {
      getOwner().execDecorateMajorTimeColumn(scale, columns);
    }

  }

  protected final void interceptDecorateMinorTimeColumn(TimeScale scale, MajorTimeColumn majorColumn, MinorTimeColumn minorColumn) throws ProcessingException {
    List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions = getAllExtensions();
    ActivityMapDecorateMinorTimeColumnChain<RI, AI> chain = new ActivityMapDecorateMinorTimeColumnChain<RI, AI>(extensions);
    chain.execDecorateMinorTimeColumn(scale, majorColumn, minorColumn);
  }

  protected final void interceptActivityCellSelected(ActivityCell<RI, AI> cell) throws ProcessingException {
    List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions = getAllExtensions();
    ActivityMapActivityCellSelectedChain<RI, AI> chain = new ActivityMapActivityCellSelectedChain<RI, AI>(extensions);
    chain.execActivityCellSelected(cell);
  }

  protected final void interceptDisposeActivityMap() throws ProcessingException {
    List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions = getAllExtensions();
    ActivityMapDisposeActivityMapChain<RI, AI> chain = new ActivityMapDisposeActivityMapChain<RI, AI>(extensions);
    chain.execDisposeActivityMap();
  }

  protected final TimeScale interceptCreateTimeScale() throws ProcessingException {
    List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions = getAllExtensions();
    ActivityMapCreateTimeScaleChain<RI, AI> chain = new ActivityMapCreateTimeScaleChain<RI, AI>(extensions);
    return chain.execCreateTimeScale();
  }

  protected final void interceptDecorateActivityCell(ActivityCell<RI, AI> cell) throws ProcessingException {
    List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions = getAllExtensions();
    ActivityMapDecorateActivityCellChain<RI, AI> chain = new ActivityMapDecorateActivityCellChain<RI, AI>(extensions);
    chain.execDecorateActivityCell(cell);
  }

  protected final void interceptInitActivityMap() throws ProcessingException {
    List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions = getAllExtensions();
    ActivityMapInitActivityMapChain<RI, AI> chain = new ActivityMapInitActivityMapChain<RI, AI>(extensions);
    chain.execInitActivityMap();
  }

  protected final void interceptCellAction(RI resourceId, MinorTimeColumn column, ActivityCell<RI, AI> activityCell) throws ProcessingException {
    List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions = getAllExtensions();
    ActivityMapCellActionChain<RI, AI> chain = new ActivityMapCellActionChain<RI, AI>(extensions);
    chain.execCellAction(resourceId, column, activityCell);
  }

  protected final void interceptDecorateMajorTimeColumn(TimeScale scale, MajorTimeColumn columns) throws ProcessingException {
    List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions = getAllExtensions();
    ActivityMapDecorateMajorTimeColumnChain<RI, AI> chain = new ActivityMapDecorateMajorTimeColumnChain<RI, AI>(extensions);
    chain.execDecorateMajorTimeColumn(scale, columns);
  }
}

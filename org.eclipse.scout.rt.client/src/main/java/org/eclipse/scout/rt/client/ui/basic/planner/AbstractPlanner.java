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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.Range;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.IPlannerExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerActivityCellSelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerCellActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDecorateActivityCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisposePlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerInitPlannerChain;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IPlannerContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.PlannerContextMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

public abstract class AbstractPlanner<RI, AI> extends AbstractPropertyObserver implements IPlanner<RI, AI>, IContributionOwner, IExtensibleObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractPlanner.class);

  private boolean m_initialized;
  private EventListenerList m_listenerList;
  private IPlannerUIFacade m_activityMapUIFacade;
  private long m_minimumActivityDuration;// millis
  private List<Resource<RI>> m_resources;
  private int m_tableChanging;
  private AbstractEventBuffer<PlannerEvent> m_eventBuffer;
//  private IActivityCellObserver<RI, AI> m_cellObserver;
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractPlanner<RI, AI>, IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> m_objectExtensions;
  private int m_eventBufferLoopDetection;

  public AbstractPlanner() {
    this(true);
  }

  public AbstractPlanner(boolean callInitializer) {
    m_objectExtensions = new ObjectExtensions<AbstractPlanner<RI, AI>, IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>>(this);
    m_resources = new ArrayList<Resource<RI>>();
    m_eventBuffer = createEventBuffer();
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

  @Override
  public AbstractEventBuffer<PlannerEvent> createEventBuffer() {
    return new PlannerEventBuffer();
  }

  protected AbstractEventBuffer<PlannerEvent> getEventBuffer() {
    return m_eventBuffer;
  }

  /*
   * Configuration
   */

  @Order(10)
  protected Set<Integer> getConfiguredAvailableDisplayModes() {
    return CollectionUtility.hashSet(
        DISPLAY_MODE_INTRADAY,
        DISPLAY_MODE_DAY,
        DISPLAY_MODE_WEEK,
        DISPLAY_MODE_MONTH,
        DISPLAY_MODE_WORKWEEK,
        DISPLAY_MODE_CALENDAR_WEEK,
        DISPLAY_MODE_YEAR);
  }

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

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(130)
  protected boolean getConfiguredHeaderVisible() {
    return true;
  }

  @Order(140)
  protected int getConfiguredSelectionMode() {
    return SELECTION_MODE_MULTI_RANGE;
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @ConfigOperation
  @Order(30)
  protected void execDisplayModeChanged(int displayMode) throws ProcessingException {
    Calendar from = Calendar.getInstance();
    DateUtility.truncCalendar(from);
    Calendar to = Calendar.getInstance();
    DateUtility.truncCalendar(to);
    switch (displayMode) {
      case IPlanner.DISPLAY_MODE_INTRADAY:
      case IPlanner.DISPLAY_MODE_DAY:
        to.add(Calendar.DAY_OF_WEEK, 1);
        break;
      case IPlanner.DISPLAY_MODE_WEEK:
        from.set(Calendar.DAY_OF_WEEK, from.getFirstDayOfWeek());
        to.add(Calendar.DAY_OF_WEEK, 7);
        break;
      case IPlanner.DISPLAY_MODE_WORKWEEK:
        from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.add(Calendar.DAY_OF_WEEK, 5);
        break;
      case IPlanner.DISPLAY_MODE_MONTH:
        to.add(Calendar.MONTH, 2);
        break;
      case IPlanner.DISPLAY_MODE_CALENDAR_WEEK:
        to.add(Calendar.MONTH, 9);
        break;
      case IPlanner.DISPLAY_MODE_YEAR:
        to.add(Calendar.YEAR, 2);
        break;
    }
    setViewRange(from.getTime(), to.getTime());
  }

  @ConfigOperation
  @Order(40)
  protected void execViewRangeChanged(Range<Date> viewRange) throws ProcessingException {
  }

  @ConfigOperation
  @Order(50)
  protected void execResourcesSelected(List<Resource<RI>> resources) throws ProcessingException {
  }

  /**
   * @param activityCell
   *          may be null
   */
  @ConfigOperation
  @Order(60)
  protected void execDecorateActivityCell(Activity<RI, AI> cell) throws ProcessingException {
  }

  /**
   * @param activityCell
   *          may be null
   */
  @ConfigOperation
  @Order(70)
  protected void execActivityCellSelected(Activity<RI, AI> cell) throws ProcessingException {
  }

  /**
   * @param activityCell
   *          may be null
   */
//  @ConfigOperation
//  @Order(75)
//  protected void execCellAction(Resource<RI> resource, Activity<RI, AI> activityCell) throws ProcessingException {
//  }

  @ConfigOperation
  @Order(80)
  protected void execInitPlanner() throws ProcessingException {
  }

  @ConfigOperation
  @Order(90)
  protected void execDisposePlanner() throws ProcessingException {
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
//    m_cellObserver = new P_ActivityCellObserver();
    //
    setAvailableDisplayModes(getConfiguredAvailableDisplayModes());
    setHeaderVisible(getConfiguredHeaderVisible());
    setSelectionMode(getConfiguredSelectionMode());
    setWorkDayCount(getConfiguredWorkDayCount());
    setWorkDaysOnly(getConfiguredWorkDaysOnly());
    setFirstHourOfDay(getConfiguredFirstHourOfDay());
    setIntradayInterval(getConfiguredIntradayInterval());
    setMinimumActivityDuration(getConfiguredMinimumActivityDuration());
    setLastHourOfDay(getConfiguredLastHourOfDay());
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
    IPlannerContextMenu contextMenu = new PlannerContextMenu(this, menus.getOrderedList());
    setContextMenu(contextMenu);

    // local property observer
    addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      @SuppressWarnings("unchecked")
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(PROP_SELECTED_RESOURCES)) {
          List<Resource<RI>> resources = (List<Resource<RI>>) e.getNewValue();
          try {
            interceptResourcesSelected(resources);
          }
          catch (Exception t) {
            BEANS.get(ExceptionHandler.class).handle(t);
          }
        }
        else if (e.getPropertyName().equals(PROP_DISPLAY_MODE)) {
          try {
            //FIXME CGU add interceptor
            execDisplayModeChanged((int) e.getNewValue());
          }
          catch (Exception t) {
            BEANS.get(ExceptionHandler.class).handle(t);
          }
        }
        else if (e.getPropertyName().equals(PROP_VIEW_RANGE)) {
          try {
            //FIXME CGU add interceptor
            execViewRangeChanged((Range) e.getNewValue());
          }
          catch (Exception t) {
            BEANS.get(ExceptionHandler.class).handle(t);
          }
        }
        else if (e.getPropertyName().equals(PROP_SELECTED_ACTIVITY)) {
          Activity<RI, AI> cell = (Activity<RI, AI>) e.getNewValue();
          if (cell != null) {
            try {
              interceptActivityCellSelected(cell);
            }
            catch (Exception t) {
              BEANS.get(ExceptionHandler.class).handle(t);
            }
          }
        }
      }
    });
  }

  @Override
  public final List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> createLocalExtension() {
    return new LocalPlannerExtension<RI, AI, AbstractPlanner<RI, AI>>(this);
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

  protected IPlannerUIFacade createUIFacade() {
    return new P_PlannerUIFacade();
  }

  /*
   * Runtime
   */

  @Override
  public final void initPlanner() throws ProcessingException {
    initPlannerInternal();
    interceptInitPlanner();
    // init actions
    ActionUtility.initActions(getMenus());
  }

  protected void initPlannerInternal() throws ProcessingException {
  }

  @Override
  public final void disposePlanner() {
    disposePlannerInternal();
    try {
      interceptDisposePlanner();
    }
    catch (Exception t) {
      LOG.warn(getClass().getName(), t);
    }
  }

  protected void disposePlannerInternal() {
  }

  /*
   * Model Observer
   */
  @Override
  public void addPlannerListener(PlannerListener listener) {
    m_listenerList.add(PlannerListener.class, listener);
  }

  @Override
  public void removePlannerListener(PlannerListener listener) {
    m_listenerList.remove(PlannerListener.class, listener);
  }

//  @Override
//  public ActivityCell<RI, AI> resolveActivityCell(ActivityCell<RI, AI> cell) {
//    if (cell == null) {
//      return cell;
//    }
//    return m_activities.get(new CompositeObject(cell.getResourceId(), cell.getActivityId()));
//  }
//
//  @Override
//  public List<ActivityCell<RI, AI>> resolveActivityCells(List<? extends ActivityCell<RI, AI>> cells) {
//    if (cells == null) {
//      return CollectionUtility.emptyArrayList();
//    }
//    List<ActivityCell<RI, AI>> result = new ArrayList<ActivityCell<RI, AI>>(cells.size());
//    for (ActivityCell<RI, AI> cell : cells) {
//      if (resolveActivityCell(cell) == cell) {
//        result.add(cell);
//      }
//    }
//    return result;
//  }
//
//  @Override
//  public List<ActivityCell<RI, AI>> getActivityCells(Resource<RI> resource) {
//    ArrayList<Resource<RI>> resourceList = new ArrayList<Resource<RI>>();
//    resourceList.add(resource);
//    return getActivityCells(resourceList);
//  }

//  @Override
//  public List<ActivityCell<RI, AI>> getActivityCells(List<Resource<RI>> resources) {
//    List<ActivityCell<RI, AI>> all = new ArrayList<ActivityCell<RI, AI>>();
//    for (Resource<RI> Resource<RI> : resources) {
//      List<ActivityCell<RI, AI>> list = m_resourceToActivities.get(resource);
//      if (list != null) {
//        all.addAll(list);
//      }
//    }
//    return all;
//  }
//
//  @SuppressWarnings("unchecked")
//  @Override
//  public List<ActivityCell<RI, AI>> getAllActivityCells() {
//    return new ArrayList(m_activities.values());
//  }

//
//  @Override
//  public void addActivityCells(List<? extends ActivityCell<RI, AI>> cells) {
//    List<ActivityCell<RI, AI>> addedCells = new ArrayList<ActivityCell<RI, AI>>();
//    for (ActivityCell<RI, AI> cell : cells) {
//      CompositeObject key = new CompositeObject(cell.getResourceId(), cell.getActivityId());
//      if (!m_activities.containsKey(key)) {
//        m_activities.put(key, cell);
//        List<ActivityCell<RI, AI>> list = m_resourceToActivities.get(cell.getResourceId());
//        if (list == null) {
//          list = new ArrayList<ActivityCell<RI, AI>>();
//          m_resourceToActivities.put(cell.getResourceId(), list);
//        }
//        list.add(cell);
//        addedCells.add(cell);
//        decorateActivityCell(cell);
//        cell.setObserver(m_cellObserver);
//      }
//    }
//    if (addedCells.size() > 0) {
//      fireActivitiesInserted(addedCells);
//    }
//  }
//
//  @Override
//  public void updateActivityCells(List<? extends ActivityCell<RI, AI>> cells) {
//    cells = resolveActivityCells(cells);
//    updateActivityCellsInternal(cells);
//  }
//
//  @Override
//  public void updateActivityCellsById(List<Resource<RI>> resources) {
//    updateActivityCellsInternal(getActivityCells(resources));
//  }
//
//  // resolved cells
//  private void updateActivityCellsInternal(List<? extends ActivityCell<RI, AI>> cells) {
//    for (ActivityCell<RI, AI> cell : cells) {
//      decorateActivityCell(cell);
//    }
//    fireActivitiesUpdated(cells);
//  }
//
//  @Override
//  public void removeActivityCells(List<? extends ActivityCell<RI, AI>> cells) {
//    cells = resolveActivityCells(cells);
//    removeActivityCellsInternal(cells);
//  }
//
//  @Override
//  public void removeActivityCellsById(List<Resource<RI>> resources) {
//    removeActivityCellsInternal(getActivityCells(resources));
//  }
//
//  // cells are resolved
//  private void removeActivityCellsInternal(List<? extends ActivityCell<RI, AI>> cells) {
//    if (CollectionUtility.hasElements(cells)) {
//      for (ActivityCell<RI, AI> cell : cells) {
//        cell.setObserver(null);
//        m_activities.remove(new CompositeObject(cell.getResourceId(), cell.getActivityId()));
//        List<ActivityCell<RI, AI>> list = m_resourceToActivities.get(cell.getResourceId());
//        if (list != null) {
//          list.remove(cell);
//        }
//      }
//      fireActivitiesDeleted(cells);
//    }
//  }
//
//  @Override
//  public void removeAllActivityCells() {
//    List<ActivityCell<RI, AI>> a = getAllActivityCells();
//    if (CollectionUtility.hasElements(a)) {
//      for (ActivityCell<RI, AI> cell : a) {
//        cell.setObserver(null);
//      }
//      m_activities.clear();
//      m_resourceToActivities.clear();
//      fireAllActivitiesDeleted(a);
//    }
//  }

  @Override
  public void replaceResources(List<Resource<RI>> resources) {
    deleteAllResources();
    addResources(resources);
  }

  @Override
  public void deleteResources(List<Resource<RI>> resources) {
    setPlannerChanging(true);
    try {
      int resourceCountBefore = m_resources.size();
      List<Resource<RI>> deletedResources = new ArrayList<Resource<RI>>();
      for (Resource<RI> resource : resources) {
        m_resources.remove(resource);
        deletedResources.add(resource);
      }
      if (deletedResources.size() == resourceCountBefore) {
        fireAllResourcesDeleted();
        deselectAllResources();
        setSelectionRange(new Range<Date>());
      }
      else {
        fireResourcesDeleted(deletedResources);
        if (deselectResources(deletedResources)) {
          // Adjust selection range too if selected resources were deleted
          setSelectionRange(new Range<Date>());
        }
      }
    }
    finally {
      setPlannerChanging(false);
    }
  }

  @Override
  public void deleteAllResources() {
    setPlannerChanging(true);
    try {
      deleteResources(getResources());
    }
    finally {
      setPlannerChanging(false);
    }
  }

  @Override
  public void addResources(List<Resource<RI>> resources) {
    setPlannerChanging(true);
    try {
      //FIXME CGU copy?
      for (Resource<RI> resource : resources) {
        for (Activity<?, ?> activity : resource.getActivities()) {
          @SuppressWarnings("unchecked")
          // FIXME CGU Fix generics
          Activity<RI, AI> castedActivity = (Activity<RI, AI>) activity;
          decorateActivityCell(castedActivity);
        }
        m_resources.add(resource);
      }
      fireResourcesInserted(resources);
    }
    finally {
      setPlannerChanging(false);
    }
  }

  public void updateResource(Resource<RI> resource) throws ProcessingException {
    updateResources(CollectionUtility.arrayList(resource));
  }

  public void updateResources(List<Resource<RI>> resources) throws ProcessingException {
    if (resources == null || resources.size() == 0) {
      return;
    }

    setPlannerChanging(true);
    try {
      fireResourcesUpdated(resources);
    }
    finally {
      setPlannerChanging(false);
    }
  }

  @Override
  public List<Resource<RI>> getResources() {
    return CollectionUtility.arrayList(m_resources);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Activity<RI, AI> getSelectedActivity() {
    return (Activity<RI, AI>) propertySupport.getProperty(PROP_SELECTED_ACTIVITY);
  }

  @Override
  public void setSelectedActivityCell(Activity<RI, AI> cell) {
//    cell = resolveActivityCell(cell);
    propertySupport.setProperty(PROP_SELECTED_ACTIVITY, cell);
  }

  @Override
  public boolean isSelectedActivityCell(Activity<RI, AI> cell) {
    return getSelectedActivity() == cell;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Resource<RI>> getSelectedResources() {
    List<Resource<RI>> a = (List<Resource<RI>>) propertySupport.getProperty(PROP_SELECTED_RESOURCES);
    if (a == null) {
      a = CollectionUtility.emptyArrayList();
    }
    return a;
  }

  @Override
  public List<RI> getSelectedResourceIds() {
    List<RI> ids = new ArrayList<RI>();
    List<Resource<RI>> resources = getSelectedResources();
    for (Resource<RI> resource : resources) {
      ids.add(resource.getId());
    }
    return ids;
  }

  @Override
  public void deselectAllResources() {
    setSelectedResources(new ArrayList<Resource<RI>>());
  }

  @Override
  public boolean deselectResources(List<? extends Resource> resources) {
    List<Resource<RI>> selectedResources = getSelectedResources();
    boolean selectionChanged = selectedResources.removeAll(resources);
    if (selectionChanged) {
      setSelectedResources(selectedResources);
    }
    return selectionChanged;
  }

  @Override
  public void setSelectedResources(List<? extends Resource<RI>> resources) {
    List<Resource<RI>> internalResources = CollectionUtility.arrayList(resources);
    propertySupport.setProperty(PROP_SELECTED_RESOURCES, internalResources);

    // check whether current selected activity cell needs to be updated
    if (CollectionUtility.size(internalResources) != 1) {
      // at most one activity cell might be selected
      setSelectedActivityCell(null);
      return;
    }

    Activity<RI, AI> selectedCell = getSelectedActivity();
    if (selectedCell == null) {
      // nothing selected
      return;
    }

    Resource<RI> resource = CollectionUtility.firstElement(resources);
    if (CompareUtility.notEquals(resource, selectedCell.getResourceId())) {
      // selected cell does not belong to selected resources
      setSelectedActivityCell(null);
    }
  }

  @Override
  public void isSelectedResource(Resource<RI> resource) {
    getSelectedResources().contains(resource);
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

  protected void setContextMenu(IPlannerContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public IPlannerContextMenu getContextMenu() {
    return (IPlannerContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public <T extends IMenu> T getMenuByClass(Class<T> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

//  private void fireCellAction(Resource<RI> resource, Activity<RI, AI> activityCell) {
//    // single observer
//    try {
//      interceptCellAction(resource, activityCell);
//    }
//    catch (Exception e) {
//      BEANS.get(ExceptionHandler.class).handle(e);
//    }
//    PlannerEvent e = new PlannerEvent(this, PlannerEvent.TYPE_ACTIVITY_ACTION, resource, activityCell);
//    firePlannerEventInternal(e);
//  }

  private void fireResourcesInserted(List<Resource<RI>> resources) {
    PlannerEvent e = new PlannerEvent(this, PlannerEvent.TYPE_RESOURCES_INSERTED, resources);
    firePlannerEventInternal(e);
  }

  private void fireResourcesUpdated(List<Resource<RI>> resources) {
    PlannerEvent e = new PlannerEvent(this, PlannerEvent.TYPE_RESOURCES_UPDATED, resources);
    firePlannerEventInternal(e);
  }

  private void fireResourcesDeleted(List<Resource<RI>> resources) {
    PlannerEvent e = new PlannerEvent(this, PlannerEvent.TYPE_RESOURCES_DELETED, resources);
    firePlannerEventInternal(e);
  }

  private void fireAllResourcesDeleted() {
    PlannerEvent e = new PlannerEvent(this, PlannerEvent.TYPE_ALL_RESOURCES_DELETED);
    firePlannerEventInternal(e);
  }

  // main handler
  private void firePlannerEventInternal(PlannerEvent e) {
    if (isPlannerChanging()) {
      // buffer the event for later batch firing
      getEventBuffer().add(e);
    }
    else {
      EventListener[] listeners = m_listenerList.getListeners(PlannerListener.class);
      if (listeners != null && listeners.length > 0) {
        for (int i = 0; i < listeners.length; i++) {
          ((PlannerListener) listeners[i]).plannerChanged(e);
        }
      }
    }
  }

  // batch handler
  private void firePlannerEventBatchInternal(List<PlannerEvent> batch) {
    if (CollectionUtility.hasElements(batch)) {
      EventListener[] listeners = m_listenerList.getListeners(PlannerListener.class);
      for (EventListener l : listeners) {
        ((PlannerListener) l).plannerChangedBatch(batch);
      }
    }
  }

  /**
   * Fires events in form in of one batch <br>
   * Unnecessary events are removed or merged.
   */
  private void processEventBuffer() {
    //loop detection
    try {
      m_eventBufferLoopDetection++;
      if (m_eventBufferLoopDetection > 100) {
        LOG.error("LOOP DETECTION in " + getClass() + ". see stack trace for more details.", new Exception("LOOP DETECTION"));
        return;
      }
      //
      if (!getEventBuffer().isEmpty()) {
        List<PlannerEvent> coalescedEvents = getEventBuffer().consumeAndCoalesceEvents();
        // fire the batch and set planner to changing, otherwise a listener might trigger another events that
        // then are processed before all other listeners received that batch
        try {
          setPlannerChanging(true);
          //
          firePlannerEventBatchInternal(coalescedEvents);
        }
        finally {
          setPlannerChanging(false);
        }
      }
    }
    finally {
      m_eventBufferLoopDetection--;
    }
  }

  @Override
  public boolean isPlannerChanging() {
    return m_tableChanging > 0;
  }

  @Override
  public void setPlannerChanging(boolean b) {
    // use a stack counter because setPlannerChanging might be called in
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
            processEventBuffer();
          }
          finally {
            propertySupport.setPropertiesChanging(false);
          }
        }
      }
    }
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
  public void setHeaderVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_HEADER_VISIBLE, visible);
  }

  @Override
  public boolean isHeaderVisible() {
    return propertySupport.getPropertyBool(PROP_HEADER_VISIBLE);
  }

  @Override
  public int getDisplayMode() {
    return propertySupport.getPropertyInt(PROP_DISPLAY_MODE);
  }

  @Override
  public void setDisplayMode(int mode) {
    propertySupport.setPropertyInt(PROP_DISPLAY_MODE, mode);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<Integer> getAvailableDisplayModes() {
    return (Set<Integer>) propertySupport.getProperty(PROP_AVAILABLE_DISPLAY_MODES);
  }

  @Override
  public void setAvailableDisplayModes(Set<Integer> modes) {
    propertySupport.setProperty(PROP_AVAILABLE_DISPLAY_MODES, modes);
  }

  @Override
  public int getSelectionMode() {
    return propertySupport.getPropertyInt(PROP_SELECTION_MODE);
  }

  @Override
  public void setSelectionMode(int mode) {
    propertySupport.setPropertyInt(PROP_SELECTION_MODE, mode);
  }

  @Override
  public Range<Date> getViewRange() {
    @SuppressWarnings("unchecked")
    Range<Date> propValue = (Range<Date>) propertySupport.getProperty(PROP_VIEW_RANGE);
    // return a copy
    return new Range<Date>(propValue);
  }

  @Override
  public void setViewRange(Date minDate, Date maxDate) {
    setViewRange(new Range<Date>(minDate, maxDate));
  }

  @Override
  public void setViewRange(Range<Date> viewRange) {
    LOG.debug("Setting selection range to " + viewRange);
    propertySupport.setProperty(PROP_VIEW_RANGE, viewRange);
  }

  @Override
  public void setSelectionRange(Date beginDate, Date endDate) {
    setSelectionRange(new Range<Date>(beginDate, endDate));
  }

  @Override
  public void setSelectionRange(Range<Date> selectionRange) {
    try {
      setPlannerChanging(true);
      //
      LOG.debug("Seting selection range to " + selectionRange);
      propertySupport.setProperty(PROP_SELECTION_RANGE, selectionRange);

      // update selected activity cell based on selected time range.
      // this is a work around for enforcing an owner value change event on context menus. Actually the empty selection event
      // should be handled correctly in the GUI.
      List<Resource<RI>> selectedResources = getSelectedResources();
      if (selectedResources.size() == 1) {
        Resource<RI> resource = CollectionUtility.firstElement(selectedResources);
        List<Activity<?, ?>> activityCells = resource.getActivities();
        for (Activity<?, ?> cell : activityCells) {

          if (CompareUtility.equals(cell.getBeginTime(), selectionRange.getFrom()) &&
              (CompareUtility.equals(cell.getEndTime(), selectionRange.getTo())
              // see TimeScaleBuilder, end time is sometimes actual end time minus 1ms
              || (cell != null
                  && cell.getEndTime() != null
                  && selectionRange.getTo() != null
                  && cell.getEndTime().getTime() == selectionRange.getTo().getTime() + 1))) {
//                setSelectedActivityCell(cell);
            return;
          }
        }
      }
      setSelectedActivityCell(null);
    }
    finally {
      setPlannerChanging(false);
    }
  }

  @Override
  public Range<Date> getSelectionRange() {
    @SuppressWarnings("unchecked")
    Range<Date> propValue = (Range<Date>) propertySupport.getProperty(PROP_SELECTION_RANGE);
    // return a copy
    return new Range<Date>(propValue);
  }

  @Override
  public Date getSelectedBeginTime() {
    return getSelectionRange().getFrom();
  }

  @Override
  public Date getSelectedEndTime() {
    return getSelectionRange().getTo();
  }

  @Override
  public Object getContainer() {
    return propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls an {@link IPlanner}
   */
  public void setContainerInternal(Object container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  @Override
  public void decorateActivityCell(Activity<RI, AI> cell) {
    try {
//      cell.setObserver(null);
      //
      decorateActivityCellInternal(cell);
      interceptDecorateActivityCell(cell);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
    finally {
//      cell.setObserver(m_cellObserver);
    }
  }

  protected void decorateActivityCellInternal(Activity<RI, AI> p) {
  }

  @Override
  public IPlannerUIFacade getUIFacade() {
    return m_activityMapUIFacade;
  }

  /**
   * Private planned activity cell observer
   */
//  private class P_ActivityCellObserver implements IActivityCellObserver<RI, AI> {
//
//    @Override
//    public void cellChanged(ActivityCell<RI, AI> cell, int bitPos) {
//      ArrayList<ActivityCell<RI, AI>> cellList = new ArrayList<ActivityCell<RI, AI>>(1);
//      cellList.add(cell);
//      fireActivitiesUpdated(cellList);
//    }
//  }

  /**
   * Specialized ui facade
   */
  private class P_PlannerUIFacade implements IPlannerUIFacade<RI, AI> {

    @Override
    public void setSelectionFromUI(List<? extends Resource<RI>> resources, Range<Date> selectionRange) {
      try {
        setPlannerChanging(true);
        setSelectedResources(resources);
        setSelectionRange(selectionRange);
      }
      finally {
        setPlannerChanging(false);
      }
    }

    @Override
    public void setDisplayModeFromUI(int displayMode) {
      setDisplayMode(displayMode);
    }

    @Override
    public void setSelectedActivityCellFromUI(Activity<RI, AI> cell) {
      setSelectedActivityCell(cell);
    }

//    @Override
//    public void fireCellActionFromUI(Resource<RI> resource, Activity<RI, AI> activityCell) {
//      if (activityCell != null) {
//        setSelectedActivityCell(activityCell);
//      }
//      fireCellAction(resource, activityCell);
//    }
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalPlannerExtension<RI, AI, OWNER extends AbstractPlanner<RI, AI>> extends AbstractExtension<OWNER> implements IPlannerExtension<RI, AI, OWNER> {

    public LocalPlannerExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execActivityCellSelected(PlannerActivityCellSelectedChain<RI, AI> chain, Activity<RI, AI> cell) throws ProcessingException {
      getOwner().execActivityCellSelected(cell);
    }

    @Override
    public void execDisposePlanner(PlannerDisposePlannerChain<RI, AI> chain) throws ProcessingException {
      getOwner().execDisposePlanner();
    }

    @Override
    public void execDecorateActivityCell(PlannerDecorateActivityCellChain<RI, AI> chain, Activity<RI, AI> cell) throws ProcessingException {
      getOwner().execDecorateActivityCell(cell);
    }

    @Override
    public void execInitPlanner(PlannerInitPlannerChain<RI, AI> chain) throws ProcessingException {
      getOwner().execInitPlanner();
    }

    @Override
    public void execCellAction(PlannerCellActionChain<RI, AI> chain, Resource<RI> resource, Activity<RI, AI> activityCell) throws ProcessingException {
      //FIXME CGU check if still needed, better replace with menus
//      getOwner().execCellAction(resource, activityCell);
    }

  }

  protected final void interceptResourcesSelected(List<Resource<RI>> resources) throws ProcessingException {
    execResourcesSelected(resources);
    //FIXME CGU implement
//    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
//    PlannerActivityCellSelectedChain<RI, AI> chain = new PlannerActivityCellSelectedChain<RI, AI>(extensions);
//    chain.execActivityCellSelected(cell);
  }

  protected final void interceptActivityCellSelected(Activity<RI, AI> cell) throws ProcessingException {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerActivityCellSelectedChain<RI, AI> chain = new PlannerActivityCellSelectedChain<RI, AI>(extensions);
    chain.execActivityCellSelected(cell);
  }

  protected final void interceptDisposePlanner() throws ProcessingException {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerDisposePlannerChain<RI, AI> chain = new PlannerDisposePlannerChain<RI, AI>(extensions);
    chain.execDisposePlanner();
  }

  protected final void interceptDecorateActivityCell(Activity<RI, AI> cell) throws ProcessingException {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerDecorateActivityCellChain<RI, AI> chain = new PlannerDecorateActivityCellChain<RI, AI>(extensions);
    chain.execDecorateActivityCell(cell);
  }

  protected final void interceptInitPlanner() throws ProcessingException {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerInitPlannerChain<RI, AI> chain = new PlannerInitPlannerChain<RI, AI>(extensions);
    chain.execInitPlanner();
  }

  protected final void interceptCellAction(Resource<RI> resource, Activity<RI, AI> activityCell) throws ProcessingException {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerCellActionChain<RI, AI> chain = new PlannerCellActionChain<RI, AI>(extensions);
    chain.execCellAction(resource, activityCell);
  }

}

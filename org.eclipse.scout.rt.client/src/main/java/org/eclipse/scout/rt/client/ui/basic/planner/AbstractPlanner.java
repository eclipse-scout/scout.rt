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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
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
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.Resource;
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
   * Indicates whether this Planner should draw the
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
        else if (e.getPropertyName().equals(PROP_SELECTED_ACTIVITY_CELL)) {
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
      }
      else {
        fireResourcesDeleted(deletedResources);
        deselectResources(deletedResources);
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
          decorateActivityCell((Activity<RI, AI>) activity);
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
  public Activity<RI, AI> getSelectedActivityCell() {
    return (Activity<RI, AI>) propertySupport.getProperty(PROP_SELECTED_ACTIVITY_CELL);
  }

  @Override
  public void setSelectedActivityCell(Activity<RI, AI> cell) {
//    cell = resolveActivityCell(cell);
    propertySupport.setProperty(PROP_SELECTED_ACTIVITY_CELL, cell);
  }

  @Override
  public boolean isSelectedActivityCell(Activity<RI, AI> cell) {
    return getSelectedActivityCell() == cell;
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
  public void deselectResources(List<? extends Resource> resources) {
    List<Resource<RI>> selectedResources = getSelectedResources();
    boolean selectionChanged = selectedResources.removeAll(resources);
    if (selectionChanged) {
      setSelectedResources(selectedResources);
    }
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

    Activity<RI, AI> selectedCell = getSelectedActivityCell();
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
  public Date getBeginTime() {
    Calendar cal = Calendar.getInstance();
    Date[] a = getDays();
    if (a.length > 0) {
      cal.setTime(a[0]);
    }
    else {
      cal.setTime(DateUtility.truncDate(new Date()));
    }
    switch (getDisplayMode()) {
      case DISPLAY_MODE_INTRADAY: {
        cal.set(Calendar.HOUR_OF_DAY, getFirstHourOfDay());
        break;
      }
      case DISPLAY_MODE_DAY: {
        break;
      }
      case DISPLAY_MODE_WEEK: {
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
    switch (getDisplayMode()) {
      case DISPLAY_MODE_INTRADAY: {
        cal.set(Calendar.HOUR_OF_DAY, getLastHourOfDay());
        break;
      }
      case DISPLAY_MODE_DAY: {
        cal.add(Calendar.DATE, 1);
        break;
      }
      case DISPLAY_MODE_WEEK: {
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
  public int getDisplayMode() {
    return propertySupport.getPropertyInt(PROP_DISPLAY_MODE);
  }

  @Override
  public void setDisplayMode(int mode) {
    propertySupport.setPropertyInt(PROP_DISPLAY_MODE, mode);
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
      setPlannerChanging(true);
      //
      propertySupport.setProperty(PROP_SELECTED_BEGIN_TIME, beginTime);
      propertySupport.setProperty(PROP_SELECTED_END_TIME, endTime);

      // update selected activity cell based on selected time range.
      // this is a work around for enforcing an owner value change event on context menus. Actually the empty selection event
      // should be handled correctly in the GUI.
      List<Resource<RI>> selectedResources = getSelectedResources();
      if (selectedResources.size() == 1) {
        Resource<RI> resource = CollectionUtility.firstElement(selectedResources);
        List<Activity<?, ?>> activityCells = resource.getActivities();
        for (Activity<?, ?> cell : activityCells) {

          if (CompareUtility.equals(cell.getBeginTime(), beginTime) &&
              (CompareUtility.equals(cell.getEndTime(), endTime)
                  // see TimeScaleBuilder, end time is sometimes actual end time minus 1ms
                  || (cell != null
                  && cell.getEndTime() != null
                  && endTime != null
                  && cell.getEndTime().getTime() == endTime.getTime() + 1))) {
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
    SortedMap<CompositeObject, Activity<Integer, Integer>> sortMap = new TreeMap<>();
    Random rnd = new Random();
    int resourceIndex = 0;
    for (Resource<RI> resource : getSelectedResources()) {
      MultiTimeRange localTimeRanges = (MultiTimeRange) multiTimeRange.clone();
      for (Activity<?, ?> a : resource.getActivities()) {
        localTimeRanges.remove(a.getBeginTime(), a.getEndTime());
      }
      // now only available time ranges for that Resource<RI> are left
      for (TimeRange tr : localTimeRanges.getTimeRanges()) {
        long durationMillis = tr.getDurationMillis();
        long sortNo = chooseRandom ? rnd.nextLong() : resourceIndex;
        if (durationMillis >= preferredDuration) {
          Activity<Integer, Integer> a = new Activity<Integer, Integer>(0, 0);
          a.setBeginTime(tr.getFrom());
          a.setEndTime(new Date(tr.getFrom().getTime() + preferredDuration));
          sortMap.put(new CompositeObject(0, a.getBeginTime().getTime(), sortNo), a);
        }
        else if (durationMillis >= 15L * 60L * 1000L) {// at least 15 minutes
          Activity<Integer, Integer> a = new Activity<Integer, Integer>(0, 0);
          a.setBeginTime(tr.getFrom());
          a.setEndTime(new Date(tr.getFrom().getTime() + durationMillis));
          sortMap.put(new CompositeObject(1, -durationMillis, sortNo), a);
        }
      }
      resourceIndex++;
    }
    // the top entry of the sort map is the one with best score (lowest number)
    if (!sortMap.isEmpty()) {
      Activity<Integer, Integer> a = sortMap.get(sortMap.firstKey());
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
    TreeMap<CompositeObject, Activity<Integer, Integer>> sortMap = new TreeMap<CompositeObject, Activity<Integer, Integer>>();
    for (Resource<RI> resource : getSelectedResources()) {
      for (Activity<?, ?> a : resource.getActivities()) {
        multiTimeRange.remove(a.getBeginTime(), a.getEndTime());
      }
    }
    // now only available time ranges for that resource are left
    for (TimeRange tr : multiTimeRange.getTimeRanges()) {
      long durationMillis = tr.getDurationMillis();
      if (durationMillis >= preferredDuration) {
        Activity<Integer, Integer> a = new Activity<Integer, Integer>(0, 0);
        a.setBeginTime(tr.getFrom());
        a.setEndTime(new Date(tr.getFrom().getTime() + preferredDuration));
        sortMap.put(new CompositeObject(0, a.getBeginTime().getTime()), a);
      }
      else if (durationMillis >= 15L * 60L * 1000L) {// at least 15 minutes
        Activity<Integer, Integer> a = new Activity<Integer, Integer>(0, 0);
        a.setBeginTime(tr.getFrom());
        a.setEndTime(new Date(tr.getFrom().getTime() + durationMillis));
        sortMap.put(new CompositeObject(1, -durationMillis), a);
      }
    }
    // the top entry of the sort map is the one with best score (lowest number)
    if (!sortMap.isEmpty()) {
      Activity<Integer, Integer> a = sortMap.get(sortMap.firstKey());
      setSelectedTime(a.getBeginTime(), a.getEndTime());
    }
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
    public void setSelectionFromUI(List<? extends Resource<RI>> resources, Date beginTime, Date endTime) {
      try {
        setPlannerChanging(true);
        setSelectedResources(resources);
        setSelectedTime(beginTime, endTime);
      }
      finally {
        setPlannerChanging(false);
      }
    }

    @Override
    public void setDaysFromUI(Date[] days) {
      setDays(days);
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

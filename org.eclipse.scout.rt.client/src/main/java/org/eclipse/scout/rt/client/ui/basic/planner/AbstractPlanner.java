/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.IPlannerExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerActivitySelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDecorateActivityChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisplayModeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisposePlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerInitPlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerResourcesSelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerSelectionRangeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerViewRangeChangedChain;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IPlannerContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.PlannerContextMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("38cbdf76-8742-4c1c-89b3-ef994391a7a5")
public abstract class AbstractPlanner<RI, AI> extends AbstractWidget implements IPlanner<RI, AI>, IContributionOwner, IExtensibleObject {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractPlanner.class);

  private FastListenerList<PlannerListener> m_listenerList;
  private IPlannerUIFacade m_activityMapUIFacade;
  private final List<Resource<RI>> m_resources;
  private List<Resource<RI>> m_selectedResources = new ArrayList<>();
  private int m_tableChanging;
  private final AbstractEventBuffer<PlannerEvent> m_eventBuffer;
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractPlanner<RI, AI>, IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> m_objectExtensions;
  private int m_eventBufferLoopDetection;
  private IResourceObserver<RI> m_resourceObserver = new P_ResourceObserver();

  public AbstractPlanner() {
    this(true);
  }

  public AbstractPlanner(boolean callInitializer) {
    super(false);
    m_objectExtensions = new ObjectExtensions<>(this, false);
    m_resources = new ArrayList<>();
    m_eventBuffer = createEventBuffer();
    m_resourceObserver = new P_ResourceObserver();
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

  @Override
  public final <T> T optContribution(Class<T> contribution) {
    return m_contributionHolder.optContribution(contribution);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getMenus());
  }

  @Override
  protected void initConfigInternal() {
    m_objectExtensions.initConfig(createLocalExtension(), this::initConfig);
  }

  @Override
  public AbstractEventBuffer<PlannerEvent> createEventBuffer() {
    return BEANS.get(PlannerEventBuffer.class);
  }

  protected AbstractEventBuffer<PlannerEvent> getEventBuffer() {
    return m_eventBuffer;
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.STRING)
  @Order(5)
  protected String getConfiguredLabel() {
    return null;
  }

  @Order(10)
  protected Set<Integer> getConfiguredAvailableDisplayModes() {
    return CollectionUtility.hashSet(
        IPlannerDisplayMode.DAY,
        IPlannerDisplayMode.WEEK,
        IPlannerDisplayMode.MONTH,
        IPlannerDisplayMode.WORK_WEEK,
        IPlannerDisplayMode.CALENDAR_WEEK,
        IPlannerDisplayMode.YEAR);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  protected boolean getConfiguredHeaderVisible() {
    return true;
  }

  @Order(170)
  protected int getConfiguredSelectionMode() {
    return SELECTION_MODE_MULTI_RANGE;
  }

  @Order(175)
  protected boolean getConfiguredActivitySelectable() {
    return false;
  }

  @Order(180)
  protected int getConfiguredDisplayMode() {
    return IPlannerDisplayMode.CALENDAR_WEEK;
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @ConfigOperation
  @Order(30)
  protected void execDisplayModeChanged(int displayMode) {
    Calendar from = Calendar.getInstance();
    Calendar to = Calendar.getInstance();
    if (getViewRange() != null && getViewRange().getFrom() != null) {
      from.setTime(getViewRange().getFrom());
      to.setTime(getViewRange().getFrom());
    }
    DateUtility.truncCalendar(from);
    DateUtility.truncCalendar(to);
    switch (displayMode) {
      case IPlannerDisplayMode.DAY:
        to.add(Calendar.DAY_OF_WEEK, 1);
        break;
      case IPlannerDisplayMode.WEEK:
        from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.add(Calendar.DAY_OF_WEEK, 7);
        break;
      case IPlannerDisplayMode.WORK_WEEK:
        from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.add(Calendar.DAY_OF_WEEK, 5);
        break;
      case IPlannerDisplayMode.MONTH:
        from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.add(Calendar.MONTH, 2);
        break;
      case IPlannerDisplayMode.CALENDAR_WEEK:
        from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        to.add(Calendar.MONTH, 9);
        break;
      case IPlannerDisplayMode.YEAR:
        to.add(Calendar.YEAR, 2);
        break;
    }
    setViewRange(from.getTime(), to.getTime());
  }

  @ConfigOperation
  @Order(40)
  protected void execViewRangeChanged(Range<Date> viewRange) {
  }

  @ConfigOperation
  @Order(50)
  protected void execResourcesSelected(List<Resource<RI>> resources) {
  }

  @ConfigOperation
  @Order(60)
  protected void execSelectionRangeChanged(Range<Date> selectionRange) {
  }

  /**
   * @param activity
   *          may be null
   */
  @ConfigOperation
  @Order(70)
  protected void execActivitySelected(Activity<RI, AI> activity) {
  }

  /**
   * @param activity
   *          may be null
   */
  @ConfigOperation
  @Order(80)
  protected void execDecorateActivity(Activity<RI, AI> activity) {
  }

  @ConfigOperation
  @Order(90)
  protected void execInitPlanner() {
  }

  @ConfigOperation
  @Order(100)
  protected void execDisposePlanner() {
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initConfig() {
    super.initConfig();
    m_listenerList = new FastListenerList<>();
    m_activityMapUIFacade = createUIFacade();
    //
    setLabel(getConfiguredLabel());
    setAvailableDisplayModes(getConfiguredAvailableDisplayModes());
    setDisplayMode(getConfiguredDisplayMode());
    setDisplayModeOptions(new HashMap<>());
    initDisplayModeOptions();
    setHeaderVisible(getConfiguredHeaderVisible());
    setSelectionMode(getConfiguredSelectionMode());
    setActivitySelectable(getConfiguredActivitySelectable());
    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    OrderedCollection<IMenu> menus = new OrderedCollection<>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      IMenu menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
      menus.addOrdered(menu);
    }
    m_contributionHolder = new ContributionComposite(this);
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    menus.addAllOrdered(contributedMenus);

    try {
      injectMenusInternal(menus);
    }
    catch (Exception e) {
      LOG.error("Error occurred while dynamically contributing menus.", e);
    }
    new MoveActionNodesHandler<>(menus).moveModelObjects();
    IPlannerContextMenu contextMenu = new PlannerContextMenu(this, menus.getOrderedList());
    setContextMenu(contextMenu);

    // local property observer
    addPlannerListener(new PlannerAdapter() {
      @Override
      public void plannerChanged(PlannerEvent e) {
        if (e.getType() == PlannerEvent.TYPE_RESOURCES_SELECTED) {
          List<Resource<RI>> resources = (List<Resource<RI>>) e.getResources();
          try {
            interceptResourcesSelected(resources);
          }
          catch (Exception t) {
            BEANS.get(ExceptionHandler.class).handle(t);
          }
        }
      }
    });
    addPropertyChangeListener(e -> {
      switch (e.getPropertyName()) {
        case PROP_DISPLAY_MODE:
          try {
            interceptDisplayModeChanged((int) e.getNewValue());
          }
          catch (Exception t) {
            BEANS.get(ExceptionHandler.class).handle(t);
          }
          break;
        case PROP_VIEW_RANGE:
          try {
            interceptViewRangeChanged((Range<Date>) e.getNewValue());
          }
          catch (Exception t) {
            BEANS.get(ExceptionHandler.class).handle(t);
          }
          break;
        case PROP_SELECTION_RANGE:
          try {
            interceptSelectionRangeChanged((Range<Date>) e.getNewValue());
          }
          catch (Exception t) {
            BEANS.get(ExceptionHandler.class).handle(t);
          }
          break;
        case PROP_SELECTED_ACTIVITY:
          Activity<RI, AI> cell = (Activity<RI, AI>) e.getNewValue();
          if (cell != null) {
            try {
              interceptActivitySelected(cell);
            }
            catch (Exception t) {
              BEANS.get(ExceptionHandler.class).handle(t);
            }
          }
          break;
      }
    });
  }

  protected void initDisplayModeOptions() {
    setDisplayModeOption(IPlannerDisplayMode.DAY, new DisplayModeOptions().withInterval(30));
    setDisplayModeOption(IPlannerDisplayMode.WEEK, new DisplayModeOptions().withInterval(360));
    setDisplayModeOption(IPlannerDisplayMode.WORK_WEEK, new DisplayModeOptions().withInterval(360));
    setDisplayModeOption(IPlannerDisplayMode.MONTH, new DisplayModeOptions().withLabelPeriod(2));
    setDisplayModeOption(IPlannerDisplayMode.CALENDAR_WEEK, new DisplayModeOptions());
    setDisplayModeOption(IPlannerDisplayMode.YEAR, new DisplayModeOptions());
  }

  @Override
  public final List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> createLocalExtension() {
    return new LocalPlannerExtension<>(this);
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
  protected final void initInternal() {
    super.initInternal();
    initPlannerInternal();
    interceptInitPlanner();
  }

  protected void initPlannerInternal() {
    // NOP
  }

  @Override
  protected final void disposeInternal() {
    disposePlannerInternal();
    try {
      interceptDisposePlanner();
    }
    catch (Exception t) {
      LOG.warn(getClass().getName(), t);
    }
    super.disposeInternal();
  }

  protected void disposePlannerInternal() {
    // nop
  }

  /**
   * Model Observer
   */
  @Override
  public IFastListenerList<PlannerListener> plannerListeners() {
    return m_listenerList;
  }

  @Override
  public void replaceResources(List<Resource<RI>> resources) {
    setPlannerChanging(true);
    try {
      List<RI> selectedResourceIds = getSelectedResourceIds();
      deleteAllResources();
      addResources(resources);
      restoreSelection(selectedResourceIds);
    }
    finally {
      setPlannerChanging(false);
    }
  }

  private void restoreSelection(List<RI> selectedIds) {
    List<Resource<RI>> selectedResources = new ArrayList<>();
    if (!selectedIds.isEmpty()) {
      for (Resource<RI> resource : getResources()) {
        if (selectedIds.remove(resource.getId())) {
          selectedResources.add(resource);
          if (selectedIds.isEmpty()) {
            break;
          }
        }
      }
    }
    selectResources(selectedResources);
  }

  @Override
  public void deleteResource(Resource<RI> resource) {
    deleteResources(CollectionUtility.arrayList(resource));
  }

  @Override
  public void deleteResources(List<Resource<RI>> resources) {
    setPlannerChanging(true);
    try {
      int resourceCountBefore = m_resources.size();
      List<Resource<RI>> deletedResources = new ArrayList<>();
      for (Resource<RI> resource : resources) {
        m_resources.remove(resource);
        deletedResources.add(resource);
      }
      if (deletedResources.size() == resourceCountBefore) {
        fireAllResourcesDeleted();
        deselectAllResources();
        setSelectionRange(new Range<>());
      }
      else {
        fireResourcesDeleted(deletedResources);
        if (deselectResources(deletedResources)) {
          // Adjust selection range too if selected resources were deleted
          setSelectionRange(new Range<>());
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
      for (Resource<RI> resource : resources) {
        decorateResource(resource);
        resource.setObserver(m_resourceObserver);
        m_resources.add(resource);
      }
      fireResourcesInserted(resources);
    }
    finally {
      setPlannerChanging(false);
    }
  }

  @Override
  public void addResource(Resource<RI> resource) {
    addResources(CollectionUtility.arrayList(resource));
  }

  public void updateResource(Resource<RI> resource) {
    updateResources(CollectionUtility.arrayList(resource));
  }

  public void updateResources(List<Resource<RI>> resources) {
    if (resources == null || resources.isEmpty()) {
      return;
    }
    for (Resource<RI> resource : resources) {
      decorateResource(resource);
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
  public void setSelectedActivity(Activity<RI, AI> activity) {
    propertySupport.setProperty(PROP_SELECTED_ACTIVITY, activity);
  }

  @Override
  public boolean isSelectedActivity(Activity<RI, AI> activity) {
    return getSelectedActivity() == activity;
  }

  @Override
  public Resource<RI> getSelectedResource() {
    return CollectionUtility.firstElement(m_selectedResources);
  }

  @Override
  public List<Resource<RI>> getSelectedResources() {
    return CollectionUtility.arrayList(m_selectedResources);
  }

  @Override
  public List<RI> getSelectedResourceIds() {
    List<RI> ids = new ArrayList<>();
    List<Resource<RI>> resources = getSelectedResources();
    for (Resource<RI> resource : resources) {
      ids.add(resource.getId());
    }
    return ids;
  }

  @Override
  public void deselectAllResources() {
    selectResources(new ArrayList<>());
  }

  @Override
  public boolean deselectResource(Resource<RI> resource) {
    return deselectResources(CollectionUtility.arrayList(resource));
  }

  @Override
  public boolean deselectResources(List<? extends Resource<RI>> resources) {
    List<Resource<RI>> selectedResources = getSelectedResources();
    boolean selectionChanged = selectedResources.removeAll(resources);
    if (selectionChanged) {
      selectResources(selectedResources);
    }
    return selectionChanged;
  }

  @Override
  public void selectResource(Resource<RI> resource) {
    selectResources(CollectionUtility.arrayList(resource));
  }

  @Override
  public void selectResources(List<? extends Resource<RI>> resources) {
    setPlannerChanging(true);
    try {
      List<Resource<RI>> newSelection = new ArrayList<>(resources);
      if (newSelection.size() > 1 && !isMultiSelectResources()) {
        Resource<RI> first = newSelection.get(0);
        newSelection.clear();
        newSelection.add(first);
      }
      if (!CollectionUtility.equalsCollection(m_selectedResources, newSelection, false)) {
        m_selectedResources = new ArrayList<>(newSelection);
        List<Resource<RI>> notificationCopy = CollectionUtility.arrayList(m_selectedResources);

        fireResourcesSelected(notificationCopy);
      }
    }
    finally {
      setPlannerChanging(false);
    }
  }

  @Override
  public boolean isSelectedResource(Resource<RI> resource) {
    return getSelectedResources().contains(resource);
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

  private void fireResourcesSelected(List<Resource<RI>> resources) {
    PlannerEvent e = new PlannerEvent(this, PlannerEvent.TYPE_RESOURCES_SELECTED, resources);
    firePlannerEventInternal(e);
  }

  // main handler
  private void firePlannerEventInternal(PlannerEvent e) {
    if (isPlannerChanging()) {
      // buffer the event for later batch firing
      getEventBuffer().add(e);
    }
    else {
      plannerListeners().list().forEach(listener -> listener.plannerChanged(e));
    }
  }

  // batch handler
  private void firePlannerEventBatchInternal(List<PlannerEvent> batch) {
    if (CollectionUtility.hasElements(batch)) {
      plannerListeners().list().forEach(listener -> listener.plannerChangedBatch(batch));
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
        LOG.error("LOOP DETECTION in {}. see stack trace for more details.", getClass(), new Exception("LOOP DETECTION"));
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
  public void setLabel(String label) {
    propertySupport.setPropertyString(PROP_LABEL, label);
  }

  @Override
  public String getLabel() {
    return propertySupport.getPropertyString(PROP_LABEL);
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

  @SuppressWarnings("unchecked")
  @Override
  public Map<Integer, DisplayModeOptions> getDisplayModeOptions() {
    return (Map<Integer, DisplayModeOptions>) propertySupport.getProperty(PROP_DISPLAY_MODE_OPTIONS);
  }

  @Override
  public void setDisplayModeOptions(Map<Integer, DisplayModeOptions> displayModeOptions) {
    propertySupport.setProperty(PROP_DISPLAY_MODE_OPTIONS, displayModeOptions);
  }

  @Override
  public void setDisplayModeOption(int displayMode, DisplayModeOptions displayModeOption) {
    DisplayModeOptions previousDisplayModeOption = getDisplayModeOptions().put(displayMode, displayModeOption);
    if (ObjectUtility.notEquals(displayModeOption, previousDisplayModeOption)) {
      propertySupport.firePropertyChange(new PropertyChangeEvent(this, PROP_DISPLAY_MODE_OPTIONS, null, getDisplayModeOptions()));
    }
  }

  @Override
  public int getSelectionMode() {
    return propertySupport.getPropertyInt(PROP_SELECTION_MODE);
  }

  public boolean isMultiSelectResources() {
    return SELECTION_MODE_MULTI_RANGE == getSelectionMode();
  }

  @Override
  public void setSelectionMode(int mode) {
    propertySupport.setPropertyInt(PROP_SELECTION_MODE, mode);
  }

  @Override
  public boolean isActivitySelectable() {
    return propertySupport.getPropertyBool(PROP_ACTIVITY_SELECTABLE);
  }

  @Override
  public void setActivitySelectable(boolean selectable) {
    propertySupport.setPropertyBool(PROP_ACTIVITY_SELECTABLE, selectable);
  }

  @Override
  public Range<Date> getViewRange() {
    @SuppressWarnings("unchecked")
    Range<Date> propValue = (Range<Date>) propertySupport.getProperty(PROP_VIEW_RANGE);
    // return a copy
    return new Range<>(propValue);
  }

  @Override
  public void setViewRange(Date minDate, Date maxDate) {
    setViewRange(new Range<>(minDate, maxDate));
  }

  @Override
  public void setViewRange(Range<Date> viewRange) {
    LOG.debug("Setting view range to {}", viewRange);
    if (viewRange != null) {
      viewRange.setFrom(ensureTruncated(viewRange.getFrom(), "from"));
      viewRange.setTo(ensureTruncated(viewRange.getTo(), "to"));
    }
    propertySupport.setProperty(PROP_VIEW_RANGE, viewRange);
  }

  private Date ensureTruncated(Date date, String fromOrTo) {
    if (date == null) {
      return null;
    }
    Date truncated = DateUtility.truncDate(date);
    if (!date.equals(truncated)) {
      LOG.warn("Had to truncate {} date of range, because UI does not support intra day ranges: {}", fromOrTo, date);
    }
    return truncated;
  }

  @Override
  public void setSelectionRange(Date beginDate, Date endDate) {
    setSelectionRange(new Range<>(beginDate, endDate));
  }

  @Override
  public void setSelectionRange(Range<Date> selectionRange) {
    LOG.debug("Setting selection range to {}", selectionRange);
    propertySupport.setProperty(PROP_SELECTION_RANGE, selectionRange);
  }

  @Override
  public Range<Date> getSelectionRange() {
    @SuppressWarnings("unchecked")
    Range<Date> propValue = (Range<Date>) propertySupport.getProperty(PROP_SELECTION_RANGE);
    // return a copy
    return new Range<>(propValue);
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
  public void decorateResource(Resource<RI> resource) {
    IResourceObserver<RI> observer = resource.getObserver();
    try {
      resource.setObserver(null);
      for (Activity<?, ?> activity : resource.getActivities()) {
        @SuppressWarnings("unchecked")
        Activity<RI, AI> castedActivity = (Activity<RI, AI>) activity;
        decorateActivity(castedActivity);
      }
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
    finally {
      resource.setObserver(observer);
    }
  }

  @Override
  public void decorateActivity(Activity<RI, AI> activity) {
    IActivityObserver<RI, AI> observer = activity.getObserver();
    try {
      activity.setObserver(null);
      interceptDecorateActivity(activity);
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
    finally {
      activity.setObserver(observer);
    }
  }

  @Override
  public IPlannerUIFacade getUIFacade() {
    return m_activityMapUIFacade;
  }

  private class P_ResourceObserver implements IResourceObserver<RI> {
    @Override
    public void resourceChanged(Resource<RI> resource) {
      try {
        updateResource(resource);
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  /**
   * Specialized ui facade
   */
  private class P_PlannerUIFacade implements IPlannerUIFacade<RI, AI> {

    @Override
    public void setSelectedResourcesFromUI(List<? extends Resource<RI>> resources) {
      try {
        setPlannerChanging(true);
        selectResources(resources);
      }
      finally {
        setPlannerChanging(false);
      }
    }

    @Override
    public void setSelectionRangeFromUI(Range<Date> selectionRange) {
      setSelectionRange(selectionRange);
    }

    @Override
    public void setDisplayModeFromUI(int displayMode) {
      setDisplayMode(displayMode);
    }

    @Override
    public void setViewRangeFromUI(Range<Date> viewRange) {
      setViewRange(viewRange);
    }

    @Override
    public void setSelectedActivityFromUI(Activity<RI, AI> activity) {
      setSelectedActivity(activity);
    }
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
    public void execActivitySelected(PlannerActivitySelectedChain<RI, AI> chain, Activity<RI, AI> cell) {
      getOwner().execActivitySelected(cell);
    }

    @Override
    public void execDisposePlanner(PlannerDisposePlannerChain<RI, AI> chain) {
      getOwner().execDisposePlanner();
    }

    @Override
    public void execDecorateActivityCell(PlannerDecorateActivityChain<RI, AI> chain, Activity<RI, AI> cell) {
      getOwner().execDecorateActivity(cell);
    }

    @Override
    public void execInitPlanner(PlannerInitPlannerChain<RI, AI> chain) {
      getOwner().execInitPlanner();
    }

    @Override
    public void execResourcesSelected(PlannerResourcesSelectedChain<RI, AI> plannerResourcesSelectedChain, List<Resource<RI>> resources) {
      getOwner().execResourcesSelected(resources);
    }

    @Override
    public void execSelectionRangeChanged(PlannerSelectionRangeChangedChain<RI, AI> plannerSelectionRangeChangedChain, Range<Date> selectionRange) {
      getOwner().execSelectionRangeChanged(selectionRange);
    }

    @Override
    public void execViewRangeChanged(PlannerViewRangeChangedChain<RI, AI> plannerViewRangeChangedChain, Range<Date> viewRange) {
      getOwner().execViewRangeChanged(viewRange);
    }

    @Override
    public void execDisplayModeChanged(PlannerDisplayModeChangedChain<RI, AI> plannerDisplayModeChangedChain, int displayMode) {
      getOwner().execDisplayModeChanged(displayMode);
    }

  }

  protected final void interceptResourcesSelected(List<Resource<RI>> resources) {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerResourcesSelectedChain<RI, AI> chain = new PlannerResourcesSelectedChain<>(extensions);
    chain.execResourcesSelected(resources);
  }

  protected final void interceptSelectionRangeChanged(Range<Date> selectionRange) {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerSelectionRangeChangedChain<RI, AI> chain = new PlannerSelectionRangeChangedChain<>(extensions);
    chain.execSelectionRangeChanged(selectionRange);
  }

  protected final void interceptViewRangeChanged(Range<Date> viewRange) {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerViewRangeChangedChain<RI, AI> chain = new PlannerViewRangeChangedChain<>(extensions);
    chain.execViewRangeChanged(viewRange);
  }

  protected void interceptDisplayModeChanged(int displayMode) {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerDisplayModeChangedChain<RI, AI> chain = new PlannerDisplayModeChangedChain<>(extensions);
    chain.execDisplayModeChanged(displayMode);
  }

  protected final void interceptActivitySelected(Activity<RI, AI> cell) {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerActivitySelectedChain<RI, AI> chain = new PlannerActivitySelectedChain<>(extensions);
    chain.execActivitySelected(cell);
  }

  protected final void interceptDisposePlanner() {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerDisposePlannerChain<RI, AI> chain = new PlannerDisposePlannerChain<>(extensions);
    chain.execDisposePlanner();
  }

  protected final void interceptDecorateActivity(Activity<RI, AI> activity) {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerDecorateActivityChain<RI, AI> chain = new PlannerDecorateActivityChain<>(extensions);
    chain.execDecorateActivity(activity);
  }

  protected final void interceptInitPlanner() {
    List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions = getAllExtensions();
    PlannerInitPlannerChain<RI, AI> chain = new PlannerInitPlannerChain<>(extensions);
    chain.execInitPlanner();
  }

}

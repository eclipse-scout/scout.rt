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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * {@link ICalendarItemProducer} are defined as inner classes<br>
 */
public abstract class AbstractCalendar extends AbstractPropertyObserver implements ICalendar {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCalendar.class);

  private boolean m_initialized;
  private IMenu[] m_menus;
  private ICalendarItemProvider[] m_providers;
  private final HashMap<Class<? extends ICalendarItemProvider>, Collection<CalendarComponent>> m_componentsByProvider;
  private ICalendarUIFacade m_uiFacade;
  private int m_calendarChanging;
  private final DateTimeFormatFactory m_dateTimeFormatFactory;
  private ArrayList<CalendarEvent> m_calendarEventBuffer;
  private final EventListenerList m_listenerList;

  public AbstractCalendar() {
    this(true);
  }

  public AbstractCalendar(boolean callInitializer) {
    m_calendarEventBuffer = new ArrayList<CalendarEvent>();
    m_listenerList = new EventListenerList();
    m_dateTimeFormatFactory = new DateTimeFormatFactory();
    m_componentsByProvider = new HashMap<Class<? extends ICalendarItemProvider>, Collection<CalendarComponent>>();
    if (callInitializer) {
      initConfig();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      initConfig();
      m_initialized = true;
    }
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  @ConfigPropertyValue("null")
  protected String getConfiguredTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(500)
  @ConfigPropertyValue("6")
  protected int getConfiguredStartHour() {
    return 6;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(510)
  @ConfigPropertyValue("19")
  protected int getConfiguredEndHour() {
    return 19;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(520)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredUseOverflowCells() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(530)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredShowDisplayModeSelection() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(540)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredMarkNoonHour() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(550)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredMarkOutOfMonthDays() {
    return true;
  }

  private Class<? extends ICalendarItemProvider>[] getConfiguredProducers() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<ICalendarItemProvider>[] foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, ICalendarItemProvider.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<IMenu>[] foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  @ConfigOperation
  @Order(10)
  protected void execInitCalendar() throws ProcessingException {
  }

  @ConfigOperation
  @Order(15)
  protected void execDisposeCalendar() throws ProcessingException {
  }

  /**
   * Filter and resolve item conflicts after some {@link ICalendarItemProvider} changed their items
   * 
   * @param changedProviders
   *          is the list of provider types that changed their provided items
   * @param componentsByProvider
   *          is the life map of all provider types with their provided items.
   *          <p>
   *          Changes to the componentsByProvider map are life applied to the calendar model.<br>
   *          Often the convenience method {@link #findConflictingItems(Map, Class...)} is used to calculate conflicting
   *          items of two or more providers for removal from the map.
   *          <p>
   *          The {@link ICalendarItem}s are wrapped into {@link CalendarComponent}s to hold their originating provider
   *          and other common info.<br>
   *          Use {@link CalendarComponent#getItem()} to access the {@link ICalendarItem}.
   */
  @ConfigOperation
  @Order(20)
  protected void execFilterCalendarItems(Set<Class<? extends ICalendarItemProvider>> changedProviderTypes, Map<Class<? extends ICalendarItemProvider>, Collection<CalendarComponent>> componentsByProvider) {
  }

  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    setTitle(getConfiguredTitle());
    setSelectedDate(new Date());
    setStartHour(getConfiguredStartHour());
    setEndHour(getConfiguredEndHour());
    setUseOverflowCells(getConfiguredUseOverflowCells());
    setShowDisplayModeSelection(getConfiguredShowDisplayModeSelection());
    setMarkNoonHour(getConfiguredMarkNoonHour());
    setMarkOutOfMonthDays(getConfiguredMarkOutOfMonthDays());

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
    // producers
    ArrayList<ICalendarItemProvider> producerList = new ArrayList<ICalendarItemProvider>();
    Class<? extends ICalendarItemProvider>[] pa = getConfiguredProducers();
    for (int i = 0; i < pa.length; i++) {
      try {
        ICalendarItemProvider producer = ConfigurationUtility.newInnerInstance(this, pa[i]);
        producerList.add(producer);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    m_providers = producerList.toArray(new ICalendarItemProvider[0]);
    // attach change listener for item updates
    for (final ICalendarItemProvider p : m_providers) {
      p.addPropertyChangeListener(
          new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
              if (e.getPropertyName().equals(ICalendarItemProvider.PROP_ITEMS)) {
                updateComponentsInternal(new ICalendarItemProvider[]{p});
              }
              else if (e.getPropertyName().equals(ICalendarItemProvider.PROP_LOAD_IN_PROGRESS)) {
                updateLoadInProgressInternal();
              }
            }
          }
          );
    }
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

  /*
   * Runtime
   */

  /**
   * This is the init of the runtime model after the table and columns are built
   * and configured
   */
  @Override
  public void initCalendar() throws ProcessingException {
    execInitCalendar();
    /*
     * add property change listener to - reload calendar items when view range
     * changes
     */
    addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(PROP_VIEW_RANGE)) {
          updateComponentsInternal(m_providers);
        }
      }
    });
    updateComponentsInternal(m_providers);
  }

  private void disposeCalendarInternal() {
    for (ICalendarItemProvider p : m_providers) {
      try {
        p.disposeProvider();
      }
      catch (Throwable t) {
        LOG.warn(p.getClass().getName(), t);
      }
    }
  }

  @Override
  public void disposeCalendar() {
    disposeCalendarInternal();
    try {
      execDisposeCalendar();
    }
    catch (Throwable t) {
      LOG.warn(getClass().getName(), t);
    }
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  @Override
  public int getEndHour() {
    return (Integer) propertySupport.getProperty(PROP_END_HOUR);
  }

  @Override
  public void setEndHour(int hour) {
    propertySupport.setProperty(PROP_END_HOUR, hour);
  }

  @Override
  public int getStartHour() {
    return (Integer) propertySupport.getProperty(PROP_START_HOUR);
  }

  @Override
  public void setStartHour(int hour) {
    propertySupport.setProperty(PROP_START_HOUR, hour);
  }

  @Override
  public boolean getUseOverflowCells() {
    return (Boolean) propertySupport.getProperty(PROP_USE_OVERFLOW_CELLS);
  }

  @Override
  public void setUseOverflowCells(boolean useOverflowCells) {
    propertySupport.setProperty(PROP_USE_OVERFLOW_CELLS, useOverflowCells);
  }

  @Override
  public boolean getShowDisplayModeSelection() {
    return (Boolean) propertySupport.getProperty(PROP_SHOW_DISPLAY_MODE_SELECTION);
  }

  @Override
  public void setShowDisplayModeSelection(boolean showDisplayModeSelection) {
    propertySupport.setProperty(PROP_SHOW_DISPLAY_MODE_SELECTION, showDisplayModeSelection);
  }

  @Override
  public boolean getMarkNoonHour() {
    return (Boolean) propertySupport.getProperty(PROP_MARK_NOON_HOUR);
  }

  @Override
  public void setMarkNoonHour(boolean markNoonHour) {
    propertySupport.setProperty(PROP_MARK_NOON_HOUR, markNoonHour);
  }

  @Override
  public boolean getMarkOutOfMonthDays() {
    return (Boolean) propertySupport.getProperty(PROP_MARK_OUT_OF_MONTH_DAYS);
  }

  @Override
  public void setMarkOutOfMonthDays(boolean markOutOfMonthDays) {
    propertySupport.setProperty(PROP_MARK_OUT_OF_MONTH_DAYS, markOutOfMonthDays);
  }

  @Override
  public boolean isLoadInProgress() {
    return propertySupport.getPropertyBool(PROP_LOAD_IN_PROGRESS);
  }

  @Override
  public void setLoadInProgress(boolean b) {
    propertySupport.setPropertyBool(PROP_LOAD_IN_PROGRESS, b);
  }

  @Override
  public boolean isCalendarChanging() {
    return m_calendarChanging > 0;
  }

  @Override
  public void setCalendarChanging(boolean b) {
    // use a stack counter because setTableChanging might be called in nested
    // loops
    if (b) {
      m_calendarChanging++;
      if (m_calendarChanging == 1) {
        // 0 --> 1
        propertySupport.setPropertiesChanging(true);
      }
    }
    else {
      if (m_calendarChanging > 0) {
        m_calendarChanging--;
        if (m_calendarChanging == 0) {
          try {
            processChangeBuffer();
          }
          finally {
            propertySupport.setPropertiesChanging(false);
          }
        }
      }
    }
  }

  private void processChangeBuffer() {
    /*
     * fire events tree changes are finished now, fire all buffered events and
     * call lookups
     */
    CalendarEvent[] a = m_calendarEventBuffer.toArray(new CalendarEvent[0]);
    m_calendarEventBuffer = new ArrayList<CalendarEvent>();
    // coalesce ITEMS_CHANGED,ITEM_SELECTED events
    HashSet<Integer> types = new HashSet<Integer>();
    LinkedList<CalendarEvent> coalescedEvents = new LinkedList<CalendarEvent>();
    // reverse traversal
    for (int i = a.length - 1; i >= 0; i--) {
      switch (a[i].getType()) {
        case CalendarEvent.TYPE_COMPONENT_ACTION: {
          if (!types.contains(a[i].getType())) {
            coalescedEvents.add(0, a[i]);
            types.add(a[i].getType());
          }
          break;
        }
        default: {
          coalescedEvents.add(0, a[i]);
        }
      }
    }
    // fire the batch
    fireCalendarEventBatchInternal(coalescedEvents.toArray(new CalendarEvent[0]));
  }

  public IMenu[] getMenus() {
    return m_menus;
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
  public boolean isDisplayCondensed() {
    return propertySupport.getPropertyBool(PROP_DISPLAY_CONDENSED);
  }

  @Override
  public void setDisplayCondensed(boolean condensed) {
    propertySupport.setPropertyBool(PROP_DISPLAY_CONDENSED, condensed);
  }

  @Override
  public Date[] getViewRange() {
    Date[] a = (Date[]) propertySupport.getProperty(PROP_VIEW_RANGE);
    if (a == null) {
      a = new Date[2];
    }
    return a;
  }

  @Override
  public void setViewRange(Date minDate, Date maxDate) {
    propertySupport.setProperty(PROP_VIEW_RANGE, new Date[]{minDate, maxDate});
  }

  @Override
  public Date getSelectedDate() {
    return (Date) propertySupport.getProperty(PROP_SELECTED_DATE);
  }

  @Override
  public void setSelectedDate(Date d) {
    propertySupport.setProperty(PROP_SELECTED_DATE, d);
  }

  @Override
  public CalendarComponent getSelectedComponent() {
    return (CalendarComponent) propertySupport.getProperty(PROP_SELECTED_COMPONENT);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ICalendarItem> T getSelectedItem(Class<T> c) {
    CalendarComponent comp = getSelectedComponent();
    if (comp != null && comp.getItem() != null) {
      if (c.isAssignableFrom(comp.getItem().getClass())) {
        return (T) comp.getItem();
      }
    }
    return null;
  }

  @Override
  public void setSelectedComponent(CalendarComponent comp) {
    comp = resolveComponent(comp);
    propertySupport.setProperty(PROP_SELECTED_COMPONENT, comp);
  }

  private CalendarComponent resolveComponent(CalendarComponent comp) {
    return comp;
  }

  @Override
  public DateTimeFormatFactory getDateTimeFormatFactory() {
    return m_dateTimeFormatFactory;
  }

  @Override
  public CalendarComponent[] getComponents() {
    CalendarComponent[] a = (CalendarComponent[]) propertySupport.getProperty(PROP_COMPONENTS);
    if (a == null) {
      a = new CalendarComponent[0];
    }
    return a;
  }

  private void updateComponentsInternal(ICalendarItemProvider[] changedProviders) {
    Date[] d = getViewRange();
    if (d[0] != null && d[1] != null) {
      for (ICalendarItemProvider p : changedProviders) {
        LinkedList<CalendarComponent> components = new LinkedList<CalendarComponent>();
        for (ICalendarItem item : p.getItems(d[0], d[1])) {
          Cell cell = new Cell();
          p.decorateCell(cell, item);
          components.add(new CalendarComponent(this, p, item, cell));
        }
        m_componentsByProvider.put(p.getClass(), components);
      }
      // filter and resolve item conflicts
      HashSet<Class<? extends ICalendarItemProvider>> providerTypes = new HashSet<Class<? extends ICalendarItemProvider>>();
      for (int i = 0; i < changedProviders.length; i++) {
        providerTypes.add(changedProviders[i].getClass());
      }
      execFilterCalendarItems(providerTypes, m_componentsByProvider);
      // complete list
      TreeMap<CompositeObject, CalendarComponent> sortMap = new TreeMap<CompositeObject, CalendarComponent>();
      int index = 0;
      for (Collection<CalendarComponent> c : m_componentsByProvider.values()) {
        for (CalendarComponent comp : c) {
          sortMap.put(new CompositeObject(comp.getFromDate(), index++), comp);
        }
      }
      propertySupport.setProperty(PROP_COMPONENTS, sortMap.values().toArray(new CalendarComponent[0]));
      // validate selection
      setSelectedComponent(getSelectedComponent());
    }
  }

  @Override
  public Object getContainer() {
    return propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls an
   * {@link ICalendar}
   */
  public void setContainerInternal(Object container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  public Collection<CalendarItemConflict> findConflictingItems(Map<Class<? extends ICalendarItemProvider>, Collection<CalendarComponent>> componentsByProvider, Class<? extends ICalendarItemProvider>... providerTypes) {
    if (providerTypes != null && providerTypes.length >= 2) {
      HashMap<String, List<CalendarComponent>> classificationMap = new HashMap<String, List<CalendarComponent>>();
      for (int i = 0; i < providerTypes.length; i++) {
        Collection<CalendarComponent> a = componentsByProvider.get(providerTypes[i]);
        if (a != null) {
          for (CalendarComponent comp : a) {
            String key = StringUtility.emptyIfNull(comp.getItem().getSubject()).toLowerCase().trim();
            List<CalendarComponent> list = classificationMap.get(key);
            if (list == null) {
              list = new ArrayList<CalendarComponent>();
              classificationMap.put(key, list);
            }
            list.add(comp);
          }
        }
      }
      ArrayList<CalendarItemConflict> conflicts = new ArrayList<CalendarItemConflict>();
      for (Map.Entry<String, List<CalendarComponent>> e : classificationMap.entrySet()) {
        if (e.getValue().size() >= 2) {
          List<CalendarComponent> list = e.getValue();
          // find CalendarComponents with same Provider, break them up in separate groups for duplicate check
          // reason: all CalendarComponents of the same provider are assumed to be distinct
          HashMap<ICalendarItemProvider, ArrayList<CalendarComponent>> groups = new HashMap<ICalendarItemProvider, ArrayList<CalendarComponent>>();
          for (CalendarComponent c : list) {
            if (groups.containsKey(c.getProvider())) {
              groups.get(c.getProvider()).add(c);
            }
            else {
              ArrayList<CalendarComponent> tmp = new ArrayList<CalendarComponent>();
              tmp.add(c);
              groups.put(c.getProvider(), tmp);
            }
          }
          ArrayList<CalendarComponent> groupComp = new ArrayList<CalendarComponent>();
          for (ArrayList<CalendarComponent> g : groups.values()) {
            if (g.size() > 1) {
              groupComp.addAll(g);
            }
          }
          if (groupComp.size() == 0) {
            // no duplicate records of a provider found, start with first item
            groupComp.add(list.get(0));
          }
          for (CalendarComponent ref : groupComp) {
            ArrayList<CalendarComponent> matchList = new ArrayList<CalendarComponent>();
            double matchSum = 0;
            matchList.add(ref);
            for (CalendarComponent test : list) {
              if (ref == test || test.getProvider() == ref.getProvider()) {
                continue;
              }
              if (DateUtility.intersects(test.getFromDate(), test.getToDate(), ref.getFromDate(), ref.getToDate())) {
                matchList.add(test);
                double minOfStart = Math.min(test.getFromDate().getTime(), ref.getFromDate().getTime());
                double maxOfStart = Math.max(test.getFromDate().getTime(), ref.getFromDate().getTime());
                double minOfEnd = Math.min(test.getToDate().getTime(), ref.getToDate().getTime());
                double maxOfEnd = Math.max(test.getToDate().getTime(), ref.getToDate().getTime());
                if (maxOfEnd - minOfStart > 1e-6) {
                  matchSum += (minOfEnd - maxOfStart) / (maxOfEnd - minOfStart);
                }
                else {
                  matchSum += 1.0d;
                }
              }
            }
            if (matchList.size() >= 2) {
              conflicts.add(new CalendarItemConflict(componentsByProvider, matchList, matchSum / (matchList.size() - 1)));
            }
          }
        }
      }
      return conflicts;
    }
    else {
      return Collections.emptyList();
    }
  }

  private void updateLoadInProgressInternal() {
    boolean b = false;
    for (ICalendarItemProvider p : m_providers) {
      if (p.isLoadInProgress()) {
        b = true;
        break;
      }
    }
    setLoadInProgress(b);
  }

  @Override
  public void reloadCalendarItems() {
    for (ICalendarItemProvider p : m_providers) {
      p.reloadProvider();
    }
  }

  private IMenu[] fireComponentPopup(CalendarComponent comp) {
    if (comp != null) {
      CalendarEvent e = new CalendarEvent(this, CalendarEvent.TYPE_COMPONENT_POPUP, comp);
      // single observer for calendar-defined menus
      addComponentPopupMenus(e, comp);
      fireCalendarEventInternal(e);
      return e.getPopupMenus();
    }
    else {
      return new IMenu[0];
    }
  }

  private void addComponentPopupMenus(CalendarEvent e, CalendarComponent comp) {
    // calendar
    for (IMenu menu : getMenus()) {
      // pass 1
      if (menu.isSingleSelectionAction()) {
        // pass 2
        menu.prepareAction();
        if (menu.isVisible()) {
          e.addPopupMenu(menu);
        }
      }
    }
    // item producer
    for (IMenu menu : comp.getProvider().getMenus()) {
      // pass 1
      if (menu.isSingleSelectionAction()) {
        // pass 2
        menu.prepareAction();
        if (menu.isVisible()) {
          e.addPopupMenu(menu);
        }
      }
    }
  }

  private IMenu[] fireNewPopup() {
    CalendarEvent e = new CalendarEvent(this, CalendarEvent.TYPE_NEW_POPUP);
    // single observer for calendar-defined menus
    addNewPopupMenus(e);
    fireCalendarEventInternal(e);
    return e.getPopupMenus();
  }

  private void addNewPopupMenus(CalendarEvent e) {
    // calendar
    for (IMenu menu : getMenus()) {
      // pass 1
      if (!menu.isSingleSelectionAction()) {
        menu.prepareAction();
        if (menu.isVisible()) {
          e.addPopupMenu(menu);
        }
      }
    }
    // producers
    for (ICalendarItemProvider p : m_providers) {
      for (IMenu menu : p.getMenus()) {
        // pass 1
        if (!menu.isSingleSelectionAction()) {
          menu.prepareAction();
          if (menu.isVisible()) {
            e.addPopupMenu(menu);
          }
        }
      }
    }
  }

  /*
   * Property Observer
   */

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  /**
   * Model Observer
   */
  @Override
  public void addCalendarListener(CalendarListener listener) {
    m_listenerList.add(CalendarListener.class, listener);
  }

  @Override
  public void removeCalendarListener(CalendarListener listener) {
    m_listenerList.remove(CalendarListener.class, listener);
  }

  private void fireCalendarComponentAction() {
    CalendarComponent comp = getSelectedComponent();
    if (comp != null) {
      // single observer exec
      try {
        comp.getProvider().onItemAction(comp.getItem());
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      fireCalendarEventInternal(new CalendarEvent(this, CalendarEvent.TYPE_COMPONENT_ACTION, comp));
    }
  }

  // main handler
  private void fireCalendarEventInternal(CalendarEvent e) {
    if (isCalendarChanging()) {
      // buffer the event for later batch firing
      m_calendarEventBuffer.add(e);
    }
    else {
      EventListener[] listeners = m_listenerList.getListeners(CalendarListener.class);
      if (listeners != null && listeners.length > 0) {
        for (int i = 0; i < listeners.length; i++) {
          ((CalendarListener) listeners[i]).calendarChanged(e);
        }
      }
    }
  }

  // batch handler
  private void fireCalendarEventBatchInternal(CalendarEvent[] batch) {
    if (isCalendarChanging()) {
      LOG.error("Illegal State: firing a event batch while calendar is changing");
    }
    else {
      EventListener[] listeners = m_listenerList.getListeners(CalendarListener.class);
      if (listeners != null && listeners.length > 0) {
        for (int i = 0; i < listeners.length; i++) {
          ((CalendarListener) listeners[i]).calendarChangedBatch(batch);
        }
      }
    }
  }

  @Override
  public ICalendarUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /*
   * UI Notifications
   */
  private class P_UIFacade implements ICalendarUIFacade {
    private int m_uiProcessorCount = 0;

    protected void pushUIProcessor() {
      m_uiProcessorCount++;
    }

    protected void popUIProcessor() {
      m_uiProcessorCount--;
    }

    @Override
    public boolean isUIProcessing() {
      return m_uiProcessorCount > 0;
    }

    @Override
    public void setSelectionFromUI(Date d, CalendarComponent comp) {
      try {
        pushUIProcessor();
        //
        setSelectedDate(d);
        setSelectedComponent(comp);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setVisibleRangeFromUI(Date minDate, Date maxDate) {
      try {
        pushUIProcessor();
        //
        setViewRange(minDate, maxDate);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireReloadFromUI() {
      try {
        pushUIProcessor();
        //
        reloadCalendarItems();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public IMenu[] fireComponentPopupFromUI() {
      try {
        pushUIProcessor();
        //
        return fireComponentPopup(getSelectedComponent());
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public IMenu[] fireNewPopupFromUI() {
      try {
        pushUIProcessor();
        //
        return fireNewPopup();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireComponentActionFromUI() {
      try {
        pushUIProcessor();
        //
        fireCalendarComponentAction();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireComponentMovedFromUI(CalendarComponent comp, Date newDate) {
      try {
        pushUIProcessor();
        //
        comp = resolveComponent(comp);
        if (comp != null) {
          try {
            comp.getProvider().onItemMoved(comp.getItem(), newDate);
          }
          catch (ProcessingException e) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(e);
          }
          catch (Throwable e) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", e));
          }
        }
        fireCalendarComponentAction();
      }
      finally {
        popUIProcessor();
      }
    }

  }

}

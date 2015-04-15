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
package org.eclipse.scout.rt.client.ui.basic.calendar.provider;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderItemActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderItemMovedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderLoadItemsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderLoadItemsInBackgroundChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.ICalendarItemProviderExtension;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IProgressMonitor;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarAppointment;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarTask;

public abstract class AbstractCalendarItemProvider extends AbstractPropertyObserver implements ICalendarItemProvider, IContributionOwner, IExtensibleObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCalendarItemProvider.class);

  private IFuture<Void> m_reloadJob;
  private boolean m_initialized;
  private List<IMenu> m_menus;
  private Date m_minDateLoaded;
  private Date m_maxDateLoaded;
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractCalendarItemProvider, ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> m_objectExtensions;

  public AbstractCalendarItemProvider() {
    this(true);
  }

  public AbstractCalendarItemProvider(boolean callInitializer) {
    m_objectExtensions = new ObjectExtensions<AbstractCalendarItemProvider, ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>>(this);
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
  /**
   * @return true, if this calendar item should be draggable in the UI, false otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredMoveItemEnabled() {
    return false;
  }

  /**
   * Returns the refresh intervall
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(20)
  protected long getConfiguredRefreshIntervallMillis() {
    return 0;
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  /**
   * Produce items in the time range [minDate,maxDate]<br>
   * The result is stored in the out parameter resutlHolder
   * <p>
   * Due to an outparameter instead of a return value this method can (for performance optimization) start a background
   * process and return immediately
   * <p>
   * This method is called in the default model thread
   */
  @ConfigOperation
  @Order(30)
  protected void execLoadItems(final Date minDate, final Date maxDate, final Set<ICalendarItem> result) throws ProcessingException {
  }

  /**
   * Produce items in the time range [minDate,maxDate]<br>
   * The result is stored in the out parameter resutlHolder
   * <p>
   * Note: This method is NOT called in the default model thread, but in a background thread
   */
  @ConfigOperation
  @Order(40)
  protected void execLoadItemsInBackground(final IClientSession session, final Date minDate, final Date maxDate, final Set<ICalendarItem> result) throws ProcessingException {
    IFuture<Void> future = ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        interceptLoadItems(minDate, maxDate, result);
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent().session(session)));
    future.awaitDoneAndGet();
  }

  @ConfigOperation
  @Order(10)
  protected void execDecorateCell(Cell cell, ICalendarItem item) throws ProcessingException {
  }

  /**
   * item was moved using drag & drop
   */
  @ConfigOperation
  @Order(20)
  protected void execItemMoved(ICalendarItem item, Date newDate) throws ProcessingException {
  }

  @ConfigOperation
  @Order(50)
  protected void execItemAction(ICalendarItem item) throws ProcessingException {
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
    m_contributionHolder = new ContributionComposite(this);
    setMoveItemEnabled(getConfiguredMoveItemEnabled());
    setRefreshIntervalMillis(getConfiguredRefreshIntervallMillis());
    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
        menu.initAction();
        menus.addOrdered(menu);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + menuClazz.getName() + "'.", e));
      }
    }
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    menus.addAllOrdered(contributedMenus);

    try {
      injectMenusInternal(menus);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute menus.", e);
    }
    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    m_menus = menus.getOrderedList();
  }

  @Override
  public final List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider> createLocalExtension() {
    return new LocalCalendarItemProviderExtension<AbstractCalendarItemProvider>(this);
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

  /*
   * Runtime
   */
  @Override
  public void disposeProvider() {
    IFuture<Void> job = m_reloadJob;
    if (job != null) {
      job.cancel(true);
      m_reloadJob = null;
    }
  }

  @Override
  public final void decorateCell(Cell cell, ICalendarItem item) {
    decorateCellInternal(cell, item);
    try {
      interceptDecorateCell(cell, item);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  protected void decorateCellInternal(Cell cell, ICalendarItem item) {
    if (item instanceof ICalendarAppointment) {
      ICalendarAppointment app = (ICalendarAppointment) item;
      cell.setText(app.getSubject());
      StringBuffer buf = new StringBuffer();
      if (app.getLocation() != null) {
        if (buf.length() > 0) {
          buf.append("\n");
        }
        buf.append(app.getLocation());
      }
      if (app.getBody() != null) {
        if (buf.length() > 0) {
          buf.append("\n");
        }
        buf.append(app.getBody());
      }
      if (buf.length() > 0) {
        cell.setTooltipText(buf.toString());
      }
    }
    if (item instanceof ICalendarTask) {
      ICalendarTask task = (ICalendarTask) item;
      cell.setText(task.getSubject());
      StringBuffer buf = new StringBuffer();
      if (task.getBody() != null) {
        if (buf.length() > 0) {
          buf.append("\n");
        }
        buf.append(task.getBody());
      }
      if (buf.length() > 0) {
        cell.setTooltipText(buf.toString());
      }
    }
    cell.setBackgroundColor((item.getColor()));
  }

  @Override
  public Set<ICalendarItem> getItems(Date minDate, Date maxDate) {
    ensureItemsLoadedInternal(minDate, maxDate);
    Set<ICalendarItem> allItems = propertySupport.getPropertySet(PROP_ITEMS);
    if (CollectionUtility.hasElements(allItems)) {
      Set<ICalendarItem> list = new HashSet<ICalendarItem>(allItems.size());
      for (ICalendarItem item : allItems) {
        if (item.isIntersecting(minDate, maxDate)) {
          list.add(item);
        }
      }
      return list;
    }
    return CollectionUtility.hashSet();
  }

  @Override
  public void reloadProvider() {
    loadItemsAsyncInternal(ClientSessionProvider.currentSession(), m_minDateLoaded, m_maxDateLoaded, 250);
  }

  private void setItemsInternal(Date minDate, Date maxDate, Set<ICalendarItem> items0) {
    Set<ICalendarItem> items = CollectionUtility.hashSetWithoutNullElements(items0);
    m_minDateLoaded = minDate;
    m_maxDateLoaded = maxDate;
    propertySupport.setPropertySet(PROP_ITEMS, items);
  }

  @Override
  public List<IMenu> getMenus() {
    return CollectionUtility.arrayList(m_menus);
  }

  @Override
  public boolean isMoveItemEnabled() {
    return propertySupport.getPropertyBool(PROP_MOVE_ITEM_ENABLED);
  }

  @Override
  public void setMoveItemEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_MOVE_ITEM_ENABLED, b);
  }

  @Override
  public boolean isLoadInProgress() {
    return propertySupport.getPropertyBool(PROP_LOAD_IN_PROGRESS);
  }

  @Override
  public void setLoadInProgress(final boolean loadInProgress) {
    if (ModelJobs.isModelThread()) {
      propertySupport.setPropertyBool(PROP_LOAD_IN_PROGRESS, loadInProgress);
    }
    else {
      try {
        ModelJobs.schedule(new IRunnable() {
          @Override
          public void run() throws Exception {
            propertySupport.setPropertyBool(PROP_LOAD_IN_PROGRESS, loadInProgress);
          }
        }).awaitDoneAndGet();
      }
      catch (ProcessingException e) {
        LOG.error("Failed to update property 'loadInProgress'", e);
      }
    }
  }

  @Override
  public long getRefreshIntervalMillis() {
    return propertySupport.getPropertyLong(PROP_REFRESH_INTERVAL_MILLIS);
  }

  @Override
  public void setRefreshIntervalMillis(long m) {
    propertySupport.setPropertyLong(PROP_REFRESH_INTERVAL_MILLIS, m);
    if (m > 0) {
      loadItemsAsyncInternal(ClientSessionProvider.currentSession(), m_minDateLoaded, m_maxDateLoaded, m);
    }
  }

  @Override
  public void onItemAction(ICalendarItem item) throws ProcessingException {
    try {
      interceptItemAction(item);
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  @Override
  public void onItemMoved(ICalendarItem item, Date newDate) throws ProcessingException {
    try {
      interceptItemMoved(item, newDate);
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  private void ensureItemsLoadedInternal(Date minDate, Date maxDate) {
    if (DateUtility.isInRange(m_minDateLoaded, minDate, m_maxDateLoaded) && DateUtility.isInRange(m_minDateLoaded, maxDate, m_maxDateLoaded)) {
      // nop. [minDate,maxDate] is inside loaded range
    }
    else {
      loadItemsAsyncInternal(ClientSessionProvider.currentSession(), minDate, maxDate, 250);
    }
  }

  private synchronized void loadItemsAsyncInternal(IClientSession session, Date minDate, Date maxDate, long startDelayMillis) {
    IFuture<Void> oldJob = m_reloadJob;
    if (oldJob != null) {
      oldJob.cancel(true);
      m_reloadJob = null;
    }
    if (minDate != null && maxDate != null) {
      long refreshInterval = getRefreshIntervalMillis();
      P_ReloadJob runnable = new P_ReloadJob(minDate, maxDate);

      ClientRunContext runContext = ClientRunContexts.copyCurrent().session(session);
      if (refreshInterval > 0) {
        // interval load
        m_reloadJob = ClientJobs.scheduleWithFixedDelay(runnable, startDelayMillis, refreshInterval, TimeUnit.MILLISECONDS, ClientJobs.newInput(runContext));
      }
      else {
        // single load
        m_reloadJob = ClientJobs.schedule(runnable, startDelayMillis, TimeUnit.MILLISECONDS, ClientJobs.newInput(runContext));
      }
    }
  }

  /**
   * Reload Job
   */
  private class P_ReloadJob implements IRunnable {
    private final Set<ICalendarItem> m_result;
    private final Date m_loadingMinDate;
    private final Date m_loadingMaxDate;

    public P_ReloadJob(Date loadingMinDate, Date loadingMaxDate) {
      m_result = new HashSet<>();
      m_loadingMinDate = loadingMinDate;
      m_loadingMaxDate = loadingMaxDate;
    }

    @Override
    public void run() throws Exception {
      if (IProgressMonitor.CURRENT.get().isCancelled()) {
        return;
      }

      // set loading property in scout
      setLoadInProgress(true);
      try {
        // call user code
        try {
          interceptLoadItemsInBackground(ClientSessionProvider.currentSession(), m_loadingMinDate, m_loadingMaxDate, m_result);
        }
        catch (ProcessingException e) {
          if (!e.isInterruption()) {
            LOG.error(null, e);
          }
        }

        IFuture<Void> future = ModelJobs.schedule(new IRunnable() {
          @Override
          public void run() throws Exception {
            synchronized (AbstractCalendarItemProvider.this) {
              if (!IProgressMonitor.CURRENT.get().isCancelled()) {
                setItemsInternal(m_loadingMinDate, m_loadingMaxDate, m_result);
              }
            }
          }
        });
        future.awaitDoneAndGet();
      }
      finally {
        setLoadInProgress(false);
      }
    }
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalCalendarItemProviderExtension<OWNER extends AbstractCalendarItemProvider> extends AbstractExtension<OWNER> implements ICalendarItemProviderExtension<OWNER> {

    public LocalCalendarItemProviderExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execLoadItems(CalendarItemProviderLoadItemsChain chain, Date minDate, Date maxDate, Set<ICalendarItem> result) throws ProcessingException {
      getOwner().execLoadItems(minDate, maxDate, result);
    }

    @Override
    public void execItemAction(CalendarItemProviderItemActionChain chain, ICalendarItem item) throws ProcessingException {
      getOwner().execItemAction(item);
    }

    @Override
    public void execLoadItemsInBackground(CalendarItemProviderLoadItemsInBackgroundChain chain, IClientSession session, Date minDate, Date maxDate, Set<ICalendarItem> result) throws ProcessingException {
      getOwner().execLoadItemsInBackground(session, minDate, maxDate, result);
    }

    @Override
    public void execItemMoved(CalendarItemProviderItemMovedChain chain, ICalendarItem item, Date newDate) throws ProcessingException {
      getOwner().execItemMoved(item, newDate);
    }

    @Override
    public void execDecorateCell(CalendarItemProviderDecorateCellChain chain, Cell cell, ICalendarItem item) throws ProcessingException {
      getOwner().execDecorateCell(cell, item);
    }

  }

  protected final void interceptLoadItems(Date minDate, Date maxDate, Set<ICalendarItem> result) throws ProcessingException {
    List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions = getAllExtensions();
    CalendarItemProviderLoadItemsChain chain = new CalendarItemProviderLoadItemsChain(extensions);
    chain.execLoadItems(minDate, maxDate, result);
  }

  protected final void interceptItemAction(ICalendarItem item) throws ProcessingException {
    List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions = getAllExtensions();
    CalendarItemProviderItemActionChain chain = new CalendarItemProviderItemActionChain(extensions);
    chain.execItemAction(item);
  }

  protected final void interceptLoadItemsInBackground(IClientSession session, Date minDate, Date maxDate, Set<ICalendarItem> result) throws ProcessingException {
    List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions = getAllExtensions();
    CalendarItemProviderLoadItemsInBackgroundChain chain = new CalendarItemProviderLoadItemsInBackgroundChain(extensions);
    chain.execLoadItemsInBackground(session, minDate, maxDate, result);
  }

  protected final void interceptItemMoved(ICalendarItem item, Date newDate) throws ProcessingException {
    List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions = getAllExtensions();
    CalendarItemProviderItemMovedChain chain = new CalendarItemProviderItemMovedChain(extensions);
    chain.execItemMoved(item, newDate);
  }

  protected final void interceptDecorateCell(Cell cell, ICalendarItem item) throws ProcessingException {
    List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions = getAllExtensions();
    CalendarItemProviderDecorateCellChain chain = new CalendarItemProviderDecorateCellChain(extensions);
    chain.execDecorateCell(cell, item);
  }
}

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarAppointment;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarTask;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractCalendarItemProvider extends AbstractPropertyObserver implements ICalendarItemProvider {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCalendarItemProvider.class);

  public static final long DAY_MILLIS = 24L * 3600L * 1000L;
  public static final long MONTH_MILLIS = DAY_MILLIS * 32L;

  private P_ReloadJob m_reloadJob;

  private boolean m_initialized;
  private List<IMenu> m_menus;
  private Date m_minDateLoaded;
  private Date m_maxDateLoaded;

  public AbstractCalendarItemProvider() {
    this(true);
  }

  public AbstractCalendarItemProvider(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      initConfig();
      ensureItemsLoadedInternal(new Date(System.currentTimeMillis() - MONTH_MILLIS), new Date(System.currentTimeMillis() + MONTH_MILLIS));
      m_initialized = true;
    }
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredMoveItemEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(20)
  protected long getConfiguredRefreshIntervallMillis() {
    return 0;
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    List<Class<? extends IMenu>> foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
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
    ClientSyncJob job = new ClientSyncJob(getClass().getSimpleName() + " load items", session) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        execLoadItems(minDate, maxDate, result);
      }
    };
    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      // nop
    }
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

  protected void initConfig() {
    setMoveItemEnabled(getConfiguredMoveItemEnabled());
    setRefreshIntervalMillis(getConfiguredRefreshIntervallMillis());
    // menus
    List<IMenu> menuList = new ArrayList<IMenu>();
    for (Class<? extends IMenu> menuClazz : getDeclaredMenus()) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
        menu.initAction();
        menuList.add(menu);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + menuClazz.getName() + "'.", e));
      }
    }

    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute menus.", e);
    }
    m_menus = menuList;
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
  @Override
  public void disposeProvider() {
    P_ReloadJob job = m_reloadJob;
    if (job != null) {
      job.cancel();
      m_reloadJob = null;
    }
  }

  @Override
  public final void decorateCell(Cell cell, ICalendarItem item) {
    decorateCellInternal(cell, item);
    try {
      execDecorateCell(cell, item);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", e));
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
    loadItemsAsyncInternal(ClientSyncJob.getCurrentSession(), m_minDateLoaded, m_maxDateLoaded, 250);
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
  public void setLoadInProgress(boolean b) {
    propertySupport.setPropertyBool(PROP_LOAD_IN_PROGRESS, b);
  }

  private void setLoadInProgressInSyncJob(final boolean b) {
    ClientSyncJob job = new ClientSyncJob(getClass().getSimpleName() + " prepare", ClientSyncJob.getCurrentSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        setLoadInProgress(b);
      }
    };
    job.schedule();
  }

  @Override
  public long getRefreshIntervalMillis() {
    return propertySupport.getPropertyLong(PROP_REFRESH_INTERVAL_MILLIS);
  }

  @Override
  public void setRefreshIntervalMillis(long m) {
    propertySupport.setPropertyLong(PROP_REFRESH_INTERVAL_MILLIS, m);
    if (m > 0) {
      loadItemsAsyncInternal(ClientSyncJob.getCurrentSession(), m_minDateLoaded, m_maxDateLoaded, m);
    }
  }

  @Override
  public void onItemAction(ICalendarItem item) throws ProcessingException {
    try {
      execItemAction(item);
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  @Override
  public void onItemMoved(ICalendarItem item, Date newDate) throws ProcessingException {
    try {
      execItemMoved(item, newDate);
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  private void ensureItemsLoadedInternal(Date minDate, Date maxDate) {
    if (DateUtility.isInRange(m_minDateLoaded, minDate, m_maxDateLoaded) && DateUtility.isInRange(m_minDateLoaded, maxDate, m_maxDateLoaded)) {
      // nop. [minDate,maxDate] is inside loaded range
    }
    else {
      loadItemsAsyncInternal(ClientSyncJob.getCurrentSession(), minDate, maxDate, 250);
    }
  }

  private synchronized void loadItemsAsyncInternal(IClientSession session, Date minDate, Date maxDate, long startDelayMillis) {
    P_ReloadJob oldJob = m_reloadJob;
    if (oldJob != null) {
      oldJob.cancel();
      m_reloadJob = null;
    }
    if (minDate != null && maxDate != null) {
      m_reloadJob = new P_ReloadJob(session, minDate, maxDate);
      m_reloadJob.schedule(startDelayMillis);
    }
  }

  /**
   * Reload Job
   */
  private class P_ReloadJob extends ClientAsyncJob {
    private final Set<ICalendarItem> m_result;
    private final Date m_loadingMinDate;
    private final Date m_loadingMaxDate;

    public P_ReloadJob(IClientSession session, Date loadingMinDate, Date loadingMaxDate) {
      super(AbstractCalendarItemProvider.this.getClass().getSimpleName() + " reload", session);
      m_result = new HashSet<ICalendarItem>();
      m_loadingMinDate = loadingMinDate;
      m_loadingMaxDate = loadingMaxDate;
    }

    @Override
    protected IStatus runStatus(final IProgressMonitor monitor) {
      // set loading property in scout
      setLoadInProgressInSyncJob(true);
      if (!monitor.isCanceled()) {
        // call user code
        try {
          execLoadItemsInBackground(ClientSyncJob.getCurrentSession(), m_loadingMinDate, m_loadingMaxDate, m_result);
        }
        catch (ProcessingException e) {
          if (!e.isInterruption()) {
            LOG.error(null, e);
          }
        }
        ClientSyncJob setItemsJob = new ClientSyncJob(AbstractCalendarItemProvider.this.getClass().getSimpleName() + " setItems", ClientSyncJob.getCurrentSession()) {
          @Override
          protected void runVoid(IProgressMonitor monitor2) throws Throwable {
            synchronized (AbstractCalendarItemProvider.this) {
              if (!monitor.isCanceled()) {
                setItemsInternal(m_loadingMinDate, m_loadingMaxDate, m_result);
                reschedule(monitor);
              }
            }
          }

          private void reschedule(final IProgressMonitor monitor2) {
            long n = getRefreshIntervalMillis();
            if (n > 0 && !monitor2.isCanceled()) {
              //-> Rescheduling (and cancelling a currently running job) should only happen, if a previous job actually succeeded in loading the calendar items
              //    AND loading dates are still consistent with ui (If they are not consistent, monitor is cancled during setItemsInternal(...))
              loadItemsAsyncInternal(ClientSyncJob.getCurrentSession(), m_loadingMinDate, m_loadingMaxDate, n);
            }
          }
        };
        setItemsJob.schedule();
        try {
          setItemsJob.join();
        }
        catch (InterruptedException e) {
          // nop
        }
        setLoadInProgressInSyncJob(false);
      }
      return Status.OK_STATUS;
    }
  }

}

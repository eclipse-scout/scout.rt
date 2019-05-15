/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.ClientConfigProperties.MemoryPolicyProperty;
import org.eclipse.scout.rt.client.extension.ClientSessionChains.ClientSessionLoadSessionChain;
import org.eclipse.scout.rt.client.extension.ClientSessionChains.ClientSessionStoreSessionChain;
import org.eclipse.scout.rt.client.extension.IClientSessionExtension;
import org.eclipse.scout.rt.client.session.ClientSessionStopHelper;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.internal.VirtualDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.context.SharedVariableMap;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.services.common.security.ILogoutService;
import org.eclipse.scout.rt.shared.session.IGlobalSessionListener;
import org.eclipse.scout.rt.shared.session.ISessionListener;
import org.eclipse.scout.rt.shared.session.SessionData;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClientSession extends AbstractPropertyObserver implements IClientSession, IExtensibleObject {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractClientSession.class);

  private final EventListenerList m_eventListeners;
  private final IExecutionSemaphore m_modelJobSemaphore = Jobs.newExecutionSemaphore(1).seal();

  // state
  private final Object m_stateLock;
  private volatile boolean m_active;
  private volatile boolean m_stopping;
  private final Semaphore m_permitToSaveBeforeClosing = new Semaphore(1);
  private final Semaphore m_permitToStop = new Semaphore(1);
  private int m_exitCode = 0;

  // model
  private String m_id;
  private IDesktop m_desktop;
  private VirtualDesktop m_virtualDesktop;
  private Subject m_subject;

  private final SharedVariableMap m_sharedVariableMap;

  private IMemoryPolicy m_memoryPolicy;
  private final SessionData m_sessionData;
  private ScoutTexts m_scoutTexts;
  private UserAgent m_userAgent;
  private final ObjectExtensions<AbstractClientSession, IClientSessionExtension<? extends AbstractClientSession>> m_objectExtensions;
  private URI m_browserUri;

  public AbstractClientSession(boolean autoInitConfig) {
    m_eventListeners = new EventListenerList();
    m_sessionData = new SessionData();
    m_stateLock = new Object();
    m_userAgent = UserAgent.get();
    m_subject = Subject.getSubject(AccessController.getContext());
    m_objectExtensions = new ObjectExtensions<AbstractClientSession, IClientSessionExtension<? extends AbstractClientSession>>(this, true);
    m_scoutTexts = new ScoutTexts();
    m_sharedVariableMap = new SharedVariableMap();

    setLocale(NlsLocale.get());

    if (autoInitConfig) {
      interceptInitConfig();
    }
  }

  @Override
  public final List<? extends IClientSessionExtension<? extends AbstractClientSession>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected IClientSessionExtension<? extends AbstractClientSession> createLocalExtension() {
    return new LocalClientSessionExtension<AbstractClientSession>(this);
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredSingleThreadSession() {
    return false;
  }

  @Override
  public String getId() {
    return m_id;
  }

  /**
   * Returns the userId of the subject holding the current session
   */
  @Override
  public String getUserId() {
    return getSharedContextVariable("userId", String.class);
  }

  /**
   * Returns the {@link ScoutTexts} instance assigned to the type (class) of the current ClientSession.
   * <p>
   * Override this method to set the application specific texts implementation
   * </p>
   */
  @Override
  public ScoutTexts getTexts() {
    return m_scoutTexts;
  }

  @Override
  public final Locale getLocale() {
    return (Locale) propertySupport.getProperty(PROP_LOCALE);
  }

  @Override
  public final void setLocale(Locale locale) {
    propertySupport.setProperty(PROP_LOCALE, locale);
    NlsLocale.set(locale);
  }

  @Override
  public UserAgent getUserAgent() {
    if (m_userAgent == null) {
      m_userAgent = UserAgents.createDefault();
      LOG.warn("UserAgent not set; using default [default={}]", m_userAgent);
    }
    return m_userAgent;
  }

  @Override
  public void setUserAgent(UserAgent userAgent) {
    UserAgent.set(userAgent);
    m_userAgent = userAgent;
  }

  @Override
  public URI getBrowserURI() {
    return m_browserUri;
  }

  @Override
  public boolean isActive() {
    return m_active;
  }

  private void setActive(boolean b) {
    synchronized (m_stateLock) {
      m_active = b;
      m_stateLock.notifyAll();
    }
  }

  @Override
  public Map<String, Object> getSharedVariableMap() {
    return CollectionUtility.copyMap(m_sharedVariableMap);
  }

  /**
   * Do not use this method directly. Create specific (typed) methods instead to access shared variables. (like
   * {@link #getUserId()})
   * <p>
   * Returns the variables shared with the server. Shared variables are automatically updated on the client by client
   * notifications when changed on the server.
   * </p>
   */
  protected <T> T getSharedContextVariable(String name, Class<T> type) {
    Object o = m_sharedVariableMap.get(name);
    return TypeCastUtility.castValue(o, type);
  }

  @Override
  public final Object getStateLock() {
    return m_stateLock;
  }

  /*
   * Properties
   */
  protected void initConfig() {
    m_virtualDesktop = new VirtualDesktop();
    m_browserUri = resolveBrowserUri();

    setMemoryPolicy(resolveMemoryPolicy());
  }

  /**
   * Resolves the browser URI, or <code>null</code> if could not be resolved.
   * <p>
   * The default implementation looks in the current calling context for the URL. Typically, that URL is set only during
   * session initialization.
   *
   * @see org.eclipse.scout.rt.ui.html.UiSession#createAndStartClientSession()
   */
  protected URI resolveBrowserUri() {
    String url = PropertyMap.CURRENT.get().get("url");
    if (url == null) {
      return null;
    }

    try {
      return new URI(url);
    }
    catch (URISyntaxException e) {
      LOG.warn("Cannot read browser url: {}", url, e);
      return null;
    }
  }

  /**
   * Returns the memory policy to be used.
   */
  protected IMemoryPolicy resolveMemoryPolicy() {
    switch (CONFIG.getPropertyValue(MemoryPolicyProperty.class)) {
      case "small":
        return new SmallMemoryPolicy();
      case "medium":
        return new MediumMemoryPolicy();
      default:
        return new LargeMemoryPolicy();
    }
  }

  /**
   * replace the shared variable map with a new version.
   *
   * @param newMap
   */
  @Override
  public void replaceSharedVariableMapInternal(SharedVariableMap newMap) {
    m_sharedVariableMap.updateInternal(newMap);
  }

  /**
   * @deprecated use {@link #initializeSharedVariables()}, will be removed in Scout 7.1
   */
  @Deprecated
  protected void initializeSharedVariables(long timeout, TimeUnit unit) {
    initializeSharedVariables();
  }

  /**
   * Pings the server to get the initial shared variables. Blocks until the initial version of the shared variables is
   * available or the timeout is reached.
   *
   * @throws ProcessingException
   *           if interrupted (and the variables are not initialized)
   */
  protected void initializeSharedVariables() {
    BEANS.get(IPingService.class).ping("");
  }

  @Override
  public void start(String sessionId) {
    Assertions.assertFalse(m_stopping, "Session cannot be started again");
    Assertions.assertNotNull(sessionId, "Session id must not be null");
    if (isActive()) {
      throw new IllegalStateException("session is active");
    }
    m_id = sessionId;
    interceptLoadSession();
    setActive(true);

    fireSessionChangedEvent(new SessionEvent(this, SessionEvent.TYPE_STARTED));
    LOG.info("Client session started [session={}, user={}]", this, getUserId());
  }

  /**
   * Initialize the properties of the client session. This method is called in the process of the initialization right
   * before the session is activated.
   */
  @ConfigOperation
  @Order(10)
  protected void execLoadSession() {
  }

  /**
   * This method is called just before the session is stopped and can be overwritten to persist properties of the client
   * session
   */
  @ConfigOperation
  @Order(20)
  protected void execStoreSession() {
  }

  @Override
  public IDesktop getDesktop() {
    return m_desktop;
  }

  @Override
  public IDesktop getDesktopElseVirtualDesktop() {
    return m_desktop != null ? m_desktop : m_virtualDesktop;
  }

  @Override
  public void setDesktop(IDesktop desktop) {
    if (desktop == null) {
      throw new IllegalArgumentException("argument must not be null");
    }
    if (m_desktop != null) {
      throw new IllegalStateException("desktop is active");
    }
    m_desktop = desktop;
    if (m_virtualDesktop != null) {
      for (DesktopListener listener : m_virtualDesktop.getDesktopListeners()) {
        m_desktop.addDesktopListener(listener);
      }
      for (Map.Entry<String, EventListenerList> e : m_virtualDesktop.getPropertyChangeListenerMap().entrySet()) {
        String propName = e.getKey();
        EventListenerList list = e.getValue();
        if (propName == null) {
          for (PropertyChangeListener listener : list.getListeners(PropertyChangeListener.class)) {
            m_desktop.addPropertyChangeListener(listener);
          }
        }
        else {
          for (PropertyChangeListener listener : list.getListeners(PropertyChangeListener.class)) {
            m_desktop.addPropertyChangeListener(propName, listener);
          }
        }
      }
      addDataChangeListeners(m_desktop, false, m_virtualDesktop.getDataChangeListenerMap());
      addDataChangeListeners(m_desktop, true, m_virtualDesktop.getDataChangeDesktopInForegroundListeners());
      m_virtualDesktop = null;
    }
    m_desktop.initDesktop();
  }

  private final static Object[] NULL_DATA_TYPES = new Object[0];

  /**
   * Add data change listeners on the given desktop.
   */
  protected void addDataChangeListeners(IDesktop desktop, boolean requiresDesktopInForeground, Map<Object, EventListenerList> dataChangeListenerMap) {
    for (Map.Entry<Object, EventListenerList> e : dataChangeListenerMap.entrySet()) {
      Object[] dataTypes = e.getKey() == null ? NULL_DATA_TYPES : new Object[]{e.getKey()};
      EventListenerList list = e.getValue();
      for (DataChangeListener listener : list.getListeners(DataChangeListener.class)) {
        if (requiresDesktopInForeground) {
          desktop.addDataChangeDesktopInForegroundListener(listener, dataTypes);
        }
        else {
          desktop.addDataChangeListener(listener, dataTypes);
        }
      }
    }
  }

  /**
   * Close the session
   */
  @Override
  public void stop() {
    stop(m_exitCode);
  }

  @Override
  public void stop(int exitCode) {
    LOG.info("Enter stop({}) of clientSession {}", exitCode, this);

    try {
      if (m_desktop != null) {
        if (m_permitToSaveBeforeClosing.tryAcquire()) {
          LOG.info("Call desktop.doBeforeClosingInternal of clientSession {}", this);
          if (!m_desktop.doBeforeClosingInternal()) {
            m_permitToSaveBeforeClosing.release();
            LOG.info("Not stopping clientSession, user veto on {}", this);
            return;
          }
        }
      }
    }
    catch (RuntimeException | PlatformError e) {
      LOG.error("Failed to decently handle doBeforeClosingInternal", e);
    }

    // --- Point of no return ---
    ClientSessionStopHelper helper = BEANS.get(ClientSessionStopHelper.class);
    IFuture<?> termLoop = helper.scheduleJobTerminationLoop(this);
    try {
      m_stopping = true;

      if (!m_permitToStop.tryAcquire()) {
        // we are already stopping (or have been stopped)
        LOG.warn("Not stopping clientSession, could not acquire stop lock of {}", this);
        return;
      }

      LOG.info("Begin stop of clientSession {}, point of no return", this);
      m_exitCode = exitCode;
      try {
        fireSessionChangedEvent(new SessionEvent(this, SessionEvent.TYPE_STOPPING));
      }
      catch (RuntimeException | PlatformError e) {
        LOG.error("Failed to send STOPPING event.", e);
      }

      try {
        interceptStoreSession();
      }
      catch (RuntimeException | PlatformError e) {
        LOG.error("Failed to store the client session.", e);
      }

      if (m_desktop != null) {
        try {
          m_desktop.closeInternal();
        }
        catch (RuntimeException | PlatformError e) {
          LOG.error("Failed to close the desktop.", e);
        }
        finally {
          m_desktop = null;
        }
      }

      inactivateSession();
    }
    finally {
      termLoop.cancel(true);
    }
  }

  /**
   * @deprecated since 6.1 this code moved to
   *             {@link ClientSessionStopHelper#scheduleJobTerminationTimer(IClientSession)}
   */
  @Deprecated
  protected void cancelRunningJobs() {
    //nop
  }

  protected void inactivateSession() {
    try {
      ILogoutService logoutService = BEANS.opt(ILogoutService.class);
      if (logoutService != null) {
        logoutService.logout();
      }
    }
    finally {
      setActive(false);
      fireSessionChangedEvent(new SessionEvent(this, SessionEvent.TYPE_STOPPED));
      LOG.info("Client session stopped [session={}, user={}]", this, getUserId());
    }
  }

  @Override
  public boolean isStopping() {
    return m_stopping;
  }

  @Override
  public int getExitCode() {
    return m_exitCode;
  }

  @Override
  public IMemoryPolicy getMemoryPolicy() {
    return m_memoryPolicy;
  }

  @Override
  public void setMemoryPolicy(IMemoryPolicy memoryPolicy) {
    if (m_memoryPolicy != null) {
      m_memoryPolicy.removeNotify();
    }
    m_memoryPolicy = memoryPolicy;
    if (m_memoryPolicy != null) {
      m_memoryPolicy.addNotify();
    }
  }

  @Override
  public Subject getSubject() {
    return m_subject;
  }

  @Override
  public void setSubject(Subject subject) {
    m_subject = subject;
  }

  @Override
  public void setData(String key, Object value) {
    m_sessionData.set(key, value);
  }

  @Override
  public Object computeDataIfAbsent(String key, Callable<?> producer) {
    return m_sessionData.computeIfAbsent(key, producer);
  }

  @Override
  public Object getData(String key) {
    return m_sessionData.get(key);
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalClientSessionExtension<OWNER extends AbstractClientSession> extends AbstractExtension<OWNER> implements IClientSessionExtension<OWNER> {

    public LocalClientSessionExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execStoreSession(ClientSessionStoreSessionChain chain) {
      getOwner().execStoreSession();
    }

    @Override
    public void execLoadSession(ClientSessionLoadSessionChain chain) {
      getOwner().execLoadSession();
    }

  }

  protected final void interceptStoreSession() {
    List<? extends IClientSessionExtension<? extends AbstractClientSession>> extensions = getAllExtensions();
    ClientSessionStoreSessionChain chain = new ClientSessionStoreSessionChain(extensions);
    chain.execStoreSession();
  }

  protected final void interceptLoadSession() {
    List<? extends IClientSessionExtension<? extends AbstractClientSession>> extensions = getAllExtensions();
    ClientSessionLoadSessionChain chain = new ClientSessionLoadSessionChain(extensions);
    chain.execLoadSession();
  }

  @Override
  public void addListener(ISessionListener sessionListener) {
    m_eventListeners.add(ISessionListener.class, sessionListener);
  }

  @Override
  public void removeListener(ISessionListener sessionListener) {
    m_eventListeners.remove(ISessionListener.class, sessionListener);
  }

  @Override
  public IExecutionSemaphore getModelJobSemaphore() {
    return m_modelJobSemaphore;
  }

  protected void fireSessionChangedEvent(final SessionEvent event) {
    List<ISessionListener> listeners = new ArrayList<>();
    listeners.addAll(Arrays.asList(m_eventListeners.getListeners(ISessionListener.class))); // session specific listeners
    listeners.addAll(BEANS.all(IGlobalSessionListener.class)); // global listeners
    for (final ISessionListener listener : listeners) {
      try {
        listener.sessionChanged(event);
      }
      catch (RuntimeException e) {
        if (event.getType() != SessionEvent.TYPE_STOPPED && event.getType() != SessionEvent.TYPE_STOPPING) {
          throw e; // throw if not stopping
        }
        // stopping: give all listeners a chance to do their cleanup
        LOG.warn("Error in session listener {}.", listener.getClass(), e);
      }
    }
  }

  @Override
  public String toString() {
    return super.toString() + "[id = " + getId() + "]";
  }
}

/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client;

import static java.util.Collections.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.ClientConfigProperties.MemoryPolicyProperty;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ClientSessionChains.ClientSessionLoadSessionChain;
import org.eclipse.scout.rt.client.extension.ClientSessionChains.ClientSessionStoreSessionChain;
import org.eclipse.scout.rt.client.extension.IClientSessionExtension;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionStopHelper;
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
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
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
import org.eclipse.scout.rt.shared.session.SessionMetricsHelper;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClientSession extends AbstractPropertyObserver implements IClientSession, IExtensibleObject {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractClientSession.class);

  protected static final String SESSION_TYPE = "client";

  protected final SessionMetricsHelper m_sessionMetrics = BEANS.get(SessionMetricsHelper.class);

  private final FastListenerList<ISessionListener> m_eventListeners;
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
  private volatile Subject m_subject;

  private final SharedVariableMap m_sharedVariableMap;
  private Set<String> m_exposedSharedVariables;

  private IMemoryPolicy m_memoryPolicy;
  private final SessionData m_sessionData;
  private UserAgent m_userAgent;
  private final ObjectExtensions<AbstractClientSession, IClientSessionExtension<? extends AbstractClientSession>> m_objectExtensions;
  private URI m_browserUri;

  public AbstractClientSession(boolean autoInitConfig) {
    m_eventListeners = new FastListenerList<>();
    m_sessionData = new SessionData();
    m_stateLock = new Object();
    m_userAgent = UserAgent.get();
    m_subject = Subject.getSubject(AccessController.getContext());
    m_objectExtensions = new ObjectExtensions<>(this, true);
    m_sharedVariableMap = new SharedVariableMap();
    m_exposedSharedVariables = null;

    m_sessionMetrics.sessionCreated(SESSION_TYPE);
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
    return new LocalClientSessionExtension<>(this);
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), this::initConfig);
  }

  @Override
  public Set<String> getExposedSharedVariables() {
    var exposedVariables = m_exposedSharedVariables;
    if (exposedVariables == null) {
      return emptySet();
    }
    return unmodifiableSet(exposedVariables);
  }

  /**
   * @return A {@link Set} of shared variable names (see {@link #getSharedVariableMap()}) that should be sent to the
   * browser. Default returns {@code null} (no properties are synced).
   */
  @Order(100)
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Set<String> getConfiguredExposedSharedVariables() {
    return null;
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
    m_exposedSharedVariables = getConfiguredExposedSharedVariables();

    setMemoryPolicy(resolveMemoryPolicy());
    m_sharedVariableMap.addPropertyChangeListener(e -> {
      if ("values".equals(e.getPropertyName())) {
        ModelJobs.schedule(() -> propertySupport.firePropertyChange(PROP_SHARED_VARIABLE_MAP, e.getOldValue(), e.getNewValue()),
            ModelJobs.newInput(ClientRunContexts.copyCurrent()));
      }
    });
  }

  /**
   * Resolves the browser URI or {@code null} if it could not be resolved.
   * <p>
   * The default implementation looks in the current calling context for the URL. Typically, that URL is set only during
   * session initialization.
   *
   * @see org.eclipse.scout.rt.ui.html.UiSession#createAndStartClientSession(Locale, UserAgent, Map)
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
   *          map to replace the current one with
   */
  @Override
  public void replaceSharedVariableMapInternal(SharedVariableMap newMap) {
    m_sharedVariableMap.updateInternal(newMap);
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
      throw new IllegalArgumentException("desktop must not be null");
    }
    if (desktop == m_desktop) {
      return;
    }
    if (m_desktop != null) {
      throw new IllegalStateException("desktop is already set and cannot be changed");
    }
    m_desktop = desktop;
    if (m_virtualDesktop != null) {
      m_virtualDesktop.setRealDesktop(desktop);
      m_virtualDesktop = null;
    }
    m_desktop.init();
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
        if (m_permitToSaveBeforeClosing.tryAcquire()) {//NOSONAR
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
          m_desktop.dispose();
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
      m_sessionMetrics.sessionDestroyed(SESSION_TYPE);
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
  public IFastListenerList<ISessionListener> sessionListeners() {
    return m_eventListeners;
  }

  @Override
  public IExecutionSemaphore getModelJobSemaphore() {
    return m_modelJobSemaphore;
  }

  protected void fireSessionChangedEvent(final SessionEvent event) {
    // session specific listeners
    sessionListeners().list().forEach(listener -> handleSessionEvent(listener, event));
    // global listeners
    BEANS.all(IGlobalSessionListener.class).forEach(listener -> handleSessionEvent(listener, event));
  }

  protected void handleSessionEvent(ISessionListener listener, SessionEvent event) {
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

  @Override
  public String toString() {
    return super.toString() + "[id = " + getId() + "]";
  }
}

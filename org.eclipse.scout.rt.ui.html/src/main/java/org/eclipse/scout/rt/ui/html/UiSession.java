/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.math.BigInteger;
import java.security.AccessController;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopUIFacade;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.server.commons.servlet.CookieUtility;
import org.eclipse.scout.rt.server.commons.servlet.HttpClientInfo;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpResourceCache;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.job.filter.event.SessionJobEventFilter;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterRegistry;
import org.eclipse.scout.rt.ui.html.json.JsonClientSession;
import org.eclipse.scout.rt.ui.html.json.JsonEventProcessor;
import org.eclipse.scout.rt.ui.html.json.JsonLocale;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonRequest.RequestType;
import org.eclipse.scout.rt.ui.html.json.JsonRequestHelper;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiSession implements IUiSession {

  private static final Logger LOG = LoggerFactory.getLogger(UiSession.class);

  private static final long ROOT_ID = 1;
  private static final String EVENT_LOCALE_CHANGED = "localeChanged";
  private static final String EVENT_DISPOSE_ADAPTER = "disposeAdapter";
  private static final String EVENT_RELOAD_PAGE = "reloadPage";
  /**
   * in milliseconds
   */
  private static final long ADDITIONAL_POLLING_DELAY = 100;
  private static final int MAX_RESPONSE_HISTORY_SIZE = 10;
  // Because static fields are initialized before the bean manager is ready (class is initialized by Jandex scan),
  // the following beans must be set lazily. See the corresponding static getters.
  private static final LazyValue<HttpSessionHelper> HTTP_SESSION_HELPER = new LazyValue<>(HttpSessionHelper.class);
  private static final LazyValue<JsonRequestHelper> JSON_REQUEST_HELPER = new LazyValue<>(JsonRequestHelper.class);
  private static final LazyValue<SecureRandom> SECURE_RANDOM = new LazyValue<>(new Callable<SecureRandom>() {
    @Override
    public SecureRandom call() throws Exception {
      return SecurityUtility.createSecureRandom();
    }
  });

  private final JsonAdapterRegistry m_jsonAdapterRegistry;
  private final JsonEventProcessor m_jsonEventProcessor;
  private final P_RootAdapter m_rootJsonAdapter;
  private final AtomicLong m_jsonAdapterSeq = new AtomicLong(ROOT_ID);
  private final AtomicLong m_responseSequenceNo = new AtomicLong(1);
  /**
   * Synchronized map. When iterating over the map, synchronize on the map object
   */
  private final SortedMap<Long, JsonResponse> m_responseHistory = Collections.synchronizedSortedMap(new TreeMap<Long, JsonResponse>());
  private final ReentrantLock m_uiSessionLock = new ReentrantLock();
  private final HttpContext m_httpContext = new HttpContext();
  private final BlockingQueue<Object> m_pollerQueue = new ArrayBlockingQueue<>(1, true);
  private final Object m_pollerQueueLock = new Object();
  private final Object m_notificationToken = new Object();
  private final IHttpResourceCache m_httpResourceCache = BEANS.get(HttpResourceCache.class);

  private volatile boolean m_initialized;
  private volatile ISessionStore m_sessionStore;
  private volatile String m_uiSessionId;
  private volatile IClientSession m_clientSession;
  private volatile JsonResponse m_currentJsonResponse;
  private volatile JsonRequest m_currentJsonRequest;
  private volatile boolean m_processingJsonRequest;
  private volatile boolean m_disposing;
  private volatile boolean m_disposed;
  private volatile IRegistrationHandle m_uiDataAvailableListener;
  private volatile long m_lastAccessedTime;
  private volatile RunMonitor m_pollerMonitor;

  public UiSession() {
    m_jsonAdapterRegistry = createJsonAdapterRegistry();
    m_jsonEventProcessor = createJsonEventProcessor();
    m_rootJsonAdapter = new P_RootAdapter(this);
  }

  protected JsonAdapterRegistry createJsonAdapterRegistry() {
    return new JsonAdapterRegistry();
  }

  protected final JsonAdapterRegistry jsonAdapterRegistry() {
    return m_jsonAdapterRegistry;
  }

  protected JsonEventProcessor createJsonEventProcessor() {
    return new JsonEventProcessor(this);
  }

  protected final JsonEventProcessor jsonEventProcessor() {
    return m_jsonEventProcessor;
  }

  protected final AtomicLong jsonAdapterSeq() {
    return m_jsonAdapterSeq;
  }

  protected final AtomicLong responseSequenceNo() {
    return m_responseSequenceNo;
  }

  @Override
  public ReentrantLock uiSessionLock() {
    return m_uiSessionLock;
  }

  protected final HttpContext httpContext() {
    return m_httpContext;
  }

  protected final BlockingQueue<Object> pollerQueue() {
    return m_pollerQueue;
  }

  protected final Object notificationToken() {
    return m_notificationToken;
  }

  protected final RunMonitor pollerMonitor() {
    return m_pollerMonitor;
  }

  @Override
  public void init(HttpServletRequest req, HttpServletResponse resp, JsonStartupRequest jsonStartupReq) {
    if (currentSubject() == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }
    if (m_initialized) {
      throw new IllegalStateException("Already initialized");
    }
    m_initialized = true;

    // Set initial "touch" time
    touch();

    try {
      m_httpContext.set(req, resp);
      m_currentJsonRequest = jsonStartupReq;
      HttpSession httpSession = req.getSession();

      // Create a special startup response (explicitly _without_ sequenceNo)
      m_currentJsonResponse = createJsonStartupResponse();

      // Assign uiSessionId (https://www.owasp.org/index.php/Cross-Site_Request_Forgery_%28CSRF%29_Prevention_Cheat_Sheet#General_Recommendation:_Synchronizer_Token_Pattern)
      m_uiSessionId = createUiSessionId(jsonStartupReq);
      m_currentJsonResponse.getStartupData().put("uiSessionId", m_uiSessionId);

      // Remember the store here, because getting it from an invalidated httpSession does not work (there might even be dead locks!)
      m_sessionStore = getHttpSessionHelper().getSessionStore(httpSession);

      // Look up the requested client session (create and start a new one if necessary)
      m_clientSession = getOrCreateClientSession(httpSession, req, jsonStartupReq);

      // Add a cookie with the preferred user-language
      storePreferredLocaleInCookie(resp, m_clientSession.getLocale());

      // Apply theme from model to HTTP session and cookie
      boolean reloadPage = initUiTheme(req, resp, httpSession);

      // When theme changes, stop initialization (UI session will not be used anyway) and instruct the client to reload the page
      if (reloadPage) {
        putReloadPageStartupData();
        LOG.info("Requested page reload for new UiSession with ID {}", m_uiSessionId);
        return;
      }

      // Register job listener to signal poller once possible UI data to be transported to the UI is available.
      installUiDataAvailableListener(m_clientSession);

      // Create a new JsonAdapter for the client session
      JsonClientSession<?> jsonClientSessionAdapter = createClientSessionAdapter(m_clientSession);

      // Start desktop
      startDesktop(jsonStartupReq.getSessionStartupParams());

      // Fill startupData with everything that is needed to start the session on the UI
      putInitializationStartupData(jsonClientSessionAdapter.getId());

      putUrlHintsStartupData();

      LOG.info("UiSession with ID {} initialized", m_uiSessionId);
    }
    finally {
      m_httpContext.clear();
      m_currentJsonRequest = null;
    }
  }

  protected JsonResponse createJsonResponse() {
    return new JsonResponse(m_responseSequenceNo.getAndIncrement());
  }

  protected JsonResponse createJsonStartupResponse() {
    JsonResponse response = new JsonResponse();
    response.markAsStartupResponse();
    return response;
  }

  protected String createUiSessionId(JsonStartupRequest jsonStartupReq) {
    String id = new BigInteger(130, getSecureRandom()).toString(32); // http://stackoverflow.com/questions/29183818/why-use-tostring32-and-not-tostring36
    return jsonStartupReq.getPartId() + ":" + id;
  }

  protected IClientSession getOrCreateClientSession(HttpSession httpSession, HttpServletRequest req, JsonStartupRequest jsonStartupReq) {
    String requestedClientSessionId = jsonStartupReq.getClientSessionId();
    IClientSession clientSession = sessionStore().getClientSessionForUse(requestedClientSessionId);

    if (clientSession != null) {
      // Found existing client session
      LOG.info("Using cached client session [clientSessionId={}]", clientSession.getId());
      return clientSession;
    }

    // No client session for the requested ID was found, so create one
    clientSession = createAndStartClientSession(req.getLocale(), createUserAgent(jsonStartupReq), jsonStartupReq.getSessionStartupParams());
    LOG.info("Created new client session [clientSessionId={}, userAgent={}]", clientSession.getId(), clientSession.getUserAgent());
    // Ensure session is active
    if (!clientSession.isActive()) {
      throw new UiException("ClientSession is not active, there must have been a problem with loading or starting [clientSessionId=" + clientSession.getId() + "]");
    }

    // At this point we have a valid, active clientSession. There is no need to put in the session store
    // here, because that will automatically be done, when the UI session is registered with the store.
    // The lock in JsonMessageRequestHandler ensures that the same UI session cannot be initialized
    // in parallel.
    return clientSession;
  }

  protected UserAgent createUserAgent(JsonStartupRequest jsonStartupReq) {
    HttpClientInfo httpClientInfo = HttpClientInfo.get(currentHttpRequest());
    UserAgents userAgentBuilder = UserAgents
        .create()
        .withUiLayer(UiLayer.HTML)
        .withUiDeviceType(UiDeviceType.DESKTOP)
        .withUiEngineType(httpClientInfo.getEngineType())
        .withUiSystem(httpClientInfo.getSystem())
        .withDeviceId(httpClientInfo.getUserAgent());

    JSONObject userAgent = jsonStartupReq.getUserAgent();
    if (userAgent != null) {
      // TODO [7.0] cgu: it would be great if UserAgent could be changed dynamically, to switch from mobile to tablet mode on the fly, should be done as event in JsonClientSession
      String uiDeviceTypeStr = userAgent.optString("deviceType", null);
      if (uiDeviceTypeStr != null) {
        userAgentBuilder.withUiDeviceType(UiDeviceType.createByIdentifier(uiDeviceTypeStr));
      }
      String uiLayerStr = userAgent.optString("uiLayer", null);
      if (uiLayerStr != null) {
        userAgentBuilder.withUiLayer(UiLayer.createByIdentifier(uiLayerStr));
      }
      boolean touch = userAgent.optBoolean("touch", false);
      userAgentBuilder.withTouch(touch);
    }
    return userAgentBuilder.build();
  }

  protected IClientSession createAndStartClientSession(Locale locale, UserAgent userAgent, Map<String, String> sessionStartupParams) {
    return BEANS.get(ClientSessionProvider.class).provide(ClientRunContexts.copyCurrent()
        .withLocale(locale)
        .withUserAgent(userAgent)
        .withProperties(sessionStartupParams)); // Make startup parameters available at {@link PropertyMap#CURRENT} during client session is starting.
  }

  protected void storePreferredLocaleInCookie(HttpServletResponse resp, Locale locale) {
    CookieUtility.addPersistentCookie(resp, PREFERRED_LOCALE_COOKIE_NAME, locale.toLanguageTag());
  }

  /**
   * Updates the locale Cookie but only when a current HTTP response exists. Which means when the locale of the session
   * changes during a client-job, the cookie cannot be updated.
   */
  protected void updatePreferredLocaleCookie(Locale locale) {
    HttpServletResponse resp = currentHttpResponse();
    if (resp != null) {
      storePreferredLocaleInCookie(resp, locale);
    }
  }

  /**
   * Info: instead of reload the current page in the browser, we could build a servlet-filter which determines what
   * theme the user has _before_ the client-session is created. However the 'reload' will only be performed in the case
   * where the browser sends a cookie that doesn't match the user-settings which should not happen often.
   *
   * @return Whether or not the page must be reloaded by the browser (required when theme changes after client-session
   *         has been initialized)
   */
  protected boolean initUiTheme(HttpServletRequest req, HttpServletResponse resp, HttpSession httpSession) {
    String modelTheme = m_clientSession.getDesktop().getTheme();
    String currentTheme = UiThemeUtility.getTheme(req);
    if (modelTheme == null) {
      modelTheme = ObjectUtility.nvl(currentTheme, UiThemeUtility.getConfiguredTheme());
      m_clientSession.getDesktop().setTheme(currentTheme);
    }
    boolean reloadPage = !modelTheme.equals(currentTheme);
    UiThemeUtility.storeTheme(resp, httpSession, modelTheme);
    LOG.debug("UI theme model={} current={} reloadPage={}", modelTheme, currentTheme, reloadPage);
    return reloadPage;
  }

  protected JsonClientSession<?> createClientSessionAdapter(final IClientSession clientSession) {
    // Ensure adapter is created in model job, because the model might be accessed during the adapter's initialization
    final IFuture<JsonClientSession<?>> future = ModelJobs.schedule(new Callable<JsonClientSession<?>>() {

      @Override
      public JsonClientSession<?> call() throws Exception {
        return (JsonClientSession<?>) createJsonAdapter(clientSession, m_rootJsonAdapter);
      }
    }, ModelJobs.newInput(
        ClientRunContexts.copyCurrent()
            .withSession(clientSession, true))
        .withName("Starting JsonClientSession")
        .withExceptionHandling(null, false /* propagate */)); // exception handling done by caller

    return BEANS.get(UiJobs.class).awaitAndGet(future);
  }

  protected void startDesktop(Map<String, String> sessionStartupParams) {
    final IFuture<Void> future = ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        IDesktop desktop = m_clientSession.getDesktop();
        IDesktopUIFacade uiFacade = desktop.getUIFacade();
        if (!desktop.isOpened()) {
          uiFacade.openFromUI();
        }
        uiFacade.fireGuiAttached();
      }
    }, ModelJobs.newInput(
        ClientRunContexts.copyCurrent()
            .withSession(m_clientSession, true)
            .withProperties(sessionStartupParams)) // Make startup parameters available at {@link PropertyMap#CURRENT} during desktop attaching
        .withName("Starting Desktop")
        .withExceptionHandling(null, false /* propagate */)); // exception handling done by caller

    BEANS.get(UiJobs.class).awaitAndGet(future);
  }

  protected void putReloadPageStartupData() {
    final JSONObject startupData = m_currentJsonResponse.getStartupData();
    startupData.put("reloadPage", true);
    startupData.put("clientSessionId", m_clientSession.getId()); // Send back clientSessionId to allow the browser to attach to the same client session on page reload
  }

  protected void putInitializationStartupData(final String clientSessionAdapterId) {
    final IFuture<Locale> future = ModelJobs.schedule(new Callable<Locale>() {
      @Override
      public Locale call() throws Exception {
        return m_clientSession.getLocale();
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent()
        .withSession(m_clientSession, true))
        .withName("Looking up Locale")
        .withExceptionHandling(null, false)); // exception handling done by caller

    final JSONObject startupData = m_currentJsonResponse.getStartupData();
    startupData.put("clientSessionId", m_clientSession.getId()); // Send back clientSessionId to allow the browser to attach to the same client session on page reload
    startupData.put("clientSession", clientSessionAdapterId);
    putLocaleData(startupData, BEANS.get(UiJobs.class).awaitAndGet(future));
  }

  protected void putUrlHintsStartupData() {
    final JSONObject startupData = m_currentJsonResponse.getStartupData();
    if (UrlHints.isInspectorHint(currentHttpRequest())) {
      startupData.put("inspector", true);
    }
  }

  @Override
  public final boolean isInitialized() {
    return m_initialized;
  }

  protected final ISessionStore sessionStore() {
    return m_sessionStore;
  }

  @Override
  public final String getUiSessionId() {
    return m_uiSessionId;
  }

  /**
   * do not use this internal method
   */
  protected final void setUiSessionIdInternal(String uiSessionId) {
    m_uiSessionId = uiSessionId;
  }

  @Override
  public final String getClientSessionId() {
    return (m_clientSession == null ? null : m_clientSession.getId());
  }

  @Override
  public final IClientSession getClientSession() {
    return m_clientSession;
  }

  /**
   * do not use this internal method
   */
  protected final void setClientSessionInternal(IClientSession clientSession) {
    m_clientSession = clientSession;
  }

  @Override
  public void touch() {
    m_lastAccessedTime = System.currentTimeMillis();
  }

  @Override
  public long getLastAccessedTime() {
    return m_lastAccessedTime;
  }

  @Override
  public void dispose() {

    // Inform the model the UI has been detached. There are different cases we handle here:
    // 1. page reload (unload event) dispose method is called once
    // 2. logout (Session.stop()) dispose method is called _twice_, 1st call sets the disposing flag,
    //    on the 2nd call, the desktop is already gone.
    if (!m_disposing) {
      // Current thread is the model thread if dispose is called by clientSession.stop(), otherwise (e.g. page reload) dispose is called from the UI thread
      if (ModelJobs.isModelThread()) {
        getClientSession().getDesktop().getUIFacade().fireGuiDetached();
      }
      else {
        final ClientRunContext clientRunContext = ClientRunContexts.copyCurrent().withSession(m_clientSession, true);
        ModelJobs.schedule(new IRunnable() {
          @Override
          public void run() throws Exception {
            getClientSession().getDesktop().getUIFacade().fireGuiDetached();
          }
        }, ModelJobs.newInput(clientRunContext)
            .withName("Detaching Gui")
            .withExceptionHandling(null, false)); // Propagate exception to caller (UIServlet)
      }
    }

    if (isProcessingJsonRequest()) {
      // If there is a request in progress just mark the session as being disposed.
      // The actual disposing happens before returning to the client, see processJsonRequest().
      m_disposing = true;
      return;
    }

    LOG.info("Disposing UI session with ID {}...", m_uiSessionId);
    if (m_disposed) {
      LOG.trace("UI session with ID {} already disposed.", m_uiSessionId);
      return;
    }
    m_disposed = true;

    sessionStore().unregisterUiSession(this); // also stops and removes client session if necessary

    uninstallUiDataAvailableListener();
    signalPoller(); // Notify waiting requests - should not delay web-container shutdown

    m_jsonAdapterRegistry.disposeAdapters();
    m_httpContext.clear();
    m_currentJsonResponse = null;
  }

  @Override
  public final boolean isDisposed() {
    return m_disposed;
  }

  protected final void setDisposedInternal(boolean disposed) {
    m_disposed = disposed;
  }

  protected final boolean isDisposing() {
    return m_disposing;
  }

  protected final void setDisposingInternal(boolean disposing) {
    m_disposing = disposing;
  }

  protected Subject currentSubject() {
    return Subject.getSubject(AccessController.getContext());
  }

  @Override
  public JsonResponse currentJsonResponse() {
    return m_currentJsonResponse;
  }

  /**
   * do not use this internal method
   */
  protected final void setCurrentJsonResponseInternal(JsonResponse jsonResponse) {
    m_currentJsonResponse = jsonResponse;
  }

  protected final JsonRequest currentJsonRequest() {
    return m_currentJsonRequest;
  }

  /**
   * do not use this internal method
   */
  protected final void setCurrentJsonRequestInternal(JsonRequest jsonRequest) {
    m_currentJsonRequest = jsonRequest;
  }

  @Override
  public HttpServletRequest currentHttpRequest() {
    return m_httpContext.getRequest();
  }

  @Override
  public HttpServletResponse currentHttpResponse() {
    return m_httpContext.getResponse();
  }

  @Override
  public void confirmResponseProcessed(Long sequenceNo) {
    if (sequenceNo == null) {
      return;
    }
    // Update response history
    int removeCount = 0;
    // Synchronize for iteration over Collections.synchronizedList
    synchronized (m_responseHistory) {
      for (Iterator<Long> it = m_responseHistory.keySet().iterator(); it.hasNext();) {
        Long key = it.next();
        if (key <= sequenceNo) {
          it.remove();
          removeCount++;
        }
      }
    }
    LOG.debug("Cleaned up response history (-{}). New content: {} [#ACK={}, uiSessionId={}]", removeCount, m_responseHistory.keySet(), sequenceNo, m_uiSessionId);
  }

  @Override
  public JSONObject processJsonRequest(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse, final JsonRequest jsonRequest) {
    final ClientRunContext clientRunContext = ClientRunContexts.copyCurrent().withSession(m_clientSession, true);

    m_httpContext.set(servletRequest, servletResponse);
    m_currentJsonRequest = jsonRequest;
    try {
      m_processingJsonRequest = true;
      try {
        // 1. Process the JSON request.
        ModelJobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            processJsonRequestInternal();
          }
        }, ModelJobs.newInput(clientRunContext)
            .withName("Processing JSON request")
            .withExecutionHint(UiJobs.EXECUTION_HINT_POLL_REQUEST, jsonRequest.getRequestType() == RequestType.POLL_REQUEST)
            // Handle exceptions instantaneously in job manager, and not by submitter.
            // That is because the submitting thread might not be waiting anymore, because interrupted or returned because requiring 'user interaction'.
            .withExceptionHandling(BEANS.get(ExceptionHandler.class), true));

        // 2. Wait for all model jobs of the session.
        BEANS.get(UiJobs.class).awaitModelJobs(m_clientSession, ExceptionHandler.class);
      }
      finally {
        // Reset this flag _before_ the "response-to-json" job (#3), because writing to the response while transforming would be unsafe and unreliable.
        m_processingJsonRequest = false;
      }

      // 3. Transform the response to JSON.
      final IFuture<JSONObject> future = ModelJobs.schedule(newResponseToJsonTransformer(), ModelJobs.newInput(clientRunContext.copy()
          .withRunMonitor(BEANS.get(RunMonitor.class))) // separate RunMonitor to not cancel 'response-to-json' job once processing is cancelled
          .withName("Transforming response to JSON")
          .withExecutionHint(UiJobs.EXECUTION_HINT_RESPONSE_TO_JSON)
          .withExecutionHint(UiJobs.EXECUTION_HINT_POLL_REQUEST, jsonRequest.getRequestType() == RequestType.POLL_REQUEST)
          .withExceptionHandling(null, false)); // Propagate exception to caller (UIServlet)
      try {
        return BEANS.get(UiJobs.class).awaitAndGet(future);
      }
      catch (ThreadInterruptedError e) { // NOSONAR
        future.cancel(true);
        return null;
      }
      catch (FutureCancelledError e) { // NOSONAR
        return null;
      }
    }
    finally {
      m_httpContext.clear();
      m_currentJsonRequest = null;
      if (m_disposing) {
        dispose();
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("Adapter count after request: {}", m_jsonAdapterRegistry.size());
      }
    }
  }

  /**
   * Indicates if currently a /json request is being processed in such a way that anything written to the current JSON
   * response will be taken back to the UI with that call. If this flag is <code>false</code>, the poller has to be
   * awaken to send something to the UI ({@link #signalPoller()}).
   */
  protected final boolean isProcessingJsonRequest() {
    return m_processingJsonRequest;
  }

  /**
   * do not use this internal method
   */
  protected final void setProcessingJsonRequest(boolean processingJsonRequest) {
    m_processingJsonRequest = processingJsonRequest;
  }

  /**
   * <b>Do not call this internal method directly!</b> It should only be called by
   * {@link #processJsonRequest(HttpServletRequest, JsonRequest)} which ensures that the required state is set up
   * correctly (and will be cleaned up later) and is run as a model job.
   */
  protected void processJsonRequestInternal() {
    m_jsonEventProcessor.processEvents(m_currentJsonRequest, m_currentJsonResponse);
  }

  /**
   * <b>Do not call this internal method directly!</b> It should only be called by the "response-to-json transformer" (
   * {@link #newResponseToJsonTransformer()}) which ensures that the required state is set up correctly (and will be
   * cleaned up later) and is run as a model job.
   */
  protected JSONObject responseToJsonInternal() {
    // Remember response in history
    if (m_currentJsonResponse.getSequenceNo() != null) {
      synchronized (m_responseHistory) {
        if (m_responseHistory.size() > MAX_RESPONSE_HISTORY_SIZE) {
          // Remove oldest entry to free up memory (protection against malicious clients that send no or wrong #ACKs)
          Long oldestSeqNo = m_responseHistory.firstKey();
          LOG.warn("Max. response history size exceeded for UI session {}, dropping oldest response #{}", m_uiSessionId, oldestSeqNo);
          m_responseHistory.remove(oldestSeqNo);
        }
        m_responseHistory.put(m_currentJsonResponse.getSequenceNo(), m_currentJsonResponse);
        LOG.debug("Added response #{} to history {} for UI session {}", m_currentJsonResponse.getSequenceNo(), m_responseHistory.keySet(), m_uiSessionId);
      }
    }

    // Convert response to JSON (must be done in model thread due to potential model access inside the toJson() method).
    return m_currentJsonResponse.toJson();
  }

  /**
   * @return a new {@link Callable} that returns the current {@link JsonResponse} as JSON and prepares a new current
   *         json response for future jobs. The callable <b>must</b> be called from a model job.
   */
  protected Callable<JSONObject> newResponseToJsonTransformer() {
    return new Callable<JSONObject>() {
      @Override
      public JSONObject call() throws Exception {
        try {
          return responseToJsonInternal();
        }
        catch (RuntimeException e) {
          LOG.warn("Error while transforming response to JSON: {}", m_currentJsonResponse, e);
          // Return UI error, with same sequenceNo to keep response queue order consistent
          return getJsonRequestHelper().createUnrecoverableFailureResponse(m_currentJsonResponse.getSequenceNo());
        }
        finally {
          // Create a new JSON response for future jobs. This is also done in case of an exception, because apparently the
          // response is corrupt and the exception is likely to happen again.
          m_currentJsonResponse = createJsonResponse();
        }
      }
    };
  }

  @Override
  public JSONObject processFileUpload(HttpServletRequest req, HttpServletResponse res, final IBinaryResourceConsumer resourceConsumer, final List<BinaryResource> uploadResources, final Map<String, String> uploadProperties) {
    final ClientRunContext clientRunContext = ClientRunContexts.copyCurrent().withSession(m_clientSession, true);

    m_httpContext.set(req, res);
    try {
      m_processingJsonRequest = true;
      try {
        // 1. Process the JSON request.
        ModelJobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            resourceConsumer.consumeBinaryResource(uploadResources, uploadProperties);
          }
        }, ModelJobs.newInput(clientRunContext)
            .withName("Processing file upload request")
            // Handle exceptions instantaneously in job manager, and not by submitter.
            // That is because the submitting thread might not be waiting anymore, because interrupted or returned because requiring 'user interaction'.
            .withExceptionHandling(BEANS.get(ExceptionHandler.class), true));

        // 2. Wait for all model jobs of the session.
        BEANS.get(UiJobs.class).awaitModelJobs(m_clientSession, ExceptionHandler.class);
      }
      finally {
        // Reset this flag _before_ the "response-to-json" job (#3), because writing to the response while transforming would be unsafe and unreliable.
        m_processingJsonRequest = false;
      }

      // 3. Transform the response to JSON.
      final IFuture<JSONObject> future = ModelJobs.schedule(newResponseToJsonTransformer(), ModelJobs.newInput(clientRunContext.copy()
          .withRunMonitor(BEANS.get(RunMonitor.class))) // separate RunMonitor to not cancel 'response-to-json' job once processing is cancelled
          .withName("Transforming response to JSON")
          .withExecutionHint(UiJobs.EXECUTION_HINT_RESPONSE_TO_JSON)
          .withExceptionHandling(null, false)); // Propagate exception to caller (UIServlet)
      try {
        return BEANS.get(UiJobs.class).awaitAndGet(future);
      }
      catch (ThreadInterruptedError e) { // NOSONAR
        future.cancel(true);
        return null;
      }
      catch (FutureCancelledError e) { // NOSONAR
        return null;
      }
    }
    finally {
      m_httpContext.clear();
      if (m_disposing) {
        dispose();
      }
    }
  }

  @Override
  public void processCancelRequest() {
    // Cancel all running model jobs for the requested session (interrupt if necessary)
    Jobs.getJobManager().cancel(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(getClientSession()))
        .andMatchNotExecutionHint(UiJobs.EXECUTION_HINT_RESPONSE_TO_JSON)
        .andMatchNotExecutionHint(UiJobs.EXECUTION_HINT_POLL_REQUEST)
        .andMatchNotExecutionHint(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED)
        .toFilter(), true);
  }

  @Override
  public JSONObject processSyncResponseQueueRequest(JsonRequest jsonRequest) {
    LOG.debug("Synchronize response queue {} for UI session {}", m_responseHistory.keySet(), m_uiSessionId);
    synchronized (m_responseHistory) {
      if (m_responseHistory.isEmpty()) {
        return null;
      }
      Long lastSentSequenceNo = m_responseHistory.lastKey();
      JsonResponse combinedResponse = new JsonResponse(lastSentSequenceNo);
      for (JsonResponse response : m_responseHistory.values()) {
        combinedResponse.combine(response);
      }
      return combinedResponse.toJson();
    }
  }

  @Override
  public void logout() {
    LOG.info("Logging out from UI session with ID {} [clientSessionId={}, processingJsonRequest={}]", m_uiSessionId, getClientSessionId(), isProcessingJsonRequest());

    // Redirect client to "you are now logged out" screen
    if (isProcessingJsonRequest()) {
      boolean platformValid = (Platform.get() != null && Platform.get().getState() == IPlatform.State.PlatformStarted);
      if (m_currentJsonResponse != null && platformValid) {
        m_currentJsonResponse.addActionEvent(getUiSessionId(), "logout", createLogoutEventData());
      }
    }

    // Dispose UI session. This will also remove the client session from the session store.
    dispose();
    LOG.info("Logged out successfully from UI session with ID {}", m_uiSessionId);
  }

  protected JSONObject createLogoutEventData() {
    JSONObject obj = new JSONObject();
    obj.put("redirectUrl", getLogoutRedirectUrl());
    return obj;
  }

  @Override
  public String getLogoutRedirectUrl() {
    return "logout";
  }

  @Override
  public final IJsonAdapter<?> getRootJsonAdapter() {
    return m_rootJsonAdapter;
  }

  @Override
  public String createUniqueId() {
    return "" + m_jsonAdapterSeq.incrementAndGet();
  }

  @Override
  public IJsonAdapter<?> getJsonAdapter(String id) {
    return m_jsonAdapterRegistry.getById(id);
  }

  @Override
  public <M> List<IJsonAdapter<M>> getJsonAdapters(M model) {
    return m_jsonAdapterRegistry.getByModel(model);
  }

  @Override
  public List<IJsonAdapter<?>> getJsonChildAdapters(IJsonAdapter<?> parent) {
    return m_jsonAdapterRegistry.getByParentAdapter(parent);
  }

  @Override
  public <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model, IJsonAdapter<?> parent) {
    return getJsonAdapter(model, parent, true);
  }

  @Override
  public <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model, IJsonAdapter<?> parent, boolean checkRoot) {
    A jsonAdapter = m_jsonAdapterRegistry.getByModelAndParentAdapter(model, parent);
    if (jsonAdapter == null && checkRoot) {
      jsonAdapter = m_jsonAdapterRegistry.getByModelAndParentAdapter(model, getRootJsonAdapter());
    }
    return jsonAdapter;
  }

  @Override
  public <M, A extends IJsonAdapter<? super M>> A getOrCreateJsonAdapter(M model, IJsonAdapter<?> parent) {
    A jsonAdapter = getJsonAdapter(model, parent);
    if (jsonAdapter != null) {
      return jsonAdapter;
    }
    return createJsonAdapter(model, parent);
  }

  @Override
  public <M, A extends IJsonAdapter<? super M>> A createJsonAdapter(M model, IJsonAdapter<?> parent) {
    A jsonAdapter = newJsonAdapter(model, parent);

    // because it's a new adapter we must add it to the response
    m_currentJsonResponse.addAdapter(jsonAdapter);
    return jsonAdapter;
  }

  /**
   * Creates an adapter instance for the given model using {@link MainJsonObjectFactory} and calls the
   * <code>init()</code> method on the created instance.
   */
  protected <M, A extends IJsonAdapter<? super M>> A newJsonAdapter(M model, IJsonAdapter<?> parent) {
    String id = createUniqueId();
    @SuppressWarnings("unchecked")
    A adapter = (A) MainJsonObjectFactory.get().createJsonAdapter(model, this, id, parent);
    adapter.init();
    return adapter;
  }

  @Override
  public void registerJsonAdapter(IJsonAdapter<?> jsonAdapter) {
    m_jsonAdapterRegistry.add(jsonAdapter);
  }

  @Override
  public void unregisterJsonAdapter(String id) {
    // Remove it from the registry. All subsequent calls of "getAdapter(id)" will return null.
    m_jsonAdapterRegistry.remove(id);
    // Remove it completely from the response (including events targeting the adapter).
    m_currentJsonResponse.removeJsonAdapter(id);
  }

  /**
   * Installs a job listener that notifies the poller whenever some UI data are possibly available to be transported to
   * the UI. That means, that a model job on the given client session completed, or requires 'UI interaction', or is a
   * periodic job with a round completed. However, the poller is only signaled if no user request is currently being
   * processed. This enables sending JSON responses back to the UI without direct user interaction.
   */
  protected void installUiDataAvailableListener(final IClientSession clientSession) {
    // Ensure no listener is currently registered.
    uninstallUiDataAvailableListener();

    // Register new job listener.
    m_uiDataAvailableListener = Jobs.getJobManager().addListener(
        ModelJobs.newEventFilterBuilder()
            .andMatch(new SessionJobEventFilter(clientSession))
            .andMatchNotExecutionHint(UiJobs.EXECUTION_HINT_POLL_REQUEST) // events for poll-requests are not of interest
            .andMatchNotExecutionHint(UiJobs.EXECUTION_HINT_RESPONSE_TO_JSON) // events for response-to-json are not of interest
            .andMatch(newUiDataAvailableFilter()) // filter which evaluates to 'true' once possible UI data is available
            .andMatch(new IFilter<JobEvent>() {

              @Override
              public boolean accept(JobEvent event) {
                // Release poll-request only if there is currently no regular request processing.
                // If such a request is available, the result of the event's model job will be included in the response of that request.
                return !isProcessingJsonRequest();
              }
            })
            .toFilter(),
        new IJobListener() {
          @Override
          public void changed(JobEvent event) {
            LOG.trace("Model job finished. Wake up 'poll-request'. [job={}, eventType={}]", event.getData().getFuture().getJobInput().getName(), event.getType());
            signalPoller();
          }
        });
  }

  /**
   * Creates a filter to accept jobs, which possibly provide data to be transported to the UI. That means, that such a
   * job either transitioned into 'DONE' state, or requires 'UI interaction', or is a periodic job with a round
   * completed.
   */
  protected IFilter<JobEvent> newUiDataAvailableFilter() {
    return new IFilter<JobEvent>() {

      @Override
      public boolean accept(final JobEvent event) {
        switch (event.getType()) {
          case JOB_STATE_CHANGED: {
            return isJobDone(event.getData().getState(), event.getData().getFuture());
          }
          case JOB_EXECUTION_HINT_ADDED: {
            return ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED.equals(event.getData().getExecutionHint()); // UI data available because job was marked with 'UI_INTERACTION_REQUIRED'.
          }
          default: {
            return false;
          }
        }
      }

      private boolean isJobDone(final JobState jobState, final IFuture<?> future) {
        if (jobState == JobState.DONE) {
          // UI data possibly available because job completed.
          return true;
        }
        if (jobState == JobState.PENDING && !future.isSingleExecution()) {
          // UI data possibly available because a non 'one-shot' job completed a round.
          return true;
        }
        return false;
      }
    };
  }

  protected void uninstallUiDataAvailableListener() {
    if (m_uiDataAvailableListener != null) {
      m_uiDataAvailableListener.dispose();
      m_uiDataAvailableListener = null;
    }
  }

  @Override
  public void waitForBackgroundJobs(final int pollWaitSeconds) throws InterruptedException {
    // If another poller is currently blocking, interrupt it. This ensures that max. 1 polling
    // request is waiting for background jobs at the same time (relevant when the UI reconnects
    // after being offline).
    synchronized (m_pollerQueueLock) {
      if (m_pollerMonitor != null) {
        m_pollerMonitor.cancel(true);
      }
      m_pollerMonitor = RunMonitor.CURRENT.get();
    }

    LOG.trace("Wait for max. {} seconds until background job terminates or wait timeout occurs...", pollWaitSeconds);
    final long maxPollWait = pollWaitSeconds * 1000;
    final long t0 = System.currentTimeMillis();
    long pollWait = maxPollWait;
    boolean wait = true;
    while (wait) {
      // Wait until notified by m_modelJobFinishedListener (when a background job has finished) or a timeout occurs
      Object notificationToken = m_pollerQueue.poll(pollWait, TimeUnit.MILLISECONDS);
      long elapsed = System.currentTimeMillis() - t0;
      // Calculate remaining poll wait time
      pollWait = maxPollWait - elapsed;
      if (notificationToken == null || pollWait < 100 || m_disposed || !m_currentJsonResponse.isEmpty()) {
        // Stop wait loop for one of the following reasons:
        // 1. Timeout has occurred -> return always, even with empty answer
        // 2. Remaining poll wait time would less than 100 ms -> same as no. 1
        // 3. Session is disposed
        // 4. Poller was waken up by m_modelJobFinishedListener and JsonResponse is not empty
        wait = false;
      }
      else {
        // Continue wait loop, because timeout has not yet elapsed and JsonResponse is empty
        LOG.trace("Background job terminated, but there is nothing to respond. Going back to sleep for max. {} ms.", pollWait);
      }
    }
    if (!m_disposed) {
      // Wait a short additional time to allow some sort of "coalescing background job result".
      // This prevents too many short polling requests if many jobs finish at the same time.
      Thread.sleep(ADDITIONAL_POLLING_DELAY);
    }

    synchronized (m_pollerQueueLock) {
      m_pollerMonitor = null;
    }
    LOG.trace("Background job terminated. Continue request processing...");
  }

  /**
   * Signals the 'poll-request' to return to the UI. This method never blocks.
   * <p>
   * Internally, a notification token is put into the poller-queue. If a thread is waiting, it will wake up. If no
   * thread is waiting, the token remains in the queue, and the next thread that polls the queue will get the token
   * immediately. If the queue is full (i.e. there is already a token in the queue), this method does nothing.
   */
  @SuppressWarnings("findbugs:RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  protected void signalPoller() {
    m_pollerQueue.offer(m_notificationToken);
  }

  @Override
  public void sendLocaleChangedEvent(Locale locale) {
    JSONObject jsonEvent = new JSONObject();
    putLocaleData(jsonEvent, locale);
    m_currentJsonResponse.addActionEvent(getUiSessionId(), EVENT_LOCALE_CHANGED, jsonEvent);
    updatePreferredLocaleCookie(locale);
  }

  /**
   * Writes <code>"locale"</code> and <code>"textMap"</code> according to the given <code>locale</code> into the given
   * JSON object.
   */
  protected void putLocaleData(JSONObject json, Locale locale) {
    json.put("locale", JsonLocale.toJson(locale));
    json.put("textMap", getTextMap(locale));
  }

  protected JSONObject getTextMap(Locale locale) {
    // Collect textKeys
    Set<String> textKeys = new TreeSet<String>();
    for (IUiTextContributor contributor : BEANS.all(IUiTextContributor.class)) {
      contributor.contributeUiTextKeys(textKeys);
      LOG.debug("Gathered UI text keys from contributor {}", contributor);
    }

    // Resolve texts with the given locale
    JSONObject map = new JSONObject();
    for (String textKey : textKeys) {
      String text = TEXTS.getWithFallback(locale, textKey, null);
      if (text != null) {
        map.put(textKey, text);
      }
      else {
        LOG.warn("Could not find text for contributed UI text key '{}'", textKey);
      }
    }
    return map;
  }

  @Override
  public void sendDisposeAdapterEvent(IJsonAdapter<?> adapter) {
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put("adapter", adapter.getId());
    m_currentJsonResponse.addActionEvent(getUiSessionId(), EVENT_DISPOSE_ADAPTER, jsonEvent);
  }

  @Override
  public void updateTheme(String theme) {
    UiThemeUtility.storeTheme(currentHttpResponse(), sessionStore().getHttpSession(), theme);
    sendReloadPageEvent();
    LOG.info("UI theme changed to: {}", theme);
  }

  @Override
  public void sendReloadPageEvent() {
    m_currentJsonResponse.addActionEvent(getUiSessionId(), EVENT_RELOAD_PAGE);
  }

  @Override
  public IHttpResourceCache getHttpResourceCache() {
    return m_httpResourceCache;
  }

  protected static HttpSessionHelper getHttpSessionHelper() {
    return HTTP_SESSION_HELPER.get();
  }

  protected static JsonRequestHelper getJsonRequestHelper() {
    return JSON_REQUEST_HELPER.get();
  }

  protected static SecureRandom getSecureRandom() {
    return SECURE_RANDOM.get();
  }

  /**
   * Gets the {@link IUiSession} of the given {@link JSONObject}.
   *
   * @param req
   *          The {@link HttpServletRequest} for which the {@link IUiSession} should be returned.
   * @param jsonObject
   *          The {@link JSONObject} that contains the content of the request.
   * @return The {@link IUiSession} of the given {@link HttpServletRequest} or <code>null</code>.
   */
  public static IUiSession get(HttpServletRequest req, JSONObject jsonObject) {
    if (req == null || jsonObject == null) {
      return null;
    }

    return get(req, new JsonRequest(jsonObject));
  }

  /**
   * Gets the {@link IUiSession} of the given {@link JsonRequest}.
   *
   * @param req
   *          The {@link HttpServletRequest} for which the {@link IUiSession} should be returned.
   * @param jsonReq
   *          The {@link JsonRequest}.
   * @return The {@link IUiSession} of the given {@link HttpServletRequest} or <code>null</code>.
   */
  public static IUiSession get(HttpServletRequest req, JsonRequest jsonReq) {
    if (req == null || jsonReq == null) {
      return null;
    }
    return get(req, jsonReq.getUiSessionId());
  }

  /**
   * Gets the {@link IUiSession} with the given id from the given {@link HttpServletRequest}.
   *
   * @param req
   *          The {@link HttpServletRequest} for which the {@link IUiSession} should be returned.
   * @param uiSessionId
   *          The id of the UI session.
   * @return The {@link IUiSession} of the given {@link HttpServletRequest} or <code>null</code>.
   */
  public static IUiSession get(HttpServletRequest req, String uiSessionId) {
    if (req == null || uiSessionId == null) {
      return null;
    }
    HttpSession httpSession = req.getSession(false);
    if (httpSession == null) {
      return null;
    }
    ISessionStore sessionStore = getHttpSessionHelper().getSessionStore(httpSession);
    return sessionStore.getUiSession(uiSessionId);
  }

  private static class P_RootAdapter extends AbstractJsonAdapter<Object> {

    public P_RootAdapter(IUiSession uiSession) {
      super(new Object(), uiSession, ROOT_ID + "", null);
    }

    @Override
    public String getObjectType() {
      return "GlobalAdapter";
    }
  }

  public static class HttpContext {

    private HttpServletRequest m_req;
    private HttpServletResponse m_resp;

    public synchronized void clear() {
      m_req = null;
      m_resp = null;
    }

    public synchronized void set(HttpServletRequest req, HttpServletResponse resp) {
      m_req = req;
      m_resp = resp;
    }

    public synchronized HttpServletRequest getRequest() {
      return m_req;
    }

    public synchronized HttpServletResponse getResponse() {
      return m_resp;
    }
  }
}

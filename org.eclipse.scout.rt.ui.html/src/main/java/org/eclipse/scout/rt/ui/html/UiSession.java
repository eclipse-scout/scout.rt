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

import java.security.AccessController;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.job.filter.event.SessionJobEventFilter;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.shared.ui.IUiLayer;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterRegistry;
import org.eclipse.scout.rt.ui.html.json.JsonClientSession;
import org.eclipse.scout.rt.ui.html.json.JsonEventProcessor;
import org.eclipse.scout.rt.ui.html.json.JsonLocale;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonRequest.RequestType;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiSession implements IUiSession, HttpSessionBindingListener {

  private static final Logger LOG = LoggerFactory.getLogger(UiSession.class);

  /**
   * Prefix for name of HTTP session attribute that is used to store the associated {@link IClientSession}s.
   * <p>
   * The full attribute name is: <b><code>{@link #CLIENT_SESSION_ATTRIBUTE_NAME_PREFIX} + clientSessionId</code></b>
   */
  public static final String CLIENT_SESSION_ATTRIBUTE_NAME_PREFIX = "scout.htmlui.clientsession."/*+clientSessionId*/;

  private static final long ROOT_ID = 1;
  private static final String EVENT_INITIALIZED = "initialized";
  private static final String EVENT_LOCALE_CHANGED = "localeChanged";
  private static final String EVENT_DISPOSE_ADAPTER = "disposeAdapter";
  private static final String EVENT_RELOAD_PAGE = "reloadPage";

  private final JsonAdapterRegistry m_jsonAdapterRegistry;
  private final P_RootAdapter m_rootJsonAdapter;

  private volatile boolean m_initialized;
  private volatile String m_uiSessionId;
  private volatile IClientSession m_clientSession;
  private final AtomicLong m_jsonAdapterSeq = new AtomicLong(ROOT_ID);
  private final AtomicLong m_responseSequenceNo = new AtomicLong(1);
  private volatile JsonResponse m_currentJsonResponse;
  private volatile JsonRequest m_currentJsonRequest;
  /**
   * Note: This variable is referenced by reflection (!) in JsonTestUtility.endRequest() The variable is accessed by
   * different threads, thus all methods on HttpContext are synchronized.
   */
  private final HttpContext m_currentHttpContext = new HttpContext();
  private volatile HttpSession m_currentHttpSession;
  private final JsonEventProcessor m_jsonEventProcessor;
  private volatile boolean m_disposing;
  private volatile boolean m_disposed;
  private final ReentrantLock m_uiSessionLock = new ReentrantLock();
  private volatile IJobListenerRegistration m_uiDataAvailableListener;
  private final BlockingQueue<Object> m_pollerQueue = new ArrayBlockingQueue<>(1, true);
  private final Object m_notificationToken = new Object();
  private volatile long m_lastAccessedTime;

  public UiSession() {
    m_currentJsonResponse = createJsonResponse();
    m_jsonAdapterRegistry = createJsonAdapterRegistry();
    m_rootJsonAdapter = new P_RootAdapter(this);
    m_jsonEventProcessor = createJsonEventProcessor();
  }

  protected JsonResponse createJsonResponse() {
    JsonResponse response = new JsonResponse();
    response.assignSequenceNo(m_responseSequenceNo.getAndIncrement());
    return response;
  }

  protected JsonAdapterRegistry createJsonAdapterRegistry() {
    return new JsonAdapterRegistry();
  }

  protected JsonEventProcessor createJsonEventProcessor() {
    return new JsonEventProcessor(this);
  }

  protected JSONObject getTextMap(Locale locale) {
    // Collect textKeys
    Set<String> textKeys = new TreeSet<String>();
    for (IUiTextContributor contributor : BEANS.all(IUiTextContributor.class)) {
      contributor.contributeUiTextKeys(textKeys);
      LOG.debug("Gathered ui text keys from contributor {}", contributor);
    }

    // Resolve texts with the given locale
    JSONObject map = JsonObjectUtility.newOrderedJSONObject();
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
        if (jobState == JobState.PENDING
            && (future.getExecutionMode() == JobInput.EXECUTION_MODE_PERIODIC_AT_FIXED_RATE || future.getExecutionMode() == JobInput.EXECUTION_MODE_PERIODIC_WITH_FIXED_DELAY)) {
          // UI data possibly available because periodic job completed round.
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
  public void init(HttpServletRequest req, HttpServletResponse resp, JsonStartupRequest jsonStartupReq) {
    if (currentSubject() == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }
    if (m_initialized) {
      throw new IllegalStateException("Already initialized");
    }
    m_initialized = true;
    if (isDisposed()) {
      throw new IllegalStateException("UiSession is disposed");
    }
    touch();

    try {
      m_currentHttpContext.set(req, resp);
      m_currentJsonRequest = jsonStartupReq;

      m_uiSessionId = jsonStartupReq.getUiSessionId();
      HttpSession httpSession = req.getSession();
      m_currentHttpSession = httpSession;

      // Look up the requested client session (create and start a new one if necessary)
      m_clientSession = getOrCreateClientSession(httpSession, req, jsonStartupReq);

      // At this point we have a valid, active clientSession. Therefore, we may now safely store it in the HTTP session
      storeClientSessionInHttpSession(httpSession, m_clientSession);

      // Add a cookie with the preferred user-language
      storePreferredLocaleInCookie(resp, m_clientSession.getLocale());

      // Apply theme from model to HTTP session and cookie
      boolean reloadTheme = initUiTheme(req, resp, httpSession);

      // Register job listener to signal poller once possible UI data to be transported to the UI is available.
      installUiDataAvailableListener(m_clientSession);

      // Create a new JsonAdapter for the client session
      JsonClientSession<?> jsonClientSessionAdapter = createClientSessionAdapter(m_clientSession);

      // Start desktop
      fireDesktopOpened();

      if (reloadTheme) {
        // When theme changes during initialization, send page reload event instead of "initialized" event
        sendReloadPageEvent();
      }
      else {
        // Send "initialized" event
        sendInitializationEvent(jsonClientSessionAdapter.getId());
      }

      LOG.info("UiSession with ID {} initialized", m_uiSessionId);
    }
    finally {
      m_currentHttpContext.clear();
      m_currentJsonRequest = null;
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
    String modelTheme = UiThemeUtility.defaultIfNull(m_clientSession.getDesktop().getTheme());
    String currentTheme = UiThemeUtility.defaultIfNull(UiThemeUtility.getTheme(req));
    boolean reloadPage = !modelTheme.equals(currentTheme);
    UiThemeUtility.storeTheme(resp, httpSession, modelTheme);
    LOG.debug("UI theme model={} current={} reloadPage={}", modelTheme, currentTheme, reloadPage);
    return reloadPage;
  }

  @Override
  public boolean isInitialized() {
    return m_initialized;
  }

  @Override
  public ReentrantLock uiSessionLock() {
    return m_uiSessionLock;
  }

  @Override
  public void touch() {
    m_lastAccessedTime = System.currentTimeMillis();
  }

  @Override
  public long getLastAccessedTime() {
    return m_lastAccessedTime;
  }

  protected IClientSession getOrCreateClientSession(HttpSession httpSession, HttpServletRequest req, JsonStartupRequest jsonStartupReq) {
    String requestedClientSessionId = jsonStartupReq.getClientSessionId();
    IClientSession clientSession = null;
    if (StringUtility.hasText(requestedClientSessionId)) {
      clientSession = loadClientSessionFromHttpSession(httpSession, requestedClientSessionId);
    }

    if (clientSession != null) {
      // Found existing client session
      LOG.info("Using cached client session [clientSessionId={}]", clientSession.getId());
    }
    else {
      // No client session for the requested ID was found, so create one
      clientSession = createAndStartClientSession(req.getLocale(), createUserAgent(jsonStartupReq), jsonStartupReq.getSessionStartupParams());
      LOG.info("Created new client session [clientSessionId={}, userAgent={}]", clientSession.getId(), clientSession.getUserAgent());
      // Ensure session is active
      if (!clientSession.isActive()) {
        throw new UiException("ClientSession is not active, there must have been a problem with loading or starting [clientSessionId=" + clientSession.getId() + "]");
      }
    }
    return clientSession;
  }

  protected IClientSession loadClientSessionFromHttpSession(HttpSession httpSession, String clientSessionId) {
    if (clientSessionId == null) {
      return null;
    }
    return (IClientSession) httpSession.getAttribute(getClientSessionAttributeName(clientSessionId));
  }

  protected void storeClientSessionInHttpSession(HttpSession httpSession, IClientSession clientSession) {
    IClientSession existingClientSession = loadClientSessionFromHttpSession(httpSession, clientSession.getId());

    // Implementation note: The cleanup listener is triggered, when the attribute value is changed.
    // This happens in two cases:
    //   1. when the attribute is set manually
    //   2. the entire session is invalidated.
    if (existingClientSession != clientSession) {
      String clientSessionAttributeName = getClientSessionAttributeName(clientSession.getId());
      httpSession.setAttribute(clientSessionAttributeName, clientSession);
      httpSession.setAttribute(clientSessionAttributeName + ".cleanup", new P_ClientSessionCleanupHandler(clientSession));
    }
  }

  protected String getClientSessionAttributeName(String clientSessionId) {
    return CLIENT_SESSION_ATTRIBUTE_NAME_PREFIX + clientSessionId;
  }

  protected void storePreferredLocaleInCookie(HttpServletResponse resp, Locale locale) {
    CookieUtility.addCookie(resp, PREFERRED_LOCALE_COOKIE_NAME, locale.toLanguageTag());
  }

  /**
   * Updates the locale Cookie but only when a current HTTP response exists. Which means when the locale of the session
   * changes during a client-job, the cookie cannot be updated.
   */
  protected void updatePreferredLocaleCookie(Locale locale) {
    HttpServletResponse resp = m_currentHttpContext.getResponse();
    if (resp != null) {
      storePreferredLocaleInCookie(resp, locale);
    }
  }

  protected IClientSession createAndStartClientSession(Locale locale, UserAgent userAgent, Map<String, String> sessionStartupParams) {
    return BEANS.get(ClientSessionProvider.class).provide(ClientRunContexts.copyCurrent()
        .withLocale(locale)
        .withUserAgent(userAgent)
        .withProperties(sessionStartupParams)); // Make startup parameters available at {@link PropertyMap#CURRENT} during client session is starting.
  }

  protected JsonClientSession<?> createClientSessionAdapter(final IClientSession clientSession) {
    // Ensure adapter is created in model job, because the model might be accessed during the adapter's initialization
    final IFuture<JsonClientSession<?>> future = ModelJobs.schedule(new Callable<JsonClientSession<?>>() {

      @Override
      public JsonClientSession<?> call() throws Exception {
        return (JsonClientSession<?>) createJsonAdapter(clientSession, m_rootJsonAdapter);
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent()
        .withSession(clientSession, true))
        .withName("Starting JsonClientSession")
        .withExceptionHandling(null, false /* propagate */)); // exception handling done by caller

    return BEANS.get(UiJobs.class).awaitAndGet(future);
  }

  protected void fireDesktopOpened() {
    final IFuture<Void> future = ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        IDesktop desktop = m_clientSession.getDesktop();

        if (!desktop.isOpened()) {
          desktop.getUIFacade().fireDesktopOpenedFromUI();
        }
        if (!desktop.isGuiAvailable()) {
          desktop.getUIFacade().fireGuiAttached();
        }
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent()
        .withSession(m_clientSession, true))
        .withName("Starting Desktop")
        .withExceptionHandling(null, false /* propagate */)); // exception handling done by caller

    BEANS.get(UiJobs.class).awaitAndGet(future);
  }

  protected void sendReloadPageEvent() {
    m_currentJsonResponse.addActionEvent(getUiSessionId(), EVENT_RELOAD_PAGE, new JSONObject());
  }

  protected void sendInitializationEvent(final String clientSessionAdapterId) {
    final IFuture<Locale> future = ModelJobs.schedule(new Callable<Locale>() {

      @Override
      public Locale call() throws Exception {
        return m_clientSession.getLocale();
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent()
        .withSession(m_clientSession, true))
        .withName("Looking up Locale")
        .withExceptionHandling(null, false /* propagate */)); // exception handling done by caller

    final JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    jsonEvent.put("clientSessionId", m_clientSession.getId()); // Send back clientSessionId to allow the browser to attach to the same client session on page reload
    jsonEvent.put("clientSession", clientSessionAdapterId);
    putLocaleData(jsonEvent, BEANS.get(UiJobs.class).awaitAndGet(future));

    m_currentJsonResponse.addActionEvent(m_uiSessionId, EVENT_INITIALIZED, jsonEvent);
  }

  protected UserAgent createUserAgent(JsonStartupRequest jsonStartupReq) {
    IUiLayer uiLayer = UiLayer.HTML;
    IUiDeviceType uiDeviceType = UiDeviceType.DESKTOP;
    String browserId = currentHttpRequest().getHeader("User-Agent");
    JSONObject userAgent = jsonStartupReq.getUserAgent();
    if (userAgent != null) {
      // FIXME cgu: it would be great if UserAgent could be changed dynamically, to switch from mobile to tablet mode on the fly, should be done as event in JsonClientSession
      String uiDeviceTypeStr = userAgent.optString("deviceType", null);
      if (uiDeviceTypeStr != null) {
        uiDeviceType = UiDeviceType.createByIdentifier(uiDeviceTypeStr);
      }
      String uiLayerStr = userAgent.optString("uiLayer", null);
      if (uiLayerStr != null) {
        uiLayer = UiLayer.createByIdentifier(uiLayerStr);
      }
    }
    return UserAgent.create(uiLayer, uiDeviceType, browserId);
  }

  protected Subject currentSubject() {
    return Subject.getSubject(AccessController.getContext());
  }

  @Override
  public void dispose() {
    m_disposed = true;
    if (isProcessingJsonRequest()) {
      // If there is a request in progress just mark the session as being disposed.
      // The actual disposing happens before returning to the client, see processJsonRequest().
      m_disposing = true;
      return;
    }

    // Notify waiting requests - should not delay web-container shutdown
    uninstallUiDataAvailableListener();
    signalPoller();

    m_jsonAdapterRegistry.disposeAdapters();
    m_currentHttpContext.clear();
    m_currentJsonResponse = null;
    m_currentHttpSession = null;
  }

  @Override
  public boolean isDisposed() {
    return m_disposed;
  }

  protected JsonAdapterRegistry getJsonAdapterRegistry() {
    return m_jsonAdapterRegistry;
  }

  @Override
  public IJsonAdapter<?> getRootJsonAdapter() {
    return m_rootJsonAdapter;
  }

  @Override
  public String getUiSessionId() {
    return m_uiSessionId;
  }

  @Override
  public String getClientSessionId() {
    return (m_clientSession == null ? null : m_clientSession.getId());
  }

  @Override
  public IClientSession getClientSession() {
    return m_clientSession;
  }

  public long getJsonAdapterSeq() {
    return m_jsonAdapterSeq.get();
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
  public <M, A extends IJsonAdapter<? super M>> A newJsonAdapter(M model, IJsonAdapter<?> parent) {
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

  @Override
  public JsonResponse currentJsonResponse() {
    return m_currentJsonResponse;
  }

  protected JsonRequest currentJsonRequest() {
    return m_currentJsonRequest;
  }

  @Override
  public HttpServletRequest currentHttpRequest() {
    return m_currentHttpContext.getRequest();
  }

  @Override
  public HttpSession currentHttpSession() {
    return m_currentHttpSession;
  }

  public JsonEventProcessor jsonEventProcessor() {
    return m_jsonEventProcessor;
  }

  @Override
  public JSONObject processJsonRequest(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse, final JsonRequest jsonRequest) {
    final ClientRunContext clientRunContext = ClientRunContexts.copyCurrent().withSession(m_clientSession, true);

    m_currentHttpContext.set(servletRequest, servletResponse);
    m_currentJsonRequest = jsonRequest;
    try {
      // 1. Process the JSON request.
      ModelJobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          processJsonRequestInternal();
        }
      }, ModelJobs.newInput(clientRunContext)
          .withName("Processing JSON request")
          .withExecutionHint(UiJobs.EXECUTION_HINT_POLL_REQUEST, RequestType.POLL_REQUEST.equals(jsonRequest.getRequestType()))
          // Handle exceptions instantaneously in job manager, and not by submitter.
          // That is because the submitting thread might not be waiting anymore, because interrupted or returned because requiring 'user interaction'.
          .withExceptionHandling(ExceptionHandler.class, true));

      // 2. Wait for all model jobs of the session.
      BEANS.get(UiJobs.class).awaitModelJobs(m_clientSession, ExceptionHandler.class);

      // 3. Transform the response to JSON.
      final IFuture<JSONObject> future = ModelJobs.schedule(newResponseToJsonTransformer(), ModelJobs.newInput(clientRunContext)
          .withName("Transforming response to JSON")
          .withExecutionHint(UiJobs.EXECUTION_HINT_POLL_REQUEST, RequestType.POLL_REQUEST.equals(jsonRequest.getRequestType()))
          .withExceptionHandling(null, false)); // Propagate exception to caller (UIServlet)
      try {
        return BEANS.get(UiJobs.class).awaitAndGet(future);
      }
      catch (InterruptedException | CancellationException e) {
        future.cancel(true);
        return null;
      }
    }
    finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Adapter count after request: {}", m_jsonAdapterRegistry.size());
      }
      m_currentHttpContext.clear();
      m_currentJsonRequest = null;
      if (m_disposing) {
        dispose();
      }
    }
  }

  /**
   * <b>Do not call this internal method directly!</b> It should only be called be
   * {@link #processJsonRequest(HttpServletRequest, JsonRequest)} which ensures that the required state is set up
   * correctly (and will be cleaned up later) and is run as a model job.
   */
  protected void processJsonRequestInternal() {
    jsonEventProcessor().processEvents(currentJsonRequest(), currentJsonResponse());
  }

  /**
   * @return a new {@link Callable} that returns the current {@link JsonResponse} as JSON and prepares a new current
   *         json response for future jobs. The callable <b>must</b> be called from a model job.
   */
  protected Callable<JSONObject> newResponseToJsonTransformer() {
    return new Callable<JSONObject>() {
      @Override
      public JSONObject call() throws Exception {
        // 1. Convert response to JSON (must be done in model thread due to model access).
        final JSONObject json = m_currentJsonResponse.toJson();
        // 2. Create a new JSON response for future jobs
        m_currentJsonResponse = createJsonResponse();
        // TODO [5.2] bsh, awe, dwi: Should we catch errors from toJson() and create a new json response in the finally?
        return json;
      }
    };
  }

  @Override
  public JSONObject processFileUpload(HttpServletRequest req, HttpServletResponse res, final IBinaryResourceConsumer resourceConsumer, final List<BinaryResource> uploadResources, final Map<String, String> uploadProperties) {
    final ClientRunContext clientRunContext = ClientRunContexts.copyCurrent().withSession(m_clientSession, true);

    m_currentHttpContext.set(req, res);
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
          .withExceptionHandling(ExceptionHandler.class, true));

      // 2. Wait for all model jobs of the session.
      BEANS.get(UiJobs.class).awaitModelJobs(m_clientSession, ExceptionHandler.class);

      // 3. Transform the response to JSON.
      final IFuture<JSONObject> future = ModelJobs.schedule(newResponseToJsonTransformer(), ModelJobs.newInput(clientRunContext)
          .withName("Transforming response to JSON")
          .withExceptionHandling(null, false)); // Propagate exception to caller (UIServlet)
      try {
        return BEANS.get(UiJobs.class).awaitAndGet(future);
      }
      catch (InterruptedException | CancellationException e) {
        future.cancel(true);
        return null;
      }
    }
    finally {
      m_currentHttpContext.clear();
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
        .andMatchNotExecutionHint(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED)
        .toFilter(), true);
  }

  @Override
  public void waitForBackgroundJobs(int pollWaitSeconds) {
    boolean wait = true;
    while (wait) {
      LOG.trace("Wait for max. {} seconds until background job terminates or wait timeout occurs...", pollWaitSeconds);
      try {
        long t0 = System.currentTimeMillis();
        // Wait until notified by m_modelJobFinishedListener (when a background job has finished) or a timeout occurs
        Object notificationToken = m_pollerQueue.poll(pollWaitSeconds, TimeUnit.SECONDS);
        int durationSeconds = (int) Math.round((System.currentTimeMillis() - t0) / 1000d);
        int newPollWaitSeconds = pollWaitSeconds - durationSeconds;
        if (notificationToken == null || newPollWaitSeconds <= 0 || currentJsonResponse() == null || !currentJsonResponse().isEmpty()) {
          // Stop wait loop for one of the following reasons:
          // 1. Timeout has occurred -> return always, even with empty answer
          // 2. Remaining pollWaitTime would be zero -> same as no. 1
          // 3. Session is disposed (-> currentJsonResponse is null)
          // 4. Poller was waken up by m_modelJobFinishedListener and JsonResponse is not empty
          wait = false;
        }
        else {
          // Continue wait loop, because timeout has not yet elapsed and JsonResponse is empty
          LOG.trace("Background job terminated, but there is nothing to respond. Going back to sleep.");
          pollWaitSeconds = newPollWaitSeconds;
        }
      }
      catch (java.lang.InterruptedException e) {
        LOG.warn("Interrupted while waiting for notification token", e);
      }
    }
    LOG.trace("Background job terminated. Continue request processing...");
    if (!m_disposed) {
      return;
    }
    // Wait at least 100ms to allow some sort of "coalescing background job result" (if many jobs
    // finish at the same time, we want to prevent too many polling requests).
    SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
  }

  /**
   * Signals the 'poll-request' to return to the UI. This method never blocks.
   * <p>
   * Internally, a notification token is put into the poller-queue. If a thread is waiting, it will wake up. If no
   * thread is waiting, the token remains in the queue, and the next thread that polls the queue will get the token
   * immediately. If the queue is full (i.e. there is already a token in the queue), this method does nothing.
   */
  protected void signalPoller() {
    m_pollerQueue.offer(m_notificationToken);
  }

  protected boolean isProcessingJsonRequest() {
    return currentJsonRequest() != null;
  }

  @Override
  public void sendLocaleChangedEvent(Locale locale) {
    JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    putLocaleData(jsonEvent, locale);
    currentJsonResponse().addActionEvent(getUiSessionId(), EVENT_LOCALE_CHANGED, jsonEvent);
    updatePreferredLocaleCookie(locale);
  }

  @Override
  public void updateTheme(String theme) {
    UiThemeUtility.storeTheme(m_currentHttpContext.getResponse(), m_currentHttpSession, theme);
    sendReloadPageEvent();
    LOG.info("UI theme changed to: {}", theme);
  }

  /**
   * Writes <code>"locale"</code> and <code>"textMap"</code> according to the given <code>locale</code> as JSON into the
   * given <code>jsonEvent</code>.
   */
  protected void putLocaleData(JSONObject jsonEvent, Locale locale) {
    jsonEvent.put("locale", JsonLocale.toJson(locale));
    jsonEvent.put("textMap", getTextMap(locale));
  }

  @Override
  public void sendDisposeAdapterEvent(IJsonAdapter<?> adapter) {
    JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    jsonEvent.put("adapter", adapter.getId());
    currentJsonResponse().addActionEvent(getUiSessionId(), EVENT_DISPOSE_ADAPTER, jsonEvent);
  }

  @Override
  public void logout() {
    LOG.info("Logging out from UI session with ID {} [clientSessionId={}]", m_uiSessionId, getClientSessionId());
    HttpSession httpSession = currentHttpSession();
    if (httpSession != null) {
      // This will cause P_ClientSessionCleanupHandler.valueUnbound() to be executed
      try {
        httpSession.invalidate();
      }
      catch (IllegalStateException e) {
        // May happen in tomcat if session was already invalidated (e.g. due to expiration)
        // We need to keep a reference to the session and cannot call req.getSession(false) because the model may call logout without having a pending request
        // TODO [5.2] cgu: verify with BSH if this statement is true.
        // FIXME cgu: This will generate a deadlock in jetty (tomcat will throw the exception). Happens if client session close job is running when UIsession.valueUnboudn is called.
        LOG.info("Session already invalidated");
      }
    }
    JsonResponse jsonResponse = currentJsonResponse();
    if (jsonResponse != null) {
      jsonResponse.addActionEvent(getUiSessionId(), getLogoutRedirectUrl(), createLogoutEventData());
    }
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
  public boolean isInspectorHint() {
    HttpServletRequest req = currentHttpRequest();
    return (req != null && UiHints.isInspectorHint(req));
  }

  /**
   * An instance of this class should be added to the HTTP session for each client session. If the HTTP session is
   * invalidated, this listener is called and can shutdown the client session model.
   */
  protected static class P_ClientSessionCleanupHandler implements HttpSessionBindingListener {

    private final IClientSession m_clientSession;

    public P_ClientSessionCleanupHandler(IClientSession clientSession) {
      m_clientSession = clientSession;
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
      // Ensure client session is stopped. Do this inside a model job, but do _not_ wait for it, because closing the
      // desktop will eventually call logout(), where we try to invalidate the HTTP session again (which would block
      // forever, if this method was still executing).
      ModelJobs.schedule(new IRunnable() {
        @Override
        public void run() {
          LOG.info("Shutting down client session with ID {} due to invalidation of HTTP session", m_clientSession.getId());
          // Dispose model (if session was not already stopped earlier by itself).
          // Session inactivation is executed delayed (see AbstractClientSession#getMaxShutdownWaitTime(), that's why desktop may already be null
          if (m_clientSession.isActive() && m_clientSession.getDesktop() != null) {
            m_clientSession.getDesktop().getUIFacade().fireDesktopClosingFromUI(true);
          }
          LOG.info("Client session with ID {} terminated.", m_clientSession.getId());
        }
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent().withSession(m_clientSession, true))
          .withName("Closing desktop due to HTTP session invalidation"));
    }
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

  // === HttpSessionBindingListener ===

  @Override
  public void valueBound(HttpSessionBindingEvent event) {
  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent event) {
    if (ModelJobs.isModelThread()) {
      dispose(); // already in model job
      LOG.info("UI session with ID {} unbound from HTTP session.", m_uiSessionId);
      return;
    }
    try {
      m_uiSessionLock.lock();
      ModelJobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          dispose();
          LOG.info("UI session with ID {} unbound from HTTP session.", m_uiSessionId);
        }
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent()
          .withSession(getClientSession(), true)))
          .awaitDone();
    }
    catch (InterruptedException e) {
      LOG.error("Interrupted while waiting for the UISession to be disposed", e);
    }
    finally {
      m_uiSessionLock.unlock();
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

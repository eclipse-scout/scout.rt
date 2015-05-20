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
package org.eclipse.scout.rt.ui.html;

import java.security.AccessController;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobFutureFilters.ModelJobFilter;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.shared.TEXTS;
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
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonLocale;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class UiSession implements IUiSession, HttpSessionBindingListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UiSession.class);

  /**
   * Prefix for name of HTTP session attribute that is used to store the associated {@link IClientSession}s.
   * <p>
   * The full attribute name is: <b><code>{@link #CLIENT_SESSION_ATTRIBUTE_NAME_PREFIX} + clientSessionId</code></b>
   */
  public static final String CLIENT_SESSION_ATTRIBUTE_NAME_PREFIX = "scout.htmlui.clientsession."/*+m_clientSessionId*/;
  private static final long ROOT_ID = 1;

  private final JsonAdapterRegistry m_jsonAdapterRegistry;
  private final P_RootAdapter m_rootJsonAdapter;

  private boolean m_initialized;
  private String m_clientSessionId;
  private String m_uiSessionId;
  private JsonClientSession<? extends IClientSession> m_jsonClientSession;
  private long m_jsonAdapterSeq = ROOT_ID;
  private JsonResponse m_currentJsonResponse;
  private JsonRequest m_currentJsonRequest;
  /**
   * Note: This variable is referenced by reflection (!) in JsonTestUtility.endRequest()
   * The variable is accessed by different threads, thus it is an atomic reference.
   */
  private final AtomicReference<HttpServletRequest> m_currentHttpRequest = new AtomicReference<>();
  private final JsonEventProcessor m_jsonEventProcessor;
  private boolean m_disposing;
  private final ReentrantLock m_uiSessionLock = new ReentrantLock();
  private IJobListener m_modelJobFinishedListener;
  private final ArrayBlockingQueue<Object> m_backgroundJobNotificationQueue = new ArrayBlockingQueue<>(1, true);
  private final Object m_notificationToken = new Object();

  public UiSession() {
    m_currentJsonResponse = createJsonResponse();
    m_jsonAdapterRegistry = createJsonAdapterRegistry();
    m_rootJsonAdapter = new P_RootAdapter(this);
    m_jsonEventProcessor = createJsonEventProcessor();

    // When a model job finishes but no client request is currently being processed, the background poller has to be notified.
    // This enables sending JSON responses to the UI without user interaction. Note that a model job is also considered
    // "finished", when it is blocked, e.g. form.waitFor().
    m_modelJobFinishedListener = new IJobListener() {
      @Override
      public void changed(JobEvent event) {
        LOG.trace("Model job finished: " + event.getFuture().getJobInput().name() + ". Notify waiting requests...");
        notifyPollingBackgroundJobRequests();
      }
    };
    Jobs.getJobManager().addListener(new IFilter<JobEvent>() {
      @Override
      public boolean accept(JobEvent event) {
        // a) It has to be a model job
        if (!ModelJobFilter.INSTANCE.accept(event.getFuture())) {
          return false;
        }
        // b) It has to be in a state that is considered "finished"
        switch (event.getType()) {
          case BLOCKED:
          case DONE:
          case REJECTED:
          case SHUTDOWN:
            // continue
            break;
          default:
            return false;
        }
        // c) No client request must currently being processed (because in that case, the result of the
        // model job will be returned as payload of the current JSON response).
        if (isProcessingClientRequest()) {
          return false;
        }
        // d) The model job's session has to match our session
        ClientRunContext runContext = (ClientRunContext) event.getFuture().getJobInput().runContext();
        return (runContext.session() == getClientSession());
      }
    }, m_modelJobFinishedListener);
  }

  protected boolean isInitialized() {
    return m_initialized;
  }

  protected JsonResponse createJsonResponse() {
    return new JsonResponse();
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
      LOG.info("Gathered ui text keys from contributor " + contributor);
    }

    // Resolve texts with the given locale
    JSONObject map = JsonObjectUtility.newOrderedJSONObject();
    for (String textKey : textKeys) {
      JsonObjectUtility.putProperty(map, textKey, TEXTS.get(locale, textKey));
    }
    return map;
  }

  @Override
  public void init(HttpServletRequest httpRequest, JsonStartupRequest jsonStartupRequest) {
    if (currentSubject() == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }
    if (m_initialized) {
      throw new IllegalStateException("Already initialized");
    }
    m_initialized = true;

    m_currentHttpRequest.set(httpRequest);
    m_currentJsonRequest = jsonStartupRequest;
    m_clientSessionId = jsonStartupRequest.getClientSessionId();
    m_uiSessionId = jsonStartupRequest.getUiSessionId();

    HttpSession httpSession = httpRequest.getSession();

    // Look up the requested client session (create and start a new one if necessary)
    IClientSession clientSession = getOrCreateClientSession(httpSession, httpRequest, jsonStartupRequest);

    // At this point we have a valid, active clientSession. Therefore, we may now safely store it in the HTTP session
    storeClientSessionInHttpSession(httpSession, clientSession);

    // Create a new JsonAdapter for the client session
    m_jsonClientSession = createClientSessionAdapter(clientSession);

    // Handle detach
    handleDetach(jsonStartupRequest, httpSession);

    // Start desktop
    fireDesktopOpened();

    // Send "initialized" event
    sendInitializationEvent();
    LOG.info("UiSession with ID " + m_uiSessionId + " initialized");
  }

  @Override
  public ReentrantLock uiSessionLock() {
    return m_uiSessionLock;
  }

  protected IClientSession getOrCreateClientSession(HttpSession httpSession, HttpServletRequest request, JsonStartupRequest jsonStartupRequest) {
    IClientSession clientSession = loadClientSessionFromHttpSession(httpSession);

    if (clientSession != null) {
      // Found existing client session
      LOG.info("Using cached client session [clientSessionId=" + m_clientSessionId + "]");
    }
    else {
      // No client session for the requested ID was found, so create one
      LOG.info("Creating new client session [clientSessionId=" + m_clientSessionId + "]");
      clientSession = createAndStartClientSession(request.getLocale(), createUserAgent(jsonStartupRequest), extractSessionInitParams(jsonStartupRequest.getCustomParams()));
      // Ensure session is active
      if (!clientSession.isActive()) {
        throw new JsonException("ClientSession is not active, there must have been a problem with loading or starting [clientSessionId=" + m_clientSessionId + "]");
      }
    }
    return clientSession;
  }

  protected IClientSession loadClientSessionFromHttpSession(HttpSession httpSession) {
    if (m_clientSessionId == null) {
      throw new IllegalStateException("Missing clientSessionId in JSON request");
    }
    IClientSession clientSession = (IClientSession) httpSession.getAttribute(CLIENT_SESSION_ATTRIBUTE_NAME_PREFIX + m_clientSessionId);
    return clientSession;
  }

  protected void storeClientSessionInHttpSession(HttpSession httpSession, IClientSession clientSession) {
    IClientSession existingClientSession = loadClientSessionFromHttpSession(httpSession);

    // Implementation note: The cleanup listener is triggered, when the attribute value is changed.
    // This happens in two cases:
    //   1. when the attribute is set manually
    //   2. the entire session is invalidated.
    if (existingClientSession != clientSession) {
      String clientSessionAttributeName = getClientSessionAttributeName();
      httpSession.setAttribute(clientSessionAttributeName, clientSession);
      httpSession.setAttribute(clientSessionAttributeName + ".cleanup", new P_ClientSessionCleanupHandler(m_clientSessionId, clientSession));
    }
  }

  protected String getClientSessionAttributeName() {
    if (m_clientSessionId == null) {
      throw new IllegalStateException("Missing clientSessionId in JSON request");
    }
    return CLIENT_SESSION_ATTRIBUTE_NAME_PREFIX + m_clientSessionId;
  }

  protected IClientSession createAndStartClientSession(Locale locale, UserAgent userAgent, Map<String, String> sessionInitParams) {
    try {
      ClientRunContext ctx = ClientRunContexts.empty().locale(locale).userAgent(userAgent);
      for (Map.Entry<String, String> e : sessionInitParams.entrySet()) {
        ctx.propertyMap().put(e.getKey(), e.getValue());
      }
      return BEANS.get(ClientSessionProvider.class).provide(ctx);
    }
    catch (ProcessingException e) {
      throw new JsonException("Error while creating new client session for clientSessionId=" + m_clientSessionId, e);
    }
  }

  /**
   * extract the params used to create a new {@link IClientSession}
   */
  protected Map<String, String> extractSessionInitParams(JSONObject customParams) {
    Map<String, String> customParamsMap = new HashMap<String, String>();
    if (customParams != null) {
      JSONArray names = customParams.names();
      for (int i = 0; i < names.length(); i++) {
        customParamsMap.put(names.optString(i), customParams.optString(names.optString(i)));
      }
    }
    return customParamsMap;
  }

  protected JsonClientSession<?> createClientSessionAdapter(final IClientSession clientSession) {
    // Ensure adapter is created in model job, because the model might be accessed during the adapter's initialization
    return JobUtility.runModelJobAndAwait("startUp jsonClientSession", clientSession, new Callable<JsonClientSession<?>>() {
      @Override
      public JsonClientSession<?> call() throws Exception {
        return (JsonClientSession<?>) createJsonAdapter(clientSession, m_rootJsonAdapter);
      }
    });
  }

  protected void handleDetach(JsonStartupRequest jsonStartupRequest, HttpSession httpSession) {
    String parentUiSessionId = jsonStartupRequest.getParentUiSessionId();
    if (parentUiSessionId != null) {
      IUiSession parentUiSession = (IUiSession) httpSession.getAttribute(HTTP_SESSION_ATTRIBUTE_PREFIX + parentUiSessionId);
      if (parentUiSession != null) {
        LOG.info("Attaching uiSession '" + m_uiSessionId + "' to parentUiSession '" + parentUiSessionId + "'");
        // TODO BSH Detach | Actually do something
      }
    }
  }

  protected void fireDesktopOpened() {
    JobUtility.runModelJobAndAwait("start up desktop", m_jsonClientSession.getModel(), Callables.callable(new IRunnable() {
      @Override
      public void run() throws Exception {
        IClientSession clientSession = ClientSessionProvider.currentSession();
        IDesktop desktop = clientSession.getDesktop();

        if (!desktop.isOpened()) {
          desktop.getUIFacade().fireDesktopOpenedFromUI();
        }
        if (!desktop.isGuiAvailable()) {
          desktop.getUIFacade().fireGuiAttached();
        }
      }
    }));
  }

  protected void sendInitializationEvent() {
    JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    JsonObjectUtility.putProperty(jsonEvent, "clientSession", m_jsonClientSession.getId());
    Locale sessionLocale = JobUtility.runModelJobAndAwait("fetch locale from model", m_jsonClientSession.getModel(), new Callable<Locale>() {
      @Override
      public Locale call() throws Exception {
        return m_jsonClientSession.getModel().getLocale();
      }
    });
    putLocaleData(jsonEvent, sessionLocale);
    m_currentJsonResponse.addActionEvent(m_uiSessionId, "initialized", jsonEvent);
  }

  protected UserAgent createUserAgent(JsonStartupRequest jsonStartupRequest) {
    IUiLayer uiLayer = UiLayer.HTML;
    IUiDeviceType uiDeviceType = UiDeviceType.DESKTOP;
    String browserId = currentHttpRequest().getHeader("User-Agent");
    JSONObject userAgent = jsonStartupRequest.getUserAgent();
    if (userAgent != null) {
      // FIXME CGU: it would be great if UserAgent could be changed dynamically, to switch from mobile to tablet mode on the fly, should be done as event in JsonClientSession
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
    if (isProcessingClientRequest()) {
      // If there is a request in progress just mark the session as being disposed.
      // The actual disposing happens before returning to the client, see processRequest.
      m_disposing = true;
      return;
    }

    Jobs.getJobManager().removeListener(m_modelJobFinishedListener);

    // Notify waiting requests - should not delay web-container shutdown
    notifyPollingBackgroundJobRequests();
    m_jsonAdapterRegistry.disposeAllJsonAdapters();
    m_currentJsonResponse = null;
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
    return m_clientSessionId;
  }

  @Override
  public IClientSession getClientSession() {
    return (m_jsonClientSession == null ? null : m_jsonClientSession.getModel());
  }

  @Override
  public JsonClientSession<? extends IClientSession> getJsonClientSession() {
    return m_jsonClientSession;
  }

  public long getJsonAdapterSeq() {
    return m_jsonAdapterSeq;
  }

  @Override
  public String createUniqueIdFor(IJsonAdapter jsonAdapter) {
    // FIXME CGU create id based on scout object for automatic gui testing, use @classId? or CustomWidgetIdGenerator from scout.ui.rwt bundle?
    return "" + (++m_jsonAdapterSeq);
  }

  @Override
  public IJsonAdapter<?> getJsonAdapter(String id) {
    return m_jsonAdapterRegistry.getJsonAdapter(id);
  }

  @Override
  public List<IJsonAdapter<?>> getJsonChildAdapters(IJsonAdapter<?> parent) {
    return m_jsonAdapterRegistry.getJsonAdapters(parent);
  }

  @Override
  public <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model, IJsonAdapter<?> parent) {
    return getJsonAdapter(model, parent, true);
  }

  @Override
  public <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model, IJsonAdapter<?> parent, boolean checkRoot) {
    A jsonAdapter = m_jsonAdapterRegistry.getJsonAdapter(model, parent);
    if (jsonAdapter == null && checkRoot) {
      jsonAdapter = m_jsonAdapterRegistry.getJsonAdapter(model, getRootJsonAdapter());
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
   * <code>init()</code> method
   * on the created instance.
   */
  public <M, A extends IJsonAdapter<? super M>> A newJsonAdapter(M model, IJsonAdapter<?> parent) {
    String id = createUniqueIdFor(null); // FIXME CGU
    @SuppressWarnings("unchecked")
    A adapter = (A) MainJsonObjectFactory.get().createJsonAdapter(model, this, id, parent);
    adapter.init();
    return adapter;
  }

  @Override
  public void registerJsonAdapter(IJsonAdapter<?> jsonAdapter) {
    m_jsonAdapterRegistry.addJsonAdapter(jsonAdapter, jsonAdapter.getParent());
  }

  @Override
  public void unregisterJsonAdapter(String id) {
    // Remove it from the registry. All subsequent calls of "getAdapter(id)" will return null.
    m_jsonAdapterRegistry.removeJsonAdapter(id);
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
    return m_currentHttpRequest.get();
  }

  public JsonEventProcessor jsonEventProcessor() {
    return m_jsonEventProcessor;
  }

  @Override
  public JSONObject processRequest(HttpServletRequest httpRequest, JsonRequest jsonRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Adapter count before request: " + m_jsonAdapterRegistry.getJsonAdapterCount());
    }

    try {
      m_currentHttpRequest.set(httpRequest);
      m_currentJsonRequest = jsonRequest;

      // Process events in model job
      JobUtility.runModelJobAndAwait("event-processing", getClientSession(), Callables.callable(new IRunnable() {
        @Override
        public void run() throws Exception {
          processRequestInternal();
        }
      }));

      // Wait for any other model jobs that might have been scheduled during event processing. If there are any
      // (e.g. "data fetched" from smart fields), we want to return their results to the UI in the same request.
      JobUtility.awaitAllModelJobs(getClientSession());

      // Convert the collected response to JSON. It is important that this is done in a
      // model job, because during toJson(), the model might be accessed.
      JSONObject result = JobUtility.runModelJobAndAwait("response-to-json", getClientSession(), new Callable<JSONObject>() {
        @Override
        public JSONObject call() throws Exception {
          JSONObject json = responseToJsonInternal();
          // Create new jsonResponse instance after JSON object has been created
          // This must happen synchronized (as it always is, in a model-job) to avoid concurrency issues
          // FIXME AWE: (json-layer) ausprobieren, ob die currentResponse auch im Fall von einer Exception zurück gesetzt werden muss.
          m_currentJsonResponse = createJsonResponse();
          return json;
        }
      });
      return result;
    }
    finally {
      m_currentHttpRequest.set(null);
      if (m_disposing) {
        dispose();
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("Adapter count after request: " + m_jsonAdapterRegistry.getJsonAdapterCount());
      }
    }
  }

  /**
   * <b>Do not call this internal method directly!</b> It should only be called be
   * {@link #processRequest(HttpServletRequest, JsonRequest)} which ensures that the required
   * state is set up correctly (and will be cleaned up later) and is run as a model job.
   */
  protected void processRequestInternal() {
    jsonEventProcessor().processEvents(currentJsonRequest(), currentJsonResponse());
  }

  /**
   * <b>Do not call this internal method directly!</b> It should only be called be
   * {@link #processRequest(HttpServletRequest, JsonRequest)} which ensures that the required
   * state is set up correctly (and will be cleaned up later) and is run as a model job.
   */
  protected JSONObject responseToJsonInternal() {
    return currentJsonResponse().toJson();
  }

  @Override
  public void waitForBackgroundJobs() {
    LOG.trace("Wait until background job terminates...");
    try {
      m_backgroundJobNotificationQueue.poll(1, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for notification token", e);
    }
    finally {
      LOG.trace("Background job terminated. Continue request processing...");
    }
  }

  protected void notifyPollingBackgroundJobRequests() {
    // Put a notification token in the queue of size 1. If a thread is waiting, it will wake up. If no thread is waiting, the token
    // remains in the queue, and the next thread that polls the queue will get the token immediately. If the queue is full (i.e. there
    // is already a token in the queue), this method does nothing. This method will never block.
    m_backgroundJobNotificationQueue.offer(m_notificationToken);
  }

  protected boolean isProcessingClientRequest() {
    return currentHttpRequest() != null;
  }

  @Override
  public void sendLocaleChangedEvent(Locale locale) {
    JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    putLocaleData(jsonEvent, locale);
    currentJsonResponse().addActionEvent(getUiSessionId(), "localeChanged", jsonEvent);
  }

  /**
   * Writes <code>"locale"</code> and <code>"textMap"</code> according to the given <code>locale</code> as JSON into the
   * given <code>jsonEvent</code>.
   */
  protected void putLocaleData(JSONObject jsonEvent, Locale locale) {
    JsonObjectUtility.putProperty(jsonEvent, "locale", JsonLocale.toJson(locale));
    JsonObjectUtility.putProperty(jsonEvent, "textMap", getTextMap(locale));
  }

  @Override
  public void logout() {
    HttpServletRequest req = currentHttpRequest();
    if (req == null) {
      // when timeout occurs, logout is called without a http context
      return;
    }
    LOG.info("Logging out from UI session with ID " + m_uiSessionId + " [clientSessionId=" + m_clientSessionId + "]");
    HttpSession httpSession = req.getSession(false);
    if (httpSession != null) {
      // This will cause P_ClientSessionCleanupHandler.valueUnbound() to be executed
      httpSession.invalidate();
    }
    currentJsonResponse().addActionEvent(getUiSessionId(), "logout");
    LOG.info("Logged out successfully from UI session with ID " + m_uiSessionId);
  }

  @Override
  public boolean isInspectorHint() {
    HttpServletRequest req = currentHttpRequest();
    return (req != null && UiHints.isInspectorHint(req));
  }

  /**
   * An instance of this class should be added to the HTTP session for each
   * client session. If the HTTP session is invalidated, this listener is
   * called and can shutdown the client session model.
   */
  protected static class P_ClientSessionCleanupHandler implements HttpSessionBindingListener {

    private final String m_clientSessionId;
    private final IClientSession m_clientSession;

    public P_ClientSessionCleanupHandler(String clientSessionId, IClientSession clientSession) {
      m_clientSessionId = clientSessionId;
      m_clientSession = clientSession;
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
      LOG.info("Shutting down client session with ID " + m_clientSessionId + " due to invalidation of HTTP session");
      // Dispose model (if session was not already stopped earlier by itself)
      if (m_clientSession.isActive()) {
        JobUtility.runModelJobAndAwait("fireDesktopClosingFromUI", m_clientSession, Callables.callable(new IRunnable() {
          @Override
          public void run() throws Exception {
            m_clientSession.getDesktop().getUIFacade().fireDesktopClosingFromUI(true);
          }
        }));
      }
      LOG.info("Client session with ID " + m_clientSessionId + " terminated.");
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
    dispose();
    LOG.info("UI session with ID " + m_uiSessionId + " unbound from HTTP session.");
  }
}

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
package org.eclipse.scout.rt.ui.html.json;

import java.security.AccessController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.Scout5ExtensionUtil;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.shared.ui.IUiLayer;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UiLayer2;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractJsonSession implements IJsonSession, HttpSessionBindingListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonSession.class);

  private final JsonAdapterFactory m_jsonAdapterFactory;
  private final JsonAdapterRegistry m_jsonAdapterRegistry;

  /**
   * Contains IDs to adapters which must be removed <i>after</i> a request has been processed.
   * This concept was introduced, because an adapter can be created, attached and disposed in
   * a single request. When we'd remove the adapters immediately we sometimes have the situation
   * where an event in the response, references an adapter that has already been disposed. With
   * this solution this situation is avoided.
   */
  private final Set<String> m_unregisterAdapterSet = new HashSet<String>();

  private JsonClientSession m_jsonClientSession;
  private String m_jsonSessionId;
  private long m_jsonAdapterSeq;
  private JsonResponse m_currentJsonResponse;
  private JsonRequest m_currentJsonRequest;
  private HttpServletRequest m_currentHttpRequest;
  private JsonEventProcessor m_jsonEventProcessor;

  public AbstractJsonSession() {
    m_currentJsonResponse = createJsonResponse();
    m_jsonAdapterFactory = createJsonAdapterFactory();
    m_jsonAdapterRegistry = createJsonAdapterRegistry();
  }

  protected JsonResponse createJsonResponse() {
    return new JsonResponse();
  }

  protected JsonAdapterFactory createJsonAdapterFactory() {
    return new JsonAdapterFactory();
  }

  protected JsonAdapterRegistry createJsonAdapterRegistry() {
    return new JsonAdapterRegistry();
  }

  /**
   * Returns a list of text keys (which must exist in a *.nls file) that are sent to the HTML client on start-up.
   * The texts returned by this method should be used as static UI texts. All other texts are are sent as regular
   * (form-)data and must not be added here.
   */
  protected List<String> getTextKeys() {
    return Arrays.asList(
        "LoadOptions",
        "NoOptions",
        "OneOption",
        "NumOptions",
        "InvalidDateFormat",
        "ResetTableColumns",
        "FilterBy",
        "SearchFor",
        "TableRowCount0",
        "TableRowCount1",
        "TableRowCount",
        "NumRowsSelected",
        "SelectAll",
        "SelectNone",
        "NumRowsFiltered",
        "NumRowsFilteredBy",
        "RemoveFilter",
        "NumRowsLoaded",
        "ReloadData",
        "ShowEveryDate",
        "GroupedByWeekday",
        "GroupedByMonth",
        "GroupedByYear",
        "Count",
        "ConnectionInterrupted",
        "ConnectionReestablished",
        "Reconnecting");
  }

  private JSONObject getTextMap() {
    JSONObject map = new JSONObject();
    for (String textKey : getTextKeys()) {
      JsonObjectUtility.putProperty(map, textKey, TEXTS.get(textKey));
    }
    return map;
  }

  @Override
  public void init(HttpServletRequest request, JsonStartupRequest jsonStartupRequest) {
    m_currentHttpRequest = request;
    m_currentJsonRequest = jsonStartupRequest;
    m_jsonSessionId = jsonStartupRequest.getJsonSessionId();
    if (currentSubject() == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }

    IClientSession clientSession;
    synchronized (this) {
      HttpSession httpSession = m_currentHttpRequest.getSession();
      // Lookup the requested client session
      String clientSessionId = jsonStartupRequest.getClientSessionId();
      if (clientSessionId == null) {
        throw new IllegalStateException("Missing clientSessionId in JSON request");
      }
      String clientSessionAttributeName = "scout.htmlui.session.client." + clientSessionId;
      clientSession = (IClientSession) httpSession.getAttribute(clientSessionAttributeName);
      if (clientSession != null) {
        // Found existing client session
        LOG.info("Using cached client session [clientSessionId=" + clientSessionId + "]");
      }
      else {
        // No client session for the requested ID was found, so create one and store it in the map
        LOG.info("Creating new client session [clientSessionId=" + clientSessionId + "]");
        //FIXME CGU session must be started later, see JsonClientSession
        //return SERVICES.getService(IClientSessionRegistryService.class).newClientSession(clientSessionClass(), subject, UUID.randomUUID().toString(), userAgent);
        try {
          LocaleThreadLocal.set(request.getLocale());
          //
          clientSession = createClientSession();
          initClientSession(clientSession, jsonStartupRequest);
        }
        finally {
          LocaleThreadLocal.set(null);
        }
        httpSession.setAttribute(clientSessionAttributeName, clientSession);
        httpSession.setAttribute(clientSessionAttributeName + ".cleanup", new P_ClientSessionCleanupHandler(clientSessionId, clientSession));
      }
    }
    m_jsonClientSession = (JsonClientSession) getOrCreateJsonAdapter(clientSession);
    m_jsonEventProcessor = createJsonEventProcessor();
    startUpClientSession(clientSession);

    JSONObject jsonEvent = new JSONObject();
    JsonObjectUtility.putProperty(jsonEvent, "clientSession", m_jsonClientSession.getId());
    JsonObjectUtility.putProperty(jsonEvent, "textMap", getTextMap());
    m_currentJsonResponse.addActionEvent(m_jsonSessionId, "initialized", jsonEvent);
    LOG.info("JsonSession with ID " + m_jsonSessionId + " initialized");
  }

  /**
   * @return a new {@link IClientSession} that is not yet initialized, so
   *         {@link IClientSession#startSession(org.osgi.framework.Bundle)} was not yet called
   */
  protected abstract IClientSession createClientSession();

  protected JsonEventProcessor createJsonEventProcessor() {
    return new JsonEventProcessor(m_jsonClientSession);
  }

  /**
   * initialize the properties of the {@link IClientSession} but does not yet start it
   * {@link IClientSession#startSession(org.osgi.framework.Bundle)} was not yet called
   */
  protected void initClientSession(IClientSession clientSession, JsonStartupRequest jsonStartupRequest) {
    UserAgent userAgent = createUserAgent(jsonStartupRequest);
    clientSession.setUserAgent(userAgent);
    clientSession.setSubject(currentSubject());
    clientSession.setVirtualSessionId(UUID.randomUUID().toString());
    //custom props
    HashMap<String, String> customProps = new HashMap<String, String>();
    JSONObject obj = jsonStartupRequest.getCustomParams();
    if (obj != null) {
      JSONArray names = jsonStartupRequest.getCustomParams().names();
      for (int i = 0; i < names.length(); i++) {
        customProps.put(names.optString(i), obj.optString(names.optString(i)));
      }
    }
    Scout5ExtensionUtil.ISession_initCustomParams(clientSession, customProps);
  }

  protected void startUpClientSession(IClientSession clientSession) {
    ClientSyncJob job = new ClientSyncJob("AbstractJsonSession#startClientSession", clientSession) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        m_jsonClientSession.startUp();
      }
    };
    job.runNow(new NullProgressMonitor());
    try {
      job.throwOnError();
    }
    catch (ProcessingException e) {
      throw new JsonException(e);
    }
    if (!clientSession.isActive()) {
      throw new JsonException("ClientSession is not active, there must have been a problem with loading or starting");
    }
  }

  protected UserAgent createUserAgent(JsonStartupRequest jsonStartupRequest) {
    IUiLayer uiLayer = UiLayer2.HTML;
    IUiDeviceType uiDeviceType = UiDeviceType.DESKTOP;
    String browserId = m_currentHttpRequest.getHeader("User-Agent");
    JSONObject userAgent = jsonStartupRequest.getUserAgent();
    if (userAgent != null) {
      // FIXME CGU it would be great if UserAgent could be changed dynamically, to switch from mobile to tablet mode on the fly, should be done as event in JsonClientSession
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
    m_jsonAdapterRegistry.dispose();
    m_currentJsonResponse = null;
  }

  @Override
  public String getJsonSessionId() {
    return m_jsonSessionId;
  }

  @Override
  public IClientSession getClientSession() {
    return m_jsonClientSession.getModel();
  }

  public JsonClientSession getJsonClientSession() {
    return m_jsonClientSession;
  }

  public long getJsonAdapterSeq() {
    return m_jsonAdapterSeq;
  }

  @Override
  public String createUniqueIdFor(IJsonAdapter jsonAdapter) {
    //FIXME CGU create id based on scout object for automatic gui testing, use @classId? or CustomWidgetIdGenerator from scout.ui.rwt bundle?
    return "" + (++m_jsonAdapterSeq);
  }

  /**
   * Creates an adapter instance for the given model and calls the <code>attach()</code> method on the created instance.
   */
  protected IJsonAdapter<?> createJsonAdapter(Object model) {
    String id = createUniqueIdFor(null); // FIXME CGU
    IJsonAdapter<?> adapter = m_jsonAdapterFactory.createJsonAdapter(model, this, id);
    adapter.attach();
    return adapter;
  }

  @Override
  public IJsonAdapter<?> getJsonAdapter(String id) {
    return m_jsonAdapterRegistry.getJsonAdapter(id);
  }

  @Override
  public <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model) {
    return m_jsonAdapterRegistry.getJsonAdapter(model);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <M, A extends IJsonAdapter<? super M>> A getOrCreateJsonAdapter(M model) {
    A jsonAdapter = getJsonAdapter(model);
    if (jsonAdapter != null) {
      return jsonAdapter;
    }
    jsonAdapter = (A) createJsonAdapter(model);
    // because it's a new adapter we must add it to the response
    m_currentJsonResponse.addAdapter(jsonAdapter); // TODO AWE: (json) in registerJsonAdapter verschieben? analog unregisterJsonAdapter
    return jsonAdapter;
  }

  @Override
  public void registerJsonAdapter(IJsonAdapter<?> adapter) {
    m_jsonAdapterRegistry.addJsonAdapter(adapter);
  }

  @Override
  public void unregisterJsonAdapter(String id) {
    m_unregisterAdapterSet.add(id);
  }

  @Override
  public void flush() {
    LOG.debug("Flush. Remove these adapter IDs from registry: " + m_unregisterAdapterSet);
    for (String id : m_unregisterAdapterSet) {
      m_jsonAdapterRegistry.removeJsonAdapter(id);
    }
    m_unregisterAdapterSet.clear();
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
    return m_currentHttpRequest;
  }

  public JsonEventProcessor getJsonEventProcessor() {
    return m_jsonEventProcessor;
  }

  @Override
  public JSONObject processRequest(HttpServletRequest httpRequest, JsonRequest jsonRequest) {
    try {
      m_currentHttpRequest = httpRequest;
      m_currentJsonRequest = jsonRequest;
      if (jsonRequest.isStartupRequest()) {
        m_jsonClientSession.processRequestLocale(httpRequest.getLocale());
      }
      JsonResponse jsonResponse = currentJsonResponse();
      m_jsonEventProcessor.processEvents(m_currentJsonRequest, jsonResponse);
      return jsonResponse.toJson();
    }
    finally {
      // FIXME CGU really finally? what if exception occurs and some events are already delegated to the model?
      // reset event map (aka jsonResponse) when response has been sent to client
      m_currentJsonResponse = createJsonResponse();
      flush();
    }
  }

  @Override
  public void valueBound(HttpSessionBindingEvent event) {
  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent event) {
    dispose();
    LOG.info("JSON session with ID " + m_jsonSessionId + " unbound from HTTP session.");
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
      LOG.info("Shutting down client session with ID " + m_clientSessionId + " due to invalidation of HTTP session...");

      // Dispose model (if session was not already stopped earlier by itself)
      if (m_clientSession.isActive()) {
        ClientJob job = new ClientSyncJob("Disposing client session", m_clientSession) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            m_clientSession.getDesktop().getUIFacade().fireDesktopClosingFromUI(true);
          }
        };
        job.runNow(new NullProgressMonitor());
        try {
          job.throwOnError();
        }
        catch (ProcessingException e) {
          throw new JsonException(e);
        }
      }

      LOG.info("Client session with ID " + m_clientSessionId + " terminated.");
    }
  }
}

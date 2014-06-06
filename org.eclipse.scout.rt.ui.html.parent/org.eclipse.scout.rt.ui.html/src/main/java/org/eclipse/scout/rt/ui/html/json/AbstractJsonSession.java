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
import java.util.Locale;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.shared.ui.IUiLayer;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.json.JSONObject;

public abstract class AbstractJsonSession implements IJsonSession, HttpSessionBindingListener {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonSession.class);

  private JsonClientSession m_jsonClientSession;

  // TODO AWE: JsonAdapterFactory überschreibbar machen, via Scout-service
  private final JsonAdapterFactory m_jsonAdapterFactory;

  private final JsonAdapterRegistry m_jsonAdapterRegistry;

  private long m_jsonAdapterSeq;
  private JsonResponse m_currentJsonResponse;
  private HttpServletRequest m_currentHttpRequest;
  private JsonEventProcessor m_jsonEventProcessor;

  public AbstractJsonSession() {
    m_currentJsonResponse = new JsonResponse();
    m_jsonAdapterFactory = new JsonAdapterFactory();
    m_jsonAdapterRegistry = new JsonAdapterRegistry();
  }

  @Override
  public void init(HttpServletRequest request, JsonRequest jsonReq) {
    m_currentHttpRequest = request;
    UserAgent userAgent = createUserAgent(jsonReq);
    Subject subject = initSubject();
    if (subject == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }
    IClientSession clientSession = createClientSession(userAgent, subject, request.getLocale());
    // FIXME AWE/CGU: use sessionId or use createUniqueIdFor? duplicates possible?
    // was <<jsonReq.getSessionPartId()>> before, now createUniqueIdFor is used again
    m_jsonClientSession = (JsonClientSession) getOrCreateJsonAdapter(clientSession);
    m_jsonEventProcessor = new JsonEventProcessor(m_jsonClientSession);
    if (!clientSession.isActive()) {
      throw new JsonException("ClientSession is not active, there must be a problem with loading or starting");
    }
    JSONObject json = initJsonSession(clientSession);
    m_currentJsonResponse.addCreateEvent(jsonReq.getSessionPartId(), json);
    LOG.info("JsonSession initialized");
  }

  /**
   * This call runs in a Scout job and creates the initial JSON session object with the Desktop.
   */
  private JSONObject initJsonSession(IClientSession clientSession) {
    final Holder<JSONObject> jsonHolder = new Holder<>(JSONObject.class);
    new ClientSyncJob("AbstractJsonSession#init", clientSession) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        jsonHolder.setValue(m_jsonClientSession.toJson());
      }
    }.runNow(new NullProgressMonitor());
    return jsonHolder.getValue();
  }

  protected UserAgent createUserAgent(JsonRequest jsonReq) {
    // FIXME CGU create UiLayer.HTML
    IUiLayer uiLayer = UiLayer.RAP;
    IUiDeviceType uiDeviceType = UiDeviceType.DESKTOP;
    String browserId = m_currentHttpRequest.getHeader("User-Agent");
    JSONObject userAgent = jsonReq.getUserAgent();
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

  protected Subject initSubject() {
    return Subject.getSubject(AccessController.getContext());
  }

  protected abstract Class<? extends IClientSession> clientSessionClass();

  protected IClientSession createClientSession(UserAgent userAgent, Subject subject, Locale locale) {
    LocaleThreadLocal.set(locale);
    try {
      return createClientSessionInternal(clientSessionClass(), userAgent, subject, locale, UUID.randomUUID().toString());
      //FIXME CGU session must be started later, see JsonClientSession
      //return SERVICES.getService(IClientSessionRegistryService.class).newClientSession(clientSessionClass(), subject, UUID.randomUUID().toString(), userAgent);
    }
    finally {
      LocaleThreadLocal.set(null);
    }
  }

  private IClientSession createClientSessionInternal(Class<? extends IClientSession> clazz, UserAgent userAgent, Subject subject, Locale locale, String virtualSessionId) {
    IClientSession clientSession;
    try {
      clientSession = clazz.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e) {
      throw new JsonException("Could not create client session.", e);
    }

    clientSession.setSubject(subject);
    if (virtualSessionId != null) {
      clientSession.setVirtualSessionId(virtualSessionId);
    }
    clientSession.setUserAgent(userAgent);

    return clientSession;
  }

  public void dispose() {
    m_jsonAdapterRegistry.dispose();
  }

  @Override
  public IClientSession getClientSession() {
    return m_jsonClientSession.getModel();
  }

  @Override
  public String createUniqueIdFor(IJsonAdapter jsonAdapter) {
    //FIXME CGU create id based on scout object for automatic gui testing, use @classId? or CustomWidgetIdGenerator from scout.ui.rwt bundle?
    return "" + (++m_jsonAdapterSeq);
  }

  @Override
  public IJsonAdapter<?> getOrCreateJsonAdapter(Object model) {
    IJsonAdapter<?> jsonAdapter = getJsonAdapter(model);
    if (jsonAdapter != null) {
      return jsonAdapter;
    }
    jsonAdapter = createJsonAdapter(model);
    return jsonAdapter;
  }

  @Override
  public IJsonAdapter<?> createJsonAdapter(Object model) {
    String id = createUniqueIdFor(null); //FIXME cgu
    IJsonAdapter<?> jsonAdapter = m_jsonAdapterFactory.createJsonAdapter(model, this, id);
    jsonAdapter.init();
    return jsonAdapter;
  }

  @Override
  public IJsonAdapter<?> getJsonAdapter(String id) {
    return m_jsonAdapterRegistry.getJsonAdapter(id);
  }

  @Override
  public IJsonAdapter<?> getJsonAdapter(Object model) {
    return m_jsonAdapterRegistry.getJsonAdapter(model);
  }

  @Override
  public void registerJsonAdapter(IJsonAdapter<?> adapter) {
    m_jsonAdapterRegistry.addJsonAdapter(adapter);
  }

  @Override
  public void unregisterJsonAdapter(String id) {
    m_jsonAdapterRegistry.removeJsonAdapter(id);
  }

  @Override
  public JsonResponse currentJsonResponse() {
    return m_currentJsonResponse;
  }

  @Override
  public HttpServletRequest currentHttpRequest() {
    return m_currentHttpRequest;
  }

  @Override
  public JsonResponse processRequest(HttpServletRequest httpRequest, JsonRequest jsonRequest) {
    try {
      m_currentHttpRequest = httpRequest;
      // FIXME CGU should only be done after pressing reload, maybe on get request? first we need to fix reload bug, see FIXME in AbstractJsonServlet
      m_jsonClientSession.processRequestLocale(httpRequest.getLocale());
      JsonResponse jsonResponse = currentJsonResponse();
      m_jsonEventProcessor.processEvents(jsonRequest, jsonResponse);
      return jsonResponse;
    }
    finally {
      // reset event map (aka jsonResponse) when response has been sent to client
      m_currentJsonResponse = new JsonResponse();
    }
  }

  @Override
  public void valueBound(HttpSessionBindingEvent event) {

  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent event) {
    LOG.info("Terminating json session " + event.getName() + "...");

    //Detach from model
    dispose();

    //Dispose model
    final IClientSession clientSession = getClientSession();
    if (!clientSession.isActive()) {
      //client session was probably already stopped by the model itself
      return;
    }

    new ClientSyncJob("Disposing client session", clientSession) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        clientSession.getDesktop().getUIFacade().fireDesktopClosingFromUI(true);
      }
    }.runNow(new NullProgressMonitor());
    LOG.info("Session " + event.getName() + " terminated.");
  }

}

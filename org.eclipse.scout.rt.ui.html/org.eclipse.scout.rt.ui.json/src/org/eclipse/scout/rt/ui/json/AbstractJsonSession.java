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
package org.eclipse.scout.rt.ui.json;

import java.security.AccessController;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public abstract class AbstractJsonSession implements IJsonSession {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonSession.class);

  private JsonClientSession m_jsonClientSession;
  private final Map<String, IJsonRenderer> m_jsonRenderers;
  private long m_jsonRendererSeq;
  private JsonResponse m_currentJsonResponse;
  private HttpServletRequest m_currentHttpRequest;

  public AbstractJsonSession() {
    m_jsonRenderers = new HashMap<>();
    m_currentJsonResponse = new JsonResponse();
  }

  @Override
  public void init(HttpServletRequest request, String sessionId) throws JsonUIException {
    m_currentHttpRequest = request;
    UserAgent userAgent = createUserAgent();
    Subject subject = initSubject();
    if (subject == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }
    IClientSession clientSession = createClientSession(userAgent, subject, request.getLocale());

    m_jsonClientSession = new JsonClientSession(clientSession, this, sessionId); //FIXME use sessionId use createUniqueIdFor? duplicates possible?
    m_jsonClientSession.init();

    if (!clientSession.isActive()) {
      throw new JsonUIException("ClientSession is not active, there must be a problem with loading or starting");
    }
    LOG.info("JsonSession initialized.");
  }

  protected UserAgent createUserAgent() {
    //FIXME create UiLayer.Json, or better let deliver from real gui -> html?
    return UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP);
  }

  protected Subject initSubject() {
    return Subject.getSubject(AccessController.getContext());
  }

  protected abstract Class<? extends IClientSession> clientSessionClass();

  protected IClientSession createClientSession(UserAgent userAgent, Subject subject, Locale locale) {
    LocaleThreadLocal.set(locale);
    try {
      return createClientSessionInternal(clientSessionClass(), userAgent, subject, locale, UUID.randomUUID().toString());
      //FIXME session must be started later, see JsonClientSession
      //return SERVICES.getService(IClientSessionRegistryService.class).newClientSession(clientSessionClass(), subject, UUID.randomUUID().toString(), userAgent);
    }
    finally {
      LocaleThreadLocal.set(null);
    }
  }

  private IClientSession createClientSessionInternal(Class<? extends IClientSession> clazz, UserAgent userAgent, Subject subject, Locale locale, String virtualSessionId) {
    try {
      IClientSession clientSession = clazz.newInstance();
      clientSession.setSubject(subject);
      if (virtualSessionId != null) {
        clientSession.setVirtualSessionId(virtualSessionId);
      }
      clientSession.setUserAgent(userAgent);

      return clientSession;
    }
    catch (Throwable t) {
      LOG.error("could not load session", t);
      return null;
    }
  }

  public void dispose() throws JsonUIException {
    //FIXME call dispose (from session invalidation listener and desktop close event?)
    for (IJsonRenderer renderer : m_jsonRenderers.values()) {
      renderer.dispose();
    }
  }

  @Override
  public IClientSession getClientSession() {
    return m_jsonClientSession.getModelObject();
  }

  @Override
  public String createUniqueIdFor(IJsonRenderer renderer) {
    //FIXME create id based on scout object for automatic gui testing, use @classId? or CustomWidgetIdGenerator from scout.ui.rwt bundle?
    return "" + (++m_jsonRendererSeq);
  }

  @Override
  public void registerJsonRenderer(String id, IJsonRenderer renderer) {
    m_jsonRenderers.put(id, renderer);
  }

  @Override
  public void unregisterJsonRenderer(String id) {
    m_jsonRenderers.remove(id);
  }

  @Override
  public IJsonRenderer getJsonRenderer(String id) {
    return m_jsonRenderers.get(id);
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
  public JsonResponse processRequest(HttpServletRequest httpReq, JsonRequest jsonReq) throws JsonUIException {
    m_currentHttpRequest = httpReq;

    //FIXME should only be done after pressing reload, maybe on get request? first we need to fix reload bug, see FIXME in AbstractJsonServlet
    m_jsonClientSession.processRequestLocale(httpReq.getLocale());

    final JsonResponse res = currentJsonResponse();
    //FIXME maybe rename event to action? request=actions, response=events?
    for (JsonEvent event : jsonReq.getEvents()) {
      processEvent(event, res);
    }

    //Clear event map when sent to client
    m_currentJsonResponse = new JsonResponse();
    return res;
  }

  protected void processEvent(JsonEvent event, JsonResponse res) {
    final String id = event.getEventId();
    final IJsonRenderer jsonRenderer = getJsonRenderer(id);
    if (jsonRenderer == null) {
      throw new JsonUIException("No renderer found for id " + id);
    }
    try {
      LOG.info("Handling event. Type: " + event.getEventType() + ", Id: " + id);
      jsonRenderer.handleUiEvent(event, res);
    }
    catch (Throwable t) {
      LOG.error("Handling event. Type: " + event.getEventType() + ", Id: " + id, t);
    }
  }

}

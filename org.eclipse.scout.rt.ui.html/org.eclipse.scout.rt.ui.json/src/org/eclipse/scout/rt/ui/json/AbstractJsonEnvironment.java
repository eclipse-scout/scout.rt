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
import java.util.Map;
import java.util.UUID;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.SERVICES;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonEnvironment implements IJsonEnvironment {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonEnvironment.class);

  private IClientSession m_clientSession;
  private Class<? extends IClientSession> m_clientSessionClass;
  private JSONArray m_events;
  private Map<String, IJsonRenderer> m_jsonRenderers;
  private long lastId;

  public AbstractJsonEnvironment(Class<? extends IClientSession> clientSessionClass) {
    m_clientSessionClass = clientSessionClass;
    m_events = new JSONArray();
    m_jsonRenderers = new HashMap<>();
  }

  @Override
  public void init() throws ProcessingException {
    UserAgent userAgent = createUserAgent();
    IClientSession clientSession = createClientSession(userAgent);
    if (!clientSession.isActive()) {
      throw new ProcessingException("ClientSession is not active, there must be a problem with loading or starting");
    }
    m_clientSession = clientSession;
    JsonDesktop jsonDesktop = new JsonDesktop(m_clientSession.getDesktop(), this);
    jsonDesktop.init();
    LOG.info("JsonEnvironment initialized.");
  }

  public void dispose() throws ProcessingException {
    //TODO call dispose (from session invalidation listener and desktop close event?)
    for (IJsonRenderer renderer : m_jsonRenderers.values()) {
      renderer.dispose();
    }
  }

  protected IClientSession createClientSession(UserAgent userAgent) {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      throw new SecurityException("/json request is not authenticated with a Subject");
    }
    return SERVICES.getService(IClientSessionRegistryService.class).newClientSession(m_clientSessionClass, subject, UUID.randomUUID().toString(), userAgent);
  }

  protected UserAgent createUserAgent() {
    //TODO create UiLayer.Json, or better let deliver from real gui -> html?
    return UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP);
  }

  @Override
  public JSONObject processRequest(JSONObject json) throws ProcessingException {
    try {
      String type = (String) json.get("type");
      final String id = json.getString("id");
      IJsonRenderer jsonRenderer = getJsonRenderer(id);
      if (jsonRenderer == null) {
        throw new ProcessingException("No renderer found for id " + id);
      }

      LOG.info("Handling event. Type: " + type + ", Id: " + id);
      jsonRenderer.handleUiEvent(type);

      JSONObject response = new JSONObject();
      response.put("events", m_events);
      return response;
    }
    catch (JSONException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  private IJsonRenderer getJsonRenderer(String id) {
    return m_jsonRenderers.get(id);
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
  public IClientSession getClientSession() {
    return m_clientSession;
  }

  @Override
  public String createUniqueIdFor(IJsonRenderer renderer) {
    //TODO create id based on scout object for automatic gui testing, see CustomWidgetIdGenerator from scout.ui.rwt bundle
    return ++lastId + "";
  }

  @Override
  public void addCreateEvent(IJsonRenderer jsonRenderer) {
    m_events = new JSONArray(); //TODO
    try {
      JSONObject pc = new JSONObject();
      pc.put("type", "create");
      pc.put("object", jsonRenderer.toJson());
      m_events.put(pc);
    }
    catch (ProcessingException | JSONException e) {
      LOG.error("", e);
    }
  }

  @Override
  public void addUpdateEvent(String widgetId, String name, Object newValue) {
    try {
      JSONObject pc = new JSONObject();
      pc.put("type", "update");
      pc.put("id", widgetId);
      pc.put(name, newValue);
      m_events.put(pc);
    }
    catch (JSONException e) {
      LOG.error("", e);
    }
  }

}

/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import static java.util.Collections.emptyMap;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.util.StreamUtility;
import org.eclipse.scout.rt.shared.session.ISessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONObject;

public class JsonClientSession<CLIENT_SESSION extends IClientSession> extends AbstractJsonPropertyObserver<CLIENT_SESSION> {

  private ISessionListener m_sessionListener;

  public JsonClientSession(CLIENT_SESSION model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Session";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_sessionListener != null) {
      throw new IllegalStateException();
    }
    m_sessionListener = new P_SessionListener();
    getModel().addListener(m_sessionListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_sessionListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeListener(m_sessionListener);
    m_sessionListener = null;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getDesktop());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("objectType", getObjectType());
    putAdapterIdProperty(json, "desktop", getModel().getDesktop());
    return json;
  }

  @Override
  protected void initJsonProperties(CLIENT_SESSION model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<>(IClientSession.PROP_SHARED_VARIABLE_MAP, model) {
      @Override
      protected Map<String, Object> modelValue() {
        return getExposedSharedVariables();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return MainJsonObjectFactory.get().createJsonObject(value).toJson();
      }
    });
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (IClientSession.PROP_LOCALE.equals(propertyName)) {
      getUiSession().sendLocaleChangedEvent((Locale) newValue);
    }
    else {
      super.handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }

  protected Map<String, Object> getExposedSharedVariables() {
    var exposedSharedVariables = getModel().getExposedSharedVariables();
    if (exposedSharedVariables.isEmpty()) {
      return emptyMap();
    }
    return getModel().getSharedVariableMap()
        .entrySet().stream()
        .filter(entry -> exposedSharedVariables.contains(entry.getKey()))
        .collect(StreamUtility.toMap(Entry::getKey, Entry::getValue));
  }

  protected void handleModelSessionEvent(SessionEvent event) {
    switch (event.getType()) {
      case SessionEvent.TYPE_STOPPING:
        handleModelSessionStopping();
        break;
      default:
        // NOP
    }
  }

  protected void handleModelSessionStopping() {
    getUiSession().logout();
  }

  protected class P_SessionListener implements ISessionListener {

    @Override
    public void sessionChanged(SessionEvent event) {
      handleModelSessionEvent(event);
    }
  }
}

/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid.uicallback;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback.UiCallbackEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback.UiCallbacks;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.event.IEventListener;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.json.JSONObject;

public class JsonUiCallbacks<T extends UiCallbacks> extends AbstractJsonAdapter<T> {

  private final LazyValue<JsonDataObjectHelper> m_jsonDoHelper = new LazyValue<>(JsonDataObjectHelper.class); // cached instance
  private IEventListener<UiCallbackEvent> m_listener;

  public JsonUiCallbacks(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "UiCallbacks";
  }

  protected JsonDataObjectHelper jsonDoHelper() {
    return m_jsonDoHelper.get();
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_listener != null) {
      throw new IllegalStateException();
    }
    m_listener = new P_UiCallbacksListener();
    getModel().getEventSupport().addListener(m_listener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_listener == null) {
      throw new IllegalStateException();
    }
    getModel().getEventSupport().removeListener(m_listener);
    m_listener = null;
  }

  protected void handleModelEvent(UiCallbackEvent event) {
    addActionEvent("uiCallback", createJsonUiCallbackEvent(event));
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("uiResponse".equals(event.getType())) {
      handleUiResponse(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiResponse(JsonEvent event) {
    JSONObject response = event.getData(); // UiCallbackResponse
    String id = response.getString("id");
    if (StringUtility.isNullOrEmpty(id)) {
      return; // invalid event
    }
    UiCallbackErrorDo error = jsonDoHelper().jsonToDataObject(response.optJSONObject("error"), UiCallbackErrorDo.class);
    if (error != null) {
      getModel().getUIFacade().fireCallbackFailed(id, error.getMessage(), error.getCode());
    }
    else {
      IDoEntity data = jsonDoHelper().jsonToDataObject(response.optJSONObject("data"), IDoEntity.class);
      getModel().getUIFacade().fireCallbackDone(id, data);
    }
  }

  protected JSONObject createJsonUiCallbackEvent(UiCallbackEvent event) {
    IWidget owner = event.getOwner();
    String ownerId = null;
    if (owner != null) {
      ownerId = getUiSession().getJsonAdapters(owner).stream()
          .findAny()
          .map(IJsonAdapter::getId)
          .orElse(null);
    }
    return new JSONObject() // UiCallbackRemoteEvent
        .put("id", event.getCallbackId())
        .put("owner", ownerId)
        .put("handlerObjectType", event.getJsHandlerObjectType())
        .put("data", jsonDoHelper().dataObjectToJson(event.getData()));
  }

  protected class P_UiCallbacksListener implements IEventListener<UiCallbackEvent> {
    @Override
    public void fireEvent(UiCallbackEvent e) {
      ModelJobs.assertModelThread();
      handleModelEvent(e);
    }
  }
}

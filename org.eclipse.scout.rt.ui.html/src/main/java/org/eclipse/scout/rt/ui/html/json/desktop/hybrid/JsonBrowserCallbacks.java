/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.BrowserCallbackEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.BrowserCallbacks;
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

public class JsonBrowserCallbacks<T extends BrowserCallbacks> extends AbstractJsonAdapter<T> {

  private final LazyValue<JsonDataObjectHelper> m_jsonDoHelper = new LazyValue<>(JsonDataObjectHelper.class); // cached instance
  private IEventListener<BrowserCallbackEvent> m_listener;

  public JsonBrowserCallbacks(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "BrowserCallbacks";
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
    m_listener = new P_BrowserCallbacksListener();
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

  protected void handleModelEvent(BrowserCallbackEvent event) {
    addActionEvent("browserCallback", createJsonBrowserCallbackEvent(event));
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("browserResponse".equals(event.getType())) {
      handleBrowserResponse(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleBrowserResponse(JsonEvent event) {
    JSONObject response = event.getData(); // BrowserCallbackResponse
    String id = response.getString("id");
    if (StringUtility.isNullOrEmpty(id)) {
      return; // invalid event
    }
    BrowserCallbackErrorDo error = jsonDoHelper().jsonToDataObject(response.optJSONObject("error"), BrowserCallbackErrorDo.class);
    if (error != null) {
      getModel().getUIFacade().fireCallbackFailed(id, error.getMessage(), error.getCode());
    }
    else {
      IDoEntity data = jsonDoHelper().jsonToDataObject(response.optJSONObject("data"), IDoEntity.class);
      getModel().getUIFacade().fireCallbackDone(id, data);
    }
  }

  protected JSONObject createJsonBrowserCallbackEvent(BrowserCallbackEvent event) {
    IWidget owner = event.getOwner();
    String ownerId = null;
    if (owner != null) {
      ownerId = getUiSession().getJsonAdapters(owner).stream()
          .findAny()
          .map(IJsonAdapter::getId)
          .orElse(null);
    }
    return new JSONObject()
        .put("id", event.getCallbackId())
        .put("owner", ownerId)
        .put("handlerObjectType", event.getJsHandlerObjectType())
        .put("data", jsonDoHelper().dataObjectToJson(event.getData()));
  }

  protected class P_BrowserCallbacksListener implements IEventListener<BrowserCallbackEvent> {
    @Override
    public void fireEvent(BrowserCallbackEvent e) {
      ModelJobs.assertModelThread();
      handleModelEvent(e);
    }
  }
}

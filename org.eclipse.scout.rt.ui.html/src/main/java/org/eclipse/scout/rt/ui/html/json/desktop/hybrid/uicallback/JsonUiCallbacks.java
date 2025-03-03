/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid.uicallback;

import java.util.Optional;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElements;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback.UiCallbackEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback.UiCallbacks;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.event.IEventListener;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.desktop.hybrid.JsonHybridHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUiCallbacks<T extends UiCallbacks> extends AbstractJsonAdapter<T> {

  protected static final String EVENT_CALLBACK = "callback";
  protected static final String EVENT_CALLBACK_END = "callbackEnd";

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
    handleModelCallback(event);
  }

  protected void handleModelCallback(UiCallbackEvent event) {
    String ownerId = Optional.ofNullable(event.getOwner())
        .flatMap(owner -> getUiSession().getJsonAdapters(owner).stream().findAny())
        .map(IJsonAdapter::getId)
        .orElse(null);
    JSONObject json = new JSONObject() // UiCallbackRemoteEvent
        .put("id", event.getCallbackId())
        .put("owner", ownerId)
        .put("handlerObjectType", event.getJsHandlerObjectType())
        .put("data", jsonDoHelper().dataObjectToJson(event.getData()))
        .putOpt("contextElements", BEANS.get(JsonHybridHelper.class).contextElementsToJson(getUiSession(), event.getContextElements()));

    addActionEvent(EVENT_CALLBACK, json);
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_CALLBACK_END.equals(event.getType())) {
      handleUiCallbackEnd(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiCallbackEnd(JsonEvent event) {
    JSONObject eventData = event.getData();
    String id = eventData.getString("id");
    if (StringUtility.isNullOrEmpty(id)) {
      return; // invalid event
    }
    UiCallbackErrorDo error = jsonDoHelper().jsonToDataObject(eventData.optJSONObject("error"), UiCallbackErrorDo.class);
    if (error != null) {
      getModel().getUIFacade().fireCallbackFailedFromUI(id, error.getMessage(), error.getCode());
    }
    else {
      Object data = parseData(eventData.opt("data"));
      HybridActionContextElements contextElements = BEANS.get(JsonHybridHelper.class).jsonToContextElements(getUiSession(), eventData.optJSONObject("contextElements"));
      getModel().getUIFacade().fireCallbackDoneFromUI(id, data, contextElements);
    }
  }

  protected Object parseData(Object data) {
    if (data == null) {
      return null;
    }
    if (data instanceof JSONObject) {
      return jsonDoHelper().jsonToDataObject((JSONObject) data, IDoEntity.class);
    }
    if (data instanceof JSONArray) {
      return jsonDoHelper().jsonToDataObjects((JSONArray) data, IDoEntity.class);
    }
    return data;
  }

  protected class P_UiCallbacksListener implements IEventListener<UiCallbackEvent> {

    @Override
    public void fireEvent(UiCallbackEvent e) {
      ModelJobs.assertModelThread();
      handleModelEvent(e);
    }
  }
}

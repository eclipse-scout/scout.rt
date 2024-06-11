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

import java.util.Map;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElement;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridEventListener;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonHybridManager<T extends HybridManager> extends AbstractJsonPropertyObserver<T> {
  private static final Logger LOG = LoggerFactory.getLogger(JsonHybridManager.class);
  private HybridEventListener m_listener;

  private final LazyValue<JsonDataObjectHelper> m_jsonDoHelper = new LazyValue<>(JsonDataObjectHelper.class); // cached instance

  public JsonHybridManager(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "scout.HybridManager";
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
    m_listener = new P_HybridEventListener();
    getModel().addHybridEventListener(m_listener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    getModel().removeHybridEventListener(m_listener);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonAdapterProperty<>(HybridManager.PROP_WIDGETS, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder()
            // Create global adapters only for forms because forms initiate disposing by themselves.
            // Every other adapter needs to be disposed by the hybrid manager
            .global(model -> model instanceof IForm)
            .build();
      }

      @Override
      protected Map<String, IWidget> modelValue() {
        return getModel().getWidgets();
      }

      @Override
      protected void createAdapters(Object modelValue) {
        if (modelValue == null) {
          return;
        }
        if (modelValue instanceof Map) {
          ((Map<?, ?>) modelValue).values().forEach(this::createAdapter);
          return;
        }
        throw new IllegalArgumentException("modelValue must be a Map");
      }

      @Override
      protected void disposeObsoleteAdapters(Object newModels) {
        if (newModels == null) {
          return;
        }
        if (newModels instanceof Map) {
          super.disposeObsoleteAdapters(((Map<?, ?>) newModels).values());
          return;
        }
        throw new IllegalArgumentException("modelValue must be a Map");
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        if (value instanceof Map) {
          JSONObject json = new JSONObject();
          //noinspection unchecked
          ((Map<String, Object>) value).forEach((id, widget) -> json.put(id, JsonAdapterUtility.getAdapterIdForModel(getUiSession(), widget, getParentJsonAdapter(), getFilter())));
          return json;
        }
        throw new IllegalArgumentException("modelValue must be a Map");
      }
    });
  }

  protected void handleModelHybridEvent(HybridEvent event) {
    switch (event.getType()) {
      case HybridEvent.TYPE_EVENT:
        addActionEvent("hybridEvent", createJsonHybridEvent(event));
        break;
      case HybridEvent.TYPE_WIDGET_EVENT:
        addActionEvent("hybridWidgetEvent", createJsonHybridEvent(event));
        break;
      default:
        throw new IllegalArgumentException("Unsupported event type");
    }
  }

  protected JSONObject createJsonHybridEvent(HybridEvent event) {
    return new JSONObject()
        .put("id", event.getId())
        .put("eventType", event.getEventType())
        .put("data", jsonDoHelper().dataObjectToJson(event.getData()));
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("hybridAction".equals(event.getType())) {
      handleUiHybridAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiHybridAction(JsonEvent event) {
    JSONObject eventData = event.getData();
    String id = eventData.getString("id");
    String actionType = eventData.getString("actionType");
    IDoEntity data = jsonDoHelper().jsonToDataObject(eventData.optJSONObject("data"), IDoEntity.class);

    LOG.debug("Handling hybrid action '{}' for id '{}'", actionType, id);
    try {
      getModel().getUIFacade().handleHybridActionFromUI(id, actionType, data);
    }
    catch (Exception e) {
      // Exceptions are handled differently depending on their type (e.g. VetoExceptions are displayed to the user).
      // Therefore, do not create a new exception with the error message but log an info and rethrow the original exception.
      LOG.info("Handling hybrid action '{}' for id '{}' failed", actionType, id);
      throw e;
    }
  }

  protected class P_HybridEventListener implements HybridEventListener {
    @Override
    public void handle(HybridEvent e) {
      ModelJobs.assertModelThread();
      handleModelHybridEvent(e);
    }
  }
}

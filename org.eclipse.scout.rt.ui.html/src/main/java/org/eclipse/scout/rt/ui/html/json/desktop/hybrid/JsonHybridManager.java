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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElement;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElements;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridEvent;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridEventListener;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.desktop.hybrid.converter.IHybridActionContextElementConverter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.json.JSONArray;
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
        .put("data", jsonDoHelper().dataObjectToJson(event.getData()))
        .putOpt("contextElements", contextElementsToJson(event.getContextElements()));
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
    // FIXME bsh [js-bookmark] How to handle deserialization errors and still return an 'actionEnd' event?
    IDoEntity data = jsonDoHelper().jsonToDataObject(eventData.optJSONObject("data"), IDoEntity.class);
    HybridActionContextElements contextElements = jsonToContextElements(eventData.optJSONObject("contextElements"));

    LOG.debug("Handling hybrid action '{}' for id '{}'", actionType, id);
    try {
      getModel().getUIFacade().handleHybridActionFromUI(id, actionType, data, contextElements);
    }
    catch (Exception e) {
      // Exceptions are handled differently depending on their type (e.g. VetoExceptions are displayed to the user).
      // Therefore, do not create a new exception with the error message but log an info and rethrow the original exception.
      LOG.info("Handling hybrid action '{}' for id '{}' failed", actionType, id);
      throw e;
    }
  }

  protected JSONObject contextElementsToJson(HybridActionContextElements contextElements) {
    if (contextElements == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    contextElements.getMap().forEach((key, list) -> json.putOpt(key, contextElementListToJson(list)));
    return json;
  }

  protected JSONArray contextElementListToJson(List<HybridActionContextElement> contextElements) {
    if (contextElements == null) {
      return null;
    }
    JSONArray jsonArray = new JSONArray();
    contextElements.forEach(contextElement -> jsonArray.put(contextElementToJson(contextElement)));
    return jsonArray;
  }

  protected JSONObject contextElementToJson(HybridActionContextElement contextElement) {
    if (contextElement == null) {
      return null;
    }
    IJsonAdapter<?> adapter = findAdapter(contextElement.getWidget());
    Object jsonElement = modelElementToJson(adapter, contextElement.optElement());

    JSONObject json = new JSONObject();
    json.put("widget", adapter.getId());
    json.putOpt("element", jsonElement);
    return json;
  }

  protected IJsonAdapter<?> findAdapter(IWidget widget) {
    IJsonAdapter<?> rootAdapter = getUiSession().getRootJsonAdapter();
    IJsonAdapter<?> adapter = JsonAdapterUtility.findChildAdapter(rootAdapter, widget);
    if (adapter == null) {
      throw new IllegalStateException("Adapter not found " + widget);
    }
    return adapter;
  }

  protected Object modelElementToJson(IJsonAdapter<?> adapter, Object modelElement) {
    if (modelElement == null) {
      return null;
    }
    for (IHybridActionContextElementConverter<?, ?, ?> converter : BEANS.all(IHybridActionContextElementConverter.class)) {
      Object jsonElement = converter.tryConvertToJson(adapter, modelElement);
      if (jsonElement != null) {
        return jsonElement;
      }
    }
    throw new ProcessingException("Unable to convert model element to JSON [adapter={}, modelElement={}]", adapter, modelElement);
  }

  protected HybridActionContextElements jsonToContextElements(JSONObject jsonContextElements) {
    if (jsonContextElements == null) {
      return null;
    }
    HybridActionContextElements contextElements = BEANS.get(HybridActionContextElements.class);
    jsonContextElements.keys().forEachRemaining(key -> {
      List<HybridActionContextElement> list = jsonToContextElementList(jsonContextElements.optJSONArray(key));
      contextElements.withElements(key, list);
    });
    return contextElements;
  }

  protected List<HybridActionContextElement> jsonToContextElementList(JSONArray jsonContextElements) {
    if (jsonContextElements == null) {
      return null;
    }
    List<HybridActionContextElement> list = new ArrayList<>();
    for (int i = 0; i < jsonContextElements.length(); i++) {
      JSONObject jsonContextElement = jsonContextElements.optJSONObject(i);
      HybridActionContextElement contextElement = jsonToContextElement(jsonContextElement);
      if (contextElement != null) {
        list.add(contextElement);
      }
    }
    return list;
  }

  protected HybridActionContextElement jsonToContextElement(JSONObject jsonContextElement) {
    if (jsonContextElement == null) {
      return null;
    }
    String adapterId = jsonContextElement.getString("widget");
    IJsonAdapter<?> adapter = getUiSession().getJsonAdapter(adapterId);
    IWidget widget = Assertions.assertInstance(adapter.getModel(), IWidget.class);

    Object jsonElement = jsonContextElement.opt("element");
    Object modelElement = jsonToModelElement(adapter, jsonElement);

    return HybridActionContextElement.of(widget, modelElement);
  }

  protected Object jsonToModelElement(IJsonAdapter<?> adapter, Object jsonElement) {
    if (jsonElement == null) {
      return null;
    }
    for (IHybridActionContextElementConverter<?, ?, ?> converter : BEANS.all(IHybridActionContextElementConverter.class)) {
      Object modelElement = converter.tryConvertFromJson(adapter, jsonElement);
      if (modelElement != null) {
        return modelElement;
      }
    }
    throw new ProcessingException("Unable to convert JSON to model element [adapter={}, jsonElement={}]", adapter, jsonElement);
  }

  protected class P_HybridEventListener implements HybridEventListener {
    @Override
    public void handle(HybridEvent e) {
      ModelJobs.assertModelThread();
      handleModelHybridEvent(e);
    }
  }
}

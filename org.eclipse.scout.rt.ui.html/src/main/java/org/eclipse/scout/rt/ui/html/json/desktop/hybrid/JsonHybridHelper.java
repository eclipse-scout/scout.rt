/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElement;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElements;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.desktop.hybrid.converter.IHybridActionContextElementConverter;
import org.json.JSONArray;
import org.json.JSONObject;

@ApplicationScoped
public class JsonHybridHelper {

  public JSONObject contextElementsToJson(IUiSession uiSession, HybridActionContextElements contextElements) {
    Assertions.assertNotNull(uiSession, "Missing UI session");
    if (contextElements == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    contextElements.getMap().forEach((key, list) -> json.putOpt(key, contextElementListToJson(uiSession, list)));
    return json;
  }

  protected JSONArray contextElementListToJson(IUiSession uiSession, List<HybridActionContextElement> contextElements) {
    if (contextElements == null) {
      return null;
    }
    JSONArray jsonArray = new JSONArray();
    contextElements.forEach(contextElement -> jsonArray.put(contextElementToJson(uiSession, contextElement)));
    return jsonArray;
  }

  protected JSONObject contextElementToJson(IUiSession uiSession, HybridActionContextElement contextElement) {
    if (contextElement == null) {
      return null;
    }
    IJsonAdapter<?> adapter = findAdapter(uiSession, contextElement.getWidget());
    Object jsonElement = modelElementToJson(adapter, contextElement.optElement());

    JSONObject json = new JSONObject();
    json.put("widget", adapter.getId());
    json.putOpt("element", jsonElement);
    return json;
  }

  protected IJsonAdapter<?> findAdapter(IUiSession uiSession, IWidget widget) {
    IJsonAdapter<?> rootAdapter = uiSession.getRootJsonAdapter();
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

  // -----------------------------

  public HybridActionContextElements jsonToContextElements(IUiSession uiSession, JSONObject jsonContextElements) {
    Assertions.assertNotNull(uiSession, "Missing UI session");
    if (jsonContextElements == null) {
      return null;
    }
    HybridActionContextElements contextElements = BEANS.get(HybridActionContextElements.class);
    jsonContextElements.keys().forEachRemaining(key -> {
      List<HybridActionContextElement> list = jsonToContextElementList(uiSession, jsonContextElements.optJSONArray(key));
      contextElements.withElements(key, list);
    });
    return contextElements;
  }

  protected List<HybridActionContextElement> jsonToContextElementList(IUiSession uiSession, JSONArray jsonContextElements) {
    if (jsonContextElements == null) {
      return null;
    }
    List<HybridActionContextElement> list = new ArrayList<>();
    for (int i = 0; i < jsonContextElements.length(); i++) {
      JSONObject jsonContextElement = jsonContextElements.optJSONObject(i);
      HybridActionContextElement contextElement = jsonToContextElement(uiSession, jsonContextElement);
      if (contextElement != null) {
        list.add(contextElement);
      }
    }
    return list;
  }

  protected HybridActionContextElement jsonToContextElement(IUiSession uiSession, JSONObject jsonContextElement) {
    if (jsonContextElement == null) {
      return null;
    }
    String adapterId = jsonContextElement.getString("widget");
    IJsonAdapter<?> adapter = uiSession.getJsonAdapter(adapterId);
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
}

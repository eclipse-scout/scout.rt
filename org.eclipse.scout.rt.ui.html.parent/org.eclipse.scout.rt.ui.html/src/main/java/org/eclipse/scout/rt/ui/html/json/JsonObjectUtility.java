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

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class catches the checked {@link JSONException} used on classes of org.json.* and converts them into
 * our {@link JsonException} runtime exception.
 */
public final class JsonObjectUtility {

  private JsonObjectUtility() {
    // static access only
  }

  private static JsonException toRuntimeException(JSONException e) {
    return new JsonException(e.getMessage(), e);
  }

  /**
   * Adds a property to the given JSON object and deals with exceptions.
   */
  public static JSONObject putProperty(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
      return json;
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static JSONObject putAdapterProperty(JSONObject object, IJsonSession session, String propertyName, Object model) {
    return putProperty(object, propertyName, session.getOrCreateJsonAdapter(model));
  }

  public static String getString(JSONObject json, String key) {
    try {
      return json.getString(key);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static boolean getBoolean(JSONObject json, String key) {
    try {
      return json.getBoolean(key);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static JSONObject getJSONObject(JSONArray json, int index) {
    try {
      return json.getJSONObject(index);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static JSONArray getJSONArray(JSONObject json, String key) {
    try {
      return json.getJSONArray(key);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static Object get(JSONArray json, int index) {
    try {
      return json.get(index);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static JSONObject newJSONObject(String source) {
    try {
      return new JSONObject(source);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static JSONArray newJSONArray(String source) {
    try {
      return new JSONArray(source);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static Object newJSONArray(String[] array) {
    try {
      return new JSONArray(array);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static void disposeJsonAdapters(IJsonSession session, Collection<? extends Object> models) {
    for (Object model : models) {
      disposeJsonAdapter(session, model);
    }
  }

  public static void disposeJsonAdapter(IJsonSession session, Object model) {
    IJsonAdapter<?> jsonAdapter = session.getJsonAdapter(model);
    //on session dispose, the adapters get disposed in random order, so the may already be disposed when calling this method
    if (jsonAdapter != null) {
      jsonAdapter.dispose();
    }
  }
}

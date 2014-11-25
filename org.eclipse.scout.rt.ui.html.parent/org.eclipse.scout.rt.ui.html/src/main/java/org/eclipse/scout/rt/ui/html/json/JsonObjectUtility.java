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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.beans.FastBeanInfo;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
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

  public static String getString(JSONObject json, String key) {
    try {
      return json.getString(key);
    }
    catch (JSONException e) {
      throw toRuntimeException(e);
    }
  }

  public static int getInt(JSONObject json, String key) {
    try {
      return json.getInt(key);
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

  public static JSONObject getJSONObject(JSONObject json, String key) {
    try {
      return json.getJSONObject(key);
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

  public static Object get(JSONObject json, String key) {
    try {
      return json.get(key);
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

  public static JSONArray adapterIdsToJson(Collection<IJsonAdapter<?>> adapters) {
    JSONArray array = new JSONArray();
    for (IJsonAdapter<?> adapter : adapters) {
      array.put(adapter.getId());
    }
    return array;
  }

  /**
   * Convert a java bean to a json object.
   * <p>
   * The java class may have public fields or getter/setter methods.
   * <p>
   * Valid data types are: boolean, int, long, String, byte[], array of before mentioned types.
   *
   * @param o
   *          the java bean
   */
  public static Object javaToJson(Object o) {
    if (o == null) {
      return null;
    }
    Class<?> type = o.getClass();
    //blob
    if (type == byte[].class) {
      JSONObject b64 = new JSONObject();
      putProperty(b64, "b64", Base64Utility.encode((byte[]) o));
    }
    //array
    if (type.isArray()) {
      JSONArray jarray = new JSONArray();
      int n = Array.getLength(o);
      for (int i = 0; i < n; i++) {
        jarray.put(javaToJson(Array.get(o, i)));
      }
      return jarray;
    }
    if (type == String.class) {
      return o;
    }
    if (type.isPrimitive() || type == Integer.class || type == Long.class || type == Boolean.class) {
      return o.toString();
    }
    //bean
    if (type.getName().startsWith("java.")) {
      throw new IllegalArgumentException("Cannot convert " + type + " to json object");
    }
    try {
      JSONObject jbean = new JSONObject();
      for (Field f : type.getFields()) {
        if (Modifier.isStatic(f.getModifiers())) {
          continue;
        }
        String key = f.getName();
        Object val = f.get(o);
        jbean.put(key, javaToJson(val));
      }
      FastBeanInfo beanInfo = new FastBeanInfo(type, Object.class);
      for (FastPropertyDescriptor desc : beanInfo.getPropertyDescriptors()) {
        Method m = desc.getReadMethod();
        if (m == null) {
          continue;
        }
        String key = desc.getName();
        Object val = m.invoke(o);
        jbean.put(key, javaToJson(val));
      }
      return jbean;
    }
    catch (Exception e) {
      throw new IllegalArgumentException(type + " to json", e);
    }
  }

  /**
   * Convert a json object to a java bean.
   * <p>
   * The java class may have public fields or getter/setter methods.
   * <p>
   * Valid data types are: boolean, int, long, String, byte[], array of before mentioned types.
   *
   * @param jsonObjectOrArray
   * @param type
   * @param throwForMissingProperty
   *          when set to true then throws an exception if a json property does not exist in the java object, when set
   *          to false ignores this event.
   */
  @SuppressWarnings("unchecked")
  public static <T> T jsonToJava(Object jsonObjectOrArray, Class<T> type, boolean throwForMissingProperty) {
    Object jo = jsonObjectOrArray;
    if (jo == null || jo == JSONObject.NULL) {
      return null;
    }
    //blob
    if (type == byte[].class) {
      return (T) Base64Utility.decode(getString((JSONObject) jo, "b64"));
    }
    //array
    if (jo instanceof JSONArray) {
      JSONArray jarray = (JSONArray) jo;
      int n = jarray.length();
      T array = (T) Array.newInstance(type.getComponentType(), n);
      for (int i = 0; i < n; i++) {
        Array.set(array, i, jsonToJava(jarray.opt(i), type.getComponentType(), throwForMissingProperty));
      }
      return array;
    }
    if (type == String.class) {
      return (T) jo;
    }
    if (type == int.class || type == Integer.class) {
      return (T) new Integer((String) jo);
    }
    if (type == long.class || type == Long.class) {
      return (T) new Long((String) jo);
    }
    if (type == boolean.class || type == Boolean.class) {
      return (T) new Boolean((String) jo);
    }
    //bean
    if (type.getName().startsWith("java.")) {
      throw new IllegalArgumentException("Cannot convert " + type + " from json to java object");
    }
    JSONObject jbean = (JSONObject) jo;
    T o;
    try {
      o = (T) type.newInstance();
    }
    catch (Exception e) {
      throw new IllegalArgumentException("type " + type + " object " + jo, e);
    }
    try {
      String[] nameArray = JSONObject.getNames(jbean);
      HashSet<String> missingNames = new HashSet<>(Arrays.asList(nameArray));
      for (String key : nameArray) {
        try {
          Field f = type.getField(key);
          if (Modifier.isStatic(f.getModifiers())) {
            continue;
          }
          Object val = jsonToJava(jbean.opt(key), f.getType(), throwForMissingProperty);
          f.set(o, val);
          missingNames.remove(key);
        }
        catch (NoSuchElementException nse) {
          //nop
        }
      }
      if (missingNames.size() > 0) {
        FastBeanInfo beanInfo = new FastBeanInfo(type, Object.class);
        for (FastPropertyDescriptor desc : beanInfo.getPropertyDescriptors()) {
          Method m = desc.getWriteMethod();
          if (m == null) {
            continue;
          }
          String key = desc.getName();
          Object val = jsonToJava(jbean.opt(key), m.getParameterTypes()[0], throwForMissingProperty);
          m.invoke(o, val);
          missingNames.remove(key);
        }
      }
      if (throwForMissingProperty && missingNames.size() > 0) {
        throw new IllegalArgumentException("properties " + missingNames + " do not exist in " + type);
      }
      return o;
    }
    catch (Exception e) {
      throw new IllegalArgumentException(jbean + " to " + type, e);
    }
  }

}

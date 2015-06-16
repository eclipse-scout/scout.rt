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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.beans.FastBeanInfo;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.BEANS;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JsonObjectUtility {

  private JsonObjectUtility() {
    // static access only
  }

  /**
   * Puts every property from source to json.
   *
   * @param json
   *          target object
   * @param source
   *          source object
   */
  public static void putProperties(JSONObject json, JSONObject source) {
    if (json == null || source == null) {
      return;
    }
    String[] names = JSONObject.getNames(source);
    if (names == null) {
      return;
    }
    for (String name : names) {
      json.put(name, source.opt(name));
    }
  }

  public static JSONObject newOrderedJSONObject() {
    JSONObject o = new JSONObject();
    // Try to replace the internal hash map by a LinkedHashMap. Unfortunately, there is no API for that...
    // LinkedHashMap preserves the insertion order of properties, which is helpful while debugging purposes.
    try {
      Field field = o.getClass().getDeclaredField("map");
      field.setAccessible(true);
      field.set(o, new LinkedHashMap<>());
    }
    catch (Exception e) {
      // nop
    }
    return o;
  }

  public static JSONArray adapterIdsToJson(Collection<IJsonAdapter<?>> adapters) {
    if (adapters == null) {
      return null;
    }
    JSONArray array = new JSONArray();
    for (IJsonAdapter<?> adapter : adapters) {
      array.put(adapter.getId());
    }
    return array;
  }

  /**
   * Convert a json object property to java
   * <p>
   * The java class may have public fields or getter/setter methods.
   * <p>
   * Valid data types are: boolean, int, long, String, byte[], array of before mentioned types.
   *
   * @param jsonObject
   *          {@link JSONObject}
   * @param type
   * @param throwForMissingProperty
   *          when set to true then throws an exception if a json property does not exist in the java object, when set
   *          to false ignores this event.
   */
  public static <T> T jsonObjectPropertyToJava(JSONObject jsonObject, String propertyName, Class<T> type, boolean throwForMissingProperty) {
    Object jval = getTyped(jsonObject, propertyName, type);
    return jsonValueToJava(jval, type, throwForMissingProperty);
  }

  /**
   * Convert a json array element to java
   * <p>
   * The java class may have public fields or getter/setter methods.
   * <p>
   * Valid data types are: boolean, int, long, String, byte[], array of before mentioned types.
   *
   * @param jsonArray
   *          {@link JSONArray}
   * @param type
   * @param throwForMissingProperty
   *          when set to true then throws an exception if a json property does not exist in the java object, when set
   *          to false ignores this event.
   */
  public static <T> T jsonArrayElementToJava(JSONArray jsonArray, int index, Class<T> type, boolean throwForMissingProperty) {
    if (type == Void.class) {
      return null;
    }
    Object jval = getTyped(jsonArray, index, type);
    return jsonValueToJava(jval, type, throwForMissingProperty);
  }

  @SuppressWarnings("unchecked")
  private static <T> T jsonValueToJava(Object jval, Class<T> type, boolean throwForMissingProperty) {
    if (jval == null || jval == JSONObject.NULL) {
      return null;
    }
    //basic types
    if (type == byte[].class) {
      return (T) jval;
    }
    if (type == String.class) {
      return (T) jval;
    }
    if (type == int.class || type == Integer.class) {
      return (T) jval;
    }
    if (type == long.class || type == Long.class) {
      return (T) jval;
    }
    if (type == boolean.class || type == Boolean.class) {
      return (T) jval;
    }
    //array
    if (jval instanceof JSONArray) {
      JSONArray jarray = (JSONArray) jval;
      int n = jarray.length();
      T array = (T) Array.newInstance(type.getComponentType(), n);
      for (int i = 0; i < n; i++) {
        Array.set(array, i, jsonArrayElementToJava(jarray, i, type.getComponentType(), throwForMissingProperty));
      }
      return array;
    }
    //bean
    JSONObject jbean = (JSONObject) jval;
    T o;
    try {
      o = (T) type.newInstance();
    }
    catch (Exception e) {
      throw new IllegalArgumentException("type " + type + " object " + jval, e);
    }
    try {
      HashSet<String> missingNames = new HashSet<>();
      String[] nameArray = JSONObject.getNames(jbean);
      if (nameArray != null) {
        for (String key : nameArray) {
          missingNames.add(key);
        }
        for (String key : nameArray) {
          try {
            Field f = type.getField(key);
            if (Modifier.isStatic(f.getModifiers())) {
              continue;
            }
            Object val = jsonObjectPropertyToJava(jbean, key, f.getType(), throwForMissingProperty);
            f.set(o, val);
            missingNames.remove(key);
          }
          catch (NoSuchElementException nse) {
            //nop
          }
          catch (NoSuchFieldException nse) {
            //nop
          }
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
          Object val = jsonObjectPropertyToJava(jbean, key, m.getParameterTypes()[0], throwForMissingProperty);
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

  /**
   * @return null, {@link JSONObject}, {@link JSONArray} or a basic type (int, long, boolean, byte[], String) depending
   *         on the value of <code>type</code>
   */
  private static Object getTyped(JSONObject jsonObject, String propertyName, Class<?> type) {
    Object jval = jsonObject.opt(propertyName);
    //null
    if (jval == null || jval == JSONObject.NULL) {
      return null;
    }
    //blob
    if (type == byte[].class) {
      return Base64Utility.decode(jsonObject.getJSONObject(propertyName).getString("b64"));
    }
    //array
    if (jval instanceof JSONArray) {
      return (JSONArray) jval;
    }
    if (type == String.class) {
      return jsonObject.getString(propertyName);
    }
    if (type == int.class || type == Integer.class) {
      return jsonObject.getInt(propertyName);
    }
    if (type == long.class || type == Long.class) {
      return jsonObject.getLong(propertyName);
    }
    if (type == boolean.class || type == Boolean.class) {
      return jsonObject.getBoolean(propertyName);
    }
    //bean
    if (type.getName().startsWith("java.")) {
      throw new IllegalArgumentException("Cannot convert " + type + " from json to java object");
    }
    return jsonObject.getJSONObject(propertyName);
  }

  /**
   * @return null, {@link JSONObject}, {@link JSONArray} or a basic type (int, long, boolean, byte[], String) depending
   *         on the value of <code>type</code>
   */
  private static Object getTyped(JSONArray jsonArray, int index, Class<?> type) {
    Object jval = jsonArray.opt(index);
    //null
    if (jval == null || jval == JSONObject.NULL) {
      return null;
    }
    //blob
    if (type == byte[].class) {
      return Base64Utility.decode(jsonArray.getJSONObject(index).getString("b64"));
    }
    //array
    if (jval instanceof JSONArray) {
      return (JSONArray) jval;
    }
    if (type == String.class) {
      return jsonArray.getString(index);
    }
    if (type == int.class || type == Integer.class) {
      return jsonArray.getInt(index);
    }
    if (type == long.class || type == Long.class) {
      return jsonArray.getLong(index);
    }
    if (type == boolean.class || type == Boolean.class) {
      return jsonArray.getBoolean(index);
    }
    //bean
    if (type.getName().startsWith("java.")) {
      throw new IllegalArgumentException("Cannot convert " + type + " from json to java object");
    }
    return jsonArray.getJSONObject(index);
  }

  public static void filterDefaultValues(JSONObject json) {
    if (json == null) {
      return;
    }
    IDefaultValuesFilterService filterSvc = BEANS.get(IDefaultValuesFilterService.class);
    if (filterSvc != null) {
      filterSvc.filter(json);
    }
  }

  public static void filterDefaultValues(JSONObject json, String objectType) {
    if (json == null) {
      return;
    }
    IDefaultValuesFilterService filterSvc = BEANS.get(IDefaultValuesFilterService.class);
    if (filterSvc != null) {
      filterSvc.filter(json, objectType);
    }
  }
}

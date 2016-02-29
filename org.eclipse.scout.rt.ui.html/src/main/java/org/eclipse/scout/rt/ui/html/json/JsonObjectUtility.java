/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.reflect.FastBeanInfo;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonObjectUtility {
  private static final Logger LOG = LoggerFactory.getLogger(JsonObjectUtility.class);

  private JsonObjectUtility() {
    // static access only
  }

  /**
   * @return never <code>null</code>
   */
  public static String[] getNames(JSONObject source) {
    if (source == null || source.length() == 0) {
      return new String[0];
    }
    String[] names = new String[source.length()];
    int i = 0;
    for (Iterator<String> it = source.keys(); it.hasNext();) {
      String key = it.next();
      names[i++] = key;
    }
    return names;
  }

  /**
   * Puts every property from source to target.
   *
   * @param target
   *          target object
   * @param source
   *          source object
   */
  public static void putProperties(JSONObject target, JSONObject source) {
    if (target == null || source == null) {
      return;
    }
    for (String prop : getNames(source)) {
      target.put(prop, source.opt(prop));
    }
  }

  /**
   * Puts every property from source to target. If a property exists in both objects and both values are of type
   * {@link JSONObject}, the method is called recursively on those two objects.
   *
   * @param target
   *          target object
   * @param source
   *          source object
   */
  public static void mergeProperties(JSONObject target, JSONObject source) {
    if (target == null || source == null) {
      return;
    }
    for (String prop : getNames(source)) {
      Object newValue = source.opt(prop);
      Object oldValue = target.opt(prop);
      if (newValue instanceof JSONObject && oldValue instanceof JSONObject) {
        // Merge recursively
        mergeProperties((JSONObject) oldValue, (JSONObject) newValue);
      }
      else {
        // Override (values are incompatible, cannot be merged)
        target.put(prop, newValue);
      }
    }
  }

  /**
   * Returns the given JSON object as formatted string with indent 2. <code>null</code> is returned as
   * <code>"null"</code>.
   */
  public static String toString(JSONObject json) {
    if (json == null) {
      return "null";
    }
    try {
      return json.toString(2);
    }
    catch (JSONException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Unexpected error while converting JSON to string", e);
      }
      return json.toString();
    }
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
    if (type == String.class) {
      return (T) jval;
    }
    if (type == byte[].class) {
      return (T) new JsonByteArray((String) jval).getBytes();
    }
    if (type == Date.class) {
      return (T) new JsonDate((String) jval).asJavaDate();
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
      String[] nameArray = getNames(jbean);
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
      return jsonObject.getString(propertyName);
    }
    //date
    if (type == Date.class) {
      return jsonObject.getString(propertyName);
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
      return jsonArray.getString(index);
    }
    //date
    if (type == Date.class) {
      return jsonArray.getString(index);
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

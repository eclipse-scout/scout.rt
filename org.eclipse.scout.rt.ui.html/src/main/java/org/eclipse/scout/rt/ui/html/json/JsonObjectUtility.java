/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import static org.eclipse.scout.rt.platform.util.StreamUtility.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.IntStream;

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
   * Like {@link JSONObject#optLong(String)}, but returns <code>null</code> if the property does not exist or is null
   * (the other method can only return primitives).
   */
  public static Long optLong(JSONObject json, String propertyName) {
    if (json == null || propertyName == null) {
      return null;
    }
    // method optLong(propertyName) returns 0L for values (e.g. strings) which are not convertible to a long value, use optLong(propertyName, fallback) method to return null for not convertible values
    long value = json.optLong(propertyName, -1);
    if (value == -1) {
      // Check if the value is really -1
      long value2 = json.optLong(propertyName, -2);
      if (value2 == -2) {
        return null;
      }
    }
    return value;
  }

  /**
   * Like {@link JSONObject#optDouble(String)}, but returns <code>null</code> if the property does not exist or is null
   * (the other method can only return primitives).
   */
  @SuppressWarnings("squid:S1244")
  public static Double optDouble(JSONObject json, String propertyName) {
    if (json == null || propertyName == null) {
      return null;
    }
    // method optDouble(propertyName) returns NaN for values (e.g. strings) which are not convertible to a double value, use optDouble(propertyName, fallback) method to return null for not convertible values
    double value = json.optDouble(propertyName, -1);
    if (value == -1) {
      // Check if the value is really -1
      double value2 = json.optDouble(propertyName, -2);
      if (value2 == -2) {
        return null;
      }
    }
    return value;
  }

  /**
   * Like {@link JSONObject#optInt(String)}, but returns <code>null</code> if the property does not exist or is null
   * (the other method can only return primitives).
   */
  public static Integer optInt(JSONObject json, String propertyName) {
    if (json == null || propertyName == null) {
      return null;
    }
    // method optInt(propertyName) returns 0 for values (e.g. strings) which are not convertible to an int value, use optInt(propertyName, fallback) method to return null for not convertible values
    int value = json.optInt(propertyName, -1);
    if (value == -1) {
      // Check if the value is really -1
      int value2 = json.optInt(propertyName, -2);
      if (value2 == -2) {
        return null;
      }
    }
    return value;
  }

  /**
   * Iff the given value is not <code>null</code>, it is added to the {@link JSONArray}. Otherwise, nothing happens.
   */
  public static void putIfNotNull(JSONArray jsonArray, Object value) {
    if (jsonArray != null && value != null) {
      jsonArray.put(value);
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

  /**
   * Convert a json object property to java
   * <p>
   * The java class may have public fields or getter/setter methods.
   * <p>
   * Valid data types are: boolean, int, long, String, byte[], array of before mentioned types.
   *
   * @param jsonObject
   *          {@link JSONObject}
   * @param throwForMissingProperty
   *          when set to true then throws an exception if a json property does not exist in the java object, when set
   *          to false ignores this event.
   */
  public static <T> T jsonObjectPropertyToJava(JSONObject jsonObject, String propertyName, Class<T> type, boolean throwForMissingProperty) {
    Object jval = getTyped(jsonObject, propertyName, type);
    return jsonValueToJava(jval, type, throwForMissingProperty);
  }

  /**
   * Recursively unwraps the {@link JSONObject} given into a {@link Map}.<br>
   * This is the inverse operation to {@link JSONObject#wrap(Object)}
   *
   * @param obj
   *          The {@link JSONObject} to unwrap or {@code null}.
   * @return A {@link LinkedHashMap} holding all attributes of the given {@link JSONObject}.
   */
  @SuppressWarnings("findbugs:EC_UNRELATED_TYPES_USING_POINTER_EQUALITY")
  public static Map<String, Object> unwrap(JSONObject obj) {
    if (obj == null || obj == JSONObject.NULL) {
      return null;
    }
    return obj.keySet().stream()
        .map(key -> new SimpleImmutableEntry<>(key, unwrap(obj.opt(key))))
        .collect(toMap(LinkedHashMap::new, Entry::getKey, Entry::getValue, throwingMerger()));
  }

  /**
   * Recursively unwraps the {@link JSONArray} given into an {@link Object} array.<br>
   * This is the inverse operation to {@link JSONObject#wrap(Object)}
   *
   * @param jsonArr
   *          The {@link JSONArray} to unwrap or {@code null}.
   * @return An {@link Object} array holding all items of the given {@link JSONArray}.
   */
  @SuppressWarnings("findbugs:EC_UNRELATED_TYPES_USING_POINTER_EQUALITY")
  public static Object[] unwrap(JSONArray jsonArr) {
    if (jsonArr == null || jsonArr == JSONObject.NULL) {
      return null;
    }
    return IntStream.range(0, jsonArr.length())
        .mapToObj(jsonArr::opt)
        .map(JsonObjectUtility::unwrap)
        .toArray();
  }

  /**
   * Recursively unwraps the {@link Object} given:
   * <ol>
   * <li>Returns {@code null} if the object is {@code null} or {@link JSONObject#NULL}.</li>
   * <li>Returns a {@link Map} if the given object is a {@link JSONObject}.</li>
   * <li>Returns an {@link Object} array if the given object is a {@link JSONArray}.</li>
   * <li>Otherwise returns the input object.</li>
   * </ol>
   * This is the inverse operation to {@link JSONObject#wrap(Object)}
   *
   * @param o
   *          The {@link Object} to unwrap or {@code null}.
   * @return The unwrapped object.
   */
  public static Object unwrap(Object o) {
    if (o == null || o == JSONObject.NULL) {
      return null;
    }
    if (o instanceof JSONObject) {
      return unwrap((JSONObject) o);
    }
    if (o instanceof JSONArray) {
      return unwrap((JSONArray) o);
    }
    return o;
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
      o = type.getConstructor().newInstance();
    }
    catch (Exception e) {
      throw new IllegalArgumentException("type " + type + " object " + jval, e);
    }
    try {
      Set<String> missingNames = new HashSet<>();
      String[] nameArray = getNames(jbean);
      if (nameArray != null) {
        Collections.addAll(missingNames, nameArray);
        for (String key : nameArray) {
          try { // NOSONAR
            Field f = type.getField(key);
            if (Modifier.isStatic(f.getModifiers())) {
              continue;
            }
            Object val = jsonObjectPropertyToJava(jbean, key, f.getType(), throwForMissingProperty);
            f.set(o, val);
            missingNames.remove(key);
          }
          catch (NoSuchElementException | NoSuchFieldException e) { // NOSONAR
            //nop
          }
        }
      }
      if (!missingNames.isEmpty()) {
        FastBeanInfo beanInfo = new FastBeanInfo(type, Object.class);
        for (FastPropertyDescriptor desc : beanInfo.getPropertyDescriptors()) {
          String key = desc.getName();
          if (!missingNames.contains(key)) {
            continue;
          }
          Method m = desc.getWriteMethod();
          if (m == null) {
            continue;
          }
          Object val = jsonObjectPropertyToJava(jbean, key, m.getParameterTypes()[0], throwForMissingProperty);
          m.invoke(o, val);
          missingNames.remove(key);
        }
      }
      if (throwForMissingProperty && !missingNames.isEmpty()) {
        throw new IllegalArgumentException("properties " + missingNames + " do not exist in " + type);
      }
      return o;
    }
    catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
      throw new IllegalArgumentException(jbean + " to " + type, e);
    }
  }

  /**
   * @return null, {@link JSONObject}, {@link JSONArray} or a basic type (int, long, boolean, byte[], String) depending
   *         on the value of <code>type</code>
   */
  @SuppressWarnings("DuplicatedCode")
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
      return jval;
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
  @SuppressWarnings("DuplicatedCode")
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
      return jval;
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

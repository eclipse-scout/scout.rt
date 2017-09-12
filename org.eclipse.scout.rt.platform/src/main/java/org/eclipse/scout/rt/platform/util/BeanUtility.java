/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.FastBeanInfo;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.reflect.IPropertyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BeanUtility {
  private static final Logger LOG = LoggerFactory.getLogger(BeanUtility.class);

  private static final Object BEAN_INFO_CACHE_LOCK;
  private static final Map<CompositeObject/*Class,Class*/, FastBeanInfo> BEAN_INFO_CACHE;
  private static final Map<Class, Class> PRIMITIVE_COMPLEX_CLASS_MAP;
  private static final Map<Class, Class> COMPLEX_PRIMITIVE_CLASS_MAP;

  static {
    BEAN_INFO_CACHE_LOCK = new Object();
    BEAN_INFO_CACHE = new HashMap<>();
    // primitive -> complex classes mappings
    PRIMITIVE_COMPLEX_CLASS_MAP = new HashMap<>();
    PRIMITIVE_COMPLEX_CLASS_MAP.put(boolean.class, Boolean.class);
    PRIMITIVE_COMPLEX_CLASS_MAP.put(byte.class, Byte.class);
    PRIMITIVE_COMPLEX_CLASS_MAP.put(char.class, Character.class);
    PRIMITIVE_COMPLEX_CLASS_MAP.put(short.class, Short.class);
    PRIMITIVE_COMPLEX_CLASS_MAP.put(int.class, Integer.class);
    PRIMITIVE_COMPLEX_CLASS_MAP.put(long.class, Long.class);
    PRIMITIVE_COMPLEX_CLASS_MAP.put(float.class, Float.class);
    PRIMITIVE_COMPLEX_CLASS_MAP.put(double.class, Double.class);
    // complex -> primitive classes mappings
    COMPLEX_PRIMITIVE_CLASS_MAP = new HashMap<>();
    for (Entry<Class, Class> entry : PRIMITIVE_COMPLEX_CLASS_MAP.entrySet()) {
      COMPLEX_PRIMITIVE_CLASS_MAP.put(entry.getValue(), entry.getKey());
    }
  }

  private BeanUtility() {
  }

  /**
   * @return all properties of from up to (and excluding) to stopClazz, filtering with filter
   */
  public static Map<String, Object> getProperties(Object from, Class<?> stopClazz, IPropertyFilter filter) {
    Map<String, Object> map = new HashMap<>();
    try {
      FastPropertyDescriptor[] props = getFastPropertyDescriptors(from.getClass(), stopClazz, filter);
      for (FastPropertyDescriptor fromProp : props) {
        Method readMethod = fromProp.getReadMethod();
        if (readMethod != null) {
          Object value = readMethod.invoke(from, (Object[]) null);
          map.put(fromProp.getName(), value);
        }
      }
    }
    catch (Exception e) {
      throw new ProcessingException("object: " + from, e);
    }
    return map;
  }

  /**
   * @param lenient
   *          true just logs warnings on exceptions, false throws exceptions set all properties on to, filtering with
   *          filter
   */
  public static void setProperties(Object to, Map<String, Object> map, boolean lenient, IPropertyFilter filter) {
    FastBeanInfo toInfo = getFastBeanInfo(to.getClass(), null);
    for (Entry<String, Object> entry : map.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      try {
        FastPropertyDescriptor desc = toInfo.getPropertyDescriptor(name);
        if (desc != null && (filter == null || filter.accept(desc))) {
          Method writeMethod = desc.getWriteMethod();
          if (writeMethod != null) {
            writeMethod.invoke(to, new Object[]{TypeCastUtility.castValue(value, writeMethod.getParameterTypes()[0])});
          }
        }
      }
      catch (Exception e) {
        if (lenient) {
          LOG.warn("Could not set property property '{}' to value '{}'", name, value, e);
        }
        else {
          throw new ProcessingException("property " + name + " with value " + value, e);
        }
      }
    }
  }

  /**
   * Get all property descriptors from this class up to (and excluding) stopClazz
   * <p>
   * Getting bean properties using {@link Introspector} can be very slow and time consuming.
   * <p>
   * This hi-speed property introspector only inspects bean names, types and read/write methods.
   * <p>
   * The results are cached for further speed optimization.
   */
  public static FastBeanInfo getFastBeanInfo(Class<?> beanClass, Class<?> stopClass) {
    if (beanClass == null) {
      return new FastBeanInfo(beanClass, stopClass);
    }
    synchronized (BEAN_INFO_CACHE_LOCK) {
      CompositeObject key = new CompositeObject(beanClass, stopClass);
      FastBeanInfo info = BEAN_INFO_CACHE.computeIfAbsent(key, k -> new FastBeanInfo(beanClass, stopClass));
      return info;
    }
  }

  /**
   * Clear the cache used by {@link #getBeanInfoEx(Class, Class)}
   */
  public static void clearFastBeanInfoCache() {
    synchronized (BEAN_INFO_CACHE_LOCK) {
      BEAN_INFO_CACHE.clear();
    }
  }

  /**
   * Get all properties from this class up to (and excluding) stopClazz
   *
   * @param filter
   */
  public static FastPropertyDescriptor[] getFastPropertyDescriptors(Class<?> clazz, Class<?> stopClazz, IPropertyFilter filter) {
    FastBeanInfo info = getFastBeanInfo(clazz, stopClazz);
    FastPropertyDescriptor[] a = info.getPropertyDescriptors();
    ArrayList<FastPropertyDescriptor> filteredProperties = new ArrayList<>(a.length);
    for (FastPropertyDescriptor pd : a) {
      if (filter != null && !(filter.accept(pd))) {
        // ignore it
      }
      else {
        filteredProperties.add(pd);
      }
    }
    return filteredProperties.toArray(new FastPropertyDescriptor[filteredProperties.size()]);
  }

  /**
   * Creates a new instance of the given class and init parameters. The constructor is derived from the parameter types.
   *
   * @param <T>
   * @param c
   *          The class a new instance is created for.
   * @param parameterTypes
   *          The parameter types used for determining the constructor used for creating the new instance.
   * @param parameters
   *          The parameter objects the new instance is initialized with.
   * @return Returns a new instance of the given class or <code>null</code>, if no matching constructor can be found.
   * @since 3.8.1
   */
  public static <T> T createInstance(Class<T> c, Object... parameters) {
    if (parameters == null || parameters.length == 0) {
      return createInstance(c, null, null);
    }
    Class<?>[] parameterTypes = new Class<?>[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i] != null) {
        parameterTypes[i] = parameters[i].getClass();
      }
    }
    return createInstance(c, parameterTypes, parameters);
  }

  /**
   * Creates a new instance of the given class using the constructor that matches the given parameter types. The
   * resulting object is initialized with the given parameters.
   *
   * @param <T>
   * @param c
   *          The class a new instance is created for.
   * @param parameterTypes
   *          The parameter types used for determining the constructor used for creating the new instance.
   * @param parameters
   *          The parameter objects the new instance is initialized with.
   * @return Returns a new instance of the given class or <code>null</code>, if no matching constructor can be found.
   * @since 3.8.1
   */
  public static <T> T createInstance(Class<T> c, Class<?>[] parameterTypes, Object[] parameters) {
    Constructor<T> ctor = findConstructor(c, parameterTypes);
    if (ctor != null) {
      try {
        return ctor.newInstance(parameters);
      }
      catch (Exception t) {
        LOG.info("Exception while instantiating new object [class={}, parameterTypes={}, parameters={}]", c, parameterTypes, parameters, t);
        throw new ProcessingException("Exception while instantiating new object", t);
      }
    }
    return null;
  }

  /**
   * Finds the best matching constructor in the given class having the given parameter types or super classes of them.
   *
   * @param c
   *          The class the constructor is searched for.
   * @param parameterTypes
   *          A possibly empty vararg list of required constructor parameter types.
   * @return Returns the exact constructor of the given class and the given list of parameter types, the best matching
   *         one or <code>null</code>, if none can be found.
   * @throws ProcessingException
   *           A {@link ProcessingException} is thrown if there are multiple constructors satisfying the given
   *           constructor specification.
   * @since 3.8.1
   */
  public static <T> Constructor<T> findConstructor(Class<T> c, Class<?>... parameterTypes) {
    if (c == null) {
      return null;
    }
    // find exact constructor
    try {
      Constructor<T> ctor = c.getConstructor(parameterTypes);
      if (ctor != null) {
        return ctor;
      }
    }
    catch (NoSuchMethodException e) { // NOSONAR
      LOG.debug("exact constructor does not exist");
    }
    // default constructor is not available
    if (parameterTypes == null || parameterTypes.length == 0) {
      return null;
    }
    // find best matching constructor
    NavigableMap<Integer, Set<Constructor<T>>> candidates = new TreeMap<>();
    for (Constructor<?> ctor : c.getConstructors()) {
      int distance = 0;
      Class<?>[] ctorParameters = ctor.getParameterTypes();
      if (ctorParameters.length == parameterTypes.length) {
        // check parameters
        for (int i = 0; i < parameterTypes.length; i++) {
          int currentParamDistance = computeTypeDistance(ctorParameters[i], parameterTypes[i]);
          if (currentParamDistance == -1 && parameterTypes[i] != null) {
            if (parameterTypes[i].isPrimitive()) {
              // try auto-boxing
              currentParamDistance = computeTypeDistance(ctorParameters[i], PRIMITIVE_COMPLEX_CLASS_MAP.get(parameterTypes[i]));
            }
            else if (COMPLEX_PRIMITIVE_CLASS_MAP.containsKey(parameterTypes[i])) {
              // try auto-unboxing
              currentParamDistance = computeTypeDistance(ctorParameters[i], COMPLEX_PRIMITIVE_CLASS_MAP.get(parameterTypes[i]));
            }
          }
          if (currentParamDistance == -1) {
            distance = -1;
            break;
          }
          distance += currentParamDistance;
        }
        if (distance >= 0) {
          // collect candidates
          @SuppressWarnings("unchecked")
          Constructor<T> candidate = (Constructor<T>) ctor;
          Set<Constructor<T>> rankedCtors = candidates.computeIfAbsent(distance, k -> new HashSet<>());
          rankedCtors.add(candidate);
        }
      }
    }
    //
    if (candidates.isEmpty()) {
      return null;
    }
    // check ambiguity
    Set<Constructor<T>> ctors = candidates.firstEntry().getValue();
    if (ctors.isEmpty()) {
      return null;
    }
    if (ctors.size() == 1) {
      return CollectionUtility.firstElement(ctors);
    }
    throw new ProcessingException("More than one constructors found due to ambiguous parameter types [class=" + c + ", parameterTypes=" + Arrays.toString(parameterTypes) + "]");
  }

  /**
   * Computes the distance between the given two types.
   * <p/>
   * <table border="0" cellpadding="1" cellspacing="2">
   * <tr align="left">
   * <th align="left">Value</th>
   * <th align="left">Description</th>
   * </tr>
   * <tr>
   * <td align="center" valign="top">-1</td>
   * <td>the type distance cannot be computed. Possible problems are that the <code>declaredType</code> is not
   * assignable from the <code>actualType</code>.</td>
   * </tr>
   * <tr>
   * <td align="center" valign="top">0</td>
   * <td>perfect match (i.e. <code>declaredType == actualType</code>)</td>
   * </tr>
   * <tr>
   * <td align="center" valign="top">&gt;1 (<em>n</em>)</td>
   * <td>the <code>declaredType</code> is a superclass of <code>actualType</code>. The distance between the two types is
   * <em>n</em></td>
   * </tr>
   * </table>
   *
   * @param declaredType
   *          The method parameter's declared type.
   * @param actualType
   *          The type of the object used in the actual method invocation.
   * @return Returns -1 if the distance cannot be computed or the declared type is not assignable from the actual type.
   *         It returns 0 for a perfect match (i.e. <code>declaredType == actualType</code> and a number &gt;0
   *         otherwise.
   * @since 3.8.1
   */
  public static int computeTypeDistance(Class<?> declaredType, Class<?> actualType) {
    if (declaredType == null) {
      return -1;
    }
    if (actualType == null) {
      // a null type is treated like a null-object method invocation. Hence the actualType null matches all parameter
      // types except primitive (since auto-unboxing would throw a NPE)
      return declaredType.isPrimitive() ? -1 : 0;
    }
    if (declaredType == actualType) {
      // perfect match
      return 0;
    }
    if (!declaredType.isAssignableFrom(actualType)) {
      // declaredType is not a superclass of actualType
      return -1;
    }
    // compute type distance
    // 1. collect super classes
    Class<?> superClass = actualType.getSuperclass();
    Class<?>[] interfaces = actualType.getInterfaces();
    Class<?>[] superClasses;
    if (superClass == null) {
      superClasses = interfaces;
    }
    else {
      superClasses = new Class<?>[interfaces.length + 1];
      superClasses[0] = superClass;
      System.arraycopy(interfaces, 0, superClasses, 1, interfaces.length);
    }
    // 2. compute minimal superclass distance by recursion
    int minSuperClassesDistance = -1;
    for (Class<?> c : superClasses) {
      int distance = computeTypeDistance(declaredType, c);
      if (distance == 0) {
        // super class is perfect parameter match
        minSuperClassesDistance = 0;
        break;
      }
      else if (distance > 0) {
        if (minSuperClassesDistance == -1) {
          minSuperClassesDistance = distance;
        }
        else {
          minSuperClassesDistance = Math.min(minSuperClassesDistance, distance);
        }
      }
    }
    // 3. evaluate result
    if (minSuperClassesDistance == -1) {
      return -1;
    }
    return minSuperClassesDistance + 1;
  }

  @SuppressWarnings("unchecked")
  public static <T> List<Class<? extends T>> getInterfacesHierarchy(Class<?> type, Class<T> filterClass) {
    Set<Class<?>> resultSet = new HashSet<>();
    List<Class<?>> workList = new ArrayList<>();
    List<Class<?>> lookAheadList = new ArrayList<>();
    if (type.isInterface()) {
      lookAheadList.add(type);
    }
    else {
      Class<?> test = type;
      while (test != null) {
        lookAheadList.addAll(Arrays.asList(test.getInterfaces()));
        test = test.getSuperclass();
      }
    }
    while (!lookAheadList.isEmpty()) {
      workList = lookAheadList;
      lookAheadList = new ArrayList<>();
      for (Class<?> c : workList) {
        if (!resultSet.contains(c)) {
          resultSet.add(c);
          // look ahead
          Class<?>[] ifs = c.getInterfaces();
          if (ifs.length > 0) {
            lookAheadList.addAll(Arrays.asList(ifs));
          }
        }
      }
    }
    Map<CompositeObject, Class<? extends T>> resultMap = new TreeMap<>();
    int index = 0;
    for (Class<?> c : resultSet) {
      if (filterClass.isAssignableFrom(c)) {
        int depth = 0;
        Class<T> test = (Class<T>) c;
        while (test != null) {
          depth++;
          Class[] xa = test.getInterfaces();
          test = null;
          if (xa != null) {
            for (Class x : xa) {
              if (filterClass.isAssignableFrom(x)) {
                test = x;
              }
            }
          }
        }
        resultMap.put(new CompositeObject(depth, index++), (Class<T>) c);
      }
    }
    return CollectionUtility.arrayList(resultMap.values());
  }
}

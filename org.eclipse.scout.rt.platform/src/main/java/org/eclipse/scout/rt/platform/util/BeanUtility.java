/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.FastBeanInfo;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.reflect.IPropertyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BeanUtility {
  private static final Logger LOG = LoggerFactory.getLogger(BeanUtility.class);

  private static final Map<CompositeObject/*Class,Class*/, FastBeanInfo> BEAN_INFO_CACHE;
  private static final Map<Class, Class> PRIMITIVE_COMPLEX_CLASS_MAP;
  private static final Map<Class, Class> COMPLEX_PRIMITIVE_CLASS_MAP;

  static {
    BEAN_INFO_CACHE = new ConcurrentHashMap<>(10_000);
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
   * Sets all given properties to the target object.
   *
   * @param to
   *          The target object
   * @param propertyMap
   *          A map of property name to the value that should be set
   * @param lenient
   *          true just logs warnings on exceptions, false throws exceptions set all properties on to, filtering with
   *          filter
   * @param filter
   *          Filter that should be applied to properties
   * @return True if all properties were set successfully
   */
  public static boolean setProperties(Object to, Map<String, Object> propertyMap, boolean lenient, IPropertyFilter filter) {
    boolean success = true;
    FastBeanInfo toInfo = getFastBeanInfo(to.getClass(), null);
    for (Entry<String, Object> entry : propertyMap.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      try {
        FastPropertyDescriptor desc = toInfo.getPropertyDescriptor(name);
        if (desc == null && ObjectUtility.hasValue(value)) {
          // property with a value could not be found on target object
          // do not log a warning because this is an expected use case
          success = false;
        }
        if (desc != null && (filter == null || filter.accept(desc))) {
          Method writeMethod = desc.getWriteMethod();
          if (writeMethod != null) {
            writeMethod.invoke(to, new Object[]{TypeCastUtility.castValue(value, writeMethod.getParameterTypes()[0])});
          }
        }
      }
      catch (Exception e) {
        if (lenient) {
          LOG.warn("Could not set property '{}' to value '{}'", name, value, e);
          success = false;
        }
        else {
          throw new ProcessingException("property " + name + " with value " + value, e);
        }
      }
    }
    return success;
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
    CompositeObject key = new CompositeObject(beanClass, stopClass);
    FastBeanInfo info = BEAN_INFO_CACHE.computeIfAbsent(key, k -> new FastBeanInfo(beanClass, stopClass));
    return info;
  }

  /**
   * Clear the cache used by {@link #getFastBeanInfo(Class, Class)}
   */
  public static void clearFastBeanInfoCache() {
    BEAN_INFO_CACHE.clear();
  }

  /**
   * Get all properties from this class up to (and excluding) stopClazz
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
    return filteredProperties.toArray(new FastPropertyDescriptor[0]);
  }

  /**
   * Creates a new instance of the given class and init parameters. The constructor is derived from the parameter types.
   *
   * @param c
   *          The class a new instance is created for.
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

    final Constructor<?>[] publicConstructors = c.getConstructors();
    switch (publicConstructors.length) {
      case 0:
        return null;
      case 1:
        return checkParameterTypesAndCast(publicConstructors[0], parameterTypes);
    }

    final List<Constructor<T>> candidates = Stream.of(publicConstructors)
        .map(ctor -> BeanUtility.<T> checkParameterTypesAndCast(ctor, parameterTypes))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (candidates.size() <= 1) {
      return CollectionUtility.firstElement(candidates);
    }

    // find best matching constructor using type distance
    NavigableMap<Integer, Set<Constructor<T>>> weightedCandidates = new TreeMap<>();
    for (Constructor<T> ctor : candidates) {
      int distance = 0;
      // compute parameter type distances
      // Note: constructor parameter count matches requested parameter count
      //       and constructor parameter types are assignable from requested parameter types
      //       both filters are already applied by checkParameterTypesAndCast
      Class<?>[] ctorParameters = ctor.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        int currentParamDistance = computeTypeDistance(ctorParameters[i], parameterTypes[i]);
        distance += currentParamDistance;
      }
      weightedCandidates.computeIfAbsent(distance, k -> new HashSet<>()).add(ctor);
    }

    // check ambiguity
    // Note: there is always at least one entry in weightedCandidates
    Set<Constructor<T>> bestMatchingConstructors = weightedCandidates.firstEntry().getValue();
    if (bestMatchingConstructors.size() <= 1) {
      return CollectionUtility.firstElement(bestMatchingConstructors);
    }

    throw new ProcessingException("More than one constructors found due to ambiguous parameter types [class=" + c + ", parameterTypes=" + Arrays.toString(parameterTypes) + "]");
  }

  /**
   * Checks whether the given constructor can be invoked using parameters of the given types. If applicable, the
   * wild-card typed constructor is cast to a bound type parameter and returned. Otherwise, {@code null} is returned.
   */
  private static <T> Constructor<T> checkParameterTypesAndCast(Constructor<?> constructor, Class<?>... invocationParameterTypes) {
    // 1. check number of parameters
    if (constructor.getParameterCount() != (invocationParameterTypes == null ? 0 : invocationParameterTypes.length)) {
      return null;
    }

    // 2. check if actual parameter types are assignable to declared ones
    //    Note: auto-boxing and unboxing is applied if one of a <declared, actual> parameter type pair
    //          is primitiv and the other is not.
    final Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
    for (int i = 0; i < constructorParameterTypes.length; i++) {
      final Class<?> constructorParameterType = constructorParameterTypes[i];
      Class<?> invocationParameterType = invocationParameterTypes[i];

      if (constructorParameterType.isPrimitive()) {
        if (invocationParameterType == null) {
          // unboxing would raise a NPE
          return null;
        }
        if (!invocationParameterType.isPrimitive()) {
          // unbox complex type
          invocationParameterType = COMPLEX_PRIMITIVE_CLASS_MAP.get(invocationParameterType);
        }
      }
      else if (invocationParameterType == null) {
        // null is always assignable to an object type
        continue;
      }
      else if (invocationParameterType.isPrimitive()) {
        // box primitive type
        invocationParameterType = PRIMITIVE_COMPLEX_CLASS_MAP.get(invocationParameterType);
      }

      if (!constructorParameterType.isAssignableFrom(invocationParameterType)) {
        return null;
      }
    }

    @SuppressWarnings("unchecked")
    Constructor<T> casted = (Constructor<T>) constructor;
    return casted;
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
    if (PRIMITIVE_COMPLEX_CLASS_MAP.containsKey(declaredType)) {
      return PRIMITIVE_COMPLEX_CLASS_MAP.get(declaredType) == actualType ? 1 : -1;
    }
    if (PRIMITIVE_COMPLEX_CLASS_MAP.containsKey(actualType)) {
      return PRIMITIVE_COMPLEX_CLASS_MAP.get(actualType) == declaredType ? 1 : -1;
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
      if (filterClass == null || filterClass.isAssignableFrom(c)) {
        int depth = 0;
        Class<T> test = (Class<T>) c;
        while (test != null) {
          depth++;
          Class[] xa = test.getInterfaces();
          test = null;
          if (xa != null) {
            for (Class x : xa) {
              if (filterClass == null || filterClass.isAssignableFrom(x)) {
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

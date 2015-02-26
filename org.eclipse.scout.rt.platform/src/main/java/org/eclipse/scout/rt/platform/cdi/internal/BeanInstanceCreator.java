/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.cdi.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.Instance;
import org.eclipse.scout.rt.platform.cdi.OBJ;

/**
 *
 */
public class BeanInstanceCreator<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BeanInstanceCreator.class);

  private final IBean<T> m_bean;

  public BeanInstanceCreator(IBean<T> bean) {
    m_bean = Assertions.assertNotNull(bean);
  }

  public IBean<T> getBean() {
    return m_bean;
  }

  public T create() {
    return createAndInitialize(getBean().getBeanClazz());
  }

  public static <T> T createAndInitialize(Class<T> beanClazz) {
    T instance = null;
    try {
      instance = Assertions.assertNotNull(createInstance(beanClazz));
      instance = initializeInstance(instance);
    }
    catch (Exception e) {
      LOG.error(String.format("Could not instantiate '%s'.", beanClazz), e);
    }
    return instance;
  }

  public static <T> T createInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Constructor<? extends T> constructor = findConstructor(clazz);
    if (constructor != null) {
      Object[] parameters = lookupParameters(constructor.getParameterTypes());
      constructor.setAccessible(true);
      return constructor.newInstance(parameters);
    }
    else {
      LOG.error(String.format("No constructor found of '%s'. Ensure to have an empty constructor or an @Inject annotated constructor.", clazz.getName()));
      return null;
    }
  }

  public static <T> T initializeInstance(T instance) {
    // inject members
    injectMembers(instance);
    // post instantiate
    callPostConstruct(instance);
    return instance;
  }

  @SuppressWarnings("unchecked")
  private static <T> Constructor<? extends T> findConstructor(Class<? extends T> beanClazz) {
    TreeMap<CompositeObject, Constructor<?>> sortedConstructors = new TreeMap<>();
    for (Constructor<?> c : beanClazz.getDeclaredConstructors()) {
      // only constructors with @Inject annotation or empty parameter list
      if (c.getAnnotation(Inject.class) != null) {
        CompositeObject key = new CompositeObject(1, c.getParameterTypes().length, c);
        sortedConstructors.put(key, c);
      }
      else if (c.getParameterTypes().length == 0) {
        CompositeObject key = new CompositeObject(2, c.getParameterTypes().length, c);
        sortedConstructors.put(key, c);
      }
    }
    if (sortedConstructors.isEmpty()) {
      return null;
    }
    else {
      return (Constructor<? extends T>) sortedConstructors.firstEntry().getValue();
    }
  }

  protected static void injectMembers(Object instance) {
    // fields
    Set<Field> fieldCollector = new HashSet<>();
    collectFieldsInHierarchy(instance, instance.getClass(), Inject.class, fieldCollector);
    for (Field field : fieldCollector) {
      try {
        assignFieldValue(instance, field);
      }
      catch (Exception e) {
        LOG.error(String.format("Could not call inject field '%s' on '%s'.", field.getName(), instance.getClass()), e);
      }
    }
    // methods
    Map<String /*method signature*/, Method> methodCollector = new TreeMap<>();
    collectMethodsInHierarchy(instance, instance.getClass(), Inject.class, methodCollector);
    for (Method method : methodCollector.values()) {
      try {
        invokeMethod(instance, method);
      }
      catch (Exception e) {
        LOG.error(String.format("Could not call inject method '%s' on '%s'.", method.getName(), instance.getClass()), e);
      }
    }

  }

  /**
   * @param instance
   */
  protected static void callPostConstruct(Object instance) {
    Map<String /*method signature*/, Method> methodCollector = new TreeMap<>();
    collectMethodsInHierarchy(instance, instance.getClass(), PostConstruct.class, methodCollector);
    for (Method method : methodCollector.values()) {
      try {
        if (method.getParameterTypes().length == 0) {
          invokeMethod(instance, method);
        }
        else {
          throw new IllegalArgumentException(String.format("Methods with @PostConstruct must have no arguments. See '%s' on '%s',", method.getName(), method.getDeclaringClass().getName()));
        }
      }
      catch (Exception e) {
        LOG.error(String.format("Could not call initialze method '%s' on '%s'.", method.getName(), instance.getClass()));
      }
    }

  }

  private static void collectMethodsInHierarchy(Object instance, Class<?> clazz, Class<? extends Annotation> annotationClazz, Map<String /*method signature*/, Method> collector) {
    if (clazz == null || Object.class.getName().equals(clazz.getName())) {
      return;
    }
    for (Method m : clazz.getDeclaredMethods()) {
      if (m.getAnnotation(annotationClazz) != null) {
        collector.put(computeMethodSignature(m), m);
      }
    }
    collectMethodsInHierarchy(instance, clazz.getSuperclass(), annotationClazz, collector);
  }

  private static void collectFieldsInHierarchy(Object instance, Class<?> clazz, Class<? extends Annotation> annotationClazz, Set<Field> collector) {
    if (clazz == null || Object.class.getName().equals(clazz.getName())) {
      return;
    }
    for (Field field : clazz.getDeclaredFields()) {
      if (field.getAnnotation(annotationClazz) != null) {
        if (Modifier.isStatic(field.getModifiers())) {
          throw new IllegalArgumentException(String.format("Field with @Inject must not be static. See '%s' on '%s',", field.getName(), field.getDeclaringClass().getName()));
        }
        else {
          collector.add(field);
        }
      }
    }
    collectFieldsInHierarchy(instance, clazz.getSuperclass(), annotationClazz, collector);
  }

  public static Object[] lookupParameters(Type[] parameterTypes) {
    Assertions.assertNotNull(parameterTypes);
    Object[] arguments = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      arguments[i] = Assertions.assertNotNull(lookupBean(parameterTypes[i]));
    }
    return arguments;
  }

  @SuppressWarnings("unchecked")
  public static <T> T lookupBean(Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type[] parameterArgTypes = parameterizedType.getActualTypeArguments();
      if (parameterArgTypes.length > 1) {
        throw new IllegalArgumentException(String.format("Generic bindings are only allowed for Instance<Abc> injection. Type '%s' can not be found on CDI context.", type));
      }
      else if (parameterArgTypes.length == 1) {
        // instance handling
        if (parameterizedType.getRawType().equals(Instance.class)) {
          return (T) new InstanceImpl<T>(OBJ.all((Class<T>) parameterArgTypes[0]));
        }
        else {
          throw new IllegalArgumentException(String.format("Generic bindings are only allowed for Instance<Abc> injection. Type '%s' can not be found on CDI context.", type));
        }
      }
    }
    T instance = Assertions.assertNotNull(OBJ.one((Class<T>) type));
    return instance;
  }

  public static Object invokeMethod(Object instance, Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Type[] genericParameterTypes = method.getGenericParameterTypes();
    Object[] arguments = lookupParameters(genericParameterTypes);
    method.setAccessible(true);
    return method.invoke(instance, arguments);
  }

  public static void assignFieldValue(Object instance, Field field) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Object value = Assertions.assertNotNull(lookupBean(field.getGenericType()));
    field.setAccessible(true);
    field.set(instance, value);
  }

  private static String computeMethodSignature(Method m) {
    Assertions.assertNotNull(m);
    StringBuilder builder = new StringBuilder();
    if (Modifier.isPrivate(m.getModifiers())) {
      builder.append(m.getDeclaringClass().getName()).append(":");
    }
    builder.append(m.getName());
    Class<?>[] parameterTypes = m.getParameterTypes();
    if (parameterTypes.length > 0) {
      builder.append("[");
      for (int i = 0; i < parameterTypes.length; i++) {
        if (i > 0) {
          builder.append(", ");
        }
        builder.append(parameterTypes[i].getName());
      }
      builder.append("]");
    }
    return builder.toString();

  }
}

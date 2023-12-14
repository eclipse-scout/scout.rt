/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.InjectBean;
import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BeanInstanceUtil {

  private static final Logger LOG = LoggerFactory.getLogger(BeanInstanceUtil.class);

  /** Stack to keep track of beans being created to avoid circular dependencies */
  private static final ThreadLocal<Deque<Class<?>>> INSTANTIATION_STACK = ThreadLocal.withInitial(ArrayDeque::new);

  private BeanInstanceUtil() {
  }

  /**
   * Creates a new bean instance.
   *
   * @param beanClazz
   *          not null
   * @return the newly created instance
   */
  public static <T> T createBean(Class<T> beanClazz) {
    return createBean(getBeanConstructor(beanClazz));
  }

  static <T> T createBean(Constructor<T> ctor) {
    try {
      return ctor.getParameterCount() == 0 ? ctor.newInstance() : ctor.newInstance(getInjectionArguments(ctor.getParameterTypes()));
    }
    catch (Exception e) {
      throw translateException("Could not create bean [{}]", ctor.getDeclaringClass(), e);
    }
  }

  /**
   * Invokes all {@link InjectBean} annotated fields, all {@link InjectBean} and {@link PostConstruct} annotated methods
   * of the given instance.
   */
  public static void initializeBeanInstance(Object instance) {
    Class<?> clazz = instance.getClass();
    initializeBeanInstance(instance, collectInjectedFields(clazz), collectInjectedMethods(clazz), collectPostConstructMethods(clazz));
  }

  static void initializeBeanInstance(Object instance, Collection<Field> injectedFields, Collection<Method> injectedMethods, Collection<Method> postConstructMethods) {
    for (Field field : injectedFields) {
      LOG.debug("injecting field {}", field);
      try {
        Object value = BEANS.get(field.getType());
        field.set(instance, value);
      }
      catch (Exception e) {
        throw translateException("Exception while injecting field {}", field, e);
      }
    }
    for (Method method : injectedMethods) {
      LOG.debug("invoking injected method {}", method);
      try {
        method.invoke(instance, getInjectionArguments(method.getParameterTypes()));
      }
      catch (Exception e) {
        throw translateException("Exception while invoking @InjectBean method {}", method, e);
      }
    }
    for (Method method : postConstructMethods) {
      LOG.debug("invoking post-construct method {}", method);
      try {
        method.invoke(instance);
      }
      catch (Exception e) {
        throw translateException("Exception while invoking @PostConstruct method {}", method, e);
      }
    }
  }

  /**
   * Returns a new supplier which can be used multiple times to create a new initialized instance for given bean class.
   *
   * @param beanClazz
   *          type of beans to create
   * @return new supplier to create bean instances
   */
  public static <T> Supplier<T> beanInstanceCreator(Class<T> beanClazz) {
    Constructor<T> ctor = getBeanConstructor(beanClazz);
    Collection<Field> injectedFields = collectInjectedFields(beanClazz);
    Collection<Method> injectedMethods = collectInjectedMethods(beanClazz);
    Collection<Method> postConstructMethods = collectPostConstructMethods(beanClazz);
    return () -> {
      T instance = createBean(ctor);
      initializeBeanInstance(instance, injectedFields, injectedMethods, postConstructMethods);
      return instance;
    };
  }

  /**
   * Creates a new instance with given instance creator and asserts that no other instantiation of this bean is in
   * current thread already in progress (possibly due to circular dependencies).
   *
   * @param instanceCreator
   *          instance creator which will be called
   * @param beanClazz
   *          beanClazz to be checked
   * @throws BeanCreationException
   *           if the bean is already bean instantiation is already in progress
   */
  public static <T> T createAndAssertNoCircularDependency(Supplier<T> instanceCreator, Class<?> beanClazz) {
    Deque<Class<?>> stack = INSTANTIATION_STACK.get();
    if (stack.contains(beanClazz)) {
      String path = stack.stream().map(Class::getName).collect(Collectors.joining("/"));
      throw new BeanCreationException("The requested bean is currently being created. Creation path: [{}]", path);
    }
    stack.addLast(beanClazz);
    try {
      return instanceCreator.get();
    }
    finally {
      stack.removeLast();
    }
  }

  /**
   * Transforms the given exception: {@link UndeclaredThrowableException} and {@link InvocationTargetException} are
   * unpacked and wrapped into a {@link BeanCreationException}. {@link Error} are just rethrown.
   * <p>
   * <b>Note:</b> This utility must not use any features of the bean manager. Hence the usage of an
   * {@link IExceptionTranslator} is not suitable.
   */
  static RuntimeException translateException(String message, Object arg, Exception e) {
    Throwable t = e;
    while ((t instanceof UndeclaredThrowableException || t instanceof InvocationTargetException) && t.getCause() != null) {
      t = t.getCause();
    }
    if (t instanceof Error) {
      throw (Error) t;
    }
    return new BeanCreationException(message, arg, t);
  }

  /**
   * @return <em>accessible</em> bean constructor, never {@code null}
   */
  static <T> Constructor<T> getBeanConstructor(Class<T> clazz) {
    Constructor<T> injectionConstructor = findInjectionConstructor(clazz);
    if (injectionConstructor == null) {
      injectionConstructor = getDefaultConstructor(clazz);
    }
    injectionConstructor.setAccessible(true);
    return injectionConstructor;
  }

  /**
   * @return Constructor with {@link InjectBean} annotation
   * @throws BeanCreationException
   *           in case of multiple {@link InjectBean} annotated constructors
   */
  @SuppressWarnings("unchecked")
  private static <T> Constructor<T> findInjectionConstructor(Class<T> clazz) {
    Constructor<T> result = null;
    for (Constructor<?> cons : clazz.getDeclaredConstructors()) {
      if (cons.getAnnotation(InjectBean.class) != null) {
        if (result != null) {
          throw new BeanCreationException("Found multiple @InjectBean constructors in {}", clazz);
        }
        result = (Constructor<T>) cons;
      }
    }
    return result;
  }

  /**
   * @return default Constructor as defined in {@link Class#getDeclaredConstructor()}
   */
  private static <T> Constructor<T> getDefaultConstructor(Class<T> clazz) {
    try {
      return clazz.getDeclaredConstructor();
    }
    catch (Exception e) {
      throw translateException("No default bean constructor defined [{}]", clazz, e);
    }
  }

  static Object[] getInjectionArguments(Class<?>[] argTypes) {
    Object[] args = new Object[argTypes.length];
    for (int i = 0; i < argTypes.length; i++) {
      args[i] = BEANS.get(argTypes[i]);
    }
    return args;
  }

  static Collection<Method> collectInjectedMethods(Class<?> clazz) {
    return collectNonStaticMethodsWithAnnotation(clazz, InjectBean.class, true, true);
  }

  /**
   * Collects all methods in the given class or one of its super classes that are annotated with {@link PostConstruct}.
   * A post-construct method is added at most once to the resulting collection. If a method is overriding a super class
   * method, the first one on the super class hierarchy path is added which is annotated with {@link PostConstruct}.
   * Hence the declaring classes of the resulting method collection do not describe neither the first nor the last
   * defining class.
   * <p>
   * The method is package-private for testing purposes.
   *
   * @param clazz
   *          The class {@link PostConstruct}-annotated methods are searched in.
   * @return Returns a collection of <em>accessible</em> {@link PostConstruct} methods.
   * @throws BeanCreationException
   *           If unsupported methods are annotated with {@link PostConstruct} (i.e. those with parameters)
   */
  static Collection<Method> collectPostConstructMethods(Class<?> clazz) {
    return collectNonStaticMethodsWithAnnotation(clazz, PostConstruct.class, false, true);
  }

  static Collection<Method> collectPreDestroyMethods(Class<?> clazz) {
    return collectNonStaticMethodsWithAnnotation(clazz, PreDestroy.class, false, false);
  }

  private static Collection<Method> collectNonStaticMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation, boolean allowArgs, boolean throwOnError) {
    final Map<String /*method name*/, Method> collector = new LinkedHashMap<>();
    Class<?> currentClass = clazz;
    while (currentClass != null && currentClass != Object.class) {
      for (Method m : currentClass.getDeclaredMethods()) {
        if (!m.isAnnotationPresent(annotation)) {
          continue;
        }

        final int methodModifiers = m.getModifiers();
        // check static
        if (Modifier.isStatic(methodModifiers)) {
          handleError(throwOnError, "Methods annotated with @{} must not be static [method={}]", annotation.getSimpleName(), m);
          continue;
        }
        // check number of parameters
        if (!allowArgs && m.getParameterTypes().length != 0) {
          handleError(throwOnError, "Methods annotated with @{} must have no arguments [method={}]", annotation.getSimpleName(), m);
          continue;
        }

        // compute method name (special handling for private methods)
        String name = m.getName();
        if (Modifier.isPrivate(methodModifiers)) {
          name = m.getDeclaringClass().getName() + ":" + name;
        }

        if (!collector.containsKey(name)) {
          collector.put(name, m);
        }
      }
      currentClass = currentClass.getSuperclass();
    }
    return collectAsAccessible(collector);
  }

  static Collection<Field> collectInjectedFields(Class<?> clazz) {
    return collectNonStaticFieldsWithAnnotation(clazz, InjectBean.class, false);
  }

  private static Collection<Field> collectNonStaticFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation, boolean throwOnError) {
    final Map<String /*field name*/, Field> collector = new LinkedHashMap<>();
    Class<?> currentClass = clazz;
    while (currentClass != null && currentClass != Object.class) {
      for (Field f : currentClass.getDeclaredFields()) {
        if (!f.isAnnotationPresent(annotation)) {
          continue;
        }

        final int fieldModifiers = f.getModifiers();
        // check static
        if (Modifier.isStatic(fieldModifiers)) {
          handleError(throwOnError, "Fields annotated with @{} must not be static [field={}]", annotation.getSimpleName(), f);
          continue;
        }

        // compute field name (special handling for private fields)
        String name = f.getName();
        if (Modifier.isPrivate(fieldModifiers)) {
          name = f.getDeclaringClass().getName() + ":" + name;
        }

        if (!collector.containsKey(name)) {
          collector.put(name, f);
        }
      }
      currentClass = currentClass.getSuperclass();
    }
    return collectAsAccessible(collector);
  }

  private static <T extends AccessibleObject> Collection<T> collectAsAccessible(Map<?, T> collector) {
    if (collector.isEmpty()) {
      return Collections.emptyList();
    }
    Collection<T> list = new ArrayList<>();
    for (T o : collector.values()) {
      o.setAccessible(true);
      list.add(o);
    }
    return list;
  }

  private static void handleError(boolean throwOnError, String msg, Object... args) {
    if (throwOnError) {
      throw new BeanCreationException(msg, args);
    }
    LOG.error(msg, args);
  }
}

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
package org.eclipse.scout.rt.platform.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.LinkedHashMap;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BeanInstanceUtil {

  private static final Logger LOG = LoggerFactory.getLogger(BeanInstanceUtil.class);

  private BeanInstanceUtil() {
  }

  /**
   * Crates and initializes a new bean instance.
   *
   * @param beanClazz
   * @return
   */
  public static <T> T createAndInitializeBean(Class<T> beanClazz) {
    Assertions.assertNotNull(beanClazz);
    T instance = null;
    try {
      Constructor<T> cons = beanClazz.getDeclaredConstructor();
      cons.setAccessible(true);
      instance = cons.newInstance();
      initializeBeanInstance(instance);
    }
    catch (Exception e) {
      throw translateException("Could not create bean [" + beanClazz.getName() + "]", e);
    }
    return instance;
  }

  /**
   * Invokes all {@link PostConstruct} annotated methods of the given instance.
   */
  public static void initializeBeanInstance(Object instance) {
    Collection<Method> postCosntructMethods = collectPostConstructMethods(instance.getClass());
    for (Method method : postCosntructMethods) {
      LOG.debug("invoking post-construct method {}", method);
      try {
        method.setAccessible(true);
        method.invoke(instance);
      }
      catch (Exception e) {
        throw translateException("Exception while invoking @PostConstruct method", e);
      }
    }
  }

  /**
   * Transforms the given exception: {@link UndeclaredThrowableException} and {@link InvocationTargetException} are
   * unpacked and wrapped into a {@link BeanCreationException}. {@link Error} are just rethrown.
   * <p>
   * <b>Note:</b> This utility must not use any features of the bean manager. Hence the usage of an
   * {@link IExceptionTranslator} is not suitable.
   */
  private static RuntimeException translateException(String message, Exception e) {
    Throwable t = e;
    while ((t instanceof UndeclaredThrowableException || t instanceof InvocationTargetException) && t.getCause() != null) {
      t = t.getCause();
    }
    if (t instanceof Error) {
      throw (Error) t;
    }
    return new BeanCreationException(message, t);
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
   * @return Returns a collection of {@link PostConstruct} methods.
   * @throws BeanCreationException
   *           If unsupported methods are annotated with {@link PostConstruct} (i.e. those with parameters)
   */
  static Collection<Method> collectPostConstructMethods(Class<?> clazz) {
    LinkedHashMap<String /*method name*/, Method> collector = new LinkedHashMap<>();
    Class<?> currentClass = clazz;
    while (currentClass != null && currentClass != Object.class) {
      for (Method m : currentClass.getDeclaredMethods()) {
        if (!m.isAnnotationPresent(PostConstruct.class)) {
          continue;
        }

        // check number of parameters
        if (m.getParameterTypes().length != 0) {
          throw new BeanCreationException("Methods annotated with @PostConstruct must have no arguments [method={}]", m);
        }

        // compute method name (special handling for private methods)
        String name = m.getName();
        if (Modifier.isPrivate(m.getModifiers())) {
          name = m.getDeclaringClass().getName() + ":" + name;
        }

        if (!collector.containsKey(name)) {
          collector.put(name, m);
        }
      }
      currentClass = currentClass.getSuperclass();
    }
    return collector.values();
  }
}

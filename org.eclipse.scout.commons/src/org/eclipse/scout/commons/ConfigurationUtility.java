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
package org.eclipse.scout.commons;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Configuration-related utilities.
 */
public final class ConfigurationUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ConfigurationUtility.class);

  private ConfigurationUtility() {
  }

  /**
   * Filters the given class array and sorts the remaining elements according to
   * their {@link Order} annotation.
   * <p>
   * By default, the method throws an {@link IllegalArgumentException} if one of the remaining classes is not annotated
   * by {@link Order}. The behavior can be switched off by setting the system property
   * <code>bsi.debug.innerclass.order</code> to an arbitrary value.
   * 
   * @param classes
   * @param filter
   * @return
   * @throws IllegalArgumentException
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T>[] sortFilteredClassesByOrderAnnotation(Class[] classes, Class<T> filter) {
    TreeMap<CompositeObject, Class> orderedClassesMap = new TreeMap<CompositeObject, Class>();
    for (int i = 0; i < classes.length; i++) {
      if (filter.isAssignableFrom(classes[i])) {
        if (classes[i].isAnnotationPresent(Order.class)) {
          Order order = (Order) classes[i].getAnnotation(Order.class);
          orderedClassesMap.put(new CompositeObject(order.value(), i), classes[i]);
        }
        else {
          LOG.error("missing @Order annotation: " + classes[i].getName());
          orderedClassesMap.put(new CompositeObject(Double.MAX_VALUE, i), classes[i]);
        }
      }
    }
    return orderedClassesMap.values().toArray(new Class[orderedClassesMap.size()]);
  }

  /**
   * Sorts the elements according to their {@link Order} annotation.
   * <p>
   * If one of the objects is not annotated with {@link Order}, its index in the list is used as order value.
   */
  public static <T> Collection<T> sortByOrderAnnotation(Collection<T> list) {
    if(list==null){
	  return null;
	}
    TreeMap<CompositeObject, T> sortMap = new TreeMap<CompositeObject, T>();
    int index = 0;
    for (T element : list) {
      Class<?> c = element.getClass();
      if (c.isAnnotationPresent(Order.class)) {
        Order order = c.getAnnotation(Order.class);
        sortMap.put(new CompositeObject(order.value(), index), element);
      }
      else {
        sortMap.put(new CompositeObject(index, index), element);
      }
      index++;
    }
    return sortMap.values();
  }

  /**
   * Filters the given class array and returns the first occurence of an
   * instantiatable class of filter
   * 
   * @param classes
   * @param filter
   * @return first occurence of filter
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> filterClass(Class[] classes, Class<T> filter) {
    for (Class c : classes) {
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        return c;
      }
    }
    return null;
  }

  /**
   * Filters the given class array and returns all occurences of instantiatable
   * classes of filter
   * 
   * @param classes
   * @param filter
   * @return all occurences of filter
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T>[] filterClasses(Class[] classes, Class<T> filter) {
    ArrayList<Class<T>> list = new ArrayList<Class<T>>();
    for (Class c : classes) {
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        list.add(c);
      }
    }
    return list.toArray(new Class[0]);
  }

  /**
   * get all declared classes (inner types) of the specified class and all its
   * super classes
   */
  public static Class[] getDeclaredPublicClasses(Class c) {
    return c.getClasses();
  }

  public static <T> T newInnerInstance(Object instance, Class<T> innerClass) throws Exception {
    if (innerClass.getDeclaringClass() != null) {
      Constructor<T> c = innerClass.getDeclaredConstructor(new Class[]{innerClass.getDeclaringClass()});
      return c.newInstance(new Object[]{instance});
    }
    else {
      return innerClass.newInstance();
    }
  }

  /**
   * @return true if the declared method is overwritten in implementationType
   */
  public static boolean isMethodOverwrite(Class<?> declaringType, String methodName, Class[] parameterTypes, Class<?> implementationType) {
    try {
      Method declaredMethod;
      try {
        declaredMethod = declaringType.getDeclaredMethod(methodName, parameterTypes);
      }
      catch (NoSuchMethodException e) {
        LOG.error("cannot find declared method " + declaringType.getName() + "." + methodName, e);
        return false;
      }
      Class<?> c = implementationType;
      while (c != null && c != declaringType) {
        try {
          //check if method is avaliable
          c.getDeclaredMethod(declaredMethod.getName(), declaredMethod.getParameterTypes());
          return true;
        }
        catch (NoSuchMethodException e) {
          //nop
        }
        //up
        c = c.getSuperclass();
      }
    }
    catch (Throwable t) {
      LOG.error("declaringType=" + declaringType + ", methodName=" + methodName + ", parameterTypes=" + parameterTypes + ", implementationType=" + implementationType, t);
    }
    return false;
  }
}

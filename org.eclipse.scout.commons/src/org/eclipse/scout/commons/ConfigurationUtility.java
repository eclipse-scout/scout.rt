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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.commons.annotations.IOrdered;
import org.eclipse.scout.commons.annotations.InjectFieldTo;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
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
          if (!classes[i].isAnnotationPresent(Replace.class)) {
            LOG.error("missing @Order annotation: " + classes[i].getName());
          }
          orderedClassesMap.put(new CompositeObject(Double.MAX_VALUE, i), classes[i]);
        }
      }
    }
    return orderedClassesMap.values().toArray(new Class[orderedClassesMap.size()]);
  }

  /**
   * @deprecated use {@link #sortByOrder(Collection)} instead. Will be removed in release 3.10.
   */
  @Deprecated
  public static <T> Collection<T> sortByOrderAnnotation(Collection<T> list) {
    return sortByOrder(list);
  }

  /**
   * Sorts the elements according to their order:
   * <ol>
   * <li>If an {@link Order} annotation is present, its {@link Order#value()} is used</li>
   * <li>If a {@link Replace} annotation is present, the superclass' order is used</li>
   * <li>If the object implements {@link IOrdered}, {@link IOrdered#getOrder()} is used</li>
   * <li>Finally, the index in the original collection is used</li>
   * </ol>
   * 
   * @since 3.8.1
   */
  public static <T> Collection<T> sortByOrder(Collection<T> list) {
    if (list == null) {
      return null;
    }
    TreeMap<CompositeObject, T> sortMap = new TreeMap<CompositeObject, T>();
    int index = 0;
    for (T element : list) {
      Class<?> c = element.getClass();
      double order;
      Order orderAnnotation;
      while ((orderAnnotation = c.getAnnotation(Order.class)) == null && c.isAnnotationPresent(Replace.class)) {
        c = c.getSuperclass();
      }
      if (orderAnnotation != null) {
        order = orderAnnotation.value();
      }
      else if (element instanceof IOrdered) {
        order = ((IOrdered) element).getOrder();
      }
      else {
        order = (double) index;
      }
      sortMap.put(new CompositeObject(order, index), element);
      index++;
    }
    return sortMap.values();
  }

  /**
   * Filters the given class array and returns the first occurrence of an
   * instantiable class of filter
   * 
   * @param classes
   * @param filter
   * @return first occurrence of filter, might be annotated with {@link InjectFieldTo} or {@link Replace}
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
   * same as {@link #filterClass(Class[], Class)} but ignoring classes with {@link InjectFieldTo} and {@link Replace}
   * annotations
   * 
   * @since 3.8.1
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> filterClassIgnoringInjectFieldAnnotation(Class[] classes, Class<T> filter) {
    for (Class c : classes) {
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        if (!isInjectFieldAnnotationPresent(c)) {
          return c;
        }
      }
    }
    return null;
  }

  /**
   * Filters the given class array and returns all occurrences of instantiable
   * classes of filter
   * 
   * @param classes
   * @param filter
   * @return all occurrences of filter
   * @since 3.8.1
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
   * same as {@link #filterClasses(Class[], Class)} but ignoring classes with {@link InjectFieldTo} and {@link Replace}
   * annotations
   * 
   * @since 3.8.1
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T>[] filterClassesIgnoringInjectFieldAnnotation(Class[] classes, Class<T> filter) {
    ArrayList<Class<T>> list = new ArrayList<Class<T>>();
    for (Class c : classes) {
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        if (!isInjectFieldAnnotationPresent(c)) {
          list.add(c);
        }
      }
    }
    return list.toArray(new Class[0]);
  }

  /**
   * same as {@link #filterClasses(Class[], Class)} but only accepting classes with {@link InjectFieldTo} and
   * {@link Replace} annotations
   * 
   * @since 3.8.1
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T>[] filterClassesWithInjectFieldAnnotation(Class[] classes, Class<T> filter) {
    ArrayList<Class<T>> list = new ArrayList<Class<T>>();
    for (Class c : classes) {
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        if (isInjectFieldAnnotationPresent(c)) {
          list.add(c);
        }
      }
    }
    return list.toArray(new Class[0]);
  }

  /**
   * @return Returns <code>true</code> if the given class is annotated by {@link InjectFieldTo} or {@link Replace}.
   *         Otherwise <code>false</code>.
   *         <p/>
   *         <b>Note:</b> This method throws a {@link NullPointerException} if the given class is null.
   */
  public static boolean isInjectFieldAnnotationPresent(Class<?> c) {
    return c.isAnnotationPresent(InjectFieldTo.class) || c.isAnnotationPresent(Replace.class);
  }

  /**
   * get all declared classes (inner types) of the specified class and all its
   * super classes
   */
  public static Class[] getDeclaredPublicClasses(Class c) {
    return c.getClasses();
  }

  public static <T> T newInnerInstance(Object instance, Class<T> innerClass) throws Exception {
    if (innerClass.getDeclaringClass() != null && (innerClass.getModifiers() & Modifier.STATIC) == 0) {
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

  /**
   * @return Returns the given objects enclosing container type, i.e the first class on the enclosing classes path that
   *         is abstract or the outermost enclosing class. The latter is the primary type.
   */
  public static Class<?> getEnclosingContainerType(Object o) {
    if (o == null) {
      return null;
    }
    Class<?> c = o.getClass();
    while (!Modifier.isAbstract(c.getModifiers()) && c.getEnclosingClass() != null) {
      c = c.getEnclosingClass();
    }
    return c;
  }

  /**
   * Returns a new array without those classes, that are replaced by another class. The returned array is a new
   * instance, except there are no replacing classes. Replacing classes are annotated with {@link Replace}. Replacing
   * classes are reordered according to their nearest {@link Order} annotation that is found up the type hierarchy.
   * 
   * @param classes
   * @return
   * @since 3.8.2
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<? extends T>[] removeReplacedClasses(Class<? extends T>[] classes) {
    Set<Class<? extends T>> replacingClasses = getReplacingLeafClasses(classes);
    if (replacingClasses.isEmpty()) {
      // there are no replacing classes -> return original array
      return classes;
    }

    // compute resulting list of ordered classes
    List<Class<? extends T>> list = new ArrayList<Class<? extends T>>();
    for (Class<? extends T> c : classes) {
      list.add(c);
    }

    for (Class<? extends T> replacingClass : replacingClasses) {
      boolean reorder = !replacingClass.isAnnotationPresent(Order.class);
      boolean reordered = false;

      // handle transitive replacements
      Class<?> classToBeReplaced = replacingClass.getSuperclass();
      while (classToBeReplaced.isAnnotationPresent(Replace.class)) {
        // reorder replacement if necessary
        if (reorder && !reordered && classToBeReplaced.isAnnotationPresent(Order.class)) {
          reordered = moveBefore(list, replacingClass, (Class<? extends T>) classToBeReplaced);
        }
        list.remove(classToBeReplaced);
        classToBeReplaced = classToBeReplaced.getSuperclass();
      }

      // reorder replacement if necessary
      if (reorder && !reordered) {
        moveBefore(list, replacingClass, (Class<? extends T>) classToBeReplaced);
      }
      list.remove(classToBeReplaced);
    }

    return list.toArray(new Class[list.size()]);
  }

  /**
   * Computes a map based on the given classes that contains replaced classes pointing to their replacing classes. This
   * method never returns <code>null</code>.
   * <p/>
   * <b>Example:</b> Given the following two classes
   * 
   * <pre>
   * public class A {
   * }
   * 
   * &#064;Replace
   * public class B extends A {
   * }
   * </pre>
   * 
   * The invocation of <code>getReplacementMapping(new Class[] {B.class, String.class})</code> returns a map containing
   * <code>&lt;A.class, B.class&gt;</code>.
   * 
   * @param classes
   * @return
   * @since 3.8.2
   */
  public static <T> Map<Class<?>, Class<? extends T>> getReplacementMapping(Class<? extends T>[] classes) {
    Set<Class<? extends T>> replacingClasses = getReplacingLeafClasses(classes);
    if (replacingClasses.isEmpty()) {
      // there are no replacing classes -> return original array
      return Collections.emptyMap();
    }

    // compute resulting replacement mapping
    Map<Class<?>, Class<? extends T>> mappings = new HashMap<Class<?>, Class<? extends T>>();
    for (Class<? extends T> c : replacingClasses) {
      Class<?> tmpClass = c;
      do {
        tmpClass = tmpClass.getSuperclass();
        mappings.put(tmpClass, c);
      }
      while (tmpClass.isAnnotationPresent(Replace.class));
    }
    return mappings;
  }

  /**
   * Computes the set of classes that are annotated with {@link Replace} and removes transitive dependencies, so that
   * the most specific classes are returned.
   * <p/>
   * <b>Example:</b> Given the following two classes
   * 
   * <pre>
   * public class A {
   * }
   * 
   * &#064;Replace
   * public class B extends A {
   * }
   * </pre>
   * 
   * The invocation of <code>getReplacingLeafClasses(new Class[] {A.class, B.class, String.class})</code> returns a set
   * that contains <code>B.class</code> only. <code>String.class</code> is not annotated with {@link Replace} and
   * <code>A.class</code> is not a leaf replacement, but further replaced by <code>B.class</code>.
   * 
   * @param classes
   * @return Returns the set of replacing leaf classes or an empty set.
   * @since 3.8.2
   */
  public static <T> Set<Class<? extends T>> getReplacingLeafClasses(Class<? extends T>[] classes) {
    // gather all replacing and replaced classes (i.e. those annotated with @Replace and their super classes)
    Set<Class<? extends T>> replacingClasses = new HashSet<Class<? extends T>>();
    Set<Class<?>> replacedClasses = new HashSet<Class<?>>();
    for (Class<? extends T> c : classes) {
      if (c.isAnnotationPresent(Replace.class)) {
        replacingClasses.add(c);
        Class<?> tmpClass = c;
        do {
          tmpClass = tmpClass.getSuperclass();
          replacedClasses.add(tmpClass);
        }
        while (tmpClass.isAnnotationPresent(Replace.class));
      }
    }

    if (replacingClasses.isEmpty()) {
      return Collections.emptySet();
    }

    // remove transitive replacements (e.g. if A replaces B and B replaces C, A and B are replacing classes but we are interested in A only)
    replacingClasses.removeAll(replacedClasses);
    return replacingClasses;
  }

  /**
   * Moves the given element before the reference element. Both are expected to be part of the given list. If the
   * reference element is not in the list, the list remains untouched. If the element to move is not part of the list,
   * it is added before the reference element.
   * 
   * @param list
   * @param element
   * @param referenceElement
   * @return Returns <code>true</code> if the element has been moved or inserted. Otherwise <code>false</code>.
   * @since 3.8.2
   */
  private static <T> boolean moveBefore(List<T> list, T element, T referenceElement) {
    int index = list.indexOf(referenceElement);
    if (index != -1) {
      list.remove(element);
      list.add(index, element);
      return true;
    }
    return false;
  }
}

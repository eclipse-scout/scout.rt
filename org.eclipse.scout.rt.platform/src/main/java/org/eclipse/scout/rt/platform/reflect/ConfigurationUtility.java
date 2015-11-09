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
package org.eclipse.scout.rt.platform.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration-related utilities.
 */
public final class ConfigurationUtility {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtility.class);

  private ConfigurationUtility() {
  }

  /**
   * Filters the given class array and sorts the remaining elements according to their {@link Order} annotation.
   *
   * @param classes
   * @param filter
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> List<Class<? extends T>> sortFilteredClassesByOrderAnnotation(List<? extends Class> classes, Class<T> filter) {
    TreeMap<CompositeObject, Class<? extends T>> orderedClassesMap = new TreeMap<CompositeObject, Class<? extends T>>();
    int i = 0;
    for (Class candidate : classes) {
      if (filter.isAssignableFrom(candidate)) {
        if (candidate.isAnnotationPresent(Order.class)) {
          Order order = (Order) candidate.getAnnotation(Order.class);
          orderedClassesMap.put(new CompositeObject(order.value(), i), (Class<T>) candidate);
        }
        else {
          if (!candidate.isAnnotationPresent(Replace.class)) {
            LOG.error("missing @Order annotation: {}", candidate.getName());
          }
          orderedClassesMap.put(new CompositeObject(Double.MAX_VALUE, i), (Class<T>) candidate);
        }
        i++;
      }
    }
    return CollectionUtility.arrayList(orderedClassesMap.values());
  }

  /**
   * Filters the given class array and returns the first occurrence of an instantiable class of filter
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
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()) && !isInjectFieldAnnotationPresent(c)) {
        return c;
      }
    }
    return null;
  }

  /**
   * Filters the given class array and returns all occurrences of instantiable classes of filter
   *
   * @param classes
   * @param filter
   * @return all occurrences of filter
   * @since 3.8.1
   */
  @SuppressWarnings("unchecked")
  public static <T> List<Class<T>> filterClasses(Class[] classes, Class<T> filter) {
    List<Class<T>> result = new ArrayList<Class<T>>(classes.length);
    for (Class c : classes) {
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        result.add(c);
      }
    }
    return result;
  }

  /**
   * same as {@link #filterClasses(Class[], Class)} but ignoring classes with {@link InjectFieldTo} and {@link Replace}
   * annotations
   *
   * @since 3.8.1
   */
  @SuppressWarnings("unchecked")
  public static <T> List<Class<T>> filterClassesIgnoringInjectFieldAnnotation(Class[] classes, Class<T> filter) {
    List<Class<T>> list = new ArrayList<Class<T>>(classes.length);
    for (Class c : classes) {
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        if (!isInjectFieldAnnotationPresent(c)) {
          list.add(c);
        }
      }
    }
    return list;
  }

  /**
   * same as {@link #filterClasses(Class[], Class)} but only accepting classes with {@link InjectFieldTo} and
   * {@link Replace} annotations
   *
   * @since 3.8.1
   */
  @SuppressWarnings("unchecked")
  public static <T> List<Class<T>> filterClassesWithInjectFieldAnnotation(Class[] classes, Class<T> filter) {
    List<Class<T>> list = new ArrayList<Class<T>>(classes.length);
    for (Class c : classes) {
      if (filter.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        if (isInjectFieldAnnotationPresent(c)) {
          list.add(c);
        }
      }
    }
    return list;
  }

  /**
   * @return Returns <code>true</code> if the given class is annotated by {@link InjectFieldTo} or {@link Replace}.
   *         Otherwise <code>false</code>.
   * @throws NullPointerException
   *           if the given class is null.
   */
  public static boolean isInjectFieldAnnotationPresent(Class<?> c) {
    return c.isAnnotationPresent(InjectFieldTo.class) || c.isAnnotationPresent(Replace.class);
  }

  /**
   * get all declared classes (inner types) of the specified class and all its super classes
   */
  public static Class[] getDeclaredPublicClasses(Class c) {
    return c.getClasses();
  }

  public static <T> T newInnerInstance(Object instance, Class<T> innerClass) {
    try {
      if (innerClass.getDeclaringClass() != null && (innerClass.getModifiers() & Modifier.STATIC) == 0) {
        Constructor<T> c = innerClass.getDeclaredConstructor(new Class[]{innerClass.getDeclaringClass()});
        return c.newInstance(new Object[]{instance});
      }
      else {
        return innerClass.newInstance();
      }
    }
    catch (InvocationTargetException ite) {
      Throwable t = ite.getCause();
      while (t != null) {
        if (t instanceof ProcessingException) {
          ProcessingException pe = (ProcessingException) t;
          pe.addContextMessage("InnerClass=" + innerClass);
          throw pe;
        }
        t = t.getCause();
      }
      throw new ProcessingException("Error creating instance of '" + innerClass + "'.", ite.getCause());
    }
    catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      throw new ProcessingException("Error creating instance of '" + innerClass + "'.", e);
    }
  }

  /**
   * @return true if the declared method is overwritten in implementationType
   */
  public static boolean isMethodOverwrite(Class<?> declaringType, String methodName, Class[] parameterTypes, Class<?> implementationType) {
    Assertions.assertNotNull(declaringType, "declaringType must not be null");
    Assertions.assertNotNull(methodName, "methodName must not be null");
    Method declaredMethod;
    try {
      declaredMethod = declaringType.getDeclaredMethod(methodName, parameterTypes);
    }
    catch (NoSuchMethodException | SecurityException e) {
      LOG.error("cannot find declared method {}.{}", declaringType.getName(), methodName, e);
      return false;
    }
    Class<?> c = implementationType;
    while (c != null && c != declaringType) {
      try {
        //check if method is avaliable
        c.getDeclaredMethod(declaredMethod.getName(), declaredMethod.getParameterTypes());
        return true;
      }
      catch (NoSuchMethodException | SecurityException e) {
        //nop
      }
      //up
      c = c.getSuperclass();
    }
    return false;
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
  public static <T> List<Class<? extends T>> removeReplacedClasses(List<? extends Class<? extends T>> classes) {
    Set<Class<? extends T>> replacingClasses = getReplacingLeafClasses(classes);
    if (replacingClasses.isEmpty()) {
      // there are no replacing classes -> return original list copy
      return CollectionUtility.arrayList(classes);
    }

    // compute resulting list of ordered classes
    List<Class<? extends T>> list = new ArrayList<Class<? extends T>>(classes.size());
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

    return list;
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
  public static <T> Map<Class<?>, Class<? extends T>> getReplacementMapping(List<? extends Class<? extends T>> classes) {
    Set<Class<? extends T>> replacingClasses = getReplacingLeafClasses(classes);
    if (replacingClasses.isEmpty()) {
      // there are no replacing classes
      return new HashMap<Class<?>, Class<? extends T>>(0);
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
  public static <T> Set<Class<? extends T>> getReplacingLeafClasses(List<? extends Class<? extends T>> classes) {
    // gather all replacing and replaced classes (i.e. those annotated with @Replace and their super classes)
    Set<Class<? extends T>> replacingClasses = new HashSet<Class<? extends T>>(classes.size());
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
      return replacingClasses;
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

  /**
   * Returns the value of the {@link ClassId} annotation or the class name as fallback, the annotation is undefined.
   * <p>
   * If the class is replaced, the id of the replaced field is used ({@link ClassId}).
   * </p>
   *
   * @param clazz
   * @return annotated id or class name fallback
   * @since 3.10.0
   */
  public static String getAnnotatedClassIdWithFallback(Class<?> clazz) {
    return getAnnotatedClassIdWithFallback(clazz, false);
  }

  /**
   * Returns the value of the {@link ClassId} annotation or the class name as fallback, the annotation is undefined.
   * <p>
   * If the class is replaced, the id of the replaced field is used ({@link ClassId}).
   * </p>
   *
   * @param clazz
   * @param simpleName
   *          use the simple class name instead of the fully qualified class name.
   * @return annotated id or class name fallback
   * @since 3.10.0
   */
  public static String getAnnotatedClassIdWithFallback(Class<?> clazz, boolean simpleName) {
    Class<?> replaced = getOriginalClass(clazz);
    ClassId id = replaced.getAnnotation(ClassId.class);
    String annotatedClassId = (id == null) ? null : id.value();
    if (annotatedClassId != null) {
      return annotatedClassId;
    }
    else if (simpleName && !StringUtility.isNullOrEmpty(replaced.getSimpleName())) {
      return replaced.getSimpleName();
    }
    return replaced.getName();
  }

  /**
   * If the class is replacing another class, the one that is being replaced is returned. Otherwise the class itself is
   * returned.
   *
   * @return class to be replaced
   */
  private static Class<?> getOriginalClass(Class<?> c) {
    while (c.isAnnotationPresent(Replace.class)) {
      c = c.getSuperclass();
    }
    return c;
  }
}

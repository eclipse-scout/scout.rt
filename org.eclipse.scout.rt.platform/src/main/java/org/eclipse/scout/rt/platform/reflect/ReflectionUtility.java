/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Reflection Utility to change final modifiers on fields
 */
public final class ReflectionUtility {

  private ReflectionUtility() {
  }

  public static void removeFinalFlagOnField(Field f) throws NoSuchFieldException, IllegalAccessException {
    Field reflectedModifier = Field.class.getDeclaredField("modifiers");
    reflectedModifier.setAccessible(true);
    int modifiers = (Integer) reflectedModifier.get(f);
    modifiers = modifiers & ~Modifier.FINAL;
    reflectedModifier.set(f, modifiers);
    reflectedModifier.setAccessible(false);
  }

  public static void setFinalFlagOnField(Field f) throws NoSuchFieldException, IllegalAccessException {
    Field reflectedModifier = Field.class.getDeclaredField("modifiers");
    reflectedModifier.setAccessible(true);
    int modifiers = (Integer) reflectedModifier.get(f);
    modifiers = modifiers | Modifier.FINAL;
    reflectedModifier.set(f, modifiers);
    reflectedModifier.setAccessible(false);
  }

  /**
   * Returns the interfaces which are implemented by the given class or its super types. However, only direct interfaces
   * are returned, and not the whole interface hierarchy.
   */
  public static Class<?>[] getInterfaces(Class<?> clazz) {
    if (clazz.isInterface()) {
      return new Class<?>[]{clazz};
    }

    final Set<Class<?>> interfaces = new HashSet<>();
    while (!Object.class.equals(clazz)) {
      interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
      clazz = clazz.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[0]);
  }

  /**
   * Returns the constructor with the given parameter types, or <code>null</code> if not found.
   */
  public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>[] paramTypes) {
    for (final Constructor<?> candidate : clazz.getConstructors()) {
      if (Arrays.equals(paramTypes, candidate.getParameterTypes())) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Returns the first element which has an annotation of the given type declared.
   */
  public static <ANNOTATION extends Annotation> ANNOTATION getAnnotation(final Class<ANNOTATION> annotationClass, final AnnotatedElement... elements) {
    for (final AnnotatedElement element : elements) {
      final ANNOTATION annotation = element.getAnnotation(annotationClass);
      if (annotation != null) {
        return annotation;
      }
    }
    return null;
  }
}

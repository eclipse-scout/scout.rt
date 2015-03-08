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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reflection Utility to change final modifiers on fields
 */
public class ReflectionUtility {

  private ReflectionUtility() {
  }

  public static void removeFinalFlagOnField(Field f) throws Exception {
    Field reflectedModifier = Field.class.getDeclaredField("modifiers");
    reflectedModifier.setAccessible(true);
    int modifiers = (Integer) reflectedModifier.get(f);
    modifiers = modifiers & ~Modifier.FINAL;
    reflectedModifier.set(f, modifiers);
    reflectedModifier.setAccessible(false);
  }

  public static void setFinalFlagOnField(Field f) throws Exception {
    Field reflectedModifier = Field.class.getDeclaredField("modifiers");
    reflectedModifier.setAccessible(true);
    int modifiers = (Integer) reflectedModifier.get(f);
    modifiers = modifiers | Modifier.FINAL;
    reflectedModifier.set(f, modifiers);
    reflectedModifier.setAccessible(false);
  }

  /**
   * Returns the interfaces which are implemented by the given class or its super types. However, only direct
   * interfaces are returned, and not the whole interface hierarchy.
   */
  public static Class<?>[] getInterfaces(Class<?> clazz) {
    if (clazz.isInterface()) {
      return new Class<?>[]{clazz};
    }

    final List<Class<?>> interfaces = new ArrayList<>();
    while (!Object.class.equals(clazz)) {
      interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
      clazz = clazz.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[interfaces.size()]);
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
}

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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
}

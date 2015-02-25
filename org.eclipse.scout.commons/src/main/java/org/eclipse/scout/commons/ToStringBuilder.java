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
package org.eclipse.scout.commons;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder to create a formatted {@link Object#toString()} output.
 */
public class ToStringBuilder {

  private final String m_identifier;
  private final List<Object> m_builder;

  public ToStringBuilder(final Object instance) {
    m_builder = new ArrayList<>();
    m_identifier = createIdentifier(instance);
  }

  public ToStringBuilder attr(final String name, final Object value) {
    m_builder.add(new SimpleEntry<>(name, value));
    return this;
  }

  public ToStringBuilder attr(final String name, final boolean value) {
    attr(name, (Object) value);
    return this;
  }

  public ToStringBuilder attr(final String name, final char value) {
    attr(name, (Object) value);
    return this;
  }

  public ToStringBuilder attr(final String name, final short value) {
    attr(name, (Object) value);
    return this;
  }

  public ToStringBuilder attr(final String name, final int value) {
    attr(name, (Object) value);
    return this;
  }

  public ToStringBuilder attr(final String name, final long value) {
    attr(name, (Object) value);
    return this;
  }

  public ToStringBuilder attr(final String name, final float value) {
    attr(name, (Object) value);
    return this;
  }

  public ToStringBuilder attr(final String name, final double value) {
    attr(name, (Object) value);
    return this;
  }

  public ToStringBuilder attr(final String name, final Object... values) {
    attr(name, Arrays.asList(values));
    return this;
  }

  public ToStringBuilder attr(final Object value) {
    m_builder.add(value);
    return this;
  }

  public ToStringBuilder ref(final String name, final Object obj) {
    attr(name, createIdentifier(obj));
    return this;
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", m_identifier, StringUtility.join(", ", m_builder));
  }

  /**
   * Creates the identifier for the given {@link Object} consisting of classname and hashcode.
   */
  private static String createIdentifier(final Object instance) {
    final Class<?> clazz = resolveClass(instance);

    return String.format("%s@%s", clazz.getSimpleName(), Integer.toHexString(instance.hashCode()));
  }

  /**
   * Resolves the class to be displayed for the given instance. For anonymous classes, the superclass or interface is
   * returned.
   */
  private static Class<?> resolveClass(final Object instance) {
    final Class<?> clazz = instance.getClass();
    if (clazz.isAnonymousClass()) {
      final Class<?>[] interfaces = clazz.getInterfaces();
      if (interfaces.length > 0) {
        return interfaces[0];
      }
      else {
        return clazz.getSuperclass();
      }
    }
    else {
      return instance.getClass();
    }
  }
}

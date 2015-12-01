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
package org.eclipse.scout.rt.platform.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

  /**
   * Appends the given {@code Object} value, even if <code>null</code>.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final Object value) {
    attr(name, value, true);
    return this;
  }

  /**
   * Appends the given {@code Object} value depending on the given <code>appendIfNull</code> argument.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final Object value, final boolean appendIfNull) {
    if (value != null || appendIfNull) {
      m_builder.add(new SimpleEntry<>(name, value));
    }
    return this;
  }

  /**
   * Appends the given {@code String} value, even if <code>null</code>.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final String value) {
    attr(name, value, true);
    return this;
  }

  /**
   * Appends the given {@code String} value depending on the given <code>appendIfNullOrEmpty</code> argument.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final String value, final boolean appendIfNullOrEmpty) {
    if (!StringUtility.isNullOrEmpty(value) || appendIfNullOrEmpty) {
      m_builder.add(new SimpleEntry<>(name, value));
    }
    return this;
  }

  /**
   * Appends the given {@code boolean} value.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final boolean value) {
    attr(name, (Object) value);
    return this;
  }

  /**
   * Appends the given {@code char} value.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final char value) {
    attr(name, (Object) value);
    return this;
  }

  /**
   * Appends the given {@code short} value.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final short value) {
    attr(name, (Object) value);
    return this;
  }

  /**
   * Appends the given {@code int} value.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final int value) {
    attr(name, (Object) value);
    return this;
  }

  /**
   * Appends the given {@code long} value.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final long value) {
    attr(name, (Object) value);
    return this;
  }

  /**
   * Appends the given {@code float} value.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final float value) {
    attr(name, (Object) value);
    return this;
  }

  /**
   * Appends the given {@code double} value.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final double value) {
    attr(name, (Object) value);
    return this;
  }

  /**
   * Appends the given {@code varArg} values; <code>null</code> values are filtered; multiple values are separated by
   * comma.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final Object... values) {
    final Collection<?> collection = (values == null ? Collections.emptyList() : Arrays.asList(values));
    attr(name, collection, true);
    return this;
  }

  /**
   * Appends the given {@code Collection} values; <code>null</code> values are filtered; multiple values are separated
   * by comma.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final Collection<?> values) {
    attr(name, values, true);
    return this;
  }

  /**
   * Appends the given {@code Collection} values; <code>null</code> values are filtered; multiple values are separated
   * by comma; if empty, the collection is only appended if <code>appendIfEmpty</code> is set to <code>true</code>.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final String name, final Collection<?> values, final boolean appendIfEmpty) {
    final String value = StringUtility.join(",", values);
    if (StringUtility.hasText(value) || appendIfEmpty) {
      m_builder.add(new SimpleEntry<>(name, String.format("[%s]", value)));
    }
    return this;
  }

  /**
   * Appends the given {@code Object} value, but only if not <code>null</code>.
   *
   * @return <code>this</code> supporting the fluent API
   */
  public ToStringBuilder attr(final Object value) {
    if (value != null) {
      m_builder.add(value);
    }
    return this;
  }

  /**
   * Appends the given {@code Object} reference, or 'null' if <code>null</code>.
   *
   * @return <code>this</code> supporting the fluent API
   */
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
  private static String createIdentifier(final Object object) {
    if (object == null) {
      return "null";
    }
    else {
      final Class<?> clazz = resolveClass(object);
      return String.format("%s@%s", clazz.getSimpleName(), Integer.toHexString(object.hashCode()));
    }
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

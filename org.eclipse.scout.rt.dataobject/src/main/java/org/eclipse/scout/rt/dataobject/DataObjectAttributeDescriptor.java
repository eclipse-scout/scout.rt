/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * Descriptor of a {@link IDoEntity} attribute.
 */
public final class DataObjectAttributeDescriptor {
  private final String m_name;
  private final ParameterizedType m_type;
  private final Optional<String> m_formatPattern;
  private final Method m_accessor;

  public DataObjectAttributeDescriptor(String name, ParameterizedType type, Optional<String> formatPattern, Method accessor) {
    m_name = name;
    m_type = type;
    m_formatPattern = formatPattern;
    m_accessor = accessor;
  }

  /**
   * @return Attribute name as declared by the accessor method or the {@link AttributeName} annotation.
   */
  public String getName() {
    return m_name;
  }

  /**
   * @return {@link DoValue}, {@link DoList}, {@link DoSet} or {@link DoCollection} parameterized type of attribute.
   */
  public ParameterizedType getType() {
    return m_type;
  }

  /**
   * @return {@link Method} to access the attribute within the {@link DoEntity}.
   */
  public Method getAccessor() {
    return m_accessor;
  }

  /**
   * @return attribute format pattern declared by the (optional) {@link ValueFormat} annotation
   */
  public Optional<String> getFormatPattern() {
    return m_formatPattern;
  }

  @Override
  public String toString() {
    return DataObjectAttributeDescriptor.class.getSimpleName() + " [name=" + m_name + " type=" + m_type + " formatPattern=" + m_formatPattern + " accessor=" + m_accessor + "]";
  }
}

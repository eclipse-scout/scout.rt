/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;

import com.fasterxml.jackson.databind.JavaType;

/**
 * Holds type information of a JSON attribute declared as {@link DoNode} within an {@link IDoEntity}.
 */
public class AttributeType {

  private final JavaType m_javaType;
  private final boolean m_doValue;

  public static AttributeType ofDoCollection(JavaType javaType) {
    return new AttributeType(javaType, false);
  }

  public static AttributeType ofDoValue(JavaType javaType) {
    return new AttributeType(javaType, true);
  }

  protected AttributeType(JavaType javaType, boolean doValue) {
    m_javaType = javaType;
    m_doValue = doValue;
  }

  /**
   * @return Jackson's {@link JavaType} of the {@link DoNode}'s type parameter.
   */
  public JavaType getJavaType() {
    return m_javaType;
  }

  /**
   * @return {@code true} if the declared attribute type could have been resolved (e.g. if an actual type can be derived
   * from the {@link DoNode}'s type parameter). Otherwise {@code false}.
   */
  public boolean isKnown() {
    return m_javaType.getRawClass() != Object.class;
  }

  /**
   * @return {@code true} if this instance references a {@link DoCollection}-typed attribute. Otherwise {@code false}.
   */
  public boolean isDoCollection() {
    return !m_doValue;
  }

  /**
   * @return {@code true} if this instance references a {@link DoValue}-typed attribute. Otherwise {@code false}.
   */
  public boolean isDoValue() {
    return m_doValue;
  }
}

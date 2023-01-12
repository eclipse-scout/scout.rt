/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.holders;

import java.io.Serializable;

/**
 * Name/Value pair used in sql bind base list for named binds
 */
public class NVPair implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_name;
  private final Object m_value;
  private final Class m_nullType;

  public NVPair(String name, Object value) {
    m_name = name;
    m_value = value;
    m_nullType = null;
  }

  public NVPair(String name, Object value, Class nullType) {
    m_name = name;
    m_value = value;
    m_nullType = nullType;
  }

  public String getName() {
    return m_name;
  }

  public Object getValue() {
    return m_value;
  }

  public Class getNullType() {
    return m_nullType;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getName() + "=" + getValue() + "]";
  }
}

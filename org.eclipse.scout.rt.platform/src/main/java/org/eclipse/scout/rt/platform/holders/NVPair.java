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

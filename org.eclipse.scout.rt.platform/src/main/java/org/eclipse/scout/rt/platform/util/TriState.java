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
package org.eclipse.scout.rt.platform.util;

/**
 * A Boolean with three states: {@value TriState#TRUE}, {@value TriState#FALSE} and {@value TriState#UNDEFINED}
 * <p>
 * The value is internally represented as {@value Boolean#TRUE}, {@value Boolean#FALSE} and null
 * <p>
 * Comparison using '==' is supported since the three states are singleton states.
 */
public enum TriState {

  FALSE(Boolean.FALSE),
  TRUE(Boolean.TRUE),
  UNDEFINED(null);

  private final Boolean m_value;

  TriState(Boolean value) {
    this.m_value = value;
  }

  /**
   * @return true, false or null
   */
  public Boolean getBooleanValue() {
    return m_value;
  }

  /**
   * @return 1, 0 or null
   */
  public Integer getIntegerValue() {
    return (m_value != null ? (m_value ? 1 : 0) : null);
  }

  public boolean isTrue() {
    return m_value != null && m_value.booleanValue();
  }

  public boolean isFalse() {
    return m_value != null && !m_value.booleanValue();
  }

  public boolean isUndefined() {
    return m_value == null;
  }

  /**
   * Boolean: true -&gt; true false -&gt; false null -&gt; null
   * <p>
   * Number: 1 -&gt; true 0 -&gt; false null -&gt; null other-&gt; null
   * <p>
   * String: "true" -&gt; true "false" -&gt; false "1" -&gt; true "0" -&gt; false null -&gt; null other -&gt; null
   */
  public static TriState parse(Object value) {
    if (value == null) {
      return UNDEFINED;
    }
    else if (value instanceof TriState) {
      return (TriState) value;
    }
    else if (value instanceof Boolean) {
      return parseBoolean((Boolean) value);
    }
    else if (value instanceof Number) {
      return parseInt(((Number) value).intValue());
    }
    else if (value instanceof String) {
      return parseString((String) value);
    }
    else {
      throw new IllegalArgumentException("value of unknown type " + value + " [" + value.getClass() + "]");
    }
  }

  private static TriState parseBoolean(Boolean value) {
    if (value) {
      return TRUE;
    }
    else {
      return FALSE;
    }
  }

  private static TriState parseInt(int i) {
    switch (i) {
      case 0:
        return FALSE;
      case 1:
        return TRUE;
      default:
        return UNDEFINED;
    }
  }

  private static TriState parseString(String value) {
    if ("true".equals(value)) {
      return TRUE;
    }
    else if ("false".equals(value)) {
      return FALSE;
    }
    else if ("0".equals(value)) {
      return FALSE;
    }
    else if ("1".equals(value)) {
      return TRUE;
    }
    else {
      return UNDEFINED;
    }
  }
}

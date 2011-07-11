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

/**
 * A TriState is a Boolean value with three states: {@value TriState#TRUE}, {@value TriState#FALSE} and
 * {@value TriState#UNDEFINED} The value is
 * internally represented as {@value Boolean#TRUE}, {@value Boolean#FALSE} and
 * null
 */
public final class TriState implements java.io.Serializable, Comparable<TriState> {
  /**
   * The <code>Boolean</code> object corresponding to the value <code>true</code>.
   */
  public static final TriState TRUE = new TriState(Boolean.TRUE);

  /**
   * The <code>Boolean</code> object corresponding to the value <code>false</code>.
   */
  public static final TriState FALSE = new TriState(Boolean.FALSE);

  /**
   * The <code>Boolean</code> object corresponding to the value <code>null</code>.
   */
  public static final TriState UNDEFINED = new TriState(null);

  private final Boolean value;

  private static final long serialVersionUID = 1L;

  /**
   * Boolean: true -> true false -> false null -> null Number: 1 -> true 0 ->
   * false null -> null other-> null String: "true" -> true "false" -> false "1"
   * -> true "0" -> false null -> null other -> null
   */
  public static TriState parseTriState(Object value) {
    if (value == null) {
      return UNDEFINED;
    }
    else if (value instanceof Boolean) {
      if ((Boolean) value) {
        return TRUE;
      }
      else {
        return FALSE;
      }
    }
    else if (value instanceof Number) {
      int i = ((Number) value).intValue();
      switch (i) {
        case 0: {
          return FALSE;
        }
        case 1: {
          return TRUE;
        }
        default: {
          return UNDEFINED;
        }
      }
    }
    else if (value instanceof String) {
      if (value.equals("true")) {
        return TRUE;
      }
      else if (value.equals("false")) {
        return FALSE;
      }
      else if (value.equals("0")) {
        return FALSE;
      }
      else if (value.equals("1")) {
        return TRUE;
      }
      else {
        return UNDEFINED;
      }
    }
    else {
      throw new IllegalArgumentException("value of unknown type " + value + " [" + value.getClass() + "]");
    }
  }

  /**
   * see {@link #parseTriState(Object)}
   */
  private TriState(Boolean value) {
    this.value = value;
  }

  /**
   * @return true, false or null
   */
  public Boolean getBooleanValue() {
    return value;
  }

  /**
   * @return 1, 0 or null
   */
  public Integer getIntegerValue() {
    return (value != null ? (value ? 1 : 0) : null);
  }

  public boolean isUndefined() {
    return value == null;
  }

  @Override
  public String toString() {
    if (value != null) {
      return value ? "true" : "false";
    }
    else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    if (value != null) {
      return value ? 1 : 0;
    }
    else {
      return 2;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (value == obj) {
      return true;
    }
    else if (obj instanceof TriState) {
      TriState t = (TriState) obj;
      int a = (value != null ? (value ? 1 : 0) : 2);
      int b = (t.value != null ? (t.value ? 1 : 0) : 2);
      return a == b;
    }
    return false;
  }

  @Override
  public int compareTo(TriState t) {
    Integer a = (value != null ? (value ? 1 : 0) : 2);
    Integer b = (t.value != null ? (t.value ? 1 : 0) : 2);
    return a.compareTo(b);
  }
}

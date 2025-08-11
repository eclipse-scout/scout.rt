/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.util.Objects;

public class BooleanDataObjectValue implements IDataObjectValue {
  private boolean m_value;

  @Override
  public Boolean getValue() {
    return m_value;
  }

  public boolean booleanValue() {
    return m_value;
  }

  public BooleanDataObjectValue withValue(boolean value) {
    m_value = value;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BooleanDataObjectValue that = (BooleanDataObjectValue) o;
    return m_value == that.m_value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(m_value);
  }

  @Override
  public String toString() {
    return "BooleanDataObjectValue [m_value=" + m_value + "]";
  }
}

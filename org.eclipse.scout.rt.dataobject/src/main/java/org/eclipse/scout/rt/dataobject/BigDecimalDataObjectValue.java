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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Objects;

public class BigDecimalDataObjectValue implements IDataObjectValue {
  private BigDecimal m_value;

  @Override
  public BigDecimal getValue() {
    return m_value;
  }

  public BigDecimalDataObjectValue withValue(BigDecimal value) {
    m_value = assertNotNull(value, "value is required."); // null is not allowed because otherwise, `null` and an IDataObjectValue holding `null` would represent the same JSON value
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BigDecimalDataObjectValue that = (BigDecimalDataObjectValue) o;
    return Objects.equals(m_value, that.m_value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(m_value);
  }

  @Override
  public String toString() {
    return "BigDecimalDataObjectValue [m_value=" + m_value + "]";
  }
}

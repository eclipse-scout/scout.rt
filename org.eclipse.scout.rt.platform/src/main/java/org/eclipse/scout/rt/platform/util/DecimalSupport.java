/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Support for working and calculating with {@link BigDecimal} within the same scales and {@link MathContext}.
 *
 * @since 8.0
 */
public class DecimalSupport {

  private final int m_scale;
  private final RoundingMode m_roundingMode;
  private final MathContext m_mathContext;
  private final BigDecimal m_zero;
  private final BigDecimal m_one;

  public DecimalSupport(int scale, MathContext mathContext) {
    m_scale = scale;
    m_roundingMode = mathContext.getRoundingMode();
    m_mathContext = mathContext;
    m_zero = new BigDecimal(0L, mathContext).setScale(scale);
    m_one = new BigDecimal(1L, mathContext).setScale(scale);
  }

  public int getScale() {
    return m_scale;
  }

  public RoundingMode getRoundingMode() {
    return m_roundingMode;
  }

  public MathContext getMathContext() {
    return m_mathContext;
  }

  public BigDecimal zero() {
    return m_zero;
  }

  public BigDecimal one() {
    return m_one;
  }

  // Create functions

  public BigDecimal create(Number n) {
    if (n == null) {
      return null;
    }
    if (n instanceof BigDecimal) {
      return ((BigDecimal) n).setScale(m_scale, m_roundingMode);
    }
    if (n instanceof Double) {
      return new BigDecimal((Double) n, m_mathContext).setScale(m_scale, m_roundingMode);
    }
    if (n instanceof Integer) {
      return new BigDecimal((Integer) n, m_mathContext).setScale(m_scale, m_roundingMode);
    }
    if (n instanceof Float) {
      return new BigDecimal((Float) n, m_mathContext).setScale(m_scale, m_roundingMode);
    }
    if (n instanceof Long) {
      return new BigDecimal((Long) n, m_mathContext).setScale(m_scale, m_roundingMode);
    }
    if (n instanceof BigInteger) {
      return new BigDecimal((BigInteger) n, m_mathContext).setScale(m_scale, m_roundingMode);
    }
    if (n instanceof Byte) {
      return new BigDecimal((Byte) n, m_mathContext).setScale(m_scale, m_roundingMode);
    }
    if (n instanceof Short) {
      return new BigDecimal((Short) n, m_mathContext).setScale(m_scale, m_roundingMode);
    }
    throw new IllegalArgumentException("invalid number [" + n.getClass().getName() + "]: " + n);
  }

  public BigDecimal create(BigDecimal d) {
    if (d == null) {
      return null;
    }
    return d.setScale(m_scale, m_roundingMode);
  }

  public BigDecimal create(Double d) {
    if (d == null) {
      return null;
    }
    return new BigDecimal(d, m_mathContext).setScale(m_scale, m_roundingMode);
  }

  public BigDecimal create(Integer i) {
    if (i == null) {
      return null;
    }
    return new BigDecimal(i.longValue(), m_mathContext).setScale(m_scale, m_roundingMode);
  }

  public BigDecimal create(Float f) {
    if (f == null) {
      return null;
    }
    return new BigDecimal(f, m_mathContext).setScale(m_scale, m_roundingMode);
  }

  public BigDecimal create(Long l) {
    if (l == null) {
      return null;
    }
    return new BigDecimal(l, m_mathContext).setScale(m_scale, m_roundingMode);
  }

  public BigDecimal create(char[] c) {
    if (c == null) {
      return null;
    }
    return new BigDecimal(c, m_mathContext).setScale(m_scale, m_roundingMode);
  }

  public BigDecimal create(String s) {
    if (s == null) {
      return null;
    }
    return new BigDecimal(s, m_mathContext).setScale(m_scale, m_roundingMode);
  }

  // null support

  public BigDecimal nvl(BigDecimal value) {
    return nvl(value, zero());
  }

  public BigDecimal nvl(BigDecimal value, BigDecimal valueWhenNull) {
    if (value == null) {
      return valueWhenNull;
    }
    return value;
  }

  // Operations

  public BigDecimal add(BigDecimal augend, BigDecimal addend) {
    if (augend == null || addend == null) {
      return null;
    }
    return addend.add(augend);
  }

  public BigDecimal sub(BigDecimal minuend, BigDecimal subtrahend) {
    if (minuend == null || subtrahend == null) {
      return null;
    }
    return minuend.subtract(subtrahend);
  }

  public BigDecimal mul(BigDecimal multiplicand, BigDecimal multiplier) {
    if (multiplicand == null || multiplier == null) {
      return null;
    }
    return multiplier.multiply(multiplicand).setScale(m_scale, m_roundingMode);
  }

  public BigDecimal div(BigDecimal dividend, BigDecimal divisor) {
    if (dividend == null || divisor == null) {
      return null;
    }
    return dividend.divide(divisor, m_scale, m_roundingMode);
  }

  public BigDecimal min(BigDecimal... d) {
    if (d == null || d.length == 0) {
      return null;
    }
    BigDecimal min = d[0];
    for (BigDecimal b : d) {
      if (b == null) {
        return null;
      }
      min = b.min(min);
    }
    return min;
  }

  public BigDecimal sqrt(BigDecimal in, int scale) {
    if (0 == BigDecimal.ZERO.compareTo(in)) {
      return in;
    }
    BigDecimal sqrt = BigDecimal.ONE;
    sqrt = sqrt.setScale(scale + 3, RoundingMode.HALF_EVEN);
    BigDecimal store = new BigDecimal(in.toString());
    boolean first = true;
    do {
      if (!first) {
        store = new BigDecimal(sqrt.toString());
      }
      else {
        first = false;
      }
      store = store.setScale(scale + 3, RoundingMode.HALF_EVEN);
      sqrt = in.divide(store, scale + 3, RoundingMode.HALF_EVEN).add(store).divide(
          BigDecimal.valueOf(2), scale + 3, RoundingMode.HALF_EVEN);
    }
    while (!store.equals(sqrt));
    return sqrt.setScale(scale, RoundingMode.HALF_EVEN);
  }

  public BigDecimal max(BigDecimal... d) {
    if (d == null || d.length == 0) {
      return null;
    }
    BigDecimal max = d[0];
    for (BigDecimal b : d) {
      if (b == null) {
        return null;
      }
      max = b.max(max);
    }
    return max;
  }

  public BigDecimal abs(BigDecimal a) {
    return a == null ? null : a.abs();
  }

  public boolean eq(BigDecimal a, BigDecimal b) {
    if (a == null && b == null) {
      return true;
    }
    else if (a == null || b == null) {
      return false;
    }
    else {
      return a.compareTo(b) == 0;
    }
  }

  public boolean lt(BigDecimal a, BigDecimal b) {
    if (a == null || b == null) {
      return false;
    }
    else {
      return a.compareTo(b) < 0;
    }
  }

  public boolean gt(BigDecimal a, BigDecimal b) {
    if (a == null || b == null) {
      return false;
    }
    else {
      return a.compareTo(b) > 0;
    }
  }

  public boolean le(BigDecimal a, BigDecimal b) {
    if (a == null || b == null) {
      return false;
    }
    else {
      return a.compareTo(b) <= 0;
    }
  }

  public boolean ge(BigDecimal a, BigDecimal b) {
    if (a == null || b == null) {
      return false;
    }
    else {
      return a.compareTo(b) >= 0;
    }
  }

  public BigDecimal round(Number value, int precision) {
    BigDecimal d = create(value);
    if (d == null) {
      return null;
    }
    return d.setScale(precision, RoundingMode.HALF_UP).setScale(m_scale);
  }
}

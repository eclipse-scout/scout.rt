/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a system-wide default {@link DecimalSupport} with a default scale and a default {@link MathContext}.
 *
 * @since 8.0
 */
public final class DECIMAL {
  private static final Logger LOG = LoggerFactory.getLogger(DECIMAL.class);

  private DECIMAL() {
  }

  public static final DecimalSupport DECIMAL_SUPPORT;

  static {
    DefaultDecimalSupportProvider provider = null;
    String defaultDecimalSupportProviderClassName = ConfigUtility.getProperty(DefaultDecimalSupportProviderProperty.KEY);
    if (defaultDecimalSupportProviderClassName != null) {
      try {
        Class<?> clazz = DECIMAL.class.getClassLoader().loadClass(defaultDecimalSupportProviderClassName);
        provider = (DefaultDecimalSupportProvider) BeanInstanceUtil.createBean(clazz);
      }
      catch (Exception e) {
        LOG.warn("Failed to create {} using instead DefaultDecimalSupportProvider", defaultDecimalSupportProviderClassName, e);
      }
    }
    if (provider == null) {
      provider = new DefaultDecimalSupportProvider();
    }
    DECIMAL_SUPPORT = provider.get();
  }

  /**
   * Constant for zero (identity element for addition).
   */
  public static final BigDecimal ZERO = DECIMAL_SUPPORT.zero();

  /**
   * Constant for one (identity element for multiplication).
   */
  public static final BigDecimal ONE = DECIMAL_SUPPORT.one();

  public static BigDecimal create(Number n) {
    return DECIMAL_SUPPORT.create(n);
  }

  public static BigDecimal create(BigDecimal n) {
    return DECIMAL_SUPPORT.create(n);
  }

  public static BigDecimal create(Double d) {
    return DECIMAL_SUPPORT.create(d);
  }

  public static BigDecimal create(Integer i) {
    return DECIMAL_SUPPORT.create(i);
  }

  public static BigDecimal create(Float f) {
    return DECIMAL_SUPPORT.create(f);
  }

  public static BigDecimal create(Long l) {
    return DECIMAL_SUPPORT.create(l);
  }

  public static BigDecimal create(char[] c) {
    return DECIMAL_SUPPORT.create(c);
  }

  public static BigDecimal create(String s) {
    return DECIMAL_SUPPORT.create(s);
  }

  public static BigDecimal nvl(BigDecimal value) {
    return DECIMAL_SUPPORT.nvl(value);
  }

  public static BigDecimal nvl(BigDecimal value, BigDecimal valueWhenNull) {
    return DECIMAL_SUPPORT.nvl(value, valueWhenNull);
  }

  public static BigDecimal add(BigDecimal augend, BigDecimal addend) {
    return DECIMAL_SUPPORT.add(augend, addend);
  }

  public static BigDecimal sub(BigDecimal minuend, BigDecimal subtrahend) {
    return DECIMAL_SUPPORT.sub(minuend, subtrahend);
  }

  public static BigDecimal mul(BigDecimal multiplicand, BigDecimal multiplier) {
    return DECIMAL_SUPPORT.mul(multiplicand, multiplier);
  }

  public static BigDecimal div(BigDecimal dividend, BigDecimal divisor) {
    return DECIMAL_SUPPORT.div(dividend, divisor);
  }

  public static BigDecimal min(BigDecimal... d) {
    return DECIMAL_SUPPORT.min(d);
  }

  public static BigDecimal sqrt(BigDecimal in, int scale) {
    return DECIMAL_SUPPORT.sqrt(in, scale);
  }

  public static BigDecimal max(BigDecimal... d) {
    return DECIMAL_SUPPORT.max(d);
  }

  public static BigDecimal abs(BigDecimal a) {
    return DECIMAL_SUPPORT.abs(a);
  }

  public static boolean eq(BigDecimal a, BigDecimal b) {
    return DECIMAL_SUPPORT.eq(a, b);
  }

  public static boolean lt(BigDecimal a, BigDecimal b) {
    return DECIMAL_SUPPORT.lt(a, b);
  }

  public static boolean gt(BigDecimal a, BigDecimal b) {
    return DECIMAL_SUPPORT.gt(a, b);
  }

  public static boolean le(BigDecimal a, BigDecimal b) {
    return DECIMAL_SUPPORT.le(a, b);
  }

  public static boolean ge(BigDecimal a, BigDecimal b) {
    return DECIMAL_SUPPORT.ge(a, b);
  }

  public static BigDecimal round(Number value, int precision) {
    return DECIMAL_SUPPORT.round(value, precision);
  }

  /**
   * Provides default {@link DecimalSupport} for static context of this class.
   */
  public static class DefaultDecimalSupportProvider {

    public DecimalSupport get() {
      return new DecimalSupport(10, new MathContext(30, RoundingMode.HALF_UP));
    }
  }

  public static class DefaultDecimalSupportProviderProperty extends AbstractClassConfigProperty<DefaultDecimalSupportProvider> {

    private static final String KEY = "scout.util.defaultDecimalSupportProvider";

    @Override
    public String getKey() {
      return KEY;
    }

    @Override
    public String description() {
      return String.format("Specifies the default DefaultDecimalSupportProvider to use. By default the '%s' is used.", getDefaultValue().getSimpleName());
    }

    @Override
    public Class<? extends DefaultDecimalSupportProvider> getDefaultValue() {
      return DefaultDecimalSupportProvider.class;
    }
  }
}

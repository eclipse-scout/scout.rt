/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.extension.ui.form.fields.bigdecimalfield.IBigDecimalFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.AbstractDecimalField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("68508a2e-690c-46e2-aa78-062e1504c0ac")
public abstract class AbstractBigDecimalField extends AbstractDecimalField<BigDecimal> implements IBigDecimalField {
  private static final BigDecimal DEFAULT_MIN_VALUE = new BigDecimal("-999999999999999999999999999999999999999999999999999999999999");
  private static final BigDecimal DEFAULT_MAX_VALUE = new BigDecimal("999999999999999999999999999999999999999999999999999999999999");

  public AbstractBigDecimalField() {
    this(true);
  }

  public AbstractBigDecimalField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  @ConfigProperty(ConfigProperty.BIG_DECIMAL)
  @Order(300)
  protected BigDecimal getConfiguredMinValue() {
    return DEFAULT_MIN_VALUE;
  }

  @Override
  @ConfigProperty(ConfigProperty.BIG_DECIMAL)
  @Order(310)
  protected BigDecimal getConfiguredMaxValue() {
    return DEFAULT_MAX_VALUE;
  }

  @Override
  protected BigDecimal getMinPossibleValue() {
    return DEFAULT_MIN_VALUE;
  }

  @Override
  protected BigDecimal getMaxPossibleValue() {
    return DEFAULT_MAX_VALUE;
  }

  /**
   * uses {@link #parseToBigDecimalInternal(String)} to parse text
   */
  @Override
  protected BigDecimal parseValueInternal(String text) {
    return parseToBigDecimalInternal(text);
  }

  protected static class LocalBigDecimalFieldExtension<OWNER extends AbstractBigDecimalField> extends LocalDecimalFieldExtension<BigDecimal, OWNER> implements IBigDecimalFieldExtension<OWNER> {

    public LocalBigDecimalFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IBigDecimalFieldExtension<? extends AbstractBigDecimalField> createLocalExtension() {
    return new LocalBigDecimalFieldExtension<>(this);
  }
}

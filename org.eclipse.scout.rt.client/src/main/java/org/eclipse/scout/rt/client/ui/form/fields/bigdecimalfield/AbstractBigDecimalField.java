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
    return AbstractBigDecimalField.DEFAULT_MIN_VALUE;
  }

  @Override
  @ConfigProperty(ConfigProperty.BIG_DECIMAL)
  @Order(310)
  protected BigDecimal getConfiguredMaxValue() {
    return AbstractBigDecimalField.DEFAULT_MAX_VALUE;
  }

  @Override
  protected BigDecimal getMinPossibleValue() {
    return AbstractBigDecimalField.DEFAULT_MIN_VALUE;
  }

  @Override
  protected BigDecimal getMaxPossibleValue() {
    return AbstractBigDecimalField.DEFAULT_MAX_VALUE;
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
    return new LocalBigDecimalFieldExtension<AbstractBigDecimalField>(this);
  }

}

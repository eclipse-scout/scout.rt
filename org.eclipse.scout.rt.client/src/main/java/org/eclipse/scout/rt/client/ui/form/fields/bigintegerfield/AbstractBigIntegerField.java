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
package org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.scout.rt.client.extension.ui.form.fields.bigintegerfield.IBigIntegerFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("4c09f9f0-84fe-4c6c-95dd-1f51e92058d8")
public abstract class AbstractBigIntegerField extends AbstractNumberField<BigInteger> implements IBigIntegerField {
  private static final BigInteger DEFAULT_MIN_VALUE = new BigInteger("-999999999999999999999999999999999999999999999999999999999999");
  private static final BigInteger DEFAULT_MAX_VALUE = new BigInteger("999999999999999999999999999999999999999999999999999999999999");

  public AbstractBigIntegerField() {
    this(true);
  }

  public AbstractBigIntegerField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BIG_INTEGER)
  @Order(250)
  @Override
  protected BigInteger getConfiguredMinValue() {
    return AbstractBigIntegerField.DEFAULT_MIN_VALUE;
  }

  @Override
  @ConfigProperty(ConfigProperty.BIG_INTEGER)
  @Order(260)
  protected BigInteger getConfiguredMaxValue() {
    return AbstractBigIntegerField.DEFAULT_MAX_VALUE;
  }

  @Override
  protected BigInteger getMinPossibleValue() {
    return AbstractBigIntegerField.DEFAULT_MIN_VALUE;
  }

  @Override
  protected BigInteger getMaxPossibleValue() {
    return AbstractBigIntegerField.DEFAULT_MAX_VALUE;
  }

  /**
   * uses {@link #parseToBigDecimalInternal(String)} to parse text and returns the result as BigInteger
   */
  @Override
  protected BigInteger parseValueInternal(String text) {
    BigInteger retVal = null;
    BigDecimal parsedVal = parseToBigDecimalInternal(text);
    if (parsedVal != null) {
      retVal = parsedVal.toBigIntegerExact();
    }
    return retVal;
  }

  protected static class LocalBigIntegerFieldExtension<OWNER extends AbstractBigIntegerField> extends LocalNumberFieldExtension<BigInteger, OWNER> implements IBigIntegerFieldExtension<OWNER> {

    public LocalBigIntegerFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IBigIntegerFieldExtension<? extends AbstractBigIntegerField> createLocalExtension() {
    return new LocalBigIntegerFieldExtension<AbstractBigIntegerField>(this);
  }

}

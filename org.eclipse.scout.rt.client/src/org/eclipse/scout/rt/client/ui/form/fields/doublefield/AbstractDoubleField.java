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
package org.eclipse.scout.rt.client.ui.form.fields.doublefield;

import java.math.BigDecimal;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.AbstractDecimalField;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;

@ClassId("65251930-63db-4da4-ae28-9a25d75dcafb")
public abstract class AbstractDoubleField extends AbstractDecimalField<Double> implements IDoubleField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDoubleField.class);

  public AbstractDoubleField() {
    this(true);
  }

  public AbstractDoubleField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  /**
   * @deprecated Will be removed with scout 3.11, use {@link #getConfiguredMinValue()}.<br>
   *             As long as this deprecated version is overridden in subclasses. This setting wins over
   *             {@link #getConfiguredMinValue()} in {@link #initConfig()}.
   */
  @Deprecated
  protected Double getConfiguredMinimumValue() {
    return getConfiguredMinValue();
  }

  /**
   * @deprecated Will be removed with scout 3.11, use {@link #getConfiguredMaxValue()}.<br>
   *             As long as this deprecated version is overridden in subclasses. This setting wins over
   *             {@link #getConfiguredMaxValue()} in {@link #initConfig()}.
   */
  @Deprecated
  protected Double getConfiguredMaximumValue() {
    return getConfiguredMaxValue();
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(300)
  @ValidationRule(ValidationRule.MIN_VALUE)
  @Override
  protected Double getConfiguredMinValue() {
    return -Double.MAX_VALUE;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(310)
  @ValidationRule(ValidationRule.MAX_VALUE)
  @Override
  protected Double getConfiguredMaxValue() {
    return Double.MAX_VALUE;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMinValue(getConfiguredMinimumValue());
    setMaxValue(getConfiguredMaximumValue());
  }

  /**
   * Set the minimum value for this field. If value is <code>null</code>, it is replaced by Double.MIN_VALUE.
   */
  @Override
  public void setMinValue(Double value) {
    super.setMinValue(value == null ? -Double.MAX_VALUE : value);
  }

  /**
   * Set the maximum value for this field. If value is <code>null</code>, it is replaced by -Double.MAX_VALUE.
   */
  @Override
  public void setMaxValue(Double value) {
    super.setMaxValue(value == null ? Double.MAX_VALUE : value);
  }

  /**
   * uses {@link #parseToBigDecimalInternal(String)} to parse text and returns the result as Double
   */
  @Override
  protected Double parseValueInternal(String text) throws ProcessingException {
    Double retVal = null;
    BigDecimal parsedVal = parseToBigDecimalInternal(text);
    if (parsedVal != null) {
      retVal = Double.valueOf(parsedVal.doubleValue());
    }
    return retVal;
  }
}

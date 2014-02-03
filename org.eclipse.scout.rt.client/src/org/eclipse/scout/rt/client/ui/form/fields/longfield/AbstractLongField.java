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
package org.eclipse.scout.rt.client.ui.form.fields.longfield;

import java.math.BigDecimal;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;

@ClassId("cfc961a1-195f-491d-94c5-762f9d86efee")
public abstract class AbstractLongField extends AbstractNumberField<Long> implements ILongField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractLongField.class);

  public AbstractLongField() {
    this(true);
  }

  public AbstractLongField(boolean callInitializer) {
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
  protected Long getConfiguredMinimumValue() {
    return getConfiguredMinValue();
  }

  /**
   * @deprecated Will be removed with scout 3.11, use {@link #getConfiguredMaxValue()}.<br>
   *             As long as this deprecated version is overridden in subclasses. This setting wins over
   *             {@link #getConfiguredMaxValue()} in {@link #initConfig()}.
   */
  @Deprecated
  protected Long getConfiguredMaximumValue() {
    return getConfiguredMaxValue();
  }

  @Override
  @ConfigProperty(ConfigProperty.LONG)
  @Order(250)
  @ValidationRule(ValidationRule.MIN_VALUE)
  protected Long getConfiguredMinValue() {
    return Long.MIN_VALUE;
  }

  @Override
  @ConfigProperty(ConfigProperty.LONG)
  @Order(260)
  @ValidationRule(ValidationRule.MAX_VALUE)
  protected Long getConfiguredMaxValue() {
    return Long.MAX_VALUE;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMinValue(getConfiguredMinimumValue());
    setMaxValue(getConfiguredMaximumValue());
  }

  /**
   * Set the minimum value for this field. If value is <code>null</code>, it is replaced by Long.MIN_VALUE.
   */
  @Override
  public void setMinValue(Long value) {
    super.setMinValue(value == null ? Long.MIN_VALUE : value);
  }

  /**
   * Set the maximum value for this field. If value is <code>null</code>, it is replaced by Long.MAX_VALUE.
   */
  @Override
  public void setMaxValue(Long value) {
    super.setMaxValue(value == null ? Long.MAX_VALUE : value);
  }

  /**
   * uses {@link #parseToBigDecimalInternal(String)} to parse text and returns the result as Long
   */
  @Override
  protected Long parseValueInternal(String text) throws ProcessingException {
    Long retVal = null;
    BigDecimal parsedVal = parseToBigDecimalInternal(text);
    if (parsedVal != null) {
      retVal = Long.valueOf(parsedVal.longValueExact());
    }
    return retVal;
  }

}

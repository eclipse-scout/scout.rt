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
package org.eclipse.scout.rt.client.ui.form.fields.integerfield;

import java.math.BigDecimal;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;

@ClassId("4418100d-db7c-40e7-84b4-29df65534671")
public abstract class AbstractIntegerField extends AbstractNumberField<Integer> implements IIntegerField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractIntegerField.class);

  public AbstractIntegerField() {
    this(true);
  }

  public AbstractIntegerField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  /**
   * @deprecated Will be removed with scout 5.0, use {@link #getConfiguredMinValue()}.<br>
   *             As long as this deprecated version is overridden in subclasses. This setting wins over
   *             {@link #getConfiguredMinValue()} in {@link #initConfig()}.
   */
  @Deprecated
  protected Integer getConfiguredMinimumValue() {
    return getConfiguredMinValue();
  }

  /**
   * @deprecated Will be removed with scout 5.0, use {@link #getConfiguredMaxValue()}.<br>
   *             As long as this deprecated version is overridden in subclasses. This setting wins over
   *             {@link #getConfiguredMaxValue()} in {@link #initConfig()}.
   */
  @Deprecated
  protected Integer getConfiguredMaximumValue() {
    return getConfiguredMaxValue();
  }

  @Override
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(250)
  @ValidationRule(ValidationRule.MIN_VALUE)
  protected Integer getConfiguredMinValue() {
    return Integer.MIN_VALUE;
  }

  @Override
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(260)
  @ValidationRule(ValidationRule.MAX_VALUE)
  protected Integer getConfiguredMaxValue() {
    return Integer.MAX_VALUE;
  }

  @Override
  @Order(270)
  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMaxIntegerDigits() {
    return 10;
  }

  @Override
  protected Integer getMinPossibleValue() {
    return Integer.MIN_VALUE;
  }

  @Override
  protected Integer getMaxPossibleValue() {
    return Integer.MAX_VALUE;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMinValue(getConfiguredMinimumValue());
    setMaxValue(getConfiguredMaximumValue());
  }

  /**
   * uses {@link #parseToBigDecimalInternal(String)} to parse text and returns the result as Integer
   */
  @Override
  protected Integer parseValueInternal(String text) throws ProcessingException {
    Integer retVal = null;
    BigDecimal parsedVal = parseToBigDecimalInternal(text);
    if (parsedVal != null) {
      retVal = Integer.valueOf(parsedVal.intValueExact());
    }
    return retVal;
  }

}

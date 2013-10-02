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

import java.math.BigInteger;
import java.text.ParsePosition;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;

public abstract class AbstractBigIntegerField extends AbstractNumberField<BigInteger> implements IBigIntegerField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBigIntegerField.class);

  public AbstractBigIntegerField() {
    this(true);
  }

  public AbstractBigIntegerField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(250)
  @ValidationRule(ValidationRule.MIN_VALUE)
  protected Long getConfiguredMinValue() {
    return null;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(260)
  @ValidationRule(ValidationRule.MAX_VALUE)
  protected Long getConfiguredMaxValue() {
    return null;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMinValue(getConfiguredMinValue() != null ? BigInteger.valueOf(getConfiguredMinValue()) : null);
    setMaxValue(getConfiguredMaxValue() != null ? BigInteger.valueOf(getConfiguredMaxValue()) : null);
  }

  @Override
  protected BigInteger parseValueInternal(String text) throws ProcessingException {
    BigInteger retVal = null;
    if (text == null) {
      text = "";
    }
    else {
      text = text.trim();
    }
    if (text.length() > 0) {
      ParsePosition p = new ParsePosition(0);
      Number n = createNumberFormat().parse(text, p);
      if (p.getErrorIndex() >= 0 || p.getIndex() != text.length()) {
        throw new ProcessingException(ScoutTexts.get("InvalidNumberMessageX", text));
      }
      retVal = new BigInteger("" + n.toString());
    }
    return retVal;
  }

}

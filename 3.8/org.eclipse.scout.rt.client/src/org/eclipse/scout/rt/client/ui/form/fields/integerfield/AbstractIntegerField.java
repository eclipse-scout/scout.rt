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

import java.text.ParsePosition;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;

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
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(250)
  @ConfigPropertyValue("null")
  @ValidationRule(ValidationRule.MIN_VALUE)
  protected Integer getConfiguredMinimumValue() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(260)
  @ConfigPropertyValue("null")
  @ValidationRule(ValidationRule.MAX_VALUE)
  protected Integer getConfiguredMaximumValue() {
    return null;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMinValue(getConfiguredMinimumValue());
    setMaxValue(getConfiguredMaximumValue());
  }

  // convert string to a real int
  @Override
  protected Integer parseValueInternal(String text) throws ProcessingException {
    Integer retVal = null;
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
      retVal = new Integer(n.intValue());
    }
    return retVal;
  }

}

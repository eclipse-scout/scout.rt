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
package org.eclipse.scout.rt.client.ui.form.fields.booleanfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractBooleanField extends AbstractValueField<Boolean> implements IBooleanField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBooleanField.class);

  private IBooleanFieldUIFacade m_uiFacade;

  public AbstractBooleanField() {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    // ticket 79554
    /* setAutoDisplayText(false); */
    propertySupport.setProperty(PROP_VALUE, false);
    // ticket 79554
    propertySupport.setProperty(PROP_DISPLAY_TEXT, execFormatValue(getValue()));
  }

  @Override
  public void setChecked(boolean b) {
    setValue(b);
  }

  @Override
  public boolean isChecked() {
    return getValue() != null && getValue().booleanValue();
  }

  // format value for display
  @Override
  protected String formatValueInternal(Boolean validValue) {
    if (validValue == null) {
      return "";
    }
    // ticket 79554
    return validValue ? ScoutTexts.get("Yes") : ScoutTexts.get("No");
  }

  // validate value for ranges
  @Override
  protected Boolean validateValueInternal(Boolean rawValue) throws ProcessingException {
    Boolean validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    validValue = rawValue;
    if (validValue == null) {
      validValue = false;
    }
    return validValue;
  }

  // convert string to a boolean
  @Override
  protected Boolean parseValueInternal(String text) throws ProcessingException {
    Boolean retVal = null;
    if (text != null && text.length() == 0) {
      text = null;
    }
    if (text != null) {
      if (text.equals("1")) {
        retVal = true;
      }
      else if (text.equalsIgnoreCase("true")) {
        retVal = true;
      }
      else {
        retVal = false;
      }
    }
    return retVal;
  }

  @Override
  public IBooleanFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private class P_UIFacade implements IBooleanFieldUIFacade {
    @Override
    public void setSelectedFromUI(boolean checked) {
      if (isEnabled() && isVisible()) {
        setChecked(checked);
      }
    }

  }

}

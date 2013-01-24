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
package org.eclipse.scout.rt.client.ui.form.fields.labelfield;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;

public abstract class AbstractLabelField extends AbstractValueField<String> implements ILabelField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractLabelField.class);

  private ILabelFieldUIFacade m_uiFacade;

  public AbstractLabelField() {
    this(true);
  }

  public AbstractLabelField(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredWrapText() {
    return false;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setWrapText(getConfiguredWrapText());
  }

  @Override
  protected String validateValueInternal(String rawValue) throws ProcessingException {
    String validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    validValue = rawValue;
    if (validValue != null && validValue.length() == 0) {
      validValue = null;
    }
    return validValue;
  }

  @Override
  public void setWrapText(boolean b) {
    propertySupport.setPropertyBool(PROP_WRAP_TEXT, b);
  }

  @Override
  public boolean isWrapText() {
    return propertySupport.getPropertyBool(PROP_WRAP_TEXT);
  }

  @Override
  public ILabelFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  // convert string to a real string
  @Override
  protected String parseValueInternal(String text) throws ProcessingException {
    if (text != null && text.length() == 0) {
      text = null;
    }
    return text;
  }

  private class P_UIFacade implements ILabelFieldUIFacade {
  }

}

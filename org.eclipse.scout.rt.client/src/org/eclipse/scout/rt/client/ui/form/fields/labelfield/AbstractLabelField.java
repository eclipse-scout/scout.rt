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

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;

@ClassId("7e531d93-ad27-4316-9529-7766059b3886")
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
  protected boolean getConfiguredWrapText() {
    return false;
  }

  /**
   * Defines if the label should be selectable or not. Default is <code>true</code>
   * 
   * @since 3.10.0-M6
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(260)
  protected boolean getConfiguredSelectable() {
    return true;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setWrapText(getConfiguredWrapText());
    setSelectable(getConfiguredSelectable());
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
  public void setSelectable(boolean b) {
    propertySupport.setPropertyBool(PROP_SELECTABLE, b);
  }

  @Override
  public boolean isSelectable() {
    return propertySupport.getPropertyBool(PROP_SELECTABLE);
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

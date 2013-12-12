/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;

/**
 * Implementation for {@link IBasicField}. Handle properties and event methods that deals with basic fields.
 * 
 * @since 3.10.0-M3
 */
public abstract class AbstractBasicField<T> extends AbstractValueField<T> implements IBasicField<T> {

  private boolean m_whileTyping;

  protected AbstractBasicField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setValidateOnAnyKey(getConfiguredValidateOnAnyKey());
  }

  /**
   * Causes the ui to send a validate event every time the input field content is changed.
   * <p>
   * Be careful when using this property since this can influence performance and the characteristics of text input.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(310)
  protected boolean getConfiguredValidateOnAnyKey() {
    return false;
  }

  @Override
  protected boolean shouldUpdateDisplayText(boolean validValueDiffersFromRawValue) {
    return !(isWhileTyping() && !validValueDiffersFromRawValue) && super.shouldUpdateDisplayText(validValueDiffersFromRawValue);
  }

  @Override
  public boolean isValidateOnAnyKey() {
    return propertySupport.getPropertyBool(PROP_VALIDATE_ON_ANY_KEY);
  }

  @Override
  public void setValidateOnAnyKey(boolean b) {
    propertySupport.setPropertyBool(PROP_VALIDATE_ON_ANY_KEY, b);
  }

  /**
   * If {@link #isValidateOnAnyKey()} is true, the {@link #execParseValue(String)}, {@link #execValidateValue(Object)}
   * and {@link #execFormatValue(Object)} will be called after each modification of the text in the field. This flag
   * tells if the user is typing text or not.
   * 
   * @return true to indicate if the user is typing text or
   *         false if the method is called on focus lost.
   */
  protected boolean isWhileTyping() {
    return m_whileTyping;
  }

  protected void setWhileTyping(boolean whileTyping) {
    m_whileTyping = whileTyping;
  }
}

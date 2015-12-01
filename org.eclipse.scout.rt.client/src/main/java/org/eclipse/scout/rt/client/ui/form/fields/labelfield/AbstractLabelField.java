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

import org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield.ILabelFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("7e531d93-ad27-4316-9529-7766059b3886")
public abstract class AbstractLabelField extends AbstractValueField<String> implements ILabelField {
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

  /**
   * Configures, if HTML rendering is enabled for this field.
   * <p>
   * Subclasses can override this method. Default is {@code false}. Make sure that any user input (or other insecure
   * input) is encoded (security), if this property is enabled.
   *
   * @return {@code true}, if HTML rendering is enabled for this field.{@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredHtmlEnabled() {
    return false;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setWrapText(getConfiguredWrapText());
    setSelectable(getConfiguredSelectable());
    setHtmlEnabled(getConfiguredHtmlEnabled());
  }

  @Override
  protected String validateValueInternal(String rawValue) {
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
  public void setHtmlEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_HTML_ENABLED, enabled);
  }

  @Override
  public boolean isHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_HTML_ENABLED);
  }

  // convert string to a real string
  @Override
  protected String parseValueInternal(String text) {
    if (text != null && text.length() == 0) {
      text = null;
    }
    return text;
  }

  protected static class LocalLabelFieldExtension<OWNER extends AbstractLabelField> extends LocalValueFieldExtension<String, OWNER> implements ILabelFieldExtension<OWNER> {

    public LocalLabelFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ILabelFieldExtension<? extends AbstractLabelField> createLocalExtension() {
    return new LocalLabelFieldExtension<AbstractLabelField>(this);
  }

}

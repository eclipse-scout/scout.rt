/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.labelfield;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield.ILabelFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield.LabelFieldChains.LabelFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("7e531d93-ad27-4316-9529-7766059b3886")
public abstract class AbstractLabelField extends AbstractValueField<String> implements ILabelField {

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
  protected boolean getConfiguredPreventInitialFocus() {
    // Selectable label fields can receive the focus, but this should only happen when the user selects text
    return true;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
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
    if (validValue != null && validValue.isEmpty()) {
      validValue = null;
    }
    return validValue;
  }

  @Override
  public ILabelFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setWrapText(boolean wrapText) {
    propertySupport.setPropertyBool(PROP_WRAP_TEXT, wrapText);
  }

  @Override
  public boolean isWrapText() {
    return propertySupport.getPropertyBool(PROP_WRAP_TEXT);
  }

  @Override
  public void setSelectable(boolean selectable) {
    propertySupport.setPropertyBool(PROP_SELECTABLE, selectable);
  }

  @Override
  public boolean isSelectable() {
    return propertySupport.getPropertyBool(PROP_SELECTABLE);
  }

  @Override
  public void setHtmlEnabled(boolean htmlEnabled) {
    propertySupport.setPropertyBool(PROP_HTML_ENABLED, htmlEnabled);
  }

  @Override
  public boolean isHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_HTML_ENABLED);
  }

  // convert string to a real string
  @Override
  protected String parseValueInternal(String text) {
    if (text != null && text.isEmpty()) {
      text = null;
    }
    return text;
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(100)
  protected void execAppLinkAction(String ref) {
  }

  @Override
  public void doAppLinkAction(String ref) {
    interceptAppLinkAction(ref);
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    LabelFieldAppLinkActionChain chain = new LabelFieldAppLinkActionChain(extensions);
    chain.execAppLinkAction(ref);
  }

  protected class P_UIFacade implements ILabelFieldUIFacade {

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      doAppLinkAction(ref);
    }
  }

  protected static class LocalLabelFieldExtension<OWNER extends AbstractLabelField> extends LocalValueFieldExtension<String, OWNER> implements ILabelFieldExtension<OWNER> {

    public LocalLabelFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execAppLinkAction(LabelFieldAppLinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }
  }

  @Override
  protected ILabelFieldExtension<? extends AbstractLabelField> createLocalExtension() {
    return new LocalLabelFieldExtension<>(this);
  }
}

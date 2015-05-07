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

import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.ScoutSdkIgnore;
import org.eclipse.scout.rt.client.extension.ui.form.fields.BasicFieldChains;
import org.eclipse.scout.rt.client.extension.ui.form.fields.BasicFieldChains.BasicFieldExecChangedDisplayTextChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IBasicFieldExtension;

/**
 * Implementation for {@link IBasicField}. Handle properties and event methods that deals with basic fields.
 *
 * @since 3.10.0-M3
 */
@ScoutSdkIgnore
@ClassId("d5a72dd8-cb1c-4dea-a568-90d77e65854e")
public abstract class AbstractBasicField<VALUE> extends AbstractValueField<VALUE> implements IBasicField<VALUE> {

  private boolean m_whileTyping;

  protected abstract class P_UIFacade implements IBasicFieldUIFacade {
    @Override
    public void setDisplayTextFromUI(String text) {
      setDisplayText(text);
    }
  }

  protected AbstractBasicField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected IBasicFieldExtension<VALUE, ? extends AbstractBasicField<VALUE>> createLocalExtension() {
    return new LocalBasicFieldExtension<VALUE, AbstractBasicField<VALUE>>(this);
  }

  /**
   * After the property {@link IValueField#PROP_DISPLAY_TEXT} changed.
   * <p>
   * Per default this happens when a new valid value is set. If {@link IBasicField#PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY}
   * is <code>true</code> this method is called after every modification in the UI.
   */
  @ConfigOperation
  @Order(225)
  protected void execChangedDisplayText() {
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setValidateOnAnyKey(getConfiguredValidateOnAnyKey());
    setUpdateDisplayTextOnModify(getConfiguredUpdateDisplayTextOnModify());
  }

  @Override
  public void setDisplayText(String s) {
    String oldDisplayText = getDisplayText();
    super.setDisplayText(s);
    if (CompareUtility.notEquals(oldDisplayText, s)) {
      interceptExecChangedDisplayText();
    }
  }

  /**
   * Causes the ui to send a validate event every time the input field content is changed.
   * <p>
   * Be careful when using this property since this can influence performance and the characteristics of text input.
   *
   * @deprecated use {@link AbstractBasicField#getConfiguredUpdateDisplayTextOnModify()} and
   *             {@link AbstractBasicField#execChangedDisplayText()} instead; will be removed in 5.1.0;
   */
  @Deprecated
  @Order(310)
  protected boolean getConfiguredValidateOnAnyKey() {
    return false;
  }

  /**
   * Indicates whether the property {@link IValueField#PROP_DISPLAY_TEXT} should be updated when the display text in the
   * UI changes.
   * <p>
   * By default the property {@link IValueField#PROP_DISPLAY_TEXT} is set from within the model and then propagated to
   * the UI. (e.g. after a value was successfully set) While the user is editing the field, before the new value is
   * actually validated and set, the model does not get notified about changes of the display text in the UI.
   * <p>
   * When set to <code>true</code> the property {@link IValueField#PROP_DISPLAY_TEXT} is kept in sync with the UI while
   * the user is editing the field. Usually this is used in combination with
   * {@link AbstractBasicField#execChangedDisplayText()}.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(315)
  protected boolean getConfiguredUpdateDisplayTextOnModify() {
    return false;
  }

  @Override
  protected boolean shouldUpdateDisplayText(boolean validValueDiffersFromRawValue) {
    return !(isWhileTyping() && !validValueDiffersFromRawValue) && super.shouldUpdateDisplayText(validValueDiffersFromRawValue);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isValidateOnAnyKey() {
    return propertySupport.getPropertyBool(PROP_VALIDATE_ON_ANY_KEY);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setValidateOnAnyKey(boolean b) {
    propertySupport.setPropertyBool(PROP_VALIDATE_ON_ANY_KEY, b);
  }

  @Override
  public void setUpdateDisplayTextOnModify(boolean b) {
    propertySupport.setPropertyBool(PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY, b);
  }

  @Override
  public boolean isUpdateDisplayTextOnModify() {
    return propertySupport.getPropertyBool(PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY);
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

  protected final void interceptExecChangedDisplayText() {
    @SuppressWarnings("unchecked")
    List<? extends IBasicFieldExtension<VALUE, ? extends AbstractBasicField<VALUE>>> extensions = (List<? extends IBasicFieldExtension<VALUE, ? extends AbstractBasicField<VALUE>>>) getAllExtensions();
    new BasicFieldChains.BasicFieldExecChangedDisplayTextChain<VALUE>(extensions).execChangedDisplayText();
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalBasicFieldExtension<VALUE_TYPE, OWNER_FIELD extends AbstractBasicField<VALUE_TYPE>> extends AbstractValueField.LocalValueFieldExtension<VALUE_TYPE, OWNER_FIELD>
      implements IBasicFieldExtension<VALUE_TYPE, OWNER_FIELD> {

    public LocalBasicFieldExtension(OWNER_FIELD owner) {
      super(owner);
    }

    @Override
    public void execChangedDisplayText(BasicFieldExecChangedDisplayTextChain<VALUE_TYPE> chain) {
      getOwner().execChangedDisplayText();
    }
  }

}

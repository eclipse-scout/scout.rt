/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.extension.ui.form.fields.BasicFieldChains;
import org.eclipse.scout.rt.client.extension.ui.form.fields.BasicFieldChains.BasicFieldExecChangedDisplayTextChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IBasicFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Implementation for {@link IBasicField}. Handle properties and event methods that deals with basic fields.
 *
 * @since 3.10.0-M3
 */
@ClassId("d5a72dd8-cb1c-4dea-a568-90d77e65854e")
public abstract class AbstractBasicField<VALUE> extends AbstractValueField<VALUE> implements IBasicField<VALUE> {

  public class P_UIFacade implements IBasicFieldUIFacade {

    @Override
    public void setDisplayTextFromUI(String text) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      setDisplayText(text);
    }

    @Override
    public void parseAndSetValueFromUI(String value) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      if (value == null) {
        value = "";
      }
      // parse always, validity might change even if text is same
      parseAndSetValue(value);
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
    setUpdateDisplayTextOnModify(getConfiguredUpdateDisplayTextOnModify());
  }

  @Override
  public void setDisplayText(String s) {
    String oldDisplayText = getDisplayText();
    super.setDisplayText(s);
    if (ObjectUtility.notEquals(oldDisplayText, s)) {
      interceptExecChangedDisplayText();
    }
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
  public void setUpdateDisplayTextOnModify(boolean b) {
    propertySupport.setPropertyBool(PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY, b);
  }

  @Override
  public boolean isUpdateDisplayTextOnModify() {
    return propertySupport.getPropertyBool(PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY);
  }

  protected final void interceptExecChangedDisplayText() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
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

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
package org.eclipse.scout.rt.client.ui.form.fields.booleanfield;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.booleanfield.IBooleanFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.ScoutTexts;

@ClassId("3f14b55f-b49b-428a-92c4-05745d6d48c4")
public abstract class AbstractBooleanField extends AbstractValueField<Boolean> implements IBooleanField {
  private IBooleanFieldUIFacade m_uiFacade;

  public AbstractBooleanField() {
    this(true);
  }

  public AbstractBooleanField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setTristateEnabled(getConfiguredTristateEnabled());
    propertySupport.setProperty(PROP_VALUE, false);
    // ticket 79554
    propertySupport.setProperty(PROP_DISPLAY_TEXT, interceptFormatValue(getValue()));
  }

  @Override
  @Order(210)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredAutoAddDefaultMenus() {
    return false;
  }

  /**
   * true: the checkbox can have a {@link #getValue()} of true, false and also null. null is the tristate and is
   * typically displayed using a filled rectangluar area.
   * <p>
   * false: the checkbox can have a {@link #getValue()} of true, false. The value is never null.
   * <p>
   * default is false
   *
   * @since 6.1
   * @return true if this checkbox supports the so-called tristate and can be {@link #setValue(Boolean)} to null in
   *         order to represent the tristate value
   */
  @Order(220)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredTristateEnabled() {
    return false;
  }

  @Override
  public void setTristateEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_TRISTATE_ENABLED, b);
  }

  @Override
  public boolean isTristateEnabled() {
    return propertySupport.getPropertyBool(PROP_TRISTATE_ENABLED);
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

  @Override
  protected Boolean validateValueInternal(Boolean rawValue) {
    rawValue = super.validateValueInternal(rawValue);
    if (!isTristateEnabled() && rawValue == null) {
      rawValue = Boolean.FALSE;
    }
    return rawValue;
  }

  // convert string to a boolean
  @Override
  protected Boolean parseValueInternal(String text) {
    Boolean retVal = null;
    if (text != null && text.length() == 0) {
      text = null;
    }
    if (text != null) {
      if ("1".equals(text)) {
        retVal = true;
      }
      else if ("true".equalsIgnoreCase(text)) {
        retVal = true;
      }
      else {
        retVal = false;
      }
    }
    return retVal;
  }

  /**
   * A boolean field is considered empty if unchecked.
   */
  @Override
  protected boolean execIsEmpty() {
    return !isChecked();
  }

  @Override
  public IBooleanFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements IBooleanFieldUIFacade {
    @Override
    public void setValueFromUI(Boolean value) {
      if (isEnabled() && isVisible()) {
        setValue(value);
      }
    }
  }

  protected static class LocalBooleanFieldExtension<OWNER extends AbstractBooleanField> extends LocalValueFieldExtension<Boolean, OWNER> implements IBooleanFieldExtension<OWNER> {

    public LocalBooleanFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IBooleanFieldExtension<? extends AbstractBooleanField> createLocalExtension() {
    return new LocalBooleanFieldExtension<AbstractBooleanField>(this);
  }
}

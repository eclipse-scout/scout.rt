/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

import org.eclipse.scout.rt.client.extension.ui.form.fields.button.IRadioButtonExtension;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Abstract class for a RadioButton.
 */
@ClassId("1221cfac-4636-4d53-8485-07872f956fc1")
public abstract class AbstractRadioButton<T> extends AbstractButton implements IRadioButton<T> {

  public AbstractRadioButton() {
    this(true);
  }

  public AbstractRadioButton(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setRadioValue(getConfiguredRadioValue());
    setWrapText(getConfiguredWrapText());
  }

  /**
   * Configures the value represented by this radio button. This is the value that is returned if you query a radio
   * button group for the current value and this button is the currently selected radio button.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return an {@code Object} representing the value of this radio button
   * @see AbstractRadioButton
   * @see AbstractRadioButtonGroup
   * @since moved to {@link AbstractRadioButton} in 4.0.0-M6
   */
  @Order(230)
  @ConfigProperty(ConfigProperty.OBJECT)
  protected T getConfiguredRadioValue() {
    return null;
  }

  @Order(240)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredWrapText() {
    return false;
  }

  @Override
  protected boolean getConfiguredFillHorizontal() {
    return true;
  }

  @Override
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_RADIO;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getRadioValue() {
    return (T) propertySupport.getProperty(PROP_RADIO_VALUE);
  }

  @Override
  public void setRadioValue(T o) {
    propertySupport.setProperty(PROP_RADIO_VALUE, o);
  }

  @Override
  public void setWrapText(boolean wrapText) {
    propertySupport.setProperty(PROP_WRAP_TEXT, wrapText);
  }

  @Override
  public boolean isWrapText() {
    return propertySupport.getPropertyBool(PROP_WRAP_TEXT);
  }

  protected static class LocalRadioButtonExtension<T, OWNER extends AbstractRadioButton<T>> extends LocalButtonExtension<OWNER> implements IRadioButtonExtension<T, OWNER> {

    public LocalRadioButtonExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IRadioButtonExtension<T, ? extends AbstractRadioButton<T>> createLocalExtension() {
    return new LocalRadioButtonExtension<>(this);
  }
}

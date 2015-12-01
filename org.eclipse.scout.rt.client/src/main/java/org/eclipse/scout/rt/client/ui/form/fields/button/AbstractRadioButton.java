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
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(230)
  protected T getConfiguredRadioValue() {
    return null;
  }

  @Override
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_RADIO;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getRadioValue() {
    return (T) propertySupport.getProperty(PROP_RADIOVALUE);
  }

  @Override
  public void setRadioValue(T o) {
    propertySupport.setProperty(PROP_RADIOVALUE, o);
  }

  protected static class LocalRadioButtonExtension<T, OWNER extends AbstractRadioButton<T>> extends LocalButtonExtension<OWNER> implements IRadioButtonExtension<T, OWNER> {

    public LocalRadioButtonExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IRadioButtonExtension<T, ? extends AbstractRadioButton<T>> createLocalExtension() {
    return new LocalRadioButtonExtension<T, AbstractRadioButton<T>>(this);
  }
}

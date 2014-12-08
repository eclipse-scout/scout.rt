package org.eclipse.scout.rt.client.extension.ui.form.fields.button;

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;

public abstract class AbstractRadioButtonExtension<T, OWNER extends AbstractRadioButton<T>> extends AbstractButtonExtension<OWNER> implements IRadioButtonExtension<T, OWNER> {

  public AbstractRadioButtonExtension(OWNER owner) {
    super(owner);
  }
}

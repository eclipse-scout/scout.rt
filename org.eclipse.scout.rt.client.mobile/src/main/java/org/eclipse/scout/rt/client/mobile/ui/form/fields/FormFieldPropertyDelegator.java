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
package org.eclipse.scout.rt.client.mobile.ui.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class FormFieldPropertyDelegator<SENDER extends IFormField, RECEIVER extends IFormField> extends PropertyDelegator<SENDER, RECEIVER> {

  public FormFieldPropertyDelegator(SENDER sendingField, RECEIVER receivingField) {
    super(sendingField, receivingField);
  }

  @Override
  public void init() {
    getReceiver().setVisible(getSender().isVisible());
    if (!getSender().isVisible()) {
      //Since AbstractFormField#calculateVisibleInternal may ignore this property only set it if it hasn't been ignored (to not override those rules)
      getReceiver().setVisibleGranted(getSender().isVisibleGranted());
    }
    getReceiver().setEnabled(getSender().isEnabled());
    if (!getSender().isEnabled()) {
      //Since AbstractFormField#calculateEnabled may ignore this property only set it if it hasn't been ignored (to not override those rules)
      getReceiver().setEnabledGranted(getSender().isEnabledGranted());
    }
    getReceiver().setLabel(getSender().getLabel());
    getReceiver().setTooltipText(getSender().getTooltipText());
  }

  @Override
  protected void handlePropertyChange(String name, Object newValue) {
    if (name.equals(IFormField.PROP_VISIBLE)) {
      getReceiver().setVisible(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_ENABLED)) {
      getReceiver().setEnabled(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_LABEL)) {
      getReceiver().setLabel(((String) newValue));
    }
    else if (name.equals(IFormField.PROP_TOOLTIP_TEXT)) {
      getReceiver().setTooltipText((String) newValue);
    }
  }

}

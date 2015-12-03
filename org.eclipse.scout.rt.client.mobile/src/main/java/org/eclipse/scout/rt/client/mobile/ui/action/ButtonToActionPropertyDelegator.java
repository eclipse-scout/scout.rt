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
package org.eclipse.scout.rt.client.mobile.ui.action;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.PropertyDelegator;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

public class ButtonToActionPropertyDelegator extends PropertyDelegator<IButton, IAction> {

  public ButtonToActionPropertyDelegator(IButton sender, IAction receiver) {
    super(sender, receiver);
  }

  @Override
  public void init() {
    super.init();

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
    getReceiver().setIconId(getSender().getIconId());
    getReceiver().setText(getSender().getLabel());
    getReceiver().setTooltipText(getSender().getTooltipText());
    getReceiver().setToggleAction(getSender().getDisplayStyle() == IButton.DISPLAY_STYLE_TOGGLE);
    getReceiver().setSelected(getSender().isSelected());
  }

  @Override
  protected void handlePropertyChange(String name, Object newValue) {
    if (name.equals(IFormField.PROP_ENABLED)) {
      getReceiver().setEnabled(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_LABEL)) {
      getReceiver().setText((String) newValue);
    }
    else if (name.equals(IFormField.PROP_TOOLTIP_TEXT)) {
      getReceiver().setTooltipText((String) newValue);
    }
    else if (name.equals(IFormField.PROP_VISIBLE)) {
      getReceiver().setVisible(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IButton.PROP_ICON_ID)) {
      getReceiver().setIconId((String) newValue);
    }
    else if (name.equals(IButton.PROP_SELECTED)) {
      getReceiver().setSelected(((Boolean) newValue).booleanValue());
    }
  }

}

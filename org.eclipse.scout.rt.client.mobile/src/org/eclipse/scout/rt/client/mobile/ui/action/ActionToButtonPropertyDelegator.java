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
package org.eclipse.scout.rt.client.mobile.ui.action;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.PropertyDelegator;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

public class ActionToButtonPropertyDelegator extends PropertyDelegator<IAction, IButton> {

  public ActionToButtonPropertyDelegator(IAction sender, IButton receiver) {
    super(sender, receiver);
  }

  @Override
  public void init() {
    super.init();

    getReceiver().setVisible(getSender().isVisible());
    if (!getSender().isVisible()) {
      getReceiver().setVisibleGranted(getSender().isVisibleGranted());
    }
    getReceiver().setEnabled(getSender().isEnabled());
    if (!getSender().isEnabled()) {
      getReceiver().setEnabledGranted(getSender().isEnabledGranted());
    }
    getReceiver().setIconId(getSender().getIconId());
    getReceiver().setLabel(getSender().getText());
    getReceiver().setTooltipText(getSender().getTooltipText());
    if (getSender().isToggleAction()) {
      getReceiver().setDisplayStyleInternal(IButton.DISPLAY_STYLE_TOGGLE);
    }
    getReceiver().setSelected(getSender().isSelected());
  }

  private void handleButtonPropertyChange(String name, Object newValue) {
    if (name.equals(IAction.PROP_ENABLED)) {
      getReceiver().setEnabled(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IAction.PROP_TEXT)) {
      getReceiver().setLabel((String) newValue);
    }
    else if (name.equals(IAction.PROP_TOOLTIP_TEXT)) {
      getReceiver().setTooltipText((String) newValue);
    }
    else if (name.equals(IAction.PROP_VISIBLE)) {
      getReceiver().setVisible(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IAction.PROP_ICON_ID)) {
      getReceiver().setIconId((String) newValue);
    }
    else if (name.equals(IAction.PROP_SELECTED)) {
      getReceiver().setSelected(((Boolean) newValue).booleanValue());
    }
  }

}

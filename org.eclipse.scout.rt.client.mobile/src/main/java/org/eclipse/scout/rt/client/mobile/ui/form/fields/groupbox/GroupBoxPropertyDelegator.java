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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.groupbox;

import org.eclipse.scout.rt.client.mobile.ui.form.fields.FormFieldPropertyDelegator;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

public class GroupBoxPropertyDelegator extends FormFieldPropertyDelegator<IGroupBox, IGroupBox> {

  public GroupBoxPropertyDelegator(IGroupBox sendingField, IGroupBox receivingField) {
    super(sendingField, receivingField);
  }

  @Override
  public void init() {
    super.init();

    getReceiver().setBorderVisible(getSender().isBorderVisible());
    getReceiver().setBorderDecoration(getSender().getBorderDecoration());
    getReceiver().setExpanded(getSender().isExpanded());
    getReceiver().setBackgroundImageName(getSender().getBackgroundImageName());
    getReceiver().setBackgroundImageHorizontalAlignment(getSender().getBackgroundImageHorizontalAlignment());
    getReceiver().setBackgroundImageVerticalAlignment(getSender().getBackgroundImageVerticalAlignment());
  }

  @Override
  protected void handlePropertyChange(String name, Object newValue) {
    super.handlePropertyChange(name, newValue);

    if (name.equals(IGroupBox.PROP_BORDER_VISIBLE)) {
      getReceiver().setBorderVisible(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IGroupBox.PROP_BORDER_DECORATION)) {
      getReceiver().setBorderDecoration(((String) newValue));
    }
    else if (name.equals(IGroupBox.PROP_EXPANDED)) {
      getReceiver().setExpanded(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_NAME)) {
      getReceiver().setBackgroundImageName((String) newValue);
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT)) {
      getReceiver().setBackgroundImageHorizontalAlignment((Integer) newValue);
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT)) {
      getReceiver().setBackgroundImageVerticalAlignment((Integer) newValue);
    }
  }
}

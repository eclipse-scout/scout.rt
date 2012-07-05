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

    getReceivingFormField().setBorderVisible(getSendingFormField().isBorderVisible());
    getReceivingFormField().setBorderDecoration(getSendingFormField().getBorderDecoration());
    getReceivingFormField().setExpanded(getSendingFormField().isExpanded());
    getReceivingFormField().setBackgroundImageName(getSendingFormField().getBackgroundImageName());
    getReceivingFormField().setBackgroundImageHorizontalAlignment(getSendingFormField().getBackgroundImageHorizontalAlignment());
    getReceivingFormField().setBackgroundImageVerticalAlignment(getSendingFormField().getBackgroundImageVerticalAlignment());
  }

  @Override
  protected void handlePropertyChange(String name, Object newValue) {
    super.handlePropertyChange(name, newValue);

    if (name.equals(IGroupBox.PROP_BORDER_VISIBLE)) {
      getReceivingFormField().setBorderVisible(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IGroupBox.PROP_BORDER_DECORATION)) {
      getReceivingFormField().setBorderDecoration(((String) newValue));
    }
    else if (name.equals(IGroupBox.PROP_EXPANDED)) {
      getReceivingFormField().setExpanded(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_NAME)) {
      getReceivingFormField().setBackgroundImageName((String) newValue);
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT)) {
      getReceivingFormField().setBackgroundImageHorizontalAlignment((Integer) newValue);
    }
    else if (name.equals(IGroupBox.PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT)) {
      getReceivingFormField().setBackgroundImageVerticalAlignment((Integer) newValue);
    }
  }
}

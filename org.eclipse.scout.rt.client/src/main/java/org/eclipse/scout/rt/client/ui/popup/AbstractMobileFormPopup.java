/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.popup;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("07574e67-a74e-4155-9f0f-9b8dadaf0af5")
public abstract class AbstractMobileFormPopup<T extends IForm> extends AbstractFormPopup<T> implements IMobilePopup<T> {

  @Override
  protected boolean getConfiguredWithGlassPane() {
    return true;
  }

  @Override
  protected void decorateForm(IForm form) {
    super.decorateForm(form);
    if (form.getRootGroupBox() != null) {
      form.getRootGroupBox().setMenuBarPosition(IGroupBox.MENU_BAR_POSITION_BOTTOM);
    }
  }
}

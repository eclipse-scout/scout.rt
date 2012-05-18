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
package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.mobile.services.IMobileNavigationService;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.forms.FormStack;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartFieldProposalForm;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3..8.0
 */
public class TabletDeviceTransformer extends AbstractDeviceTransformer {
  private static int EAST_FORM_WIDTH = 700;
  private static int TOOL_FORM_WIDTH = 300;

  @Override
  public void transformDesktop(IDesktop desktop) {
    FormStack navigationFormStack = new FormStack(IForm.VIEW_ID_CENTER);
    new FormStack(IForm.VIEW_ID_E);

    SERVICES.getService(IMobileNavigationService.class).installNavigator(navigationFormStack);
  }

  @Override
  public void transformForm(IForm form) {
    initViewSettings(form);
    form.setAskIfNeedSave(false);

    transformFormFields(form);
  }

  protected void initViewSettings(IForm form) {
    if (form instanceof IOutlineTreeForm || (form instanceof ISmartFieldProposalForm)) {
      return;
    }

    if (MobileDesktopUtility.isToolForm(form)) {
      form.setDisplayViewId(IForm.VIEW_ID_E);
      MobileDesktopUtility.setFormWidthHint(form, TOOL_FORM_WIDTH);
    }
    else if (form instanceof IOutlineTableForm || form instanceof OutlineChooserForm) {
      form.setDisplayViewId(IForm.VIEW_ID_CENTER);
      form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
    }
    else {
      MobileDesktopUtility.closeAllToolForms();

      form.setDisplayViewId(IForm.VIEW_ID_E);
      form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);

      MobileDesktopUtility.setFormWidthHint(form, EAST_FORM_WIDTH);
    }
  }

}

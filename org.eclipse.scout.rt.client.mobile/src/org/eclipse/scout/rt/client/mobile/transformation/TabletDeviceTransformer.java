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

import java.awt.Rectangle;

import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.mobile.services.IMobileNavigationService;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.forms.FormStack;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
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

  @Override
  public void transformDesktop(IDesktop desktop) {
    FormStack navigationFormStack = new FormStack(IForm.VIEW_ID_CENTER);
    new FormStack(IForm.VIEW_ID_E);

    SERVICES.getService(IMobileNavigationService.class).installNavigator(navigationFormStack);
  }

  @Override
  public void transformForm(IForm form) {
    initViewSettings(form);

    transformFormFields(form);
  }

  protected void initViewSettings(IForm form) {
    if (form instanceof IOutlineTreeForm || (form instanceof ISmartFieldProposalForm)) {
      return;
    }

    if (MobileDesktopUtility.isToolForm(form)) {
      form.setDisplayViewId(IForm.VIEW_ID_E);
      setFormWidthHint(form, 300);
    }
    else if (form instanceof IOutlineTableForm || form instanceof OutlineChooserForm) {
      form.setDisplayViewId(IForm.VIEW_ID_CENTER);
      form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
    }
    else {
      MobileDesktopUtility.closeAllToolForms();

      form.setDisplayViewId(IForm.VIEW_ID_E);
      form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);

      setFormWidthHint(form, 700);
    }
  }

  protected void setFormWidthHint(IForm form, int widthHint) {
    form.setCacheBounds(true);

    Rectangle formBounds = ClientUIPreferences.getInstance(ClientJob.getCurrentSession()).getFormBounds(form);
    if (formBounds != null) {
      return;
    }

    formBounds = new Rectangle(-1, -1, widthHint, -1);
    ClientUIPreferences.getInstance(ClientJob.getCurrentSession()).setFormBounds(form, formBounds);
  }
}

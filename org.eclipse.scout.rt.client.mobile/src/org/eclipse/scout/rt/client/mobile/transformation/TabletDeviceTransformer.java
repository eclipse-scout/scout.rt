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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.services.IMobileNavigationService;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3..8.0
 */
public class TabletDeviceTransformer extends AbstractDeviceTransformer {
  private static int DIALOG_FORM_WIDTH = 700;
  private static int EAST_FORM_WIDTH = 700;
  private static int TOOL_FORM_WIDTH = 300;

  @Override
  public void transformDesktop(IDesktop desktop) {
    List<String> navigationFormDisplayViewIds = new LinkedList<String>();
    navigationFormDisplayViewIds.add(IForm.VIEW_ID_CENTER);
    SERVICES.getService(IMobileNavigationService.class).installNavigator(navigationFormDisplayViewIds);
  }

  @Override
  protected void transformDisplayHintSettings(IForm form) {
    if (form instanceof IOutlineTreeForm) {
      return;
    }

    if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
      MobileDesktopUtility.setFormWidthHint(form, DIALOG_FORM_WIDTH);
    }
    else {
      if (MobileDesktopUtility.isToolForm(form)) {
        form.setDisplayViewId(IForm.VIEW_ID_E);
        MobileDesktopUtility.setFormWidthHint(form, TOOL_FORM_WIDTH);
      }
      else if (form instanceof IOutlineTableForm || form instanceof OutlineChooserForm) {
        form.setDisplayViewId(IForm.VIEW_ID_CENTER);
      }
      else {
        MobileDesktopUtility.closeAllToolForms();

        form.setDisplayViewId(IForm.VIEW_ID_E);
        MobileDesktopUtility.setFormWidthHint(form, EAST_FORM_WIDTH);
      }
    }

  }

  /**
   * Only moves the label to the top if it's a tool form. Regular forms have are big enough on tablet to display it on
   * the left side.
   */
  @Override
  protected void moveLabelToTop(IFormField field) {
    if (MobileDesktopUtility.isToolForm(field.getForm())) {
      super.moveLabelToTop(field);
    }
  }

}

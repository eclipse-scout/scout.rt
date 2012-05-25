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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.services.IMobileNavigationService;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.forms.FormStack;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3.8.0
 */
public class MobileDeviceTransformer extends AbstractDeviceTransformer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileDeviceTransformer.class);

  @Override
  public void transformDesktop(IDesktop desktop) {
    FormStack navigationFormStack = new FormStack(IForm.VIEW_ID_CENTER);

    SERVICES.getService(IMobileNavigationService.class).installNavigator(navigationFormStack);
  }

  @Override
  protected void transformDisplayHintSettings(IForm form) {
    if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
      form.setDisplayViewId(IForm.VIEW_ID_CENTER);
    }
    else {
      MobileDesktopUtility.setFormWidthHint(form, Integer.MAX_VALUE);
    }
  }
}

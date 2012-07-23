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
package org.eclipse.scout.rt.client.mobile.navigation;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public class OutlineChooserBreadCrumb extends BreadCrumb {

  public OutlineChooserBreadCrumb(IBreadCrumbsNavigation breadCrumbsNavigation, IForm form) {
    super(breadCrumbsNavigation, form);
  }

  @Override
  public void activate() throws ProcessingException {
    super.activate();

    //Clear any outline selection to make sure drill down works as expected again and again
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop.getOutline() != null) {
      desktop.getOutline().selectNode(null);
    }
    desktop.setOutline((IOutline) null);
  }

}

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

import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.service.AbstractService;

/**
 * @since 3.9.0
 */
public class BreadCrumbsNavigationService extends AbstractService implements IBreadCrumbsNavigationService {
  private String SESSION_DATA_KEY = "BreadCrumbsNavigationData";

  @Override
  public void install() {
    install(null);
  }

  @Override
  public void install(IDesktop desktop) {
    if (getBreadCrumbsNavigation() != null) {
      return;
    }

    IClientSession session = ClientJob.getCurrentSession();
    IBreadCrumbsNavigation data = createBreadCrumbsNavigation(desktop);
    session.setData(SESSION_DATA_KEY, data);
  }

  @Override
  public void uninstall() {
    IClientSession session = ClientJob.getCurrentSession();
    session.setData(SESSION_DATA_KEY, null);
  }

  protected IBreadCrumbsNavigation createBreadCrumbsNavigation() {
    return new BreadCrumbsNavigation();
  }

  protected IBreadCrumbsNavigation createBreadCrumbsNavigation(IDesktop desktop) {
    return new BreadCrumbsNavigation(desktop);
  }

  @Override
  public IBreadCrumbsNavigation getBreadCrumbsNavigation() {
    IClientSession session = ClientJob.getCurrentSession();
    return (IBreadCrumbsNavigation) session.getData(SESSION_DATA_KEY);
  }

}

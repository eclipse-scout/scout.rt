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
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.service.AbstractService;

/**
 * @since 3.8.0
 */
public class BreadCrumbsNavigationService extends AbstractService implements IBreadCrumbsNavigationService {
  private String SESSION_DATA_KEY = "BreadCrumbsNavigationData";

  protected IBreadCrumbsNavigation createBreadCrumbsNavigation() {
    return new BreadCrumbsNavigation();
  }

  protected IBreadCrumbsNavigation createBreadCrumbsNavigation(IDesktop desktop) {
    return new BreadCrumbsNavigation(desktop);
  }

  @Override
  public void stepBack() throws ProcessingException {
    getBreadCrumbsNavigation().stepBack();
  }

  @Override
  public boolean isSteppingBackPossible() {
    return getBreadCrumbsNavigation().isSteppingBackPossible();
  }

  @Override
  public void goHome() throws ProcessingException {
    getBreadCrumbsNavigation().goHome();
  }

  @Override
  public boolean isGoingHomePossible() {
    return getBreadCrumbsNavigation().isGoingHomePossible();
  }

  @Override
  public IForm getCurrentNavigationForm() {
    return getBreadCrumbsNavigation().getCurrentNavigationForm();
  }

  @Override
  public void addBreadCrumbsListener(IDesktop desktop, BreadCrumbsListener listener) {
    getBreadCrumbsNavigation(desktop).addBreadCrumbsListener(listener);
  }

  @Override
  public void addBreadCrumbsListener(BreadCrumbsListener listener) {
    getBreadCrumbsNavigation().addBreadCrumbsListener(listener);
  }

  @Override
  public void removeBreadCrumbsListener(BreadCrumbsListener listener) {
    getBreadCrumbsNavigation().removeBreadCrumbsListener(listener);
  }

  @Override
  public void trackDisplayViewId(String displayViewId) {
    getBreadCrumbsNavigation().trackDisplayViewId(displayViewId);
  }

  private IBreadCrumbsNavigation getBreadCrumbsNavigation(IDesktop desktop) {
    IClientSession session = ClientJob.getCurrentSession();
    IBreadCrumbsNavigation data = (IBreadCrumbsNavigation) session.getData(SESSION_DATA_KEY);

    if (data == null) {
      data = createBreadCrumbsNavigation(desktop);
      session.setData(SESSION_DATA_KEY, data);
    }
    return data;
  }

  @Override
  public IBreadCrumbsNavigation getBreadCrumbsNavigation() {
    return getBreadCrumbsNavigation(null);
  }

}

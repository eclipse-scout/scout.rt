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
package org.eclipse.scout.rt.client.mobile.services;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.mobile.navigation.IDeviceNavigator;
import org.eclipse.scout.rt.client.mobile.navigation.MobileDeviceNavigator;
import org.eclipse.scout.rt.client.mobile.ui.forms.FormStack;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.service.AbstractService;

/**
 * @since 3.8.0
 */
public class MobileNavigationService extends AbstractService implements IMobileNavigationService {
  private String SESSION_DATA_KEY = "MobileNavigatorData";

  @Override
  public void installNavigator(FormStack navigationFormStack) {
    if (getDeviceNavigator() != null) {
      return;
    }

    IClientSession session = ClientJob.getCurrentSession();
    session.setData(SESSION_DATA_KEY, createDeviceNavigator(navigationFormStack));
  }

  protected IDeviceNavigator createDeviceNavigator(FormStack navigationFormStack) {
    return new MobileDeviceNavigator(navigationFormStack);
  }

  @Override
  public void stepBack() throws ProcessingException {
    if (getDeviceNavigator() == null) {
      return;
    }

    getDeviceNavigator().stepBack();
  }

  @Override
  public boolean steppingBackPossible() {
    if (getDeviceNavigator() == null) {
      return false;
    }

    return getDeviceNavigator().isSteppingBackPossible();
  }

  @Override
  public IForm getCurrentForm() {
    if (getDeviceNavigator() == null) {
      return null;
    }

    return getDeviceNavigator().getCurrentNavigationForm();
  }

  @Override
  public IDeviceNavigator getDeviceNavigator() {
    IClientSession session = ClientJob.getCurrentSession();
    return (IDeviceNavigator) session.getData(SESSION_DATA_KEY);
  }

}

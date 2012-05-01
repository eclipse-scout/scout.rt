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
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileNavigator;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.service.AbstractService;

/**
 * @since 3.8.0
 */
public class MobileNavigationService extends AbstractService implements IMobileNavigationService {
  private String SESSION_DATA_KEY = "MobileNavigatorData";

  @Override
  public void installNavigator() {
    if (getMobileNavigator() != null) {
      return;
    }

    IClientSession session = ClientJob.getCurrentSession();
    session.setData(SESSION_DATA_KEY, new MobileNavigator());
  }

  @Override
  public void stepBack() throws ProcessingException {
    getMobileNavigator().stepBack();
  }

  @Override
  public boolean steppingBackPossible() {
    return getMobileNavigator().steppingBackPossible();
  }

  @Override
  public IForm getCurrentForm() {
    return getMobileNavigator().getCurrentForm();
  }

  private MobileNavigator getMobileNavigator() {
    IClientSession session = ClientJob.getCurrentSession();
    return (MobileNavigator) session.getData(SESSION_DATA_KEY);
  }

}

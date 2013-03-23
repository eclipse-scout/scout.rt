/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.testing.ui.rap;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.client.IGuiMock;
import org.eclipse.scout.rt.testing.client.IGuiMockService;
import org.eclipse.scout.service.AbstractService;

/**
 * Uses Selenium
 */
public class RapMockService extends AbstractService implements IGuiMockService {

  private final static RapMock s_rapMock = new RapMock();

  public RapMockService() {
    s_rapMock.initializeMock();
  }

  @Override
  public UserAgent initUserAgent() {
    return UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP);
  }

  @Override
  public IGuiMock createMock(IClientSession session) {
    s_rapMock.setClientSession(session);
    return s_rapMock;
  }

  @Override
  public void disposeServices() {
    try {
      s_rapMock.shutdownMock();
    }
    finally {
      super.disposeServices();
    }
  }
}

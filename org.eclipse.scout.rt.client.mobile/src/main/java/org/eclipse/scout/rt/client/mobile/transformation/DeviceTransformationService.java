/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

/**
 * @since 3.9.0
 */
public class DeviceTransformationService implements IDeviceTransformationService {
  private String SESSION_DATA_KEY = "DeviceTransformationServiceData";

  @Override
  public void install() {
    install(null);
  }

  @Override
  public void install(IDesktop desktop) {
    if (getDeviceTransformer() != null) {
      return;
    }

    IClientSession session = ClientSessionProvider.currentSession();
    IDeviceTransformer data = createDeviceTransformer(desktop);
    session.setData(SESSION_DATA_KEY, data);
  }

  @Override
  public void uninstall() {
    IClientSession session = ClientSessionProvider.currentSession();
    session.setData(SESSION_DATA_KEY, null);
  }

  protected IDeviceTransformer createDeviceTransformer(IDesktop desktop) {
    if (UserAgentUtility.isTabletDevice()) {
      return new TabletDeviceTransformer(desktop);
    }
    else {
      return new MobileDeviceTransformer(desktop);
    }
  }

  @Override
  public IDeviceTransformer getDeviceTransformer() {
    IClientSession session = ClientSessionProvider.currentSession();
    return (IDeviceTransformer) session.getData(SESSION_DATA_KEY);
  }

}

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

import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.eclipse.scout.service.AbstractService;

/**
 * @since 3.9.0
 */
public class DeviceTransformationService extends AbstractService implements IDeviceTransformationService {

  private String SESSION_DATA_KEY = "DeviceTransformationServiceData";

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
    return getDeviceTransformer(null);
  }

  @Override
  public IDeviceTransformer getDeviceTransformer(IDesktop desktop) {
    IClientSession session = ClientJob.getCurrentSession();
    IDeviceTransformer data = (IDeviceTransformer) session.getData(SESSION_DATA_KEY);

    if (data == null) {
      data = createDeviceTransformer(desktop);
      session.setData(SESSION_DATA_KEY, data);
    }
    return data;
  }

}

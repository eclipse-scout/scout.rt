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

import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;

public class DeviceTransformationDesktopExtension extends AbstractDesktopExtension {
  private IDeviceTransformer m_deviceTransformer;

  public DeviceTransformationDesktopExtension() {
  }

  public IDeviceTransformer getDeviceTransformer() {
    return m_deviceTransformer;
  }

  @Override
  public void setCoreDesktop(IDesktop desktop) {
    super.setCoreDesktop(desktop);

    // Install service for this desktop / client session. This needs to be done before initConfig of the desktop runs
    IDeviceTransformationService transformationService = BEANS.get(IDeviceTransformationService.class);
    transformationService.install(getCoreDesktop());
    m_deviceTransformer = transformationService.getDeviceTransformer();
  }

}

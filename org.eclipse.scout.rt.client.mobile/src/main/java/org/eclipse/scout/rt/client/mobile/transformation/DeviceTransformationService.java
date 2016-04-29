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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.session.ISessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.9.0
 */
public class DeviceTransformationService implements IDeviceTransformationService {
  private static final Logger LOG = LoggerFactory.getLogger(DeviceTransformationService.class);

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
    if (desktop == null) {
      throw new IllegalArgumentException("Desktop must not be null");
    }

    IClientSession session = ClientSessionProvider.currentSession();
    IDeviceTransformer data = createDeviceTransformer();
    data.setDesktop(desktop);
    session.setData(SESSION_DATA_KEY, data);
    session.addListener(new P_SessionListener());
    LOG.debug("DeviceTransformationService installed for session {}", session);
  }

  @Override
  public void uninstall() {
    if (getDeviceTransformer() == null) {
      return;
    }
    IClientSession session = ClientSessionProvider.currentSession();
    getDeviceTransformer().dispose();
    session.setData(SESSION_DATA_KEY, null);
    LOG.debug("DeviceTransformationService uninstalled for session {}", session);
  }

  protected IDeviceTransformer createDeviceTransformer() {
    return BEANS.get(MainDeviceTransformer.class);
  }

  @Override
  public IDeviceTransformer getDeviceTransformer() {
    IClientSession session = ClientSessionProvider.currentSession();
    return (IDeviceTransformer) session.getData(SESSION_DATA_KEY);
  }

  private class P_SessionListener implements ISessionListener {
    @Override
    public void sessionChanged(SessionEvent event) {
      if (event.getType() == SessionEvent.TYPE_STOPPED) {
        event.getSource().removeListener(this);
        uninstall();
      }
    }
  }
}

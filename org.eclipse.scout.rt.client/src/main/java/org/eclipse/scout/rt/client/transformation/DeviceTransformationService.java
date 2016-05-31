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
package org.eclipse.scout.rt.client.transformation;

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

  private static final String SESSION_DATA_KEY = "DeviceTransformationServiceData";
  private IDeviceTransformer m_nullTransformer = new NullDeviceTransformer();

  @Override
  public void install(IDesktop desktop) {
    IClientSession session = ClientSessionProvider.currentSession();
    if (session == null) {
      throw new IllegalArgumentException("No current session available");
    }
    if (desktop == null) {
      throw new IllegalArgumentException("Desktop must not be null");
    }
    if (getDeviceTransformer(session) != null) {
      // Already installed for the current session
      return;
    }

    IDeviceTransformer data = createDeviceTransformer();
    data.setDesktop(desktop);
    session.setData(SESSION_DATA_KEY, data);
    session.addListener(new P_SessionListener());
    LOG.debug("DeviceTransformationService installed for session {}", session);
  }

  @Override
  public void uninstall() {
    uninstall(ClientSessionProvider.currentSession());
  }

  protected void uninstall(IClientSession session) {
    IDeviceTransformer transformer = getDeviceTransformer(session);
    if (transformer == null) {
      // Not installed for current session
      return;
    }
    transformer.dispose();
    session.setData(SESSION_DATA_KEY, null);
    LOG.debug("DeviceTransformationService uninstalled for session {}", session);
  }

  protected IDeviceTransformer createDeviceTransformer() {
    return BEANS.get(MainDeviceTransformer.class);
  }

  @Override
  public IDeviceTransformer getDeviceTransformer() {
    IDeviceTransformer transformer = getDeviceTransformer(ClientSessionProvider.currentSession());
    if (transformer == null) {
      return m_nullTransformer;
    }
    return transformer;
  }

  protected IDeviceTransformer getDeviceTransformer(IClientSession session) {
    if (session == null) {
      return null;
    }
    Object data = session.getData(SESSION_DATA_KEY);
    if (data == null) {
      return null;
    }
    return (IDeviceTransformer) data;
  }

  private class P_SessionListener implements ISessionListener {
    @Override
    public void sessionChanged(SessionEvent event) {
      if (event.getType() == SessionEvent.TYPE_STOPPED) {
        IClientSession session = (IClientSession) event.getSource();
        session.removeListener(this);
        uninstall(session);
      }
    }
  }
}

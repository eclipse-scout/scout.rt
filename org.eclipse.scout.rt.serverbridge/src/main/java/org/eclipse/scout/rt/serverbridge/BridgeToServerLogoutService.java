/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.serverbridge;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.clientnotification.IClientNodeId;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ILogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(4900)
public class BridgeToServerLogoutService implements ILogoutService {
  private static final Logger LOG = LoggerFactory.getLogger(BridgeToServerLogoutService.class);

  @Override
  public void logout() {
    BEANS.get(IAccessControlService.class).clearCacheOfCurrentUser();

    // Manually stop session, because we don't have a HTTP session in "bridge" mode (see org.eclipse.scout.rt.server.ServiceTunnelServlet.ScoutSessionBindingListener)
    IServerSession session = ServerSessionProvider.currentSession();
    try {
      session.stop();
    }
    catch (Exception e) {
      LOG.warn("Failed to stop session.", e);
    }
    finally {
      BEANS.get(IClientNotificationService.class).unregisterSession(IClientNodeId.CURRENT.get(), session.getId(), session.getUserId());
    }
  }
}

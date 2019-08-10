/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.session;

import static org.eclipse.scout.rt.platform.util.Assertions.assertEqual;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNullOrEmpty;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSessionLifecycleHandler implements IServerSessionLifecycleHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ServerSessionLifecycleHandler.class);

  private final String m_scoutSessionId;
  private final String m_clientNodeId; // may be null
  private final ServerRunContext m_serverRunContextForSessionStart;

  public ServerSessionLifecycleHandler(String scoutSessionId, ServerRunContext serverRunContextForSessionStart) {
    m_scoutSessionId = assertNotNullOrEmpty(scoutSessionId, "sessionId must not be null or empty");
    m_serverRunContextForSessionStart = assertNotNull(serverRunContextForSessionStart, "serverRunContext must not be null");
    m_clientNodeId = serverRunContextForSessionStart.getClientNodeId();
  }

  @Override
  public String getId() {
    return m_scoutSessionId;
  }

  @Override
  public IServerSession create() {
    LOG.debug("Creating scout server session with scoutSessionId={}", getId());
    IServerSession session = BEANS.get(ServerSessionProvider.class).opt(getId(), m_serverRunContextForSessionStart);
    if (session == null) {
      return null;
    }

    assertEqual(session.getId(), getId()); // ensure mapping between the actual session and the id used in the caches matches
    if (m_clientNodeId != null) {
      BEANS.get(IClientNotificationService.class).registerSession(m_clientNodeId, session.getId(), session.getUserId());
    }
    return session;
  }

  @Override
  public void destroy(IServerSession session) {
    LOG.debug("Destroying scout server scoutSessionId={}", session.getId());
    try {
      session.stop();
    }
    finally {
      if (m_clientNodeId != null) {
        BEANS.get(IClientNotificationService.class).unregisterSession(m_clientNodeId, session.getId(), session.getUserId());
      }
    }
  }
}

package org.eclipse.scout.rt.server.session;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSessionLifecycleHandler implements IServerSessionLifecycleHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ServerSessionLifecycleHandler.class);
  private final String m_sessionId;
  private final String m_clientNodeId;
  private final ServerRunContext m_serverRunContext;

  public ServerSessionLifecycleHandler(String sessionId, String clientNodeId, ServerRunContext serverRunContext) {
    m_sessionId = sessionId;
    m_clientNodeId = clientNodeId;
    m_serverRunContext = serverRunContext;
  }

  @Override
  public String getId() {
    return m_sessionId;
  }

  @Override
  public IServerSession create() {
    LOG.debug("Creating scout server session id={}", m_sessionId);
    IServerSession session = BEANS.get(ServerSessionProvider.class).provide(m_sessionId, m_serverRunContext.copy());
    if (m_clientNodeId != null) {
      BEANS.get(IClientNotificationService.class).registerSession(m_clientNodeId, m_sessionId, session.getUserId());
    }
    return session;
  }

  @Override
  public void destroy(IServerSession session) {
    LOG.debug("Destroying scout server id={}", m_sessionId);
    session.stop();
    if (m_clientNodeId != null) {
      BEANS.get(IClientNotificationService.class).unregisterSession(m_clientNodeId, m_sessionId, session.getUserId());
    }
  }

}

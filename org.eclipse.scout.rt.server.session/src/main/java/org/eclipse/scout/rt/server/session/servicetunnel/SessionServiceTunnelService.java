/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.servicetunnel;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.servicetunnel.ServiceTunnelService;
import org.eclipse.scout.rt.server.session.IServerSession;
import org.eclipse.scout.rt.server.session.context.HttpServerSessionRunContextProducer;
import org.eclipse.scout.rt.server.session.context.ServerSessionRunContext;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.session.SessionId;

/**
 * {@link ServiceTunnelService} implementation for backends using Scout {@link IServerSession}.
 */
@Replace
public class SessionServiceTunnelService extends ServiceTunnelService {

  protected transient LazyValue<HttpServerSessionRunContextProducer> m_serverSessionRunContextProducer = new LazyValue<>(HttpServerSessionRunContextProducer.class);

  @Override
  protected ServerRunContext createServiceTunnelRunContext(ServiceTunnelRequest serviceRequest) {
    ServerSessionRunContext serverRunContext = (ServerSessionRunContext) super.createServiceTunnelRunContext(serviceRequest);

    String sessionId = SessionId.CURRENT.get();
    if (sessionId != null) {
      final HttpServletRequest req = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
      final IServerSession session = m_serverSessionRunContextProducer.get().getOrCreateScoutSession(req, serverRunContext, sessionId);
      serverRunContext.withSession(session);
    }
    return serverRunContext;
  }
}

/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextProducer;
import org.eclipse.scout.rt.server.commons.servlet.HttpClientInfo;
import org.eclipse.scout.rt.shared.session.SessionId;

/**
 * Producer for {@link ServerRunContext} instances handling HTTP requests in Scout server.
 */
public class ServerHttpRunContextProducer extends HttpRunContextProducer {

  @Override
  public RunContext produce(HttpServletRequest req, HttpServletResponse resp) {
    ServerRunContext serverRunContext = (ServerRunContext) super.produce(req, resp);
    return serverRunContext
        .withUserAgent(HttpClientInfo.get(req).toUserAgents().build())
        .withThreadLocal(SessionId.CURRENT, req.getHeader(SessionId.HTTP_HEADER_NAME));
  }
}

/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestContext;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestOptions;
import org.eclipse.scout.rt.server.commons.servlet.IHttpProxyRequestOptionsModifier;
import org.eclipse.scout.rt.shared.session.SessionId;

/**
 * Adds the {@value SessionId#HTTP_HEADER_NAME} header to the proxied request based on UI session ID read from UI request.
 */
public class ClientSessionIdHttpProxyRequestOptionsModifier implements IHttpProxyRequestOptionsModifier {

  @Override
  public void modify(HttpProxyRequestOptions options, HttpProxyRequestContext context) {
    HttpServletRequest req = context.getRequest();
    String uiSessionId = req.getHeader(IUiSession.ID_HTTP_HEADER_NAME);
    IUiSession uiSession = UiSession.get(req, uiSessionId);
    if (uiSession == null) {
      return;
    }
    String clientSessionId = uiSession.getClientSessionId();
    options.withCustomRequestHeader(SessionId.HTTP_HEADER_NAME, clientSessionId);
  }
}

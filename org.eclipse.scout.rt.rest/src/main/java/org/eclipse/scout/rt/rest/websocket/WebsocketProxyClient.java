/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.websocket;

import static org.eclipse.scout.rt.rest.websocket.IWebsocketEndpoint.sessionForLog;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint
public class WebsocketProxyClient {
  private static final Logger LOG = LoggerFactory.getLogger(WebsocketProxyClient.class);

  @OnOpen
  public void connected(Session session, EndpointConfig clientConfig) {
    LOG.info("Connected with {}", sessionForLog(session));
  }

  @OnClose
  public void disconnected(Session session, CloseReason reason) {
    LOG.info("Disconnected from {} because of {}", sessionForLog(session), reason);
  }

  @OnError
  public void disconnected(Session session, Throwable error) {
    LOG.info("Disconnected from {}", sessionForLog(session));
    if (error == null) {
      LOG.info("Disconnected from {} because of error", sessionForLog(session), error);
    }
    else {
      LOG.error("Error communicating with server", error);
    }
  }
}

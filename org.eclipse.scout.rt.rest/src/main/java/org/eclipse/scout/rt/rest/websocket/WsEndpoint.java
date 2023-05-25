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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/ws/{connection}", configurator = GetHttpSessionConfigurator.class)
public class WsEndpoint implements IWebsocketEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(WsEndpoint.class);

  private static Set<Session> s_sessions = new HashSet<>();

  @OnOpen
  public void onOpen(Session session) throws IOException {
    LOG.info("New websocket client connected: {}. RunContext: {}. Principal: {}", IWebsocketEndpoint.sessionForLog(session), RunContext.CURRENT.get(), session.getUserPrincipal());
    s_sessions.add(session);
    send("Hi client!");
  }

  @OnClose
  public void onClose(Session session) {
    LOG.info("Websocket client disconnected: {}", IWebsocketEndpoint.sessionForLog(session));
    s_sessions.remove(session);
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    LOG.error("Websocket error for {}", IWebsocketEndpoint.sessionForLog(session), throwable);
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    LOG.info("Websocket message from client {}: {}", IWebsocketEndpoint.sessionForLog(session), message);
  }

  public void send(String message) throws IOException {
    for (Session session : s_sessions) {
      session.getBasicRemote().sendText(message);
    }
  }
}

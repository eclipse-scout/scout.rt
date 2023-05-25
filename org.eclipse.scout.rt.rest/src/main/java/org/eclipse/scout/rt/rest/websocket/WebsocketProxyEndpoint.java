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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/ws/{path}", configurator = GetHttpSessionConfigurator.class)
public class WebsocketProxyEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(WebsocketProxyEndpoint.class);

  private static Map<Session, Session> s_sessions = new HashMap<>();
  private WebsocketProxyClient m_client = new WebsocketProxyClient();

  protected URI createRemoteUrl(String remoteBaseUrl, String path) {
    remoteBaseUrl = remoteBaseUrl.replace("http://", "ws://");
    remoteBaseUrl = remoteBaseUrl.replace("https://", "wss://");
    if (!remoteBaseUrl.endsWith("/")) {
      remoteBaseUrl += "/";
    }
    return URI.create(remoteBaseUrl + "ws/" + path);
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("path") String path, EndpointConfig config) throws IOException {
    try {
      LOG.info("New websocket client connecting for {}", IWebsocketEndpoint.sessionForLog(session));
      String remoteBaseUrl = (String) config.getUserProperties().get("remoteBaseUrl");
      Session clientSession = ContainerProvider.getWebSocketContainer().connectToServer(m_client, createRemoteUrl(remoteBaseUrl, path));
      clientSession.addMessageHandler(String.class, new P_WholeStringMessageHandler(session));
      s_sessions.put(session, clientSession);
    }
    catch (DeploymentException e) {
      throw new IOException(e);
    }
    LOG.info("New websocket client connected: {}. RunContext: {}. Principal: {}", IWebsocketEndpoint.sessionForLog(session), RunContext.CURRENT.get(), session.getUserPrincipal());
  }

  @OnClose
  public void onClose(Session session) throws IOException {
    LOG.info("Websocket client disconnected for {}", IWebsocketEndpoint.sessionForLog(session));
    Session clientSession = s_sessions.get(session);
    clientSession.close();
    s_sessions.remove(session);
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    LOG.error("Websocket error for {}", IWebsocketEndpoint.sessionForLog(session), throwable);
  }

  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
    LOG.info("Websocket message from browser for {}: {}", IWebsocketEndpoint.sessionForLog(session), message);
    s_sessions.get(session).getBasicRemote().sendText(message);
  }

  private static class P_WholeStringMessageHandler implements MessageHandler.Whole<String> {
    protected Session m_session;

    protected P_WholeStringMessageHandler(Session session) {
      m_session = session;
    }

    @Override
    public void onMessage(String message) {
      try {
        LOG.info("Websocket message from server for {}: {}", IWebsocketEndpoint.sessionForLog(m_session), message);
        m_session.getBasicRemote().sendText(message);
      }
      catch (IOException e) {
        throw new RuntimeException("Message cannot be sent to client", e);
      }
    }
  }
}

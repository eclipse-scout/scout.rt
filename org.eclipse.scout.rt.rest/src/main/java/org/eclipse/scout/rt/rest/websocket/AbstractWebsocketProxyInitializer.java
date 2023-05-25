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

import java.util.Arrays;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.scout.rt.platform.exception.ProcessingException;

public abstract class AbstractWebsocketProxyInitializer implements ServletContextListener {

  public abstract String getRemoteBaseUrl();

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServerContainer container = (ServerContainer) sce.getServletContext().getAttribute(ServerContainer.class.getName());
    try {
      container.addEndpoint(createConfig(WebsocketProxyEndpoint.class));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public ServerEndpointConfig createConfig(Class<?> endpointClass) throws ReflectiveOperationException {
    ServerEndpoint annotation = endpointClass.getAnnotation(ServerEndpoint.class);
    if (annotation == null) {
      throw new ProcessingException("Missing @" + ServerEndpoint.class + " annotation");
    }
    ServerEndpointConfig config = ServerEndpointConfig.Builder.create(endpointClass, annotation.value())
        .subprotocols(Arrays.asList(annotation.subprotocols()))
        .decoders(Arrays.asList(annotation.decoders()))
        .encoders(Arrays.asList(annotation.encoders()))
        .configurator(annotation.configurator().getConstructor().newInstance())
        .build();
    config.getUserProperties().put("remoteBaseUrl", getRemoteBaseUrl());
    return config;
  }
}

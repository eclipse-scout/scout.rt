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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;

import org.eclipse.scout.rt.platform.BEANS;

public class WebsocketInitializer implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServerContainer container = (ServerContainer) sce.getServletContext().getAttribute(ServerContainer.class.getName());
    try {
      for (IWebsocketEndpoint endpoint : BEANS.all(IWebsocketEndpoint.class)) {
        container.addEndpoint(endpoint.getClass());
      }
    }
    catch (DeploymentException e) {
      throw new RuntimeException(e);
    }
  }
}

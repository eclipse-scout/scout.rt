/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.dev.jetty;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer {

  public static final String WEB_APP_FOLDER_KEY = "scout.jetty.webapp.folder";
  public static final String SERVER_PORT_KEY = "scout.jetty.port";
  public static final String SESSION_TIMEOUT_KEY = "scout.jetty.session.timeout";

  public static void main(String[] args) throws Exception {
    new JettyServer().start();
  }

  protected void start() throws Exception {
    // read folder
    File webappFolder = null;
    String webappParam = System.getProperty(WEB_APP_FOLDER_KEY);
    if (webappParam == null || webappParam.length() < 1) {
      webappFolder = new File(Paths.get(".").toAbsolutePath().normalize().toFile(), "/src/main/webapp/");
    }
    else {
      webappFolder = new File(webappParam);
    }

    // port
    int port = 8080;
    String portConfig = System.getProperty(SERVER_PORT_KEY);
    if (portConfig != null && portConfig.length() > 0) {
      try {
        port = Integer.parseInt(portConfig);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    // session timeout
    int timeoutInSeconds = 15 * 60; // 15 min default
    String timeoutConfig = System.getProperty(SESSION_TIMEOUT_KEY);
    if (timeoutConfig != null && timeoutConfig.length() > 0) {
      try {
        timeoutInSeconds = Integer.parseInt(timeoutConfig);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    WebAppContext webApp = createWebApp(webappFolder, timeoutInSeconds);
    Server server = new Server(port);
    server.setHandler(webApp);
    server.start();
  }

  protected WebAppContext createWebApp(File webappDir, int sessionTimeoutSeconds) throws Exception {
    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setContextPath("/");
    webAppContext.setResourceBase(webappDir.getAbsolutePath());
    webAppContext.setParentLoaderPriority(true);
    SessionManager sessionManager = webAppContext.getSessionHandler().getSessionManager();
    sessionManager.setMaxInactiveInterval(sessionTimeoutSeconds);

    webAppContext.setConfigurationClasses(new String[]{
        "org.eclipse.jetty.webapp.WebInfConfiguration",
        "org.eclipse.jetty.webapp.WebXmlConfiguration",
        "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
        "org.eclipse.jetty.plus.webapp.PlusConfiguration",
        "org.eclipse.jetty.plus.webapp.EnvConfiguration",
    });

    webAppContext.configure();
    return webAppContext;
  }
}

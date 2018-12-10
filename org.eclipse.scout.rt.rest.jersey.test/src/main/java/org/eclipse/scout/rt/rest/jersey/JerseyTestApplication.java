/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.jersey;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JerseyTestApplication {

  private static final Logger LOG = LoggerFactory.getLogger(JerseyTestApplication.class);

  private volatile Server m_server = null;
  private int m_port;

  public static void main(String[] args) {
    IPlatform platform = Platform.get();
    platform.awaitPlatformStarted();
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  public synchronized void ensureStarted() {
    if (m_server == null) {
      try {
        startInternal();
      }
      catch (Exception e) {
        LOG.error("Fatal: Unable to start application", e);
      }
    }
  }

  public int getPort() {
    return m_port;
  }

  protected void startInternal() throws Exception {
    m_port = getListenPort();

    final Server server = createServer(m_port);
    server.start();
    m_server = server;

    if (LOG.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Server ready. The application is available on the following addresses:\n");
      sb.append("---------------------------------------------------------------------\n");
      sb.append("  http://localhost:").append(m_port).append('\n');
      String hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
      String ip = InetAddress.getLocalHost().getHostAddress();
      sb.append("  http://").append(hostname).append(":").append(m_port).append('\n');
      if (StringUtility.notEqualsIgnoreCase(hostname, ip)) {
        sb.append("  http://").append(ip).append(":").append(m_port).append('\n');
      }
      sb.append("---------------------------------------------------------------------\n");
      LOG.info(sb.toString());
    }
  }

  @SuppressWarnings("resource")
  protected Server createServer(final int port) {
    final Server server = new Server();

    // Do not emit the server version (-> security)
    final HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false);
    final ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
    connector.setPort(port);
    server.addConnector(connector);

    final Handler handler = createHandler();
    server.setHandler(handler);

    return server;
  }

  public void shutdown() {
    try {
      shutdownInternal();
    }
    catch (Exception e) {
      LOG.error("Error while shutting down application", e);
    }
  }

  protected void shutdownInternal() throws Exception {
    if (m_server != null) {
      m_server.stop();
    }
  }

  protected int getListenPort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
    catch (IOException e) {
      throw new PlatformException("Could not allocate an unused network port", e);
    }
  }

  protected Handler createHandler() {
    final ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    handler.setResourceBase(System.getProperty("java.io.tmpdir"));
    handler.setContextPath("/");

    handler.setDisplayName("Scout REST Integration Test");
    handler.addServlet(RestClientTestEchoServlet.class, "/echo");

    return handler;
  }
}

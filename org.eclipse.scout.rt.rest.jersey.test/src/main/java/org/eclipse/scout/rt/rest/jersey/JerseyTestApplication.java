/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.LocalHostAddressHelper;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.RestApplication;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JerseyTestApplication {

  private static final Logger LOG = LoggerFactory.getLogger(JerseyTestApplication.class);

  private volatile Server m_server = null;
  private volatile Server m_proxyServer = null;

  private int m_port;
  private int m_proxyPort;

  public static void main(String[] args) {
    Platform.get().awaitPlatformStarted();
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

  public int getProxyPort() {
    return m_proxyPort;
  }

  protected void startInternal() throws Exception {
    m_port = findUnusedNetworkPort();
    m_proxyPort = findUnusedNetworkPort();

    final Server server = createServer(m_port, false);
    server.start();
    m_server = server;

    final Server proxyServer = createServer(m_proxyPort, true);
    proxyServer.start();
    m_proxyServer = proxyServer;

    if (LOG.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Server ready. The application is available on the following addresses:\n");
      sb.append("---------------------------------------------------------------------\n");
      sb.append("Echo").append('\n');
      sb.append("  http://localhost:").append(m_port).append('\n');
      LocalHostAddressHelper localHostAddressHelper = BEANS.get(LocalHostAddressHelper.class);
      String hostname = localHostAddressHelper.getHostName().toLowerCase();
      String ip = localHostAddressHelper.getHostAddress();
      sb.append("  http://").append(hostname).append(":").append(m_port).append('\n');
      if (StringUtility.notEqualsIgnoreCase(hostname, ip)) {
        sb.append("  http://").append(ip).append(":").append(m_port).append('\n');
      }
      sb.append("Proxy").append('\n');
      sb.append("  http://localhost:").append(m_proxyPort).append('\n');
      sb.append("---------------------------------------------------------------------\n");
      LOG.info(sb.toString());
    }
  }

  @SuppressWarnings("resource")
  protected Server createServer(final int port, boolean proxy) {
    final Server server = new Server();

    // Do not emit the server version (-> security)
    final HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false);
    final ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
    connector.setPort(port);
    server.addConnector(connector);

    final Handler handler = createHandler(proxy);
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
    if (m_proxyServer != null) {
      m_proxyServer.stop();
    }
  }

  protected int findUnusedNetworkPort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
    catch (IOException e) {
      throw new PlatformException("Could not allocate an unused network port", e);
    }
  }

  protected Handler createHandler(boolean proxy) {
    final ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    handler.setResourceBase(System.getProperty("java.io.tmpdir"));
    handler.setContextPath("/");

    handler.setDisplayName("Scout REST Integration Test");
    if (proxy) {
      handler.addServlet(RestClientHttpProxyServlet.class, "/");
    }
    else {
      handler.addServlet(RestClientTestEchoServlet.class, "/echo");

      // register RestApplication
      ServletHolder servlet = handler.addServlet(ServletContainer.class, "/api/*");
      servlet.setInitParameter(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE.toString());
      servlet.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, RestApplication.class.getName());
      servlet.setInitOrder(1); // load-on-startup
    }

    return handler;
  }
}

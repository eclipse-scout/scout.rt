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
package org.eclipse.scout.rt.server.commons.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;

/**
 * HTTP Server supporting interception of http. Used to trigger and force errors and failures.
 * <p>
 * Webapp container used for unit testing of http client, servlets, http retry effects, etc.
 * <p>
 * The webapp consists of a servlet with path '/servlet'
 *
 * @since 9.x
 */
public class TestingHttpServer {

  @FunctionalInterface
  public interface IServletRequestHandler {
    void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
  }

  @FunctionalInterface
  public interface IChannelCall {
    void call() throws IOException, ServletException;
  }

  @FunctionalInterface
  public interface IChannelInterceptor {
    void intercept(HttpChannel channel, IChannelCall superCall) throws IOException, ServletException;
  }

  /**
   * http://172.0.0.1:33xyz/servlet
   */
  public static class FixtureServlet extends AbstractHttpServlet {
    private static final long serialVersionUID = 1L;
    public static IServletRequestHandler FIXTURE_GET;
    public static IServletRequestHandler FIXTURE_POST;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      FIXTURE_GET.handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      FIXTURE_POST.handle(req, resp);
    }
  }

  private final Server m_server;
  private final URL m_servletUrl;
  private IChannelInterceptor m_channelInterceptor;

  /**
   * default webapp with servlet at /servlet that calls the fixture set by
   * {@link #withServletGetHandler(IServletRequestHandler)}
   */
  public TestingHttpServer(int port) {
    this(port, "/", TestingHttpServer.class.getResource("/org.eclipse.scout.rt.server.commons.http.webapp"), "servlet");
  }

  protected TestingHttpServer(int port, String contextPath, URL resourceBaseUrl, String servletPath) {
    Assertions.assertNotNull(contextPath);
    Assertions.assertTrue(contextPath.startsWith("/"));
    Assertions.assertNotNull(resourceBaseUrl);
    String urlText = "http://127.0.0.1:" + port + contextPath + servletPath;
    try {
      m_servletUrl = new URL(urlText);
    }
    catch (MalformedURLException e) {
      throw new ProcessingException("create URL '{}'", urlText, e);
    }
    FixtureServlet.FIXTURE_GET = null;
    FixtureServlet.FIXTURE_POST = null;

    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setThrowUnavailableOnStartupException(true);
    webAppContext.setContextPath(contextPath);
    webAppContext.setBaseResource(Resource.newResource(resourceBaseUrl));
    webAppContext.setParentLoaderPriority(true);
    webAppContext.setConfigurationClasses(new String[]{
        "org.eclipse.jetty.webapp.WebInfConfiguration",
        "org.eclipse.jetty.webapp.WebXmlConfiguration",
        "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
    });
    interceptCreateWebAppContext(webAppContext);
    try {
      webAppContext.configure();
    }
    catch (Exception e) {
      throw new ProcessingException("configure contextPath='{}' resourceBaseUrl='{}' ", contextPath, resourceBaseUrl, e);
    }
    m_server = new Server(port) {
      @Override
      public void handle(HttpChannel channel) throws IOException, ServletException {
        if (m_channelInterceptor != null) {
          m_channelInterceptor.intercept(channel, () -> super.handle(channel));
        }
        else {
          super.handle(channel);
        }
      }
    };
    m_server.setHandler(webAppContext);
    interceptCreateServer(webAppContext);
  }

  protected void interceptCreateServer(WebAppContext webAppContext) {
  }

  protected void interceptCreateWebAppContext(WebAppContext webAppContext) {
  }

  public URL getServletUrl() {
    return m_servletUrl;
  }

  /**
   * set the current GET handler on the global {@link ServletHandler}
   */
  public TestingHttpServer withServletGetHandler(IServletRequestHandler handler) {
    FixtureServlet.FIXTURE_GET = handler;
    return this;
  }

  /**
   * set the current POST handler on the global {@link ServletHandler}
   */
  public TestingHttpServer withServletPostHandler(IServletRequestHandler handler) {
    FixtureServlet.FIXTURE_POST = handler;
    return this;
  }

  /**
   * Install a handler that intercepts all incoming requests on a channel. Can be used to simulate network interruptions
   * or socket errors.
   */
  public TestingHttpServer withChannelInterceptor(IChannelInterceptor channelInterceptor) {
    m_channelInterceptor = channelInterceptor;
    return this;
  }

  public void start() {
    try {
      m_server.start();
    }
    catch (Exception e) {
      throw new ProcessingException("start", e);
    }
  }

  public void stop() {
    try {
      FixtureServlet.FIXTURE_GET = null;
      FixtureServlet.FIXTURE_POST = null;
      m_server.stop();
    }
    catch (Exception e) {
      throw new ProcessingException("stop", e);
    }
  }
}

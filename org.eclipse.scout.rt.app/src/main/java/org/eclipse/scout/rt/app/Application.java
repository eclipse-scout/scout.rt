/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpCookie.SameSite;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationAutoCreateSelfSignedCertificateProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationCertificateAliasProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationConsoleInputHandlerEnabledProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationContextPathProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationHttpRequestMaxHeaderSizeProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationHttpSessionEnabledProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationJvmShutdownHookEnabledProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationKeyStorePasswordProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationKeyStorePathProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationPortProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationPrivateKeyPasswordProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationSessionCookieConfigHttpOnlyProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationSessionCookieConfigSameSiteProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationSessionCookieConfigSecureProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationSessionTimeoutProperty;
import org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationUseTlsProperty;
import org.eclipse.scout.rt.jetty.IServletContributor;
import org.eclipse.scout.rt.jetty.IServletFilterContributor;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.config.PropertiesHelper;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.security.ICertificateProvider;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.LocalHostAddressHelper;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  protected static final LazyValue<Application> INSTANCE = new LazyValue<>(Application.class);

  protected final AtomicReference<Server> m_server = new AtomicReference<>();

  public static void main(String[] args) {
    LOG.info("Starting platform");
    IPlatform platform = Platform.get();
    platform.awaitPlatformStarted();

    LOG.info("Starting application");
    INSTANCE.get().start();
  }

  protected void start() {
    try {
      startInternal();
    }
    catch (Exception e) {
      LOG.error("Fatal: Unable to start application", e);
      shutdown();
      throw new PlatformException("Fatal: Unable to start application", e);
    }
  }

  protected void startInternal() throws Exception {
    Server server = createServer();
    m_server.set(server);
    server.start();

    if (CONFIG.getPropertyValue(ScoutApplicationConsoleInputHandlerEnabledProperty.class)) {
      startConsoleInputHandler();
    }

    if (CONFIG.getPropertyValue(ScoutApplicationJvmShutdownHookEnabledProperty.class)) {
      registerJvmShutdownHook();
    }

    logServerReady();
  }

  protected void logServerReady() {
    if (!LOG.isInfoEnabled()) {
      return;
    }

    String protocol = CONFIG.getPropertyValue(ScoutApplicationUseTlsProperty.class) ? "https" : "http";

    LocalHostAddressHelper helper = BEANS.get(LocalHostAddressHelper.class);
    String hostname = helper.getHostName();
    String ip = helper.getHostAddress();

    int port = CONFIG.getPropertyValue(ScoutApplicationPortProperty.class);
    String contextPath = CONFIG.getPropertyValue(ScoutApplicationContextPathProperty.class);

    StringBuilder sb = new StringBuilder();
    sb.append("Server ready. The application is available on the following addresses:\n");
    sb.append("---------------------------------------------------------------------\n");
    sb.append("  ").append(protocol).append("://localhost:").append(port).append(contextPath).append('\n');
    sb.append("  ").append(protocol).append("://").append(hostname).append(":").append(port).append(contextPath).append('\n');
    if (StringUtility.notEqualsIgnoreCase(hostname, ip)) {
      sb.append("  ").append(protocol).append("://").append(ip).append(":").append(port).append(contextPath).append('\n');
    }
    sb.append("---------------------------------------------------------------------\n");
    try {
      sb.append("PID: ").append(ProcessHandle.current().pid()).append("\n");
    }
    catch (Exception e) {
      // PID not supported
    }

    if (CONFIG.getPropertyValue(ScoutApplicationConsoleInputHandlerEnabledProperty.class)) {
      sb.append("To shut the server down, type \"shutdown\" or \"s\" in the console.\n");
    }

    LOG.info(sb.toString());
  }

  @SuppressWarnings("resource")
  protected Server createServer() {
    Server server = new Server();
    ServerConnector connector;
    if (CONFIG.getPropertyValue(ScoutApplicationUseTlsProperty.class)) {
      connector = createHttpsServerConnector(server);
    }
    else {
      connector = createHttpServerConnector(server);
    }
    connector.setPort(CONFIG.getPropertyValue(ScoutApplicationPortProperty.class));
    server.addConnector(connector);
    installErrorHandler(server);

    Handler handler = createHandler();
    server.setHandler(handler);

    return server;
  }

  protected ServerConnector createHttpServerConnector(Server server) {
    HttpConfiguration httpConfig = createHttpConfiguration();
    ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(httpConfig), new HTTP2CServerConnectionFactory(httpConfig));
    return http;
  }

  protected ServerConnector createHttpsServerConnector(Server server) {
    SslContextFactory.Server sslContextFactory = createSslContextFactory();
    HttpConfiguration httpsConfig = createHttpConfiguration();
    httpsConfig.addCustomizer(new SecureRequestCustomizer());

    HttpConnectionFactory http11 = new HttpConnectionFactory(httpsConfig);
    HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(httpsConfig);

    ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
    alpn.setDefaultProtocol(http11.getProtocol());

    SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());
    ServerConnector https = new ServerConnector(server, tls, alpn, http2, http11);

    return https;
  }

  protected HttpConfiguration createHttpConfiguration() {
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false); // Do not emit the server version (-> security)
    httpConfig.setSendDateHeader(false);
    httpConfig.setSendXPoweredBy(false);

    Integer requestHeaderMaxSize = CONFIG.getPropertyValue(ScoutApplicationHttpRequestMaxHeaderSizeProperty.class);
    if (requestHeaderMaxSize != null) {
      httpConfig.setRequestHeaderSize(requestHeaderMaxSize);
    }

    return httpConfig;
  }

  protected SslContextFactory.Server createSslContextFactory() {
    Path keyStorePath = resolveKeyStorePath(CONFIG.getPropertyValue(ScoutApplicationKeyStorePathProperty.class));
    String keyStoreUri = keyStorePath == null ? null : keyStorePath.toUri().toString();
    String keyStorePassword = ObjectUtility.nvl(CONFIG.getPropertyValue(ScoutApplicationKeyStorePasswordProperty.class), "");
    String privateKeyPassword = ObjectUtility.nvl(CONFIG.getPropertyValue(ScoutApplicationPrivateKeyPasswordProperty.class), "");
    String certAlias = CONFIG.getPropertyValue(ScoutApplicationCertificateAliasProperty.class);

    boolean keyStoreExists = keyStorePath != null && Files.isRegularFile(keyStorePath);
    SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
    if (Platform.get().inDevelopmentMode() && !keyStoreExists) {
      String autoCertName = CONFIG.getPropertyValue(ScoutApplicationAutoCreateSelfSignedCertificateProperty.class);
      if (!StringUtility.hasText(certAlias)) {
        certAlias = "localhost";
      }

      LOG.info("No existing keystore was provided to setup TLS. Creating a self-signed certificate '{}'.", autoCertName);
      ICertificateProvider certificateProvider = BEANS.optional(ICertificateProvider.class)
          .orElseThrow(() -> new PlatformException("No certificate-provider available to create a self-signed certificate to use for TLS."
              + " Add a certificate-provider or specify an existing keystore using property '{}'.", BEANS.get(ScoutApplicationKeyStorePathProperty.class).getKey()));

      if (keyStoreUri == null) {
        // no path available: create in memory only
        KeyStore ks = certificateProvider.createSelfSignedCertificate(certAlias, autoCertName, keyStorePassword.toCharArray(), privateKeyPassword.toCharArray());
        sslContextFactory.setKeyStore(ks);
      }
      else {
        // a non-existing key-store path was provided: create a new keystore file at that location
        LOG.info("Storing created keystore in '{}'.", keyStoreUri);
        certificateProvider.autoCreateSelfSignedCertificate(keyStoreUri, keyStorePassword.toCharArray(), privateKeyPassword.toCharArray(), certAlias, autoCertName);
        sslContextFactory.setKeyStorePath(keyStoreUri);
      }
    }
    else {
      LOG.info("Setup TLS certificate using alias '{}' from keystore '{}'.", certAlias, keyStoreUri);
      sslContextFactory.setKeyStorePath(keyStoreUri);
    }

    sslContextFactory.setKeyStorePassword(keyStorePassword);
    sslContextFactory.setKeyManagerPassword(privateKeyPassword);
    sslContextFactory.setCertAlias(certAlias);
    sslContextFactory.setEndpointIdentificationAlgorithm("https");
    sslContextFactory.setIncludeCipherSuites(
        //TLS 1.2
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
        "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
        //TLS 1.3
        "TLS_AES_256_GCM_SHA384",
        "TLS_AES_128_GCM_SHA256");

    return sslContextFactory;
  }

  protected Path resolveKeyStorePath(String path) {
    if (!StringUtility.hasText(path)) {
      return null;
    }

    if (path.startsWith(PropertiesHelper.CLASSPATH_PREFIX)) {
      String subPath = path.substring(PropertiesHelper.CLASSPATH_PREFIX.length());
      URL res = getClass().getResource(subPath);
      if (res == null) {
        res = getClass().getClassLoader().getResource(subPath);
      }
      if (res == null) {
        res = ClassLoader.getSystemClassLoader().getResource(subPath);
      }
      if (res == null) {
        throw new ProcessingException("Missing resource defined by config property: {}={}", BEANS.get(ScoutApplicationKeyStorePathProperty.class).getKey(), path);
      }
      path = res.toExternalForm();
    }

    try {
      return Paths.get(URI.create(path));
    }
    catch (Exception e) {
      LOG.debug("Path '{}' is no valid URI. Trying to read as file path.", path, e);
      return Paths.get(path);
    }
  }

  /**
   * Installs the default error handler. This default implementation doesn't show the servlet and doesn't send any stack
   * traces to the client.
   */
  protected void installErrorHandler(Server server) {
    ErrorHandler handler = new ErrorHandler();
    handler.setShowServlet(false);
    handler.setShowMessageInTitle(false);
    handler.setShowStacks(false);
    server.setErrorHandler(handler);
  }

  protected Handler createHandler() {
    boolean sessionEnabled = CONFIG.getPropertyValue(ScoutApplicationHttpSessionEnabledProperty.class);
    LOG.info("Creating servlet context handler with {}", sessionEnabled ? "sessions" : "no sessions");

    ServletContextHandler handler = new ServletContextHandler(sessionEnabled ? ServletContextHandler.SESSIONS : ServletContextHandler.NO_SESSIONS);

    if (sessionEnabled) {
      // See https://github.com/jetty/jetty.project/blob/jetty-11.0.18/jetty-webapp/src/main/java/org/eclipse/jetty/webapp/StandardDescriptorProcessor.java#L650
      // for how session properties are applied from web.xml to Java classes.
      int sessionTimeoutInSeconds = CONFIG.getPropertyValue(ScoutApplicationSessionTimeoutProperty.class);
      boolean httpOnly = CONFIG.getPropertyValue(ScoutApplicationSessionCookieConfigHttpOnlyProperty.class);
      boolean secure = CONFIG.getPropertyValue(ScoutApplicationSessionCookieConfigSecureProperty.class);
      SameSite sameSite = CONFIG.getPropertyValue(ScoutApplicationSessionCookieConfigSameSiteProperty.class);

      LOG.info("[Session config] timeout: {} s, HTTP only: {}, secure: {}, same site: {}", sessionTimeoutInSeconds, httpOnly, secure, sameSite.getAttributeValue());

      SessionHandler sessionHandler = handler.getSessionHandler();
      sessionHandler.setMaxInactiveInterval(sessionTimeoutInSeconds);
      sessionHandler.getSessionCookieConfig().setHttpOnly(httpOnly);
      sessionHandler.getSessionCookieConfig().setSecure(secure);
      sessionHandler.setSameSite(sameSite);
    }

    handler.setDisplayName(CONFIG.getPropertyValue(ApplicationNameProperty.class));

    // Prevent LogbackServletContainerInitializer to register LogbackServletContextListener as a listener which would shut down the logger facility to early,
    // Scout platform handles shutdown of logging framework instead.
    // Only relevant in case the dependency org.eclipse.jetty:jetty-annotations is on the classpath.
    handler.setInitParameter("logbackDisableServletContainerInitializer", Boolean.TRUE.toString());

    // Register servlets/servlet filters
    BEANS.all(IServletFilterContributor.class).forEach(c -> c.contribute(handler));
    BEANS.all(IServletContributor.class).forEach(c -> c.contribute(handler));

    String contextPath = CONFIG.getPropertyValue(ScoutApplicationContextPathProperty.class);
    handler.setContextPath(contextPath);

    if (!"/".equals(contextPath)) {
      /*
       * Wraps the given <code>webAppContext</code> in a {@link P_RedirectToContextPathHandler} which redirects all GET
       * requests to URIs outside the context path to the context path.
       * <p>
       * This simplifies the use of a custom context path by redirecting all requests that do NOT start with the specified
       * context path to the context path (e.g. /login?debug=true to /myapp/login?debug=true). Custom context paths are
       * required when multiple Scout UI servers are run in parallel with different ports, because otherwise they would
       * destroy each other's HTTP session (cookies are not specific to the port, only to the host and context path).
       */
      return new P_RedirectToContextPathHandler(handler);
    }

    return handler;
  }

  protected void startConsoleInputHandler() {
    Thread t = new Thread("Console input handler") {
      @Override
      public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
          String command;
          while ((command = StringUtility.trim(br.readLine())) != null) {
            if ("shutdown".equalsIgnoreCase(command) || "s".equalsIgnoreCase(command)) {
              try { // NOSONAR
                shutdown();
                return;
              }
              catch (Exception e) {
                LOG.error("Shutdown error", e);
              }
            }
            else if (StringUtility.hasText(command)) {
              LOG.warn("Unknown command: {}", command);
            }
          }
        }
        catch (IOException e1) {
          LOG.error("Unexpected error while waiting for console command", e1);
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }

  protected void registerJvmShutdownHook() {
    Thread shutdownHook = new Thread(() -> shutdown(), "Scout-app-shutdown-hook");
    Runtime.getRuntime().addShutdownHook(shutdownHook);
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
    Server server = m_server.getAndSet(null);
    if (server != null) {
      LOG.info("Shutting down application...");
      server.stop();

      // stop platform if it is available and not yet stopped (shouldn't be stopped yet)
      IPlatform platform = Platform.peek();
      if (platform != null && platform.getState() != State.PlatformStopped) {
        platform.stop();
      }
      LOG.info("Shutdown complete");
    }
    else {
      LOG.debug("Shutdown already in progress");
    }
  }

  /**
   * {@link Handler} that can be set as the {@link Server}s main handler. It wraps the given {@link ContextHandler} and
   * redirects all GET requests for URIs outside the context path to the context path. Non-GET requests are <i>not</i>
   * redirected.
   * <p>
   * Example for contextPath = <code>/myapp</code>:
   * <table border=1>
   * <tr>
   * <th>Request URI</th>
   * <th>Redirected to</th>
   * </tr>
   * <tr>
   * <td><code>/</code></td>
   * <td><code>/myapp/</code></td>
   * </tr>
   * <tr>
   * <td><code>/?param=1</code></td>
   * <td><code>/myapp/?param=1</code></td>
   * </tr>
   * <tr>
   * <td><code>/login</code></td>
   * <td><code>/myapp/login</code></td>
   * </tr>
   * <tr>
   * <td><code>/myapp</code></td>
   * <td><i>not redirected</i></td>
   * </tr>
   * <tr>
   * <td><code>/myapp/myservlet</code></td>
   * <td><i>not redirected</i></td>
   * </tr>
   * </table>
   */
  protected static class P_RedirectToContextPathHandler extends HandlerWrapper {

    protected final String m_contextPath;

    public P_RedirectToContextPathHandler(ContextHandler contextHandler) {
      setHandler(contextHandler);
      m_contextPath = contextHandler.getContextPath();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      // No redirection for non-GET requests
      if (!"GET".equals(request.getMethod())) {
        super.handle(target, baseRequest, request, response);
        return;
      }

      // If requestURI starts with context path, redirect is not necessary -> delegate to original context handler
      String requestURI = ObjectUtility.nvl(request.getRequestURI(), "/");
      if (!"GET".equals(request.getMethod()) || requestURI.startsWith(m_contextPath)) {
        super.handle(target, baseRequest, request, response);
        return;
      }

      // Otherwise, redirect to the specified context path (while preserving all other parts of the URI)
      StringBuilder redirectUri = new StringBuilder();
      redirectUri.append(request.getScheme()).append("://").append(request.getServerName());
      if (("http".equals(request.getScheme()) && request.getServerPort() != 80) || ("https".equals(request.getScheme()) && request.getServerPort() != 443)) {
        redirectUri.append(":").append(request.getServerPort());
      }
      redirectUri.append(m_contextPath);
      if (!"/".equals(requestURI)) {
        redirectUri.append(requestURI);
      }
      if (request.getQueryString() != null) {
        redirectUri.append("?").append(request.getQueryString());
      }
      response.sendRedirect(redirectUri.toString());
    }
  }
}

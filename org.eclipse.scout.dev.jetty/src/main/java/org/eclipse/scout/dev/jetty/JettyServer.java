/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.dev.jetty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.scout.dev.jetty.JettyConfiguration.ScoutJettyCertificateAliasProperty;
import org.eclipse.scout.dev.jetty.JettyConfiguration.ScoutJettyKeyStorePasswordProperty;
import org.eclipse.scout.dev.jetty.JettyConfiguration.ScoutJettyKeyStorePathProperty;
import org.eclipse.scout.dev.jetty.JettyConfiguration.ScoutJettyPrivateKeyPasswordProperty;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.LocalHostAddressHelper;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyServer {

  public static final String WEB_APP_FOLDER_KEY = "scout.jetty.webapp.folder";
  public static final String WEB_APP_CONTEXT_PATH = "scout.jetty.webapp.contextpath";
  public static final String SERVER_PORT_KEY = "scout.jetty.port"; // see also org.eclipse.scout.rt.platform.context.NodeIdentifier.compute()

  private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);

  protected volatile Server m_server = null;

  public static void main(String[] args) {
    new JettyServer().start();
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

  protected File getWebappFolder() {
    String webappParam = System.getProperty(WEB_APP_FOLDER_KEY);
    if (webappParam == null || webappParam.isEmpty()) {
      return new File(Paths.get(".").toAbsolutePath().normalize().toFile(), "/src/main/webapp/");
    }
    return new File(webappParam);
  }

  protected int getPort() {
    return new JettyConfiguration.ScoutJettyPortProperty().getValue();
  }

  protected String getContextPath() {
    String contextPathConfig = System.getProperty(WEB_APP_CONTEXT_PATH);
    if (StringUtility.hasText(contextPathConfig)) {
      if (!contextPathConfig.startsWith("/")) {
        contextPathConfig = "/" + contextPathConfig;
      }
      return contextPathConfig;
    }
    return "/";
  }

  protected boolean isUseTls() {
    return CONFIG.getPropertyValue(ScoutJettyKeyStorePathProperty.class) != null;
  }

  protected void startInternal() throws Exception {
    int port = getPort();
    boolean useTls = isUseTls();
    File webappFolder = getWebappFolder();
    String contextPath = getContextPath();

    m_server = new Server();
    Handler handler = createContextHandler(webappFolder, contextPath);
    ServerConnector connector = createServerConnector(port, useTls);

    m_server.setHandler(handler);
    m_server.addConnector(connector);
    m_server.start();
    startConsoleInputReader();
    logServerReady(useTls ? "https" : "http", port, contextPath);
  }

  protected ServerConnector createServerConnector(int port, boolean useTls) {
    if (useTls) {
      return createHttpsServerConnector(port);
    }
    return createHttpServerConnector(port);
  }

  protected void logServerReady(String protocol, int port, String contextPath) {
    if (!LOG.isInfoEnabled()) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Server ready. The application is available on the following addresses:\n");
    sb.append("---------------------------------------------------------------------\n");
    sb.append("  ").append(protocol).append("://localhost:").append(port).append(contextPath).append("\n");
    LocalHostAddressHelper localHostAddressHelper = BEANS.get(LocalHostAddressHelper.class);
    String hostname = localHostAddressHelper.getHostName().toLowerCase(Locale.US);
    String ip = localHostAddressHelper.getHostAddress();
    sb.append("  ").append(protocol).append("://").append(hostname).append(":").append(port).append(contextPath).append("\n");
    if (StringUtility.notEqualsIgnoreCase(hostname, ip)) {
      sb.append("  ").append(protocol).append("://").append(ip).append(":").append(port).append(contextPath).append("\n");
    }
    sb.append("---------------------------------------------------------------------\n");
    sb.append("To shut the server down, type \"shutdown\" in the console.\n");
    LOG.info(sb.toString());
  }

  public void shutdown() {
    LOG.info("Shutting down...");
    try {
      shutdownInternal();
      LOG.info("Shutdown complete.");
    }
    catch (Exception e) {
      LOG.error("Error while shutting down.", e);
    }
  }

  protected void shutdownInternal() throws Exception {
    if (m_server != null) {
      m_server.stop();
    }
  }

  protected void startConsoleInputReader() {
    final Thread t = new Thread("Console input handler") {
      @Override
      public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
          String command;
          while ((command = StringUtility.trim(br.readLine())) != null) {
            if ("shutdown".equalsIgnoreCase(command)) {
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

  protected Handler createContextHandler(File webappDir, String contextPath) throws Exception {
    String resourceBase = webappDir.getAbsolutePath();
    WebAppContext webAppContext = new P_WebAppContext(resourceBase, contextPath);
    webAppContext.setThrowUnavailableOnStartupException(true);

    LOG.info("Starting Jetty with resourceBase={}", resourceBase);

    if ("/".equals(contextPath)) {
      return webAppContext;
    }

    /**
     * Wraps the given <code>webAppContext</code> in a {@link P_RedirectToContextPathHandler} which redirects all GET
     * requests to URIs outside the context path to the context path.
     * <p>
     * This simplifies the use of a custom context path by redirecting all requests that do NOT start with the specified
     * context path to the context path (e.g. /login?debug=true to /myapp/login?debug=true). Custom context paths are
     * required when multiple Scout UI servers are run in parallel with different ports, because otherwise they would
     * destroy each others HTTP session (cookies are not specific to the port, only to the host and context path).
     */
    return new P_RedirectToContextPathHandler(webAppContext);
  }

  protected ServerConnector createHttpServerConnector(int port) {
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSendServerVersion(false);
    httpConfig.setSendDateHeader(false);
    ServerConnector http = new ServerConnector(m_server, new HttpConnectionFactory(httpConfig));
    http.setPort(port);
    return http;
  }

  protected ServerConnector createHttpsServerConnector(int port) {
    SslContextFactory.Server sslContextFactory = createSslContextFactory();
    HttpConfiguration httpsConfig = new HttpConfiguration();
    httpsConfig.addCustomizer(new SecureRequestCustomizer());
    httpsConfig.setSendServerVersion(false);
    httpsConfig.setSendDateHeader(false);
    ServerConnector https = new ServerConnector(m_server, new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(httpsConfig));
    https.setPort(port);
    return https;
  }

  protected SslContextFactory.Server createSslContextFactory() {
    SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
    String keyStorePath = resolveKeyStorePath(CONFIG.getPropertyValue(ScoutJettyKeyStorePathProperty.class));
    String storepass = CONFIG.getPropertyValue(ScoutJettyKeyStorePasswordProperty.class);
    String keypass = CONFIG.getPropertyValue(ScoutJettyPrivateKeyPasswordProperty.class);
    String certAlias = CONFIG.getPropertyValue(ScoutJettyCertificateAliasProperty.class);
    LOG.info("Setup SSL certificate using alias '{}' from keystore '{}':\n{}", certAlias, keyStorePath, SecurityUtility.keyStoreToHumanReadableText(keyStorePath, storepass, null));
    sslContextFactory.setKeyStorePath(keyStorePath);
    sslContextFactory.setKeyStorePassword(storepass);
    sslContextFactory.setKeyManagerPassword(keypass);
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

  protected String resolveKeyStorePath(String path) {
    if (path.startsWith("classpath:")) {
      String subPath = path.substring(10);
      URL res;
      res = getClass().getResource(subPath);
      if (res == null) {
        res = getClass().getClassLoader().getResource(subPath);
      }
      if (res == null) {
        res = ClassLoader.getSystemClassLoader().getResource(subPath);
      }
      if (res == null) {
        throw new ProcessingException("Missing resource defined by config property: {}={}",
            BEANS.get(ScoutJettyKeyStorePathProperty.class).getKey(),
            path);
      }
      return res.toExternalForm();
    }
    return path;
  }

  protected static class P_WebAppContext extends WebAppContext {

    public P_WebAppContext(String webApp, String contextPath) {
      super(webApp, contextPath);
      _scontext = new P_Context();
    }

    /**
     * Implementation hint: This class must not be an anonymous class and must have 'public' visibility. That is because
     * some JAX-WS implementors like METRO uses reflection to access it's methods.
     */
    public class P_Context extends Context {

      /**
       * Overwritten to enable resolution of resources contained in other JARs. That is according to the method's
       * JavaDoc specification: <i>The path must begin with a / and is interpreted as relative to the current context
       * root, or relative to the /META-INF/resources directory of a JAR file inside the web application's /WEB-INF/lib
       * directory.</>
       *
       * @see javax.servlet.ServletContext#getResource(String)
       */
      @Override
      public URL getResource(String path) throws MalformedURLException {
        // 1. Look for a web application resource.
        URL url = super.getResource(path);
        if (url != null) {
          return url;
        }
        // 2. Look for a dependent JAR resource (relative to META-INF/resources).
        url = getClassLoader().getResource("META-INF/resources" + path);
        if (url != null) {
          return url;
        }

        // 3. In Dev mode only: The resource might be directly on the classpath because the IDE build copies it to the output dir.
        //                      Maven on the other hand copies it to outputDir/META-INF/resources
        if (new PlatformDevModeProperty().getValue()) {
          return getClassLoader().getResource(path);
        }
        return null;
      }

      /**
       * Overwritten to enable resolution of resources contained in other JARs. That is according to the method's
       * JavaDoc specification: <i>The path must begin with a / and is interpreted as relative to the current context
       * root, or relative to the '/META-INF/resources' directory of a JAR file inside the web application's
       * '/WEB-INF/lib' directory.</>
       *
       * @see javax.servlet.ServletContext#getResourcePaths(String)
       */
      @Override
      public Set<String> getResourcePaths(String path) {
        Set<String> resources = new HashSet<>();

        // 1. Find resources contained in other, dependent JARs.
        resources.addAll(getResourcePathsFromDependentJars(getClassLoader(), path));

        // 2. Find resource in the web application's resources (higher precedence).
        resources.addAll(super.getResourcePaths(path));
        return resources;
      }
    }
  }

  /**
   * {@link Handler} that can be set as the {@link Server}s main handler. It wraps the given {@link ContextHandler} and
   * redirects all GET requests for URIs outside of the context path to the context path. Non-GET requests are
   * <i>not</i> redirected.
   * <p>
   * Example for contextPath = <code>/myapp</code>:
   * <table border=1 cellspacing=0 cellpadding=3>
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

      // Otherwise redirect to the specified context path (while preserving all other parts of the URI)
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

  /**
   * Returns a directory-like listing of all the paths to resources which are located in other (dependent) JAR files.
   * Those JAR files are placed in the web application's '/WEB-INF/lib' directory, and their resources located in the
   * '/META-INF/resources' folder.
   * <p>
   * This method works for both, packed and unpacked JAR files, which is crucial when running the application from
   * within the IDE. see 'javax.servlet.ServletContext.getResourcePaths(String)' for the specification.
   */
  protected static Set<String> getResourcePathsFromDependentJars(ClassLoader classloader, String path) {
    path = path.endsWith(URIUtil.SLASH) ? path : path + URIUtil.SLASH;

    Set<String> resources = new HashSet<>();

    // Look for resources in the dependent JAR's resources relative to /META-INF/resources.
    try {
      Enumeration<URL> resourceUrls = classloader.getResources("META-INF/resources" + path);
      while (resourceUrls.hasMoreElements()) {
        URL resourceUrl = resourceUrls.nextElement();
        String absoluteResourcePath = resourceUrl.getPath();

        if ("jar".equals(resourceUrl.toURI().getScheme())) {
          // The resource is located within a packed JAR. (e.g. located in Maven repository)
          resources.addAll(listFilesFromJar(absoluteResourcePath, path));
        }
        else {
          // The resource is located within an unpacked JAR, which typically applies when running the server from within the IDE.
          resources.addAll(listFilesFromDirectory(absoluteResourcePath, path));
        }
      }
    }
    catch (URISyntaxException | IOException e) {
      LOG.error("Failed to get resource paths", e);
    }

    return resources;
  }

  /**
   * Returns all direct files contained in the directory 'absoluteDirectoryPath'.
   */
  protected static Set<String> listFilesFromDirectory(String absoluteDirectoryPath, String relativeDirectorySearchPath) {
    File[] listFiles = new File(absoluteDirectoryPath).listFiles();
    if (listFiles == null || listFiles.length < 1) {
      return Collections.emptySet();
    }

    Set<String> resources = new HashSet<>(listFiles.length);
    for (File file : listFiles) {
      resources.add(relativeDirectorySearchPath + file.getName() + (file.isDirectory() ? URIUtil.SLASH : ""));
    }
    return resources;
  }

  /**
   * Returns all direct files contained in a JAR in the directory 'absoluteDirectoryPath'.
   */
  protected static Set<String> listFilesFromJar(String absoluteDirectoryPath, String relativeDirectorySearchPath) throws IOException {
    Set<String> resources = new HashSet<>();

    String absoluteJarFilePath = absoluteDirectoryPath.substring(0, absoluteDirectoryPath.indexOf('!')); // path to the JAR file.

    @SuppressWarnings("bsiRulesDefinition:htmlInString")
    Pattern childResourcePattern = Pattern.compile("^META-INF/resources(?<resourcePath>" + relativeDirectorySearchPath + "[^/]+)(?<slashIfDirectory>/?)$");
    try (JarFile jarFile = new JarFile(new URL(absoluteJarFilePath).getFile())) {
      Enumeration<? extends JarEntry> jarEntries = jarFile.entries();
      while (jarEntries.hasMoreElements()) {
        final JarEntry entry = jarEntries.nextElement();
        Matcher matcher = childResourcePattern.matcher(entry.getName());
        if (matcher.find()) {
          resources.add(matcher.group("resourcePath") + matcher.group("slashIfDirectory"));
        }
      }
    }

    return resources;
  }
}

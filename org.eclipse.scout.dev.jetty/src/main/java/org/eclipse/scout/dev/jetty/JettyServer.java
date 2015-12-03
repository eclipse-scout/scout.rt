/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyServer {

  private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);

  public static final String WEB_APP_FOLDER_KEY = "scout.jetty.webapp.folder";
  public static final String WEB_APP_CONTEXT_PATH = "scout.jetty.webapp.contextpath";
  public static final String SERVER_PORT_KEY = "scout.jetty.port"; // see also org.eclipse.scout.rt.server.services.common.clustersync.ClusterSynchronizationService.createNodeId()

  public static void main(String[] args) throws Exception {
    new JettyServer().start();
  }

  protected void start() throws Exception {
    // read folder
    File webappFolder = null;
    String webappParam = System.getProperty(WEB_APP_FOLDER_KEY);
    if (webappParam == null || webappParam.isEmpty()) {
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
        LOG.error("Error while parsing value '{}' for property. Using default port {} instead. {}", portConfig, port, SERVER_PORT_KEY, e);
      }
    }

    String contextPath = "/";
    String contextPathConfig = System.getProperty(WEB_APP_CONTEXT_PATH);
    if (contextPathConfig != null && StringUtility.hasText(contextPathConfig)) {
      if (!contextPathConfig.startsWith("/")) {
        contextPathConfig = "/" + contextPathConfig;
      }
      contextPath = contextPathConfig;
    }

    WebAppContext webApp = createWebApp(webappFolder, contextPath);
    Server server = new Server(port);
    server.setHandler(webApp);
    server.start();
    if (LOG.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Server ready. To run the application, open one of the following addresses in a web browser:\n");
      sb.append("---------------------------------------------------------------------\n");
      sb.append("  http://localhost:").append(port).append(contextPath).append("\n");
      String hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
      String ip = InetAddress.getLocalHost().getHostAddress();
      sb.append("  http://").append(hostname).append(":").append(port).append(contextPath).append("\n");
      if (StringUtility.notEqualsIgnoreCase(hostname, ip)) {
        sb.append("  http://").append(ip).append(":").append(port).append(contextPath).append("\n");
      }
      sb.append("---------------------------------------------------------------------\n");
      LOG.info(sb.toString());
    }
  }

  protected WebAppContext createWebApp(File webappDir, String contextPath) throws Exception {
    String resourceBase = webappDir.getAbsolutePath();
    WebAppContext webAppContext = new P_WebAppContext();
    webAppContext.setThrowUnavailableOnStartupException(true);

    webAppContext.setContextPath(contextPath);
    webAppContext.setResourceBase(resourceBase);
    webAppContext.setParentLoaderPriority(true);
    LOG.info("Starting Jetty with resourceBase={}", resourceBase);

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

  protected class P_WebAppContext extends WebAppContext {

    public P_WebAppContext() {
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
        else {
          // 2. Look for a dependent JAR resource (relative to META-INF/resources).
          return getClassLoader().getResource("META-INF/resources" + path);
        }
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
        resources.addAll(JettyServer.getResourcePathsFromDependentJars(getClassLoader(), path));

        // 2. Find resource in the web application's resources (higher precedence).
        resources.addAll(super.getResourcePaths(path));
        return resources;
      }
    }
  }

  /**
   * Returns a directory-like listing of all the paths to resources which are located in other (dependent) JAR files.
   * Those JAR files are placed in the web application's '/WEB-INF/lib' directory, and their resources located in the
   * '/META-INF/resources' folder.
   * <p>
   * This method works for both, packed and unpacked JAR files, which is crucial when running the application from
   * within the IDE.
   *
   * @see 'javax.servlet.ServletContext.getResourcePaths(String)' for the specification.
   */
  static Set<String> getResourcePathsFromDependentJars(ClassLoader classloader, String path) {
    path = path.endsWith(URIUtil.SLASH) ? path : path + URIUtil.SLASH;

    Set<String> resources = new HashSet<>();

    // Look for resources in the dependent JAR's resources relative to /META-INF/resources.
    try {
      Enumeration<URL> resourceUrls = classloader.getResources("META-INF/resources" + path);
      while (resourceUrls.hasMoreElements()) {
        URL resourceUrl = resourceUrls.nextElement();
        String absoluteResourcePath = resourceUrl.getPath();

        if (resourceUrl.toURI().getScheme().equals("jar")) {
          // The resource is located within a packed JAR. (e.g. located in Maven repository)
          resources.addAll(JettyServer.listFilesFromJar(absoluteResourcePath, path));
        }
        else {
          // The resource is located within an unpacked JAR, which typically applies when running the server from within the IDE.
          resources.addAll(JettyServer.listFilesFromDirectory(absoluteResourcePath, path));
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
  private static Set<String> listFilesFromDirectory(String absoluteDirectoryPath, String relativeDirectorySearchPath) {
    Set<String> resources = new HashSet<>();

    for (File file : new File(absoluteDirectoryPath).listFiles()) {
      resources.add(relativeDirectorySearchPath + file.getName() + (file.isDirectory() ? URIUtil.SLASH : ""));
    }

    return resources;
  }

  /**
   * Returns all direct files contained in a JAR in the directory 'absoluteDirectoryPath'.
   */
  private static Set<String> listFilesFromJar(String absoluteDirectoryPath, String relativeDirectorySearchPath) throws IOException {
    Set<String> resources = new HashSet<>();

    String absoluteJarFilePath = absoluteDirectoryPath.substring(0, absoluteDirectoryPath.indexOf('!')); // path to the JAR file.

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

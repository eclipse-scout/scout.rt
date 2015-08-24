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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class JettyServer {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JettyServer.class);

  public static final String WEB_APP_FOLDER_KEY = "scout.jetty.webapp.folder";
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
        LOG.error("Error while parsing value '" + portConfig + "' for property. Using default port " + port + " instead." + SERVER_PORT_KEY, e);
      }
    }

    WebAppContext webApp = createWebApp(webappFolder);
    Server server = new Server(port);
    server.setHandler(webApp);
    server.start();
  }

  protected WebAppContext createWebApp(File webappDir) throws Exception {
    String resourceBase = webappDir.getAbsolutePath();
    WebAppContext webAppContext = new P_WebAppContext();
    webAppContext.setThrowUnavailableOnStartupException(true);
    webAppContext.setContextPath("/");
    webAppContext.setResourceBase(resourceBase);
    webAppContext.setParentLoaderPriority(true);
    LOG.info("Starting Jetty with resourceBase=" + resourceBase);

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
       * root, or relative to the /META-INF/resources directory of a JAR file inside the web application's /WEB-INF/lib
       * directory.</>
       *
       * @see javax.servlet.ServletContext#getResourcePaths(String)
       */
      @Override
      public Set<String> getResourcePaths(String path) {
        Set<String> resources = new HashSet<>();

        // Look for resources in the dependent JAR's resources relative to /META-INF/resources.
        try {
          Enumeration<URL> urls = getClassLoader().getResources("META-INF/resources" + path);
          while (urls.hasMoreElements()) {
            File resource = new File(urls.nextElement().getPath());
            if (!resource.exists()) {
              LOG.error("resource not found: " + resource);
              continue;
            }

            if (resource.isDirectory()) {
              String directoryPath = (path.endsWith(URIUtil.SLASH) ? path : path + URIUtil.SLASH);
              for (String resourceName : resource.list()) {
                resources.add(directoryPath + resourceName);
              }
            }
            else {
              resources.add(path);
            }
          }
        }
        catch (IOException e) {
          LOG.error("Failed to resolve resources of JARs inside the web application's /WEB-INF/lib directory", e);
        }

        // Look for the resource in the web application's resources (higher precedence).
        resources.addAll(super.getResourcePaths(path));
        return resources;
      }
    }
  }
}

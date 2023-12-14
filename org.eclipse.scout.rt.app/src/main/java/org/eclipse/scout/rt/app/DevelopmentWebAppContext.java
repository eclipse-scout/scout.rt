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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom web app context that supports resolution of resources contained in other JARs, used for development
 * environments.
 */
public class DevelopmentWebAppContext extends WebAppContext {

  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentWebAppContext.class);

  public DevelopmentWebAppContext(String webApp, String contextPath) {
    super(webApp, contextPath);
    _scontext = new P_Context();
  }

  /**
   * Returns a directory-like listing of all the paths to resources which are located in other (dependent) JAR files.
   * Those JAR files are placed in the web application's '/WEB-INF/lib' directory, and their resources located in the
   * '/META-INF/resources' folder.
   * <p>
   * This method works for both, packed and unpacked JAR files, which is crucial when running the application from
   * within the IDE. see 'javax.servlet.ServletContext.getResourcePaths(String)' for the specification.
   */
  protected Set<String> getResourcePathsFromDependentJars(ClassLoader classloader, String path) {
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
  protected Set<String> listFilesFromDirectory(String absoluteDirectoryPath, String relativeDirectorySearchPath) {
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
  protected Set<String> listFilesFromJar(String absoluteDirectoryPath, String relativeDirectorySearchPath) throws IOException {
    Set<String> resources = new HashSet<>();

    String absoluteJarFilePath = absoluteDirectoryPath.substring(0, absoluteDirectoryPath.indexOf('!')); // path to the JAR file.

    @SuppressWarnings("bsiRulesDefinition:htmlInString")
    Pattern childResourcePattern = Pattern.compile("^META-INF/resources(?<resourcePath>" + relativeDirectorySearchPath + "[^/]+)(?<slashIfDirectory>/?)$");
    try (JarFile jarFile = new JarFile(new URL(absoluteJarFilePath).getFile())) {
      Enumeration<? extends JarEntry> jarEntries = jarFile.entries();
      while (jarEntries.hasMoreElements()) {
        JarEntry entry = jarEntries.nextElement();
        Matcher matcher = childResourcePattern.matcher(entry.getName());
        if (matcher.find()) {
          resources.add(matcher.group("resourcePath") + matcher.group("slashIfDirectory"));
        }
      }
    }

    return resources;
  }

  /**
   * Implementation hint: This class must not be an anonymous class and must have 'public' visibility. That is because
   * some JAX-WS implementors like METRO uses reflection to access its methods.
   */
  public class P_Context extends Context {

    /**
     * Overwritten to enable resolution of resources contained in other JARs. That is according to the method's JavaDoc
     * specification: <i>The path must begin with a / and is interpreted as relative to the current context root, or
     * relative to the /META-INF/resources directory of a JAR file inside the web application's /WEB-INF/lib
     * directory.</>
     *
     * @see jakarta.servlet.ServletContext#getResource(String)
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
     * Overwritten to enable resolution of resources contained in other JARs. That is according to the method's JavaDoc
     * specification: <i>The path must begin with a / and is interpreted as relative to the current context root, or
     * relative to the '/META-INF/resources' directory of a JAR file inside the web application's '/WEB-INF/lib'
     * directory.</>
     *
     * @see jakarta.servlet.ServletContext#getResourcePaths(String)
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

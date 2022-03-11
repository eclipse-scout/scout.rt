/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for reading the {@link Manifest} file of a JAR.
 */
@ApplicationScoped
public class JarManifestHelper {

  private static final Logger LOG = LoggerFactory.getLogger(JarManifestHelper.class);

  public static final String MANIFEST_ATTRIBUTE_BUILD_TIME = "Build-Time";

  /**
   * See maven-master property 'maven.build.timestamp.format'
   */
  @SuppressWarnings("SpellCheckingInspection")
  public static final String MANIFEST_BUILD_TIME_PATTERN = "yyyyMMdd-HHmmss";

  /**
   * @return map with all attributes of {@link Manifest} defined by JAR containing the given
   *         {@code manifestResourceBase} class or empty map if reading attributes fails.
   */
  public Map<String, String> getAttributes(Class<?> manifestResourceBase) {
    try {
      // (1) Resolve classpath of manifestResourceBase within JAR file
      String jarClassPath = toJarClasspath(manifestResourceBase);
      if (jarClassPath == null) {
        return CollectionUtility.emptyHashMap(); // Class not loaded from a JAR, e.g. no manifest available
      }

      // (2) Read manifest of defining JAR
      URL url = new URL(jarClassPath);
      URLConnection conn = url.openConnection();
      if (conn instanceof JarURLConnection) {
        JarURLConnection jarConn = (JarURLConnection) conn;
        // read all main attributes
        Map<String, String> attributes = new HashMap<>(extractAttributes(jarConn.getManifest().getMainAttributes()));
        // read all other attributes
        jarConn.getManifest().getEntries().values().forEach(entry -> attributes.putAll(extractAttributes(entry)));
        return attributes;
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to read manifest attributes", e);
    }
    return CollectionUtility.emptyHashMap();
  }

  /**
   * Reads the manifest of the JAR containing the given {@code manifestResourceBase} class and extracts the value for
   * the attribute specified by {@code attributeName}.
   *
   * @return value of manifest attribute
   */
  public String getAttribute(Class<?> manifestResourceBase, String attributeName) {
    return getAttributes(manifestResourceBase).get(attributeName);
  }

  /**
   * Reads the manifest of the JAR containing the given {@code manifestResourceBase} class and extracts the value for
   * the {@value #MANIFEST_ATTRIBUTE_BUILD_TIME} attribute.
   *
   * @return value of manifest attribute {@value #MANIFEST_ATTRIBUTE_BUILD_TIME}
   */
  public Date getBuildDateAttribute(Class<?> manifestResourceBase) {
    return DateUtility.parse(getAttribute(manifestResourceBase, MANIFEST_ATTRIBUTE_BUILD_TIME), MANIFEST_BUILD_TIME_PATTERN);
  }

  protected String toJarClasspath(Class<?> manifestResourceBase) {
    // resolve classPath of class within jar file
    String className = manifestResourceBase.getSimpleName() + ".class";
    String classPath = manifestResourceBase.getResource(className).toString();
    if (classPath.startsWith("jar")) {
      return classPath;
    }
    return null;
  }

  protected Map<String, String> extractAttributes(Attributes attributes) {
    return attributes.entrySet().stream()
        .collect(Collectors.toMap(
            e -> Objects.toString(e.getKey()), // null key transformed to "null"
            e -> ObjectUtility.toString(e.getValue()), // null value transformed to null
            StreamUtility.ignoringMerger()));
  }
}

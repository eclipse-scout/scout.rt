/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Utility to extract config.ini entries of the formats
 * <p>
 * <li>classOrInterfaceName#propertyName=value</li>
 * <li>classOrInterfaceName/filter#propertyName=value</li>
 * <p>
 * Example entries
 * <li>com.myapp.MyService#realm=MyRealm</li>
 * <li>realm=MyRealm2</li>
 * <li>com.myapp.MyService#realm=${realm}</li>
 * <li>com.myapp.MyService/process#realm=ExecRealm</li>
 * <li>com.myapp.MyService/query#realm=QueryRealm</li>
 * <p>
 * Supports for config.ini files in an external area
 * 
 * <pre>
 * <code>
 * external.configuration.area=[url | file | ${variable}]
 * external.configuration.area.0=[url | file | ${variable}]
 * external.configuration.area.1=[url | file | ${variable}]
 * external.configuration.area.2=[url | file | ${variable}]
 * ...
 * </code>
 * </pre>
 * 
 * where ${variable} is a system property or an environment variable containing an url or a file path to the FOLDER
 * containing the config.ini
 */
public final class ConfigIniUtility {
  private static final Pattern CONFIG_LINE_PATTERN = Pattern.compile("([^#/]+)(/[^#]+)?\\#([^=]+)=(.*)");
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ConfigIniUtility.class);
  private static ConfigIniProperty[] configProperties;

  private ConfigIniUtility() {
  }

  static {
    ArrayList<ConfigIniProperty> parsingList = new ArrayList<ConfigIniProperty>();
    ArrayList<String> externalConfigPaths = new ArrayList<String>();
    parseLocalConfigIniFile(parsingList, externalConfigPaths);
    parseExternalConfigIniFiles(parsingList, externalConfigPaths);
    configProperties = parsingList.toArray(new ConfigIniProperty[parsingList.size()]);
    if (LOG.isInfoEnabled()) {
      LOG.info("Config Bean Properties");
      for (ConfigIniProperty p : configProperties) {
        LOG.info(" " + p.getBeanName() + "#" + p.getPropertyName() + (p.getFilter() != null ? "/" + p.getFilter() : "") + "=" + p.getValue());
      }
    }
  }

  public static Map<String, String> getProperties(Class beanType) {
    return getProperties(beanType, null);
  }

  public static Map<String, String> getProperties(Class beanType, String filter) {
    Map<String, String> props = new HashMap<String, String>();
    for (ConfigIniProperty p : configProperties) {
      if (matchesBeanClass(p, beanType, filter)) {
        String v = p.getValue();
        if (v != null && v.length() == 0) {
          v = null;
        }
        props.put(p.getPropertyName(), v);
      }
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("Properties for " + beanType.getName());
      for (Map.Entry<String, String> e : props.entrySet()) {
        LOG.info(" " + e.getKey() + "=" + e.getValue());
      }
    }
    return props;
  }

  private static void parseLocalConfigIniFile(List<ConfigIniProperty> parsingList, List<String> externalConfigPaths) {
    String file = "config.ini";
    BufferedReader in = null;
    try {
      URL url = null;
      if (Platform.inDevelopmentMode()) {
        url = new URL(Platform.getConfigurationLocation().getURL(), file);
      }
      else {
        URL installLocationUrl = Platform.getInstallLocation().getURL();
        url = new URL(installLocationUrl, "configuration/" + file);
      }
      in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
      String line;
      while ((line = in.readLine()) != null) {
        ConfigIniProperty p = parseConfigIniLine(line);
        if (p != null) {
          parsingList.add(p);
        }
        if (line.matches("external.configuration.area(\\.[0-9]+)?=.*")) {
          externalConfigPaths.add(line.split("=", 2)[1]);
        }
        if (line.matches("osgi.sharedConfiguration.area=.*")) {
          externalConfigPaths.add(line.split("=", 2)[1]);
        }
      }
    }
    catch (Throwable t) {
      if (in != null) {
        LOG.error("parsing " + file, t);
      }
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (Throwable fatal) {
        }
      }
    }
  }

  private static void parseExternalConfigIniFiles(List<ConfigIniProperty> parsingList, List<String> externalConfigPaths) {
    for (String path : externalConfigPaths) {
      BufferedReader in = null;
      String resolvedPath = null;
      try {
        URL url = null;
        resolvedPath = path.replaceAll("\\\\(.)", "$1");
        if (resolvedPath.matches("\\$\\{.*\\}.*")) {
          String tail = resolvedPath.replaceAll("\\$\\{.*\\}", "");
          String variable = resolvedPath.replaceAll(tail, "");
          variable = variable.substring(2, variable.length() - 1);
          resolvedPath = System.getProperty(variable, null);
          if (resolvedPath == null) {
            resolvedPath = System.getenv(variable);
          }
          resolvedPath += tail;
        }
        if (resolvedPath == null) {
          continue;
        }
        resolvedPath = resolvedPath.replace("@user.home", System.getProperty("user.home"));
        resolvedPath = resolvedPath.replace("@user.dir", System.getProperty("user.dir"));
        File f1 = new File(resolvedPath);
        if (f1.exists()) {
          if (f1.isFile()) {
            url = f1.toURI().toURL();
          }
          else {
            url = new File(resolvedPath, "config.ini").toURI().toURL();
          }
        }
        else {
          if (resolvedPath.toLowerCase().endsWith(".ini") || resolvedPath.toLowerCase().endsWith(".properties")) {
            url = new URL(resolvedPath);
          }
          else {
            url = new URL(new URL(resolvedPath), "config.ini");
          }
        }
        in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
          ConfigIniProperty p = parseConfigIniLine(line);
          if (p != null) {
            parsingList.add(p);
          }
        }
      }
      catch (Throwable t) {
        if (in != null) {
          LOG.error("parsing external file " + path + " (" + resolvedPath + ")", t);
        }
      }
      finally {
        if (in != null) {
          try {
            in.close();
          }
          catch (Throwable fatal) {
          }
        }
      }
    }
  }

  private static ConfigIniProperty parseConfigIniLine(String configLine) {
    if (configLine != null) {
      configLine = configLine.replaceAll("\\\\(.)", "$1");
      Matcher m = CONFIG_LINE_PATTERN.matcher(configLine);
      if (m.matches()) {
        String filter = m.group(2);
        if (filter != null && filter.startsWith("/")) {
          // ok
        }
        else {
          filter = null;
        }
        ConfigIniProperty p = new ConfigIniProperty(m.group(1), filter, m.group(3), BundleContextUtility.resolve(m.group(4)));
        return p;
      }
    }
    return null;
  }

  /**
   * returns true if this property bean type is equal or a supertype of the
   * parameter type
   */
  @SuppressWarnings("unchecked")
  private static boolean matchesBeanClass(ConfigIniProperty p, Class parameterType, String filter) {
    try {
      Class myType = Class.forName(p.getBeanName(), false, parameterType.getClassLoader());
      if (myType.isAssignableFrom(parameterType)) {
        if (filter == null || p.getFilter() == null || filter.equals(p.getFilter())) {
          return true;
        }
      }
    }
    catch (Throwable t) {
    }
    return false;
  }

}

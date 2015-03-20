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
package org.eclipse.scout.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Utility to extract properties stored in the <code>config.ini</code> file of scout applications.<br>
 * Properties can be simple key-value-pairs or can use the class#property format: <code>
 * <ul>
 * <li>myKey=value</li>
 * <li>my.qualified.key=value</li>
 * <li>classOrInterfaceName#propertyName=value</li>
 * <li>classOrInterfaceName/filter#propertyName=value</li>
 * </ul>
 * </code> Other properties, system properties or environment variables may be accessed using
 * <code>${variableName}</code>. These variables are then resolved once when the properties are initialized.<br>
 * Furthermore it supports storing the configs in several external config files. In that case the property
 * <code>external.configuration.area</code> must be specified:
 * <p>
 * <code>
 * <ul>
 * <li>external.configuration.area=[url | file | ${variable}]</li>
 * <li>external.configuration.area.0=[url | file | ${variable}]</li>
 * <li>external.configuration.area.1=[url | file | ${variable}]</li>
 * <li>external.configuration.area.2=[url | file | ${variable}]</li>
 * </ul>
 * </code>
 * </p>
 * Examples: <code>
 * <ul>
 * <li>customProperty=customValue</li>
 * <li>user.area=/usr/exampleUser/</li>
 * <li>com.myapp.MyService#realm=MyRealm</li>
 * <li>com.myapp.MyService#realm=${customProperty}</li>
 * <li>com.myapp.MyService/process#realm=ProcessRealm</li>
 * <li>com.myapp.MyService/query#realm=QueryRealm</li>
 * <li>com.myapp.MyService#path=${user.home}/subfolder</li>
 * </ul>
 * </code>
 */
public final class ConfigIniUtility {

  // Do not statically initialize this variable using the 'ScoutLogManager' because ConfigIniUtility is used during the ScoutLogManager's static initialization; use 'getLogger()' instead.
  private static volatile IScoutLogger logger;

  /**
   * Name of the default configuration file
   */
  public static final String DEFAULT_CONFIG_FILE_NAME = "config.ini";

  /**
   * Property to specify the configuration file name. If not specified {@value #DEFAULT_CONFIG_FILE_NAME} is used.<br>
   * This property must be set as system property or command line property.<br>
   * The file is searched on the file system first and on the classpath afterwards.
   */
  public static final String KEY_CONFIG_FILE_NAME = "scout.config.file.name";

  /**
   * Property to specify the directory in which the configuration file is stored. If not specified, no directory is used
   * (root level).<br>
   * This property must be set as system property or command line property.<br>
   * The file is searched on the file system first and on the classpath afterwards.
   */
  public static final String KEY_CONFIG_FILE_DIR = "scout.config.file.dir";

  /**
   * Property to specify if the application is running in development mode. Default is <code>false</code>.
   */
  public static final String KEY_PLATFORM_DEV_MODE = "scout.dev.mode";

  /**
   * Property key for the pre-defined working directory variable.
   */
  public static final String KEY_WORKING_DIR = "working_dir";

  /**
   * char used as delimiter for the filter in the class#property format.
   */
  public static final char FILTER_DELIM = '/';

  private static final Pattern CONFIG_LINE_PATTERN = Pattern.compile("([^#" + FILTER_DELIM + "]+)(" + FILTER_DELIM + "[^#]*)?\\#([^=]+)");
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");
  private static final Map<String, String> configProperties;

  private ConfigIniUtility() {
  }

  static {
    configProperties = new HashMap<>();
    configProperties.put(KEY_WORKING_DIR, Paths.get(".").toAbsolutePath().normalize().toFile().toString());
    Set<String> externalConfigPaths = new LinkedHashSet<>();

    parseLocalConfigIniFile(externalConfigPaths);
    parseExternalConfigIniFiles(externalConfigPaths);

    resolveAll();
  }

  /**
   * Gets all properties that are defined for the given class. Only properties in the class#property format are used
   * that matches the given fully qualified class name. Properties with a filter defined are part of the result as well.
   *
   * @param beanType
   *          The class for which the properties should be returned. May not be <code>null</code>.
   * @return A {@link Map} holding all properties for the given class.
   */
  public static Map<String, String> getProperties(Class beanType) {
    return getProperties(beanType, null);
  }

  /**
   * Gets all properties that are defined for the given class and filter. Properties that matches the given class and
   * filter and properties matching the given class without filter are returned.
   *
   * @param beanType
   *          The class for which the properties should be returned. May not be <code>null</code>.
   * @param filter
   *          The filter the properties must fullfil. may be <code>null</code>. Then all properties for the given class
   *          are returned.
   * @return A {@link Map} holding all properties for the given class and filter.
   */
  public static Map<String, String> getProperties(Class beanType, String filter) {
    Map<String, String> props = new HashMap<>();
    for (Entry<String, String> entry : configProperties.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (value != null && value.length() > 0) {
        String classProperty = getClassProperty(key, beanType, filter);
        if (classProperty != null && classProperty.length() > 0) {
          // filter accepted
          String propName = key.substring(key.length() - classProperty.length());
          props.put(propName, value);
        }
      }
    }
    return props;
  }

  /**
   * Gets the property with given key. If there is no property with given key, <code>null</code> is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in properties defined in a <code>config.ini</code> or an external <code>config.ini</code>.</li>
   * <li>in the system properties ({@link System#getProperty(String)})</li>
   * <li>in the environment variables ({@link System#getenv(String)})</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @return The value of the given property or <code>null</code>.
   */
  public static String getProperty(String key) {
    return getProperty(key, null);
  }

  /**
   * Gets the property with given key. If there is no property with given key, the given default value is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in properties defined in a <code>config.ini</code> or an external <code>config.ini</code>.</li>
   * <li>in the system properties ({@link System#getProperty(String)})</li>
   * <li>in the environment variables ({@link System#getenv(String)})</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @return The value of the given property, the given default value if the property could not be found or
   *         <code>null</code> if the key is <code>null</code>.
   */
  public static String getProperty(String key, String defaultValue) {
    if (key == null) {
      return null;
    }

    // 1. App config (config.ini)
    String value = configProperties.get(key);
    if (StringUtility.hasText(value)) {
      return value;
    }

    // 2. system config
    value = System.getProperty(key);
    if (StringUtility.hasText(value)) {
      return value;
    }

    // 3. environment config
    value = System.getenv(key);
    if (StringUtility.hasText(value)) {
      return value;
    }

    return defaultValue;
  }

  /**
   * Gets the property with given key as boolean. If a property with given key does not exist or is no valid boolean
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid boolean associated with it.
   * @return The boolean value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static boolean getPropertyBoolean(String key, boolean defaultValue) {
    String rawValue = getProperty(key);
    if (rawValue == null) {
      return defaultValue;
    }

    if (Boolean.TRUE.toString().equalsIgnoreCase(rawValue) || Boolean.FALSE.toString().equalsIgnoreCase(rawValue)) {
      return Boolean.parseBoolean(rawValue);
    }
    else {
      getLogger().warn("Invalid boolean-value for property '{}' configured: {}", key, rawValue);
      return defaultValue;
    }
  }

  /**
   * Gets the property with given key as int. If a property with given key does not exist or is no valid int
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid int associated with it.
   * @return The int value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static int getPropertyInt(String key, int defaultValue) {
    String valueRaw = getProperty(key, null);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Integer.valueOf(valueRaw);
    }
    catch (final NumberFormatException e) {
      getLogger().warn("Invalid int-value for property '{}' configured: {}", key, valueRaw);
      return defaultValue;
    }
  }

  /**
   * Gets the property with given key as long. If a property with given key does not exist or is no valid long
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid long associated with it.
   * @return The long value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static long getPropertyLong(String key, long defaultValue) {
    String valueRaw = getProperty(key, null);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Long.valueOf(valueRaw);
    }
    catch (final NumberFormatException e) {
      getLogger().warn("Invalid long-value for property '{}' configured: {}", key, valueRaw);
      return defaultValue;
    }
  }

  /**
   * Gets the property with given key as float. If a property with given key does not exist or is no valid float
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid float associated with it.
   * @return The float value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static float getPropertyFloat(String key, float defaultValue) {
    String valueRaw = getProperty(key, null);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Float.valueOf(valueRaw);
    }
    catch (final NumberFormatException e) {
      getLogger().warn("Invalid float-value for property '{}' configured: {}", key, valueRaw);
      return defaultValue;
    }
  }

  /**
   * Gets the property with given key as double. If a property with given key does not exist or is no valid double
   * value, the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid double associated with it.
   * @return The double value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public static double getPropertyDouble(String key, double defaultValue) {
    String valueRaw = getProperty(key, null);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Double.valueOf(valueRaw);
    }
    catch (final NumberFormatException e) {
      getLogger().warn("Invalid double-value for property '{}' configured: {}", key, valueRaw);
      return defaultValue;
    }
  }

  /**
   * Resolves all variables of format <code>${variableName}</code> in the given expression according to the current
   * application context.
   *
   * @param s
   *          The expression to resolve.
   * @return A {@link String} where all variables have been replaced with their values.
   * @throws IllegalArgumentException
   *           if a variable could not be resolved in the current context.
   */
  public static String resolve(String s) {
    if (s == null || s.length() == 0) {
      return s;
    }
    String t = s;
    Matcher m = VARIABLE_PATTERN.matcher(t);
    while (m.find()) {
      String key = m.group(1);

      String value = getProperty(key);
      if (!StringUtility.hasText(value)) {
        throw new IllegalArgumentException("resolving expression '" + s + "': variable ${" + key + "} is not defined in the context.");
      }

      try {
        // in case it is an URL: make absolute file path
        value = new File(new URL(value).getFile()).getAbsolutePath();
      }
      catch (Exception e) {
      }

      t = t.substring(0, m.start()) + value + t.substring(m.end());
      // next
      m = VARIABLE_PATTERN.matcher(t);
    }

    // legacy formats:
    t = t.replace("@user.home", getProperty("user.home"));
    t = t.replace("@user.dir", getProperty("user.dir"));
    return t;
  }

  protected static String getClassProperty(String key, Class beanType, String filter) {
    Matcher m = CONFIG_LINE_PATTERN.matcher(key);
    if (m.matches()) {
      String clazz = m.group(1);
      String f = m.group(2);
      String prop = m.group(3);

      if (f != null && (f.length() < 1 || f.charAt(0) != FILTER_DELIM)) {
        f = null;
      }

      try {
        if (filter == null || f == null || filter.equals(f)) {
          Class<?> myType = Class.forName(clazz, false, beanType.getClassLoader());
          if (myType.isAssignableFrom(beanType)) {
            return prop;
          }
        }
      }
      catch (Exception e) {
        //nop
      }
    }
    return null;
  }

  protected static void parseLocalConfigIniFile(Set<String> externalConfigPaths) {
    URL url = getConfigIniUrl();
    if (url == null) {
      // don't log here because the logger needs this class (cyclic dependencies)
      System.err.println("No configuration area found. Running with empty configuration.");
      return;
    }
    parseConfigIni(url, externalConfigPaths);
  }

  protected static URL getConfigIniUrl() {
    String fileName = resolve(System.getProperty(KEY_CONFIG_FILE_NAME));
    if (!StringUtility.hasText(fileName)) {
      fileName = DEFAULT_CONFIG_FILE_NAME;
    }

    String dir = resolve(System.getProperty(KEY_CONFIG_FILE_DIR));
    File configFile = new File(dir, fileName);

    // check file-system first
    if (configFile.exists() && !configFile.isDirectory() && configFile.canRead()) {
      try {
        URL ret = configFile.toURI().toURL();
        return ret;
      }
      catch (MalformedURLException e) {
        // don't log this exception because the logger needs this class (cyclic dependencies)
      }
    }

    // check classpath
    String nameOnClassPath = configFile.toString().replace('\\', '/');
    return ConfigIniUtility.class.getClassLoader().getResource(nameOnClassPath);
  }

  protected static void parseExternalConfigIniFiles(Set<String> externalConfigPaths) {
    for (String path : externalConfigPaths) {
      URL url = getExternalConfigIniUrl(path);
      if (url != null) {
        parseConfigIni(url, null /* no second level of external files*/);
      }
    }
  }

  protected static void resolveAll() {
    for (Entry<String, String> entry : configProperties.entrySet()) {
      entry.setValue(resolve(entry.getValue()));
    }
  }

  protected static void parseConfigIni(URL configIniUrl, Set<String> externalConfigPaths) {
    Properties props = new Properties();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(configIniUrl.openStream(), "UTF-8"))) {
      props.load(in);
    }
    catch (Exception t) {
      // Do not use logger here. Because the logger uses this class, it is not yet initialized
      t.printStackTrace();
      return;
    }

    if (externalConfigPaths != null) {
      Object sharedConfigArea = props.remove("osgi.sharedConfiguration.area"); // legacy
      if (sharedConfigArea != null) {
        externalConfigPaths.add(sharedConfigArea.toString());
      }
    }

    Pattern externalConfigFilePattern = Pattern.compile("external\\.configuration\\.area(\\.[0-9]+)?");
    for (Entry<Object, Object> entry : props.entrySet()) {
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if (key != null && value != null) {
        if (externalConfigFilePattern.matcher(key).matches()) {
          if (externalConfigPaths != null) {
            externalConfigPaths.add(value);
          }
        }
        else {
          configProperties.put(key, value);
        }
      }
    }

    if (externalConfigPaths != null) {
      String furtherExternalConfigFile = resolve(System.getProperty("external.configuration.file"));
      if (furtherExternalConfigFile != null) {
        externalConfigPaths.add(furtherExternalConfigFile);
      }
    }
  }

  protected static URL getExternalConfigIniUrl(String externalPath) {
    URL url = null;
    String resolvedPath = resolve(externalPath.replaceAll("\\\\(.)", "$1"));
    File f = new File(resolvedPath);

    try {
      if (f.exists()) {
        if (f.isFile()) {
          url = f.toURI().toURL();
        }
        else {
          url = new File(resolvedPath, DEFAULT_CONFIG_FILE_NAME).toURI().toURL();
        }
      }
      else {
        if (resolvedPath.toLowerCase().endsWith(".ini") || resolvedPath.toLowerCase().endsWith(".properties")) {
          url = new URL(resolvedPath);
        }
        else {
          url = new URL(new URL(resolvedPath), DEFAULT_CONFIG_FILE_NAME);
        }
      }
    }
    catch (Exception e) {
      // Do not use logger here. Because the logger uses this class, it is not yet initialized
      e.printStackTrace();
    }
    return url;
  }

  /**
   * @return creates a {@link IScoutLogger} on first use; is necessary because {@link ScoutLogManager} uses this class
   *         during its initialization.
   */
  private static IScoutLogger getLogger() {
    if (logger != null) {
      return logger;
    }

    synchronized (ConfigIniUtility.class) {
      if (logger == null) {
        logger = ScoutLogManager.getLogger(ConfigIniUtility.class);
      }
      return logger;
    }
  }
}

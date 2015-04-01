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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Utility to extract properties stored in the <code>config.ini</code> file of scout applications.
 * <p>
 * The file is located on the classpath, typically in WEB-INF/classes/config.ini
 * <p>
 * It can also be specified by setting the system property <code>-Dconfig.ini=path-to-config.ini-file</code>
 * <p>
 * Properties can be simple key-value-pairs or can use the class#property format: <code>
 * <ul>
 * <li>myKey=value</li>
 * <li>my.qualified.key=value</li>
 * <li>classOrInterfaceName#propertyName=value</li>
 * <li>classOrInterfaceName/filter#propertyName=value</li>
 * </ul>
 * </code> Other properties, system properties or environment variables may be accessed using
 * <code>${variableName}</code>. These variables are then resolved once when the properties are initialized.<br>
 * </p>
 * Examples: <code>
 * <ul>
 * <li>customProperty=customValue</li>
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
   * Property to specify the configuration file. If not specified then {@link ClassLoader#getResource(String)} with
   * "/config.ini" is used.
   */
  public static final String CONFIG_INI = "config.ini";

  /**
   * Property to specify if the application is running in development mode. Default is <code>false</code>.
   */
  public static final String KEY_PLATFORM_DEV_MODE = "scout.dev.mode";

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
    URL url = getConfigIniUrl();
    if (url != null) {
      parseConfigIni(url);
      resolveAll();
    }
    else {
      // don't log here because the logger needs this class (cyclic dependencies)
      System.err.println("No config.ini found. Running with empty configuration.");
    }
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

  protected static URL getConfigIniUrl() {
    String configIniFile = resolve(System.getProperty(CONFIG_INI));
    if (StringUtility.hasText(configIniFile)) {
      try {
        File file = new File(configIniFile);
        if (file.isFile() && file.exists() && file.canRead()) {
          return file.toURI().toURL();
        }
      }
      catch (MalformedURLException e) {
        e.printStackTrace();
        return null;
      }
    }
    return ConfigIniUtility.class.getClassLoader().getResource("/" + CONFIG_INI);
  }

  protected static void resolveAll() {
    for (Entry<String, String> entry : configProperties.entrySet()) {
      entry.setValue(resolve(entry.getValue()));
    }
  }

  protected static void parseConfigIni(URL configIniUrl) {
    Properties props = new Properties();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(configIniUrl.openStream(), "UTF-8"))) {
      props.load(in);
    }
    catch (Exception t) {
      // Do not use logger here. Because the logger uses this class, it is not yet initialized
      t.printStackTrace();
      return;
    }
    for (Entry<Object, Object> entry : props.entrySet()) {
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if (key != null && value != null) {
        configProperties.put(key, value);
      }
    }
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

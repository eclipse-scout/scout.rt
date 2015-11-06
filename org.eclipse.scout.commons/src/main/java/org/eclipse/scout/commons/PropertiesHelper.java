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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to extract properties stored in properties files.
 * <p>
 * The file is located on the classpath, typically in WEB-INF/classes/xyz.properties or in development
 * src/main/resources
 * <p>
 * It can also be specified by setting the system property <code>-Dxyz.properties=path-to-xyz.properties-file</code>
 * <p>
 * Properties are simple key-value-pairs.<br>
 * Properties may contain placeholders for other variables: <code>${variableName}</code>. These variables are then
 * resolved once when the properties are initialized.<br>
 * </p>
 * Examples: <code>
 * <ul>
 * <li>customProperty=customValue</li>
 * <li>myProperty=${customProperty}/subfolder</li>
 * </ul>
 * </code>
 */
public class PropertiesHelper {

  private static final Logger LOG = LoggerFactory.getLogger(PropertiesHelper.class);
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

  private final Map<String, String> m_configProperties;
  private final boolean m_isInitialized;

  public PropertiesHelper(String fileName) {
    m_configProperties = new HashMap<>();
    URL url = getPropertiesFileUrl(fileName);
    if (url != null) {
      parse(url);
      resolveAll();
      m_isInitialized = true;
    }
    else {
      m_isInitialized = false;
    }
  }

  /**
   * Gets the property with given key. If there is no property with given key, <code>null</code> is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in the system properties ({@link System#getProperty(String)})</li>
   * <li>in properties defined in the file.</li>
   * <li>in the environment variables ({@link System#getenv(String)})</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @return The value of the given property or <code>null</code>.
   */
  public String getProperty(String key) {
    return getProperty(key, null);
  }

  /**
   * Gets the property with given key. If there is no property with given key, the given default value is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in the system properties ({@link System#getProperty(String)})</li>
   * <li>in properties defined in the file</li>
   * <li>in the environment variables ({@link System#getenv(String)})</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @return The value of the given property, the given default value if the property could not be found or
   *         <code>null</code> if the key is <code>null</code>.
   */
  public String getProperty(String key, String defaultValue) {
    if (key == null) {
      return null;
    }

    // system config
    String value = System.getProperty(key);
    if (StringUtility.hasText(value)) {
      return value;
    }

    // properties file
    value = m_configProperties.get(key);
    if (StringUtility.hasText(value)) {
      return value;
    }

    // environment config
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
  public boolean getPropertyBoolean(String key, boolean defaultValue) {
    String rawValue = getProperty(key);
    if (rawValue == null) {
      return defaultValue;
    }

    if (Boolean.TRUE.toString().equalsIgnoreCase(rawValue) || Boolean.FALSE.toString().equalsIgnoreCase(rawValue)) {
      return Boolean.parseBoolean(rawValue);
    }
    else {
      LOG.warn("Invalid boolean-value for property '{}' configured: {}", key, rawValue);
      return defaultValue;
    }
  }

  /**
   * Gets the property with given key as int. If a property with given key does not exist or is no valid int value, the
   * given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid int associated with it.
   * @return The int value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public int getPropertyInt(String key, int defaultValue) {
    String valueRaw = getProperty(key, null);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Integer.valueOf(valueRaw);
    }
    catch (final NumberFormatException e) {
      LOG.warn("Invalid int-value for property '{}' configured: {}", key, valueRaw);
      return defaultValue;
    }
  }

  /**
   * Gets the property with given key as long. If a property with given key does not exist or is no valid long value,
   * the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid long associated with it.
   * @return The long value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public long getPropertyLong(String key, long defaultValue) {
    String valueRaw = getProperty(key, null);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Long.valueOf(valueRaw);
    }
    catch (final NumberFormatException e) {
      LOG.warn("Invalid long-value for property '{}' configured: {}", key, valueRaw);
      return defaultValue;
    }
  }

  /**
   * Gets the property with given key as float. If a property with given key does not exist or is no valid float value,
   * the given default value is returned.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid float associated with it.
   * @return The float value of the given key or the given default value otherwise.
   * @since 5.1
   * @see #getProperty(String)
   */
  public float getPropertyFloat(String key, float defaultValue) {
    String valueRaw = getProperty(key, null);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Float.valueOf(valueRaw);
    }
    catch (final NumberFormatException e) {
      LOG.warn("Invalid float-value for property '{}' configured: {}", key, valueRaw);
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
  public double getPropertyDouble(String key, double defaultValue) {
    String valueRaw = getProperty(key, null);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Double.valueOf(valueRaw);
    }
    catch (final NumberFormatException e) {
      LOG.warn("Invalid double-value for property '{}' configured: {}", key, valueRaw);
      return defaultValue;
    }
  }

  /**
   * Gets all property key names defined in the loaded .properties file.
   *
   * @return A {@link Set} copy containing all property key names.
   */
  public Set<String> getAllPropertyNames() {
    return CollectionUtility.hashSet(m_configProperties.keySet());
  }

  /**
   * Gets all properties and the corresponding values loaded from the .properties file.
   *
   * @return A {@link Map} copy containing all entries.
   */
  public Map<String, String> getAllEntries() {
    return CollectionUtility.copyMap(m_configProperties);
  }

  /**
   * @return true if the config.properties contains the key
   */
  public boolean hasProperty(String key) {
    return m_configProperties.containsKey(key);
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
  public String resolve(String s) {
    if (s == null || s.length() == 0) {
      return s;
    }
    List<String> loopDetection = new ArrayList<>();
    String t = s;
    loopDetection.add(t);
    Matcher m = VARIABLE_PATTERN.matcher(t);
    while (m.find()) {
      if (loopDetection.size() > 10) {
        throw new IllegalArgumentException("resolving expression '" + s + "': possible loop detected (more than 10 steps): " + loopDetection);
      }
      String key = m.group(1);
      String value = getProperty(key);
      if (value != null && value.contains(s)) {
        throw new IllegalArgumentException("resolving expression '" + s + "': loop detected (the resolved value contains the original expression): " + value);
      }
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
      loopDetection.add(t);
      m = VARIABLE_PATTERN.matcher(t);
    }

    // legacy formats:
    t = t.replace("@user.home", getProperty("user.home"));
    t = t.replace("@user.dir", getProperty("user.dir"));
    return t;
  }

  /**
   * Specifies if the properties file has been found and loaded.
   *
   * @return <code>true</code> if a properties file has been loaded, <code>false</code> otherwise.
   */
  public boolean isInitialized() {
    return m_isInitialized;
  }

  protected static URL getPropertiesFileUrl(String fileName) {
    String propertiesFile = System.getProperty(fileName);
    if (StringUtility.hasText(propertiesFile)) {
      try {
        File file = new File(propertiesFile);
        if (file.isFile() && file.exists() && file.canRead()) {
          return file.toURI().toURL();
        }
      }
      catch (MalformedURLException e) {
        LOG.error("Unable to load '" + propertiesFile + "'.", e);
        return null;
      }
    }
    return PropertiesHelper.class.getClassLoader().getResource(fileName);
  }

  protected void resolveAll() {
    for (Entry<String, String> entry : m_configProperties.entrySet()) {
      entry.setValue(resolve(entry.getValue()));
    }
  }

  protected void parse(URL propertiesFileUrl) {
    Properties props = new Properties();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(propertiesFileUrl.openStream(), "ISO-8859-1" /* according to .properties file spec */))) {
      props.load(in);
    }
    catch (Exception t) {
      LOG.error("Unable to parse properties file from url '" + propertiesFileUrl.toExternalForm() + "'.", t);
      return;
    }
    for (Entry<Object, Object> entry : props.entrySet()) {
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if (key != null && value != null) {
        m_configProperties.put(key, value);
      }
    }
  }
}

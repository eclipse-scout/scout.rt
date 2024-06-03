/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class to extract properties stored in a config.properties file or one of its imports.
 * <p>
 * The file is located on the classpath, typically in WEB-INF/classes/config.properties or during development in
 * src/main/resources.<br>
 * It can also be specified by setting the system property "{@code config.properties}" that contains an {@link URL}
 * pointing to the actual properties file.
 * <p>
 * The following formats are supported:
 * <ul>
 * <li>Simple Key-Value pairs:<br>
 * <code>scout.sample.mykey=sample-value</code><br>
 * Such values can be retrieved using the corresponding methods. E.g.: {@link #getProperty(String)},
 * {@link #getPropertyInt(String, int)}, {@link #getPropertyBoolean(String, boolean)}, ...</li>
 * <li>List values:<br>
 * <code>scout.sample.mylist[0]=value-of-first<br>scout.sample.mylist[1]=value-of-second</code><br>
 * List values can be retrieved using the method {@link #getPropertyList(String)} or one of its overloads.</li>
 * <li>Map values:<br>
 * <code>scout.sample.mymap[first_map_key]=value-of-first<br>scout.sample.mymap[second_map_key]=value-of-second</code><br>
 * Map values can be retrieved using the method {@link #getPropertyMap(String)} or one of its overloads.</li>
 * </ul>
 * <p>
 * A key may be part of a namespace. This way the same key can exist multiple times in the same config file put can be
 * accessed using one {@link IConfigProperty} instance. Therefore such an instance can return different values for the
 * same key but different namespaces. A namespace is separated using the pipe character (|).
 * <p>
 * Properties may contain placeholders for other properties: <code>${variableName}</code>. These values are then
 * resolved once when the properties are initialized.
 * <p>
 * The special key "<code>import</code>" is reserved and expects an URL pointing to other .properties files.<br>
 * Such a file can be on the classpath (use {@code classpath:}) or any other supported URL.
 * <p>
 * Examples: <br>
 * <code>
 * <ul>
 * <li>import[0]=classpath:myConfigs/other.properties</li>
 * <li>import[settings]=file:/C:/path/to/my/settings.properties</li>
 * <li>customProperty=customValues</li>
 * <li>myProperty=${customProperty}/subfolders</li>
 * <li>my-namespace|myMap[key0]=value0</li>
 * <li>my-namespace|myMap[key1]=value1</li>
 * </ul>
 * </code>
 */
public final class ConfigUtility {

  /**
   * Optional file defining variables used in the config.properties. This feature is helpful when launching applications
   * in development mode in the IDE or as parameterized deployments using a pre-defined config.properties that contains
   * variables. The variable file only contains variables and values but is itself not a config.property file. Thus its
   * values are not checked in any {@link IConfigurationValidator}.
   * <p>
   * A variable file is passed to the java process using the system property
   * <code>-Dscout.env=file:/path/to/my/launch.properties</code>. Alternatively such a file may be imported using the
   * special key <code>importenv</code>.
   * <p>
   * Example content of such a launch.properties assuming it is placed in the root of the eclipse workspace resp. the
   * root of the IntelliJ project folder.
   *
   * <pre>
   *   jms.url=jms:/foo/bar
   *   workspace.dir=${CURRENT_DIR}
   * </pre>
   * <p>
   * Example of a config.properties that contains variables
   *
   * <pre>
   *   scout.services.myjmsprovider.url=${jms.url}
   *   scout.services.myjmsprovider.foobarDir=${workspace.dir}
   * </pre>
   */
  public static final String SCOUT_ENV_PROPERTY = "scout.env";

  /**
   * Property to specify the configuration file. If not specified then {@link #DEFAULT_PATH_TO_CONFIG_FILE} is used.
   */
  public static final String CONFIG_FILE_PROPERTY = "config.properties";

  /**
   * Default path to config.properties file.
   */
  public static final String DEFAULT_PATH_TO_CONFIG_FILE = "classpath:config.properties";

  private static final PropertiesHelper INSTANCE = new PropertiesHelper(
      new ConfigPropertyProvider(CONFIG_FILE_PROPERTY, DEFAULT_PATH_TO_CONFIG_FILE),
      new ConfigPropertyProvider(SCOUT_ENV_PROPERTY, null));

  private ConfigUtility() {
  }

  /**
   * @see PropertiesHelper#getProperty(String)
   */
  public static String getProperty(String key) {
    return INSTANCE.getProperty(key);
  }

  /**
   * @see PropertiesHelper#getProperty(String, String)
   */
  public static String getProperty(String key, String defaultValue) {
    return INSTANCE.getProperty(key, defaultValue);
  }

  /**
   * @see PropertiesHelper#getProperty(String, String, String)
   */
  public static String getProperty(String key, String defaultValue, String namespace) {
    return INSTANCE.getProperty(key, defaultValue, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyList(String)
   */
  public static List<String> getPropertyList(String key) {
    return INSTANCE.getPropertyList(key);
  }

  /**
   * @see PropertiesHelper#getPropertyList(String, String)
   */
  public static List<String> getPropertyList(String key, String namespace) {
    return INSTANCE.getPropertyList(key, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyList(String, List)
   */
  public static List<String> getPropertyList(String key, List<String> defaultValue) {
    return INSTANCE.getPropertyList(key, defaultValue);
  }

  /**
   * @see PropertiesHelper#getPropertyList(String, List, String)
   */
  public static List<String> getPropertyList(String key, List<String> defaultValue, String namespace) {
    return INSTANCE.getPropertyList(key, defaultValue, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyMap(String)
   */
  public static Map<String, String> getPropertyMap(String key) {
    return INSTANCE.getPropertyMap(key);
  }

  /**
   * @see PropertiesHelper#getPropertyMap(String, Map)
   */
  public static Map<String, String> getPropertyMap(String key, Map<String, String> defaultValue) {
    return INSTANCE.getPropertyMap(key, defaultValue);
  }

  /**
   * @see PropertiesHelper#getPropertyMap(String, String)
   */
  public static Map<String, String> getPropertyMap(String key, String namespace) {
    return INSTANCE.getPropertyMap(key, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyMap(String, Map, String)
   */
  public static Map<String, String> getPropertyMap(String key, Map<String, String> defaultValue, String namespace) {
    return INSTANCE.getPropertyMap(key, defaultValue, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyBoolean(String, boolean)
   */
  public static boolean getPropertyBoolean(String key, boolean defaultValue) {
    return INSTANCE.getPropertyBoolean(key, defaultValue);
  }

  /**
   * @see PropertiesHelper#getPropertyBoolean(String, boolean, String)
   */
  public static boolean getPropertyBoolean(String key, boolean defaultValue, String namespace) {
    return INSTANCE.getPropertyBoolean(key, defaultValue, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyInt(String, int)
   */
  public static int getPropertyInt(String key, int defaultValue) {
    return INSTANCE.getPropertyInt(key, defaultValue);
  }

  /**
   * @see PropertiesHelper#getPropertyInt(String, int, String)
   */
  public static int getPropertyInt(String key, int defaultValue, String namespace) {
    return INSTANCE.getPropertyInt(key, defaultValue, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyLong(String, long)
   */
  public static long getPropertyLong(String key, long defaultValue) {
    return INSTANCE.getPropertyLong(key, defaultValue);
  }

  /**
   * @see PropertiesHelper#getPropertyLong(String, long, String)
   */
  public static long getPropertyLong(String key, long defaultValue, String namespace) {
    return INSTANCE.getPropertyLong(key, defaultValue, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyFloat(String, float)
   */
  public static float getPropertyFloat(String key, float defaultValue) {
    return INSTANCE.getPropertyFloat(key, defaultValue);
  }

  /**
   * @see PropertiesHelper#getPropertyFloat(String, float, String)
   */
  public static float getPropertyFloat(String key, float defaultValue, String namespace) {
    return INSTANCE.getPropertyFloat(key, defaultValue, namespace);
  }

  /**
   * @see PropertiesHelper#getPropertyDouble(String, double)
   */
  public static double getPropertyDouble(String key, double defaultValue) {
    return INSTANCE.getPropertyDouble(key, defaultValue);
  }

  /**
   * @see PropertiesHelper#getPropertyDouble(String, double, String)
   */
  public static double getPropertyDouble(String key, double defaultValue, String namespace) {
    return INSTANCE.getPropertyDouble(key, defaultValue, namespace);
  }

  /**
   * @see PropertiesHelper#getAllPropertyNames()
   */
  public static Set<String> getAllPropertyNames() {
    return INSTANCE.getAllPropertyNames();
  }

  /**
   * @see PropertiesHelper#getAllEntries()
   */
  public static Map<String, String> getAllEntries() {
    return INSTANCE.getAllEntries();
  }

  /**
   * @see PropertiesHelper#hasProperty(String)
   */
  public static boolean hasProperty(String key) {
    return INSTANCE.hasProperty(key);
  }

  /**
   * @see PropertiesHelper#hasProviderProperties()
   */
  public static boolean hasProviderProperties() {
    return INSTANCE.hasProviderProperties();
  }
}

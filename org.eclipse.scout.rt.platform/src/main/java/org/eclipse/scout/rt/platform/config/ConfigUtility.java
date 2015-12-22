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
package org.eclipse.scout.rt.platform.config;

import java.util.Map;
import java.util.Set;

/**
 * Utility to extract properties stored in the <code>config.properties</code> file of scout applications.
 * <p>
 * The file is located on the classpath, typically in WEB-INF/classes/config.properties or in development
 * src/main/resources
 * <p>
 * It can also be specified by setting the system property
 * <code>-Dconfig.properties=path-to-config.properties-file</code>
 * <p>
 * Properties are simple key-value-pairs.<br>
 * Properties may contain placeholders for other variables: <code>${variableName}</code>. These variables are then
 * resolved once when the properties are initialized.<br>
 * </p>
 * Examples:<br>
 * <code>
 * &lt;ul&gt;<br>
 *    &lt;li&gt;customProperty=customValues&lt;/li&gt;<br>
 *    &lt;li&gt;myProperty=${customProperty}/subfolders&lt;/li&gt;<br>
 * &lt;/ul&gt;<br>
 * </code>
 */
public final class ConfigUtility {

  /**
   * Property to specify the configuration file. If not specified then {@link ClassLoader#getResource(String)} with
   * "/config.properties" is used.
   */
  public static final String CONFIG_FILE_NAME = "config.properties";

  private final static PropertiesHelper INSTANCE = new PropertiesHelper(CONFIG_FILE_NAME);

  /**
   * Gets the property with given key. If there is no property with given key, <code>null</code> is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in the system properties ({@link System#getProperty(String)})</li>
   * <li>in properties defined in a <code>config.properties</code>.</li>
   * <li>in the environment variables ({@link System#getenv(String)})</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @return The value of the given property or <code>null</code>.
   */
  public static String getProperty(String key) {
    return INSTANCE.getProperty(key);
  }

  /**
   * Gets the property with given key. If there is no property with given key, the given default value is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in the system properties ({@link System#getProperty(String)})</li>
   * <li>in properties defined in a <code>config.properties</code></li>
   * <li>in the environment variables ({@link System#getenv(String)})</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @return The value of the given property, the given default value if the property could not be found or
   *         <code>null</code> if the key is <code>null</code>.
   */
  public static String getProperty(String key, String defaultValue) {
    return INSTANCE.getProperty(key, defaultValue);
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
    return INSTANCE.getPropertyBoolean(key, defaultValue);
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
  public static int getPropertyInt(String key, int defaultValue) {
    return INSTANCE.getPropertyInt(key, defaultValue);
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
  public static long getPropertyLong(String key, long defaultValue) {
    return INSTANCE.getPropertyLong(key, defaultValue);
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
  public static float getPropertyFloat(String key, float defaultValue) {
    return INSTANCE.getPropertyFloat(key, defaultValue);
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
    return INSTANCE.getPropertyDouble(key, defaultValue);
  }

  /**
   * Gets all property key names defined in the loaded config.properties file.
   *
   * @return A {@link Set} copy containing all property key names.
   */
  public static Set<String> getAllPropertyNames() {
    return INSTANCE.getAllPropertyNames();
  }

  /**
   * Gets all properties and the corresponding values loaded from the config.properties file.
   *
   * @return A {@link Map} copy containing all entries.
   */
  public static Map<String, String> getAllEntries() {
    return INSTANCE.getAllEntries();
  }

  /**
   * @return true if the config.properties contains the key
   */
  public static boolean hasProperty(String key) {
    return INSTANCE.hasProperty(key);
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
    return INSTANCE.resolve(s);
  }

  /**
   * Specifies if a config.properties has been found and loaded.
   *
   * @return <code>true</code> if a config.properties has been loaded, <code>false</code> otherwise.
   */
  public static boolean isInitialized() {
    return INSTANCE.isInitialized();
  }
}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to extract properties stored in <a href="https://en.wikipedia.org/wiki/.properties">.properties</a>
 * files.
 * <p>
 * The file is located on the classpath, typically in WEB-INF/classes/xyz.properties or during development in
 * src/main/resources
 * <p>
 * It can also be specified by setting a system property that contains an {@link URL} pointing to the actual properties
 * file.
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
 * The special key <code>import</code> is reserved and expects an URL pointing to other .properties files.<br>
 * Such a file can be on the classpath (use <code>classpath:</code>) or any other supported URL.
 * <p>
 * Examples: <br>
 * <code>
 * <ul>
 * <li>import[0]=classpath:myConfigs/other.properties</li>
 * <li>import[1]=file:/C:/path/to/my/settings.properties</li>
 * <li>customProperty=customValues</li>
 * <li>myProperty=${customProperty}/subfolders</li>
 * <li>my-namespace|myMap[key0]=value0</li>
 * <li>my-namespace|myMap[key1]=value1</li>
 * </ul>
 * </code>
 */
public class PropertiesHelper {

  private static final String PLACEHOLDER_PATTERN = "\\$\\{([^\\}]+)\\}";
  public static final String CLASSPATH_PROTOCOL_NAME = "classpath";
  public static final char PROTOCOL_DELIMITER = ':';
  public static final char NAMESPACE_DELIMITER = '|';
  public static final char COLLECTION_DELIMITER_START = '[';
  public static final char COLLECTION_DELIMITER_END = ']';
  public static final String CLASSPATH_PREFIX = CLASSPATH_PROTOCOL_NAME + PROTOCOL_DELIMITER;
  public static final String IMPORT_KEY = "import";

  private static final Logger LOG = LoggerFactory.getLogger(PropertiesHelper.class);

  private final Map<String, String> m_configProperties;
  private final boolean m_isInitialized;

  /**
   * Creates a new instance using the given {@link URL} as root config resource.
   *
   * @param propertiesFileUrl
   */
  public PropertiesHelper(URL propertiesFileUrl) {
    this(propertiesFileUrl, getSelfIgnore(propertiesFileUrl));
  }

  /**
   * Creates a new instance using the given name identifier.<br>
   * The identifier can be one of the following:
   * <ul>
   * <li>A {@link String} that contains a valid {@link URL} in external form containing at least one colon (:) to define
   * a schema/protocol.</li>
   * <li>A {@link String} without any protocol or schema that holds an absolute path on the java classpath. Such a
   * string can also have a <code>classpath:</code> prefix (which is the default if no schema is present).</li>
   * <li>The name of a system property ({@link System#getProperty(String)}) holding a value that corresponds to one of
   * the two previous options.</li>
   * </ul>
   *
   * @param name
   *          the name identifier.
   */
  public PropertiesHelper(String name) {
    this(name, Collections.singleton(name));
  }

  protected PropertiesHelper(String fileName, Set<String> importsToIgnore) {
    this(getPropertiesFileUrl(fileName), importsToIgnore);
  }

  protected PropertiesHelper(URL propertiesFileUrl, Set<String> importsToIgnore /* for loop detection */) {
    m_configProperties = new HashMap<>();
    if (propertiesFileUrl != null) {
      parse(propertiesFileUrl);
      Pattern pat = Pattern.compile(PLACEHOLDER_PATTERN);
      importAll(importsToIgnore, pat);
      resolveAll(pat);
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
   * <li>in the system properties ({@link System#getProperty(String)}).</li>
   * <li>in properties defined in the file represented by this instance or one of its imports.</li>
   * <li>in the environment variables ({@link System#getenv(String)}).</li>
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
   * <li>in the system properties ({@link System#getProperty(String)}).</li>
   * <li>in properties defined in the file represented by this instance or one of its imports.</li>
   * <li>in the environment variables ({@link System#getenv(String)}).</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use
   * @return The value of the given property, the given default value if the property could not be found or
   *         <code>null</code> if the key is <code>null</code> or empty.
   */
  public String getProperty(String key, String defaultValue) {
    return getProperty(key, defaultValue, null);
  }

  /**
   * Gets the property with given key and namespace. If there is no property with given key and namespace, the given
   * default value is returned.<br>
   * The given key is searched in the following order:
   * <ol>
   * <li>in the system properties ({@link System#getProperty(String)}).</li>
   * <li>in properties defined in the file represented by this instance or one of its imports.</li>
   * <li>in the environment variables ({@link System#getenv(String)}).</li>
   * </ol>
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          defaultValue The default value to use
   * @param namespace
   *          The namespace of the property to retrieve.
   * @return The value of the given property, the given default value if the property could not be found or
   *         <code>null</code> if the key is <code>null</code> or empty.
   */
  public String getProperty(String key, String defaultValue, String namespace) {
    if (!StringUtility.hasText(key)) {
      return null;
    }

    String propKey = toPropertyKey(key, namespace).toString();
    String value = null;

    // system config
    value = System.getProperty(propKey);
    if (StringUtility.hasText(value)) {
      return value;
    }

    // properties file
    value = m_configProperties.get(propKey);
    if (StringUtility.hasText(value)) {
      return value;
    }

    // environment config
    value = System.getenv(propKey);
    if (StringUtility.hasText(value)) {
      return value;
    }

    return defaultValue;
  }

  /**
   * Gets the {@link List} property with the given key.<br>
   * If there are list properties of the following form: <code>[myKey[0], myKey[1], myKey[2], ...]</code>, the values of
   * all these keys can be retrieved using the key <code>"myKey"</code>.<br>
   * If there is a single property with that key (e.g. just one value <code>myKey=myVal</code> without any number
   * suffix) a call to this method returns the single value in a {@link List}. This allows to prepare your application
   * for multiple values for a key even if currently only one is used.<br>
   * If there is no entry with that key, an empty {@link List} is returned.<br>
   * It is guaranteed that the order of the values in the list corresponds to the natural order of the suffixes.<br>
   * <br>
   * <b>Note</b>: As with all getProperty()-methods of this class also this method considers
   * {@link System#getProperty(String)} and {@link System#getenv(String)} as specified by
   * {@link #getProperty(String, String)}.
   *
   * @param key
   *          The key of the property without any numbered suffix.
   * @return If there are listed properties using numbered suffixes, the values of all these are returned. Otherwise a
   *         {@link List} containing the single value with this key is returned. If there is no entry with this key, an
   *         empty list is returned. Never returns <code>null</code>. The resulting {@link List} cannot be modified.
   */
  public List<String> getPropertyList(String key) {
    return getPropertyList(key, (String) null);
  }

  /**
   * Gets the {@link List} property with the given key and namespace.<br>
   * If there are list properties of the following form: <code>[ns|myKey[0], ns|myKey[1], ns|myKey[2], ...]</code>, the
   * values of all these keys can be retrieved using the key <code>"myKey"</code> and namespace <code>"ns"</code>.<br>
   * If there is a single property with that key (e.g. just one value <code>ns|myKey=myVal</code> without any number
   * suffix) a call to this method returns the single value in a {@link List}. This allows to prepare your application
   * for multiple values for a key even if currently only one is used.<br>
   * If there is no entry with that key, an empty {@link List} is returned.<br>
   * It is guaranteed that the order of the values in the list corresponds to the natural order of the suffixes.<br>
   * <br>
   * <b>Note</b>: As with all getProperty()-methods of this class also this method considers
   * {@link System#getProperty(String)} and {@link System#getenv(String)} as specified by
   * {@link #getProperty(String, String)}.
   *
   * @param key
   *          The key of the property without any numbered suffix.
   * @param namespace
   *          The namespace or <code>null</code>.
   * @return If there are listed properties using numbered suffixes, the values of all these are returned. Otherwise a
   *         {@link List} containing the single value with this key is returned. If there is no entry with this key, an
   *         empty list is returned. Never returns <code>null</code>. The resulting {@link List} cannot be modified.
   */
  public List<String> getPropertyList(String key, String namespace) {
    return getPropertyList(key, Collections.<String> emptyList(), namespace);
  }

  /**
   * Gets the {@link List} property with the given key.<br>
   * If there are list properties of the following form: <code>[myKey[0], myKey[1], myKey[2], ...]</code>, the values of
   * all these keys can be retrieved using the key <code>"myKey"</code>.<br>
   * If there is a single property with that key (e.g. just one value <code>myKey=myVal</code> without any number
   * suffix) a call to this method returns the single value in a {@link List}. This allows to prepare your application
   * for multiple values for a key even if currently only one is used.<br>
   * If there is no entry with that key, the given default value is returned.<br>
   * It is guaranteed that the order of the values in the list corresponds to the natural order of the suffixes.<br>
   * <br>
   * <b>Note</b>: As with all getProperty()-methods of this class also this method considers
   * {@link System#getProperty(String)} and {@link System#getenv(String)} as specified by
   * {@link #getProperty(String, String)}.
   *
   * @param key
   *          The key of the property without any numbered suffix.
   * @param defaultValue
   *          The default value to return if no entry exists. May be <code>null</code>.
   * @return If there are listed properties using numbered suffixes, the values of all these are returned. Otherwise a
   *         {@link List} containing the single value with this key is returned. If there is no entry with this key, the
   *         given default value is returned. The resulting list cannot be modified unless it is the default value.
   */
  public List<String> getPropertyList(String key, List<String> defaultValue) {
    return getPropertyList(key, defaultValue, null);
  }

  /**
   * Gets the {@link List} property with the given key and namespace.<br>
   * If there are list properties of the following form: <code>[ns|myKey[0], ns|myKey[1], ns|myKey[2], ...]</code>, the
   * values of all these keys can be retrieved using the key <code>"myKey"</code> and namespace <code>"ns"</code>.<br>
   * If there is a single property with that key (e.g. just one value <code>ns|myKey=myVal</code> without any number
   * suffix) a call to this method returns the single value in a {@link List}. This allows to prepare your application
   * for multiple values for a key even if currently only one is used.<br>
   * If there is no entry with that key, the given default value is returned.<br>
   * It is guaranteed that the order of the values in the list corresponds to the natural order of the suffixes.<br>
   * <br>
   * <b>Note</b>: As with all getProperty()-methods of this class also this method considers
   * {@link System#getProperty(String)} and {@link System#getenv(String)} as specified by
   * {@link #getProperty(String, String)}.
   *
   * @param key
   *          The key of the property without any numbered suffix.
   * @param defaultValue
   *          The default value to return if no entry exists. May be <code>null</code>.
   * @param namespace
   *          The namespace or <code>null</code>.
   * @return If there are listed properties using numbered suffixes, the values of all these are returned. Otherwise a
   *         {@link List} containing the single value with this key is returned. If there is no entry with this key, the
   *         given default value is returned. The resulting list cannot be modified unless it is the default value.
   */
  public List<String> getPropertyList(String key, List<String> defaultValue, String namespace) {
    if (!StringUtility.hasText(key)) {
      return defaultValue;
    }

    Map<String, String> resultAsMap = getPropertyMap(key, null, namespace);
    if (resultAsMap == null) {
      // try single key
      String value = getProperty(key, null, namespace);
      if (StringUtility.hasText(value)) {
        return Collections.singletonList(value);
      }

      return defaultValue;
    }

    String[] result = new String[resultAsMap.size()];
    for (Entry<String, String> entry : resultAsMap.entrySet()) {
      int index = toListIndex(entry.getKey(), key);
      if (index >= result.length) {
        throw newInvalidListIndexException(key, entry.getKey(), null);
      }
      result[index] = entry.getValue();
    }
    return Collections.unmodifiableList(Arrays.asList(result));
  }

  /**
   * Gets a {@link Map} property with given key.<br>
   * If there are map properties of the following form:
   * <code>[myprop[key0]=val0, myprop[key1]=val1, myprop[key2]=val2, ...]</code>, the values of all these keys can be
   * retrieved using the key <code>"myprop"</code>.<br>
   * If there is no entry with that key, an empty map is returned.<br>
   * <br>
   * <b>Note</b>: As with all getProperty()-methods of this class also this method considers
   * {@link System#getProperty(String)} and {@link System#getenv(String)} as specified by
   * {@link #getProperty(String, String)}.
   *
   * @param key
   *          The key of the property without any numbered suffix.
   * @return An unmodifiable {@link Map} holding the values with given key. If there are no values with that key the
   *         resulting map is empty. Never returns <code>null</code>.
   */
  public Map<String, String> getPropertyMap(String key) {
    return getPropertyMap(key, (String) null);
  }

  /**
   * Gets a {@link Map} property with given key.<br>
   * If there are map properties of the following form:
   * <code>[myprop[key0]=val0, myprop[key1]=val1, myprop[key2]=val2, ...]</code>, the values of all these keys can be
   * retrieved using the key <code>"myprop"</code>.<br>
   * If there is no entry with that key, the given default value is returned.<br>
   * <br>
   * <b>Note</b>: As with all getProperty()-methods of this class also this method considers
   * {@link System#getProperty(String)} and {@link System#getenv(String)} as specified by
   * {@link #getProperty(String, String)}.
   *
   * @param key
   *          The key of the property without any numbered suffix.
   * @param defaultValue
   *          The default value to return if no entry exists. May be <code>null</code>.
   * @return An unmodifiable {@link Map} if there are map values with given key and namespace. The given default value
   *         otherwise.
   */
  public Map<String, String> getPropertyMap(String key, Map<String, String> defaultValue) {
    return getPropertyMap(key, defaultValue, null);
  }

  /**
   * Gets a {@link Map} property with given key and namespace.<br>
   * If there are map properties of the following form:
   * <code>[ns|myprop[key0]=val0, ns|myprop[key1]=val1, ns|myprop[key2]=val2, ...]</code>, the values of all these keys
   * can be retrieved using the key <code>"myprop"</code> and namespace <code>"ns"</code>.<br>
   * If there is no entry with that key, the given default value is returned.<br>
   * <br>
   * <b>Note</b>: As with all getProperty()-methods of this class also this method considers
   * {@link System#getProperty(String)} and {@link System#getenv(String)} as specified by
   * {@link #getProperty(String, String)}.
   *
   * @param key
   *          The key of the property without any numbered suffix.
   * @param namespace
   *          The namespace or <code>null</code>.
   * @return An unmodifiable {@link Map} holding the values with given key and namespace. If there are no values with
   *         that key and namespace the resulting map is empty. Never returns <code>null</code>.
   */
  public Map<String, String> getPropertyMap(String key, String namespace) {
    return getPropertyMap(key, Collections.<String, String> emptyMap(), namespace);
  }

  /**
   * Gets a {@link Map} property with given key and namespace.<br>
   * If there are map properties of the following form:
   * <code>[ns|myprop[key0]=val0, ns|myprop[key1]=val1, ns|myprop[key2]=val2, ...]</code>, the values of all these keys
   * can be retrieved using the key <code>"myprop"</code> and namespace <code>"ns"</code>.<br>
   * If there is no entry with that key, the given default value is returned.<br>
   * <br>
   * <b>Note</b>: As with all getProperty()-methods of this class also this method considers
   * {@link System#getProperty(String)} and {@link System#getenv(String)} as specified by
   * {@link #getProperty(String, String)}.
   *
   * @param key
   *          The key of the property without any numbered suffix.
   * @param defaultValue
   *          The default value to return if no entry exists. May be <code>null</code>.
   * @param namespace
   *          The namespace or <code>null</code>.
   * @return An unmodifiable {@link Map} if there are map values with given key and namespace. The given default value
   *         otherwise.
   */
  public Map<String, String> getPropertyMap(String key, Map<String, String> defaultValue, String namespace) {
    if (!StringUtility.hasText(key)) {
      return defaultValue;
    }

    String keyPrefix = toCollectionKeyPrefix(key, namespace).toString();

    Map<String, String> result = new HashMap<>();
    collectMapEntriesWith(keyPrefix, System.getenv().keySet(), result);
    collectMapEntriesWith(keyPrefix, m_configProperties.keySet(), result);
    collectMapEntriesWith(keyPrefix, System.getProperties().keySet(), result);

    if (result.isEmpty()) {
      return defaultValue;
    }
    return Collections.unmodifiableMap(result);
  }

  /**
   * Gets the property with given key as boolean. If a property with given key does not exist, the given default value
   * is returned. If value of the property is no valid boolean an {@link IllegalArgumentException} is thrown. Valid
   * values are '{@code true}' and '{@code false}' (not case-sensitive).
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid boolean associated with it.
   * @return The boolean value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the property is neither {@code true} nor {@code false} (not case-sensitive).
   * @see #getProperty(String)
   */
  public boolean getPropertyBoolean(String key, boolean defaultValue) {
    return getPropertyBoolean(key, defaultValue, null);
  }

  /**
   * Gets the property with given key and namespace as boolean. If a property with given key and namespace does not
   * exist, the given default value is returned. If value of the property is no valid boolean an
   * {@link IllegalArgumentException} is thrown. Valid values are '{@code true}' and '{@code false}' (not
   * case-sensitive).
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid boolean associated with it.
   * @param namespace
   *          The namespace of the property or <code>null</code>.
   * @return The boolean value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the property is neither {@code true} nor {@code false} (not case-sensitive).
   * @see #getProperty(String)
   */
  public boolean getPropertyBoolean(String key, boolean defaultValue, String namespace) {
    String rawValue = getProperty(key, namespace);
    if (rawValue == null) {
      return defaultValue;
    }

    if (Boolean.TRUE.toString().equalsIgnoreCase(rawValue) || Boolean.FALSE.toString().equalsIgnoreCase(rawValue)) {
      return Boolean.parseBoolean(rawValue);
    }
    else {
      throw new IllegalArgumentException("Invalid boolean-value for property '" + key + "' configured: " + rawValue);
    }
  }

  /**
   * Gets the property with given key as int. If a property with given key does not exist, the given default value is
   * returned. If the value is no valid int an {@link IllegalArgumentException} is thrown.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid int associated with it.
   * @return The int value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the given property is no valid int according to {@link Integer#parseInt(String)}.
   * @see #getProperty(String)
   */
  public int getPropertyInt(String key, int defaultValue) {
    return getPropertyInt(key, defaultValue, null);
  }

  /**
   * Gets the property with given key and namespace as int. If a property with given key and namespace does not exist,
   * the given default value is returned. If the value is no valid int an {@link IllegalArgumentException} is thrown.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid int associated with it.
   * @param namespace
   *          The namespace of the property or <code>null</code>.
   * @return The int value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the given property is no valid int according to {@link Integer#parseInt(String)}.
   * @see #getProperty(String)
   */
  public int getPropertyInt(String key, int defaultValue, String namespace) {
    String valueRaw = getProperty(key, namespace);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(valueRaw);
    }
    catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid int-value for property '" + key + "' configured: " + valueRaw, e);
    }
  }

  /**
   * Gets the property with given key as long. If a property with given key does not exist, the given default value is
   * returned. If the value is no valid long an {@link IllegalArgumentException} is thrown.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid long associated with it.
   * @return The long value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the given property is no valid long according to {@link Long#parseLong(String)}.
   * @see #getProperty(String)
   */
  public long getPropertyLong(String key, long defaultValue) {
    return getPropertyLong(key, defaultValue, null);
  }

  /**
   * Gets the property with given key and namespace as long. If a property with given key and namespace does not exist,
   * the given default value is returned. If the value is no valid long an {@link IllegalArgumentException} is thrown.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid long associated with it.
   * @param namespace
   *          The namespace of the property or <code>null</code>.
   * @return The long value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the given property is no valid long according to {@link Long#parseLong(String)}.
   * @see #getProperty(String)
   */
  public long getPropertyLong(String key, long defaultValue, String namespace) {
    String valueRaw = getProperty(key, namespace);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Long.parseLong(valueRaw);
    }
    catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid long-value for property '" + key + "' configured: " + valueRaw, e);
    }
  }

  /**
   * Gets the property with given key as float. If a property with given key does not exist, the given default value is
   * returned. If the value is no valid float an {@link IllegalArgumentException} is thrown.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid float associated with it.
   * @return The float value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the given property is no valid float according to {@link Float#parseFloat(String)}.
   * @see #getProperty(String)
   */
  public float getPropertyFloat(String key, float defaultValue) {
    return getPropertyFloat(key, defaultValue, null);
  }

  /**
   * Gets the property with given key and namespace as float. If a property with given key and namespace does not exist,
   * the given default value is returned. If the value is no valid float an {@link IllegalArgumentException} is thrown.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid float associated with it.
   * @param namespace
   *          The namespace of the property or <code>null</code>.
   * @return The float value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the given property is no valid float according to {@link Float#parseFloat(String)}.
   * @see #getProperty(String)
   */
  public float getPropertyFloat(String key, float defaultValue, String namespace) {
    String valueRaw = getProperty(key, namespace);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Float.parseFloat(valueRaw);
    }
    catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid float-value for property '" + key + "' configured: " + valueRaw, e);
    }
  }

  /**
   * Gets the property with given key as double. If a property with given key does not exist, the given default value is
   * returned. If the value is no valid double an {@link IllegalArgumentException} is thrown.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid double associated with it.
   * @return The double value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the given property is no valid double according to {@link Double#parseDouble(String)}.
   * @see #getProperty(String)
   */
  public double getPropertyDouble(String key, double defaultValue) {
    return getPropertyDouble(key, defaultValue, null);
  }

  /**
   * Gets the property with given key and namespace as double. If a property with given key and namespace does not
   * exist, the given default value is returned. If the value is no valid double an {@link IllegalArgumentException} is
   * thrown.
   *
   * @param key
   *          The key of the property.
   * @param defaultValue
   *          The default value to use if the given key does not exist or as no valid double associated with it.
   * @param namespace
   *          The namespace of the property or <code>null</code>.
   * @return The double value of the given key or the given default value otherwise.
   * @since 5.1
   * @throws IllegalArgumentException
   *           if the value of the given property is no valid double according to {@link Double#parseDouble(String)}.
   * @see #getProperty(String)
   */
  public double getPropertyDouble(String key, double defaultValue, String namespace) {
    String valueRaw = getProperty(key, namespace);
    if (valueRaw == null) {
      return defaultValue;
    }

    try {
      return Double.parseDouble(valueRaw);
    }
    catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid double-value for property '" + key + "' configured: " + valueRaw, e);
    }
  }

  /**
   * Gets all property key names defined in the loaded .properties file or one of its imports.
   *
   * @return An unmodifiable {@link Set} containing all property key names.
   */
  public Set<String> getAllPropertyNames() {
    return Collections.unmodifiableSet(m_configProperties.keySet());
  }

  /**
   * Gets all properties and the corresponding values loaded from the .properties file or one of its imports.
   *
   * @return An unmodifiable {@link Map} containing all entries.
   */
  public Map<String, String> getAllEntries() {
    return Collections.unmodifiableMap(m_configProperties);
  }

  /**
   * @return {@code true} if the .properties file or one of its imports contains the given key.
   */
  public boolean hasProperty(String key) {
    return m_configProperties.containsKey(key);
  }

  /**
   * Specifies if the properties file has been found and loaded.
   *
   * @return <code>true</code> if a properties file has been loaded, <code>false</code> otherwise.
   */
  public boolean isInitialized() {
    return m_isInitialized;
  }

  protected void collectMapEntriesWith(String keyPrefix, Set<? extends Object> keySet, Map<String, String> collector) {
    for (Object propKey : keySet) {
      String k = propKey.toString();
      String mapKey = toMapKey(k, keyPrefix);
      if (mapKey != null) {
        // we can overwrite here because the old entry has already the same value
        collector.put(mapKey, getProperty(k));
      }
    }
  }

  protected int toListIndex(String mapKey, String requestedKey) {
    try {
      int i = Integer.parseInt(mapKey);
      if (i < 0) {
        throw newInvalidListIndexException(requestedKey, mapKey, null);
      }
      return i;
    }
    catch (NumberFormatException e) {
      throw newInvalidListIndexException(requestedKey, mapKey, e);
    }
  }

  protected IllegalArgumentException newInvalidListIndexException(String requestedKey, String mapKey, Throwable cause) {
    String fullKey = requestedKey + COLLECTION_DELIMITER_START + mapKey + COLLECTION_DELIMITER_END;
    return new IllegalArgumentException("Invalid list index '" + mapKey + "' in key '" + fullKey + "'. List indices must be zero-based, continuous, positive numeric values.", cause);
  }

  protected String toMapKey(String propKey, String keyPrefix) {
    if (propKey.startsWith(keyPrefix) && propKey.charAt(propKey.length() - 1) == COLLECTION_DELIMITER_END) {
      String mapKey = propKey.substring(keyPrefix.length(), propKey.length() - 1);
      if (StringUtility.hasText(mapKey)) {
        return mapKey;
      }
      else {
        throw new IllegalArgumentException("Invalid map property with key '" + propKey + "'.");
      }
    }
    return null;
  }

  protected StringBuilder toCollectionKeyPrefix(String key, String namespace) {
    return toPropertyKey(key, namespace).append(COLLECTION_DELIMITER_START);
  }

  protected StringBuilder toPropertyKey(String key, String namespace) {
    StringBuilder propKey = new StringBuilder(StringUtility.length(namespace) + key.length() + 5);
    if (namespace != null) {
      propKey.append(namespace);
      propKey.append(NAMESPACE_DELIMITER);
    }
    propKey.append(key);
    return propKey;
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
  protected String resolve(String s, Pattern pat) {
    Set<String> loopDetection = null;
    String t = s;
    Matcher m = pat.matcher(t);
    while (m.find()) {
      String key = m.group(1);
      String value = getProperty(key);

      if (!StringUtility.hasText(value)) {
        throw new IllegalArgumentException("resolving expression '" + s + "': variable ${" + key + "} is not defined in the context.");
      }
      if (value.contains(s)) {
        throw new IllegalArgumentException("resolving expression '" + s + "': loop detected (the resolved value contains the original expression): " + value);
      }

      t = t.substring(0, m.start()) + value + t.substring(m.end());

      if (loopDetection == null) {
        loopDetection = new LinkedHashSet<>();
      }
      if (loopDetection.contains(key)) {
        throw new IllegalArgumentException("resolving expression '" + s + "': loop detected: " + loopDetection);
      }
      loopDetection.add(key);
      // next
      m = pat.matcher(t);
    }

    return t;
  }

  protected static Set<String> getSelfIgnore(URL url) {
    if (url == null) {
      return Collections.emptySet();
    }
    return Collections.singleton(url.toExternalForm());
  }

  protected static URL getPropertiesFileUrl(String filePath) {
    if (!StringUtility.hasText(filePath)) {
      return null;
    }
    String sysPropFileName = System.getProperty(filePath);
    if (StringUtility.hasText(sysPropFileName)) {
      filePath = sysPropFileName;
    }

    if (filePath.indexOf(PROTOCOL_DELIMITER) < 0) {
      // no protocol specified. Default is class-path
      filePath = CLASSPATH_PREFIX + filePath;
    }

    if (filePath.startsWith(CLASSPATH_PREFIX)) {
      filePath = filePath.substring(CLASSPATH_PREFIX.length());
      if (!StringUtility.hasText(filePath)) {
        return null;
      }
      return PropertiesHelper.class.getClassLoader().getResource(filePath);
    }

    return toPropertiesFileUrl(filePath);
  }

  protected static URL toPropertiesFileUrl(String filePath) {
    try {
      return new URL(filePath);
    }
    catch (MalformedURLException e) {
      LOG.debug("Config file path '{}' is no valid URL. Trying to parse as absolute file path.", filePath, e);
    }

    try {
      File local = new File(filePath);
      if (local.isFile() && local.isAbsolute()) {
        return local.toURI().toURL();
      }
      else {
        throw new IllegalArgumentException("Invalid config file path: '" + filePath + "'.");
      }
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Unable to load config file with URL '" + filePath + "'.", e);
    }
  }

  protected void importAll(Set<String> importsToIgnore, Pattern pat) {
    List<String> rawImports = getPropertyList(IMPORT_KEY);
    if (rawImports.isEmpty()) {
      return;
    }

    List<String> imports = new ArrayList<String>(rawImports.size());
    for (String s : rawImports) {
      imports.add(resolve(s, pat));
    }

    Set<String> ignore = new HashSet<>(importsToIgnore);
    ignore.addAll(imports);

    for (String importUrl : imports) {
      if (importsToIgnore.contains(importUrl)) {
        LOG.warn("Import of '{}' skipped because already imported: {}.", importUrl, importsToIgnore);
        continue; // skip ignores (loop detection)
      }

      PropertiesHelper importHelper = new PropertiesHelper(importUrl, ignore);
      if (importHelper.isInitialized()) {
        importFrom(importHelper);
      }
      else {
        throw new IllegalArgumentException("Config import with URL '" + importUrl + "' could not be found.");
      }
    }
  }

  protected void importFrom(PropertiesHelper other) {
    for (Entry<String, String> importEntry : other.getAllEntries().entrySet()) {
      String key = importEntry.getKey();
      String valueFromImport = importEntry.getValue();

      String existing = m_configProperties.get(key);
      if (existing != null) {
        // the import contains the same key as we already have in this file. Ignore the value from the import but log duplicate.
        logDuplicateKey(key, valueFromImport, existing);
      }
      else {
        m_configProperties.put(key, valueFromImport);
      }
    }
  }

  protected void resolveAll(Pattern pat) {
    for (Entry<String, String> entry : m_configProperties.entrySet()) {
      entry.setValue(resolve(entry.getValue(), pat));
    }
  }

  private void logDuplicateKey(Object key, Object oldValue, Object newValue) {
    LOG.warn("Duplicate config key: '{}'. Old value '{}' replaced with '{}'.", key, oldValue, newValue);
  }

  protected Map<String, String> getConfigPropertyMap() {
    return m_configProperties;
  }

  protected void parse(URL propertiesFileUrl) {
    Properties props = new Properties() {
      private static final long serialVersionUID = 1L;

      @Override
      public synchronized Object put(Object key, Object value) {
        Object oldValue = super.put(key, value);
        if (oldValue != null) {
          logDuplicateKey(key, oldValue, value);
        }
        return oldValue;
      }
    };

    try (BufferedReader in = new BufferedReader(new InputStreamReader(propertiesFileUrl.openStream(), StandardCharsets.ISO_8859_1 /* according to .properties file spec */))) {
      props.load(in);
    }
    catch (Exception t) {
      throw new IllegalArgumentException("Unable to parse properties file from url '" + propertiesFileUrl.toExternalForm() + "'.", t);
    }

    for (Entry<Object, Object> entry : props.entrySet()) {
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if (StringUtility.hasText(key)) {
        m_configProperties.put(key, value); // we cannot have any duplicates here.
      }
    }
  }
}

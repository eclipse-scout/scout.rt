/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.config;

import java.net.URL;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.StreamUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.json.JSONObject;
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
 * Support for encoding/decoding, encryption/decryption of the content or parts of the content of these config files is
 * provided using a {@link ServiceLoader} of type {@link IConfigFileLoader}.
 * <p>
 * The following property formats are supported:
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
 * resolved once when the properties are initialized. For system properties and environment variables they are resolved
 * whenever the property is accessed.
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
 * <p>
 * Since 10.0 Scout also supports importing a file defining variables. This feature is helpful when launching
 * applications in development mode in an IDE using a pre-defined config.properties that contains variables (also known
 * as tags). The variable file only contains variables and config property values but is itself not a config.property
 * file. Thus its values are not checked in any {@link IConfigurationValidator}.
 * <p>
 * A variable file is passed to the java process with the system property
 * <code>-Dscout.env=file:/path/to/my/launch.properties</code>
 */
public class PropertiesHelper {

  private static final char ENVIRONMENT_VARIABLE_DOT_REPLACEMENT = '_';
  private static final Pattern PLACEHOLDER_PATTERN = PropertiesUtility.DEFAULT_VARIABLE_PATTERN;
  private static final Pattern PATH_PLACEHOLDER_PATTERN = Pattern.compile("^\\$([^\\$]+)\\$");
  public static final String CLASSPATH_PROTOCOL_NAME = "classpath";
  public static final char PROTOCOL_DELIMITER = ':';
  public static final char NAMESPACE_DELIMITER = '|';
  public static final String NAMESPACE_DELIMITER_FOR_ENV = "__";
  public static final char COLLECTION_DELIMITER_START = '[';
  public static final char COLLECTION_DELIMITER_END = ']';
  public static final String CLASSPATH_PREFIX = CLASSPATH_PROTOCOL_NAME + PROTOCOL_DELIMITER;
  public static final String IMPORT_KEY = "import";
  private static final Pattern IMPORT_PATTERN = Pattern.compile("^import(\\[[^\\]]*\\])?$");

  /**
   * The variable ${CURRENT_DIR} can be used in a property file defined with -Dscout.env=file:/path/to/file to access
   * the folder path of that file, a self-reference.
   */
  public static final String CURRENT_DIR_VARIABLE = "${CURRENT_DIR}";

  private static final Logger LOG = LoggerFactory.getLogger(PropertiesHelper.class);

  private Map<String, String> m_envProperties = new LinkedHashMap<>();
  private Map<String, String> m_configProperties = new HashMap<>();
  private boolean m_hasProviderProperties = false;
  private boolean m_initializing;

  /*
   * The following are temporary fields that are cleared after the object is initialized
   */

  private Set<Object> m_parsedFiles = new HashSet<>();
  private Set<Object> m_referencedKeys = new HashSet<>();
  private Set<String> m_importKeys = new HashSet<>();

  /**
   * Creates a new instance using the given property provider.<br>
   *
   * @param properties
   *          the property provider.
   * @see ConfigPropertyProvider
   */
  public PropertiesHelper(IPropertyProvider properties) {
    this(properties, null);
  }

  /**
   * Creates a new instance using the given config.properties and optional variable file.<br>
   *
   * @param properties
   *          the property provider.
   * @param env
   *          the environment variables. Optional
   * @see ConfigPropertyProvider
   */
  public PropertiesHelper(IPropertyProvider properties, IPropertyProvider env) {
    parseEnvFile(env);
    try {
      m_initializing = true;
      if (properties != null) {
        m_hasProviderProperties = parse(properties);
        importSystemImports(new HashSet<>(), null);
        resolveAll(PLACEHOLDER_PATTERN);

        // remove internal properties
        m_configProperties = m_configProperties.entrySet().stream()
            .filter(e -> {
              if (e.getKey().startsWith("_")) {
                if (!m_referencedKeys.contains(e.getKey())) {
                  LOG.warn("The internal key '{}' is never referenced and should be removed from config property file. [{}:{}]", e.getKey(), e.getKey(), e.getValue());
                }
                return false;
              }
              return true;
            })
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      }
    }
    finally {
      m_initializing = false;
      //free memory
      m_parsedFiles = null;
      m_referencedKeys = null;
      m_importKeys = null;
    }
  }

  /**
   * Parse env file with variables
   *
   * @param propertyProvider
   * @return true if the file exists
   */
  protected boolean parseEnvFile(IPropertyProvider propertyProvider) {
    if (propertyProvider == null) {
      return false;
    }
    List<Entry<String, String>> properties = propertyProvider.readProperties();
    if (properties == null) {
      return false;
    }
    properties.forEach(prop -> {
      String key = prop.getKey();
      String value = resolveImports(key, prop.getValue());
      if (StringUtility.hasText(key)) {
        m_envProperties.put(key, value);
      }
    });
    return true;
  }

  /**
   * Parse regular config.properties
   *
   * @param propertyProvider
   * @return true if the file exists
   */
  protected boolean parse(IPropertyProvider propertyProvider) {
    m_parsedFiles.add(propertyProvider.getPropertiesIdentifier());
    List<Entry<String, String>> properties = propertyProvider.readProperties();
    if (properties == null) {
      return false;
    }
    properties.forEach(prop -> {
      String key = prop.getKey();
      String value = resolveImports(key, prop.getValue());
      String oldValue = m_configProperties.get(key);
      if (oldValue != null && !oldValue.equals(value)) {
        if (m_referencedKeys.contains(key)) {
          LOG.error("Replacement of already used config key: '{}' is not allowed. Old value '{}' will NOT be replaced with '{}'", key, m_configProperties.get(key), value);
          return;
        }
        else {
          LOG.info("Duplicate config key: '{}'. Old value '{}' replaced with '{}'.", key, m_configProperties.get(key), value);
        }
      }
      if (StringUtility.hasText(key)) {
        m_configProperties.put(key, value);
      }
    });
    return true;
  }

  private String normalizeImportKey(String key) {
    Pattern p = Pattern.compile("^" + IMPORT_KEY + "(\\_|\\.)(.*)$");
    Matcher m = p.matcher(key);
    if (m.find()) {
      return IMPORT_KEY + "[" + m.group(2) + "]";
    }
    else {
      return key;
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

    List<Function<String, String>> propertyValueRetrievers = Arrays.asList(
        this::getSystemPropertyValue,
        this::getEnvironmentPropertyValue,
        this::getEnvFilePropertyValue,
        this::getConfigPropertyValue);

    return propertyValueRetrievers.stream()
        .map(propertyValueRetriever -> propertyValueRetriever.apply(propKey))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(defaultValue);
  }

  protected String getSystemPropertyValue(String propKey) {
    String value = System.getProperty(propKey);
    if (StringUtility.hasText(value)) {
      return resolve(value, PLACEHOLDER_PATTERN);
    }
    return null;
  }

  protected String getEnvironmentPropertyValue(String propKey) {
    String envValue = lookupEnvironmentVariableValue(propKey);
    if (StringUtility.hasText(envValue)) {
      return resolve(envValue, PLACEHOLDER_PATTERN);
    }
    return null;
  }

  protected String getEnvFilePropertyValue(String propKey) {
    String value = m_envProperties.get(propKey);
    if (StringUtility.hasText(value)) {
      return value;
    }
    return null;
  }

  protected String getConfigPropertyValue(String propKey) {
    String value = m_configProperties.get(propKey);
    if (StringUtility.hasText(value)) {
      return value;
    }
    return null;
  }

  /**
   * Returns the environment variable value corresponding to the property specified by the key, or <code>null</code>.
   * <p>
   * Attempts to find them by resolving the property name in the following order:
   * <ol>
   * <li>Original: <code>my.property</code></li>
   * <li>Periods replaced: <code>my_property</code></li>
   * <li>Original in uppercase: <code>MY.PROPERTY</code></li>
   * <li>Periods replaced, in uppercase: <code>MY_PROPERTY</code></li>
   * </ol>
   * The standard namespace delimiter (|) will always be replaced by a delimiter more suited for use in environment
   * variables (__).
   */
  protected String lookupEnvironmentVariableValue(String propKey) {
    String nsDelimiterReplacedPropKey = propKey.replace(String.valueOf(NAMESPACE_DELIMITER), NAMESPACE_DELIMITER_FOR_ENV);

    // 1. Original
    String value = getEnvironmentVariable(nsDelimiterReplacedPropKey);
    if (value != null) {
      return value;
    }

    // Periods in environment variable names are not POSIX compliant (See IEEE Standard 1003.1-2017, Chapter 8.1 "Environment Variable Definition"),
    // but supported by some shells. To allow overriding via environment variables (Bugzilla 541099) in any shell, convert them to underscores.
    // 2. With periods replaced
    String keyWithoutDots = nsDelimiterReplacedPropKey.replace('.', ENVIRONMENT_VARIABLE_DOT_REPLACEMENT);
    value = getEnvironmentVariable(keyWithoutDots);
    if (value != null) {
      logInexactEnvNameMatch(propKey, keyWithoutDots);
      return value;
    }

    // Applications may define environment variable names with lower case, but only upper case is POSIX compliant for the environment.
    // To override from a shell, we should also check for upper case.
    // 3. In Uppercase, original periods
    String uppercasedKey = nsDelimiterReplacedPropKey.toUpperCase();
    value = getEnvironmentVariable(uppercasedKey);
    if (value != null) {
      logInexactEnvNameMatch(propKey, uppercasedKey);
      return value;
    }

    // 4. In Uppercase, with periods replaced
    String keyWithoutDotsUppercased = keyWithoutDots.toUpperCase();
    value = getEnvironmentVariable(keyWithoutDotsUppercased);
    if (value != null) {
      logInexactEnvNameMatch(propKey, keyWithoutDotsUppercased);
      return value;
    }

    return null;
  }

  protected String getEnvironmentVariable(String propKey) {
    return System.getenv(propKey);
  }

  protected void logInexactEnvNameMatch(String propKey, String actualEnvVariableName) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Property '{}' resolved to environment variable '{}' by inexact match.", propKey, actualEnvVariableName);
    }
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
    return getPropertyList(key, Collections.emptyList(), namespace);
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
    return getPropertyMap(key, Collections.emptyMap(), namespace);
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
    collectMapEntriesWith(keyPrefix, m_configProperties.keySet(), this::getConfigPropertyValue, result);
    collectMapEntriesWith(keyPrefix, m_envProperties.keySet(), this::getEnvFilePropertyValue, result);
    collectMapEntriesFromEnvironment(key, namespace, result);
    collectMapEntriesWith(keyPrefix, System.getProperties().keySet(), this::getSystemPropertyValue, result);

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
  public boolean hasProviderProperties() {
    return m_hasProviderProperties;
  }

  protected void collectMapEntriesWith(String keyPrefix, Set<?> keySet, Function<String, String> propertyValueRetriever, Map<String, String> collector) {
    for (Object propKey : keySet) {
      String k = propKey.toString();
      String mapKey = toMapKey(k, keyPrefix);
      if (mapKey != null) {
        collector.put(mapKey, propertyValueRetriever.apply(k));
      }
    }
  }

  protected void collectMapEntriesFromEnvironment(String key, String namespace, Map<String, String> collector) {
    String propertyKey = toPropertyKey(key, namespace).toString();
    String valueFromEnv = lookupEnvironmentVariableValue(propertyKey);
    if (StringUtility.hasText(valueFromEnv)) {
      try {
        Map<String, String> decodedValue = parseJson(valueFromEnv);

        for (Entry<String, String> entry : decodedValue.entrySet()) {
          if (entry.getValue() == null) {
            collector.remove(entry.getKey());
          }
          else {
            collector.put(entry.getKey(), resolve(entry.getValue(), PLACEHOLDER_PATTERN));
          }
        }
      }
      catch (RuntimeException e) {
        throw new IllegalArgumentException(String.format("Error parsing value of property map '%s' as JSON value from an environment variable.", propertyKey), e);
      }
    }
  }

  /**
   * This method receives a JSON string and is expected to return a Map holding the JSON's attribute names as keys and
   * their respective values in their string representation. In addition, <code>null</code> values in the JSON object
   * must be preserved as Java <code>null</code> values. Values which are JSON objects or arrays must be preserved in
   * their string representation even though they may be reformatted (e.g. spaces removed as they will still be parsed).<br>
   * Consumers of this method should catch {@link RuntimeException} in order to handle errors that occur while parsing the
   * provided String as a JSON object.<br>
   * Although {@link PropertiesHelper} will never call this method with a <code>null</code> or empty string argument,
   * implementers are still expected to handle these cases as follows:
   * <ul>
   * <li><code>null</code> > <code>null</code></li>
   * <li>empty string > empty map
   * </ul>
   * Calls with a value not representing a JSON map must fail, e.g. arguments such as: a, true, &quot;a&quot;. This is
   * required to support reading a list property with only a single entry, in which case the reading of the JSON map
   * must fail to use the fallback instead.
   * <p>
   * Example:
   *
   * <pre>
   * {
   *     "key1": "value1",
   *     "key2": null,
   *     "key3": "",
   *     "key4": 1,
   *     "key5": "1",
   *     "key6": true,
   *     "key7": {"key71": "value71", "key72": "value72", "key73": "value73"},
   *     "key8": ["value81", "value82", "value83"]
   * }
   * </pre>
   * <p>
   * must yield the following Map
   *
   * <pre>
   * [
   *     "key1": "value1",
   *     "key2": null,
   *     "key3": "",
   *     "key4": "1",
   *     "key5": "1"
   *     "key6": "true",
   *     "key7": "{\"key71\": \"value71\", \"key72\": \"value72\", \"key73\": \"value73\"}",
   *     "key8": "[\"value81\", \"value82\", \"value83\"]"
   * ]
   * </pre>
   *
   * @param propertyValue
   *     The JSON string to parse into a {@link Map}. May be <code>null</code> or an empty string.
   * @throws RuntimeException
   *     Thrown in case of errors that occur while parsing the provided string as a JSON object (or anything
   *     else).
   */
  protected Map<String, String> parseJson(String propertyValue) {
    if (propertyValue == null) {
      return null;
    }
    if (!StringUtility.hasText(propertyValue)) {
      return Collections.emptyMap();
    }

    JSONObject jsonObject = new JSONObject(propertyValue);
    return jsonObject.keySet().stream().collect(
        StreamUtility.toMap(
            key -> key,
            key -> jsonObject.isNull(key) ? null : jsonObject.getString(key)));
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

  protected void resolveAll(Pattern pat) {
    for (Entry<String, String> entry : m_envProperties.entrySet()) {
      entry.setValue(resolve(entry.getValue(), pat));
    }
    for (Entry<String, String> entry : m_configProperties.entrySet()) {
      if (!m_importKeys.contains(entry.getKey())) {
        entry.setValue(resolve(entry.getValue(), pat));
      }
    }
  }

  /**
   * Resolves all variables of format <code>${variableName}</code> in the given expression according to the current
   * application context.
   *
   * @param value
   *          The expression to resolve.
   * @return A {@link String} where all variables have been replaced with their values.
   * @throws IllegalArgumentException
   *           if a variable could not be resolved in the current context.
   */
  @SuppressWarnings("squid:S1149")
  protected String resolve(String value, Pattern variablePattern) {
    BinaryOperator<String> replacer = (ignoredKey, variableName) -> {
      String replacement = getProperty(variableName);
      if (m_initializing) {
        m_referencedKeys.add(variableName);
      }
      return replacement;
    };
    return PropertiesUtility.resolveValue(null, value, variablePattern, replacer, true);
  }

  protected void importSystemImports(Set<String> importsToIgnore, Pattern pat) {
    Collection<String> systemImports = getPropertyMap(IMPORT_KEY).values();
    if (systemImports.isEmpty()) {
      return;
    }

    systemImports.forEach(impUrl -> {
      IPropertyProvider props = getPropertyProvider(impUrl);
      if (!m_parsedFiles.contains(props.getPropertiesIdentifier())) {
        parse(props);
      }
    });

  }

  protected Map<String, String> getConfigPropertyMap() {
    return m_configProperties;
  }

  protected String resolveImports(String key, String value) {
    if (IMPORT_PATTERN.matcher(key).find()) {
      // in case of an import evaluate system and environment write now to not import wrong properties

      value = resolve(resolveSystemProperty(key, value), PLACEHOLDER_PATTERN);
      IPropertyProvider propertyProvider = getPropertyProvider(value);
      if (!m_parsedFiles.contains(propertyProvider.getPropertiesIdentifier())) {
        parse(propertyProvider);
      }
      else {
        LOG.warn("Import of '{}' skipped because already imported: {}.", value, m_parsedFiles);
      }
      m_importKeys.add(key);
    }
    return value;
  }

  protected String resolveSystemProperty(String key, String defaultValue) {
    Function<Object, String> keyNormalizer = o -> {
      String k = o.toString().toLowerCase();
      if (k.startsWith(IMPORT_KEY)) {
        k = normalizeImportKey(k);
      }
      return k;
    };
    String value = defaultValue;
    if (StringUtility.isNullOrEmpty(key)) {
      return value;
    }

    // try env
    value = System.getenv().entrySet().stream()
        .map(e -> new AbstractMap.SimpleEntry<>(keyNormalizer.apply(e.getKey()), e.getValue()))
        .filter(e -> e.getKey().equals(key))
        .map(SimpleEntry::getValue)
        .findFirst().orElse(value);
    // try system properties
    value = System.getProperties().entrySet().stream()
        .map(e -> new AbstractMap.SimpleEntry<>(keyNormalizer.apply(e.getKey()), e.getValue().toString()))
        .filter(e -> e.getKey().equals(key))
        .map(SimpleEntry::getValue)
        .findFirst().orElse(value);
    // skip unresolved path variables (used for development mode)
    if (PATH_PLACEHOLDER_PATTERN.matcher(value).matches()) {
      return defaultValue;
    }
    return value;
  }

  protected IPropertyProvider getPropertyProvider(String configUrl) {
    return new ConfigPropertyProvider(configUrl);
  }

}

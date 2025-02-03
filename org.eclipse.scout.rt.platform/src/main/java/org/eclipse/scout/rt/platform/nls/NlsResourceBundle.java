/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NlsResourceBundle {

  private static final Logger LOG = LoggerFactory.getLogger(NlsResourceBundle.class);

  private static final String TEXT_RESOURCE_EXTENSION = "properties";

  private final NlsResourceBundle m_parent;
  private final Map<String, String> m_textMap;

  /**
   * @param parent
   *     may be null
   * @param textMap
   *     non null map without any null keys or null values
   */
  public NlsResourceBundle(NlsResourceBundle parent, Map<String, String> textMap) {
    m_parent = parent;
    Assertions.assertFalse(textMap.containsKey(null));
    Assertions.assertFalse(textMap.containsValue(null));
    m_textMap = Collections.unmodifiableMap(textMap);
  }

  /**
   * The parent bundle of this bundle if any.
   *
   * @return parent bundle
   */
  public NlsResourceBundle getParent() {
    return m_parent;
  }

  /**
   * Map with TextKey/TextValue mapping without any values from {@link #getParent()} resource.
   *
   * @return non null unmodifiable text map
   */
  public Map<String, String> getTextMap() {
    return m_textMap;
  }

  /**
   * Calls recursively the text mapping using {@link #getTextMap()} and {@link #getParent()}. Note that a mapping for
   * the same key might be visited multiple times. Visit order depends on parent order.
   */
  void collectTextMapping(BiConsumer<String, String> collector) {
    for (NlsResourceBundle rb = this; rb != null; rb = rb.m_parent) {
      for (Entry<String, String> e : rb.m_textMap.entrySet()) {
        collector.accept(e.getKey(), e.getValue());
      }
    }
  }

  /**
   * Lookups recursively the text mapping using {@link #getTextMap()} and {@link #getParent()}.
   *
   * @param key
   *     not null
   * @return null if there is no mapping for given key else most specific mapping found
   */
  public String getText(String key) {
    Assertions.assertNotNull(key);
    for (NlsResourceBundle rb = this; rb != null; rb = rb.m_parent) {
      String value = rb.m_textMap.get(key);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  public static NlsResourceBundle getBundle(NlsResourceBundle parent, String baseName, Locale locale, ClassLoader cl) {
    String suffix = getLocaleSuffix(locale);
    try {
      return NlsResourceBundle.getBundle(parent, baseName, suffix, cl);
    }
    catch (IOException e) {
      LOG.warn("Error loading nls resource with base name '{}' and suffix '{}'", baseName, suffix, e);
      return null;
    }
  }

  private static String getLocaleSuffix(Locale locale) {
    if (locale == null || Locale.ROOT.equals(locale)) {
      return "";
    }
    return "_" + locale;
  }

  /**
   * Creates and returns a new {@link NlsResourceBundle} if there is a property file found using the given base name and
   * suffix. Otherwise it returns null.
   */
  private static NlsResourceBundle getBundle(NlsResourceBundle parent, String baseName, String suffix, ClassLoader cl) throws IOException {
    String fileName = baseName.replace('.', '/') + suffix + '.' + TEXT_RESOURCE_EXTENSION;
    InputStream stream = cl.getResourceAsStream(fileName);
    if (stream != null) {
      try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
        return new NlsResourceBundle(parent, loadTextMap(reader));
      }
    }
    return null;
  }

  private static Map<String, String> loadTextMap(Reader reader) throws IOException {
    Properties properties = new Properties();
    properties.load(reader);
    Map<String, String> map = new HashMap<>();
    for (Entry<Object, Object> entry : properties.entrySet()) {
      map.put((String) entry.getKey(), (String) entry.getValue());
    }
    return map;
  }
}

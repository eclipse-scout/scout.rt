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
package org.eclipse.scout.rt.platform.context;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Map to associate properties with the current context.
 *
 * @since 3.8.2
 */
public class PropertyMap implements Iterable<Entry<Object, Object>> {

  /**
   * The {@link PropertyMap} which is currently associated with the current thread.
   */
  public static final ThreadLocal<PropertyMap> CURRENT = new ThreadLocal<>();

  /**
   * The property ID to indicate whether running in server scope.
   */
  public static final String PROP_SERVER_SCOPE = "scout.scope.server";

  /**
   * The property ID to indicate whether running in client scope.
   */
  public static final String PROP_CLIENT_SCOPE = "scout.scope.client";

  private final Map<Object, Object> m_properties;

  public PropertyMap() {
    this(null);
  }

  public PropertyMap(final PropertyMap origin) {
    m_properties = new ConcurrentHashMap<>();

    if (origin != null && !origin.m_properties.isEmpty()) {
      m_properties.putAll(origin.m_properties);
    }
  }

  /**
   * Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for
   * the key.
   */
  @SuppressWarnings("unchecked")
  public <VALUE> VALUE get(final Object key) {
    Assertions.assertNotNull(key, "Key must not be null");
    return (VALUE) m_properties.get(key);
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code defaultValue} if this map contains no mapping for
   * the key.
   */
  @SuppressWarnings("unchecked")
  public <VALUE> VALUE getOrDefault(final Object key, final VALUE defaultValue) {
    Assertions.assertNotNull(key, "Key must not be null");
    if (m_properties.containsKey(key)) {
      return (VALUE) m_properties.get(key);
    }
    else {
      return defaultValue;
    }
  }

  /**
   * Puts the given key-value association to this {@link PropertyMap}, or removes it if a <code>null</code>-value is
   * provided.
   */
  public void put(final Object key, final Object value) {
    Assertions.assertNotNull(key, "Key must not be null");
    if (value == null) {
      m_properties.remove(key);
    }
    else {
      m_properties.put(key, value);
    }
  }

  /**
   * Returns <code>true</code> if the property is contained in this map.
   */
  public boolean contains(final Object key) {
    Assertions.assertNotNull(key, "Key must not be null");
    return m_properties.containsKey(key);
  }

  public void clear() {
    m_properties.clear();
  }

  @Override
  public Iterator<Entry<Object, Object>> iterator() {
    return m_properties.entrySet().iterator();
  }

  /**
   * Checks whether the given property is contained in the current {@link PropertyMap}, and is convenience for
   * {@link PropertyMap#CURRENT#get(...)}.
   *
   * @return <code>true</code> if set in current property map, or else <code>false</code>.
   */
  public static boolean isSet(final String property) {
    final PropertyMap propertyMap = PropertyMap.CURRENT.get();
    if (propertyMap == null) {
      return false;
    }

    return propertyMap.contains(property);

  }
}

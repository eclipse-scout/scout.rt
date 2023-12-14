/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.serialization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerWhitelistProperty;

/**
 * Default whitelist used in {@link SerializationUtility} in particual in {@link BasicObjectSerializer}
 * <p>
 * Use {@link SerializationUtility} whenever possible.
 * <p>
 * Make sure to define {@link DefaultSerializerWhitelistProperty#getKey()} in the config.properties
 *
 * @since 11.0
 */
@ApplicationScoped
public class DefaultSerializerWhitelist implements Predicate<String> {

  protected Predicate<String> m_policy;
  protected final Map<String, Boolean> m_cache = new ConcurrentHashMap<>();

  @PostConstruct
  protected void postConstruct() {
    reset();
  }

  public void reset() {
    m_cache.clear();
    m_policy = SerializationUtility.createWhitelistPolicy(CONFIG.getPropertyValue(DefaultSerializerWhitelistProperty.class));
  }

  @Override
  public boolean test(String classname) {
    Boolean b = m_cache.get(classname);
    if (b == null) {
      b = m_policy.test(classname);
      m_cache.put(classname, b);
    }
    return b.booleanValue();
  }
}

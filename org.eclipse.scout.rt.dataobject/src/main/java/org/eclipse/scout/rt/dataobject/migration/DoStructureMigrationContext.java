/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

/**
 * This class represents the context used during migration of data objects.
 * <p>
 * Methods related with global context data are thread-safe ({@link #putGlobal(IDoStructureMigrationGlobalContextData)}
 * and {@link #getGlobal(Class)}). Methods related with local context data aren't. To use this context in multiple
 * threads, use {@link #copy()} in a new thread.
 */
@Bean
public class DoStructureMigrationContext {

  // maybe accessed by different threads
  protected final ConcurrentHashMap<Class<? extends IDoStructureMigrationGlobalContextData>, IDoStructureMigrationGlobalContextData> m_globalContextDataMap;

  // always access by single thread only
  protected final Map<Class<? extends IDoStructureMigrationLocalContextData>, Deque<IDoStructureMigrationLocalContextData>> m_localContextDataMap;

  public DoStructureMigrationContext() {
    m_globalContextDataMap = new ConcurrentHashMap<>();
    m_localContextDataMap = new HashMap<>();
    initDefaults();
  }

  protected DoStructureMigrationContext(DoStructureMigrationContext other) {
    m_globalContextDataMap = other.m_globalContextDataMap; // use the same one
    m_localContextDataMap = new HashMap<>(); // use new one (single thread usage only).
  }

  /**
   * Initializes default context data.
   */
  protected void initDefaults() {
    putGlobal(BEANS.get(DoStructureMigrationPassThroughLogger.class));
  }

  /**
   * Clones the context and keeps the same reference for global context data map but creates a new context map for local
   * context data.
   */
  protected DoStructureMigrationContext copy() {
    return new DoStructureMigrationContext(this);
  }

  /**
   * If the provided context data class has a {@link Bean} annotation, it is auto-created.
   *
   * @return Value for given global context data class.
   */
  public <T extends IDoStructureMigrationGlobalContextData> T getGlobal(Class<T> contextDataClass) {
    assertNotNull(contextDataClass, "contextDataClass is required");
    IDoStructureMigrationGlobalContextData contextData = m_globalContextDataMap.computeIfAbsent(contextDataClass, k -> {
      // auto-create global context data with @Bean annotation if not present yet
      if (contextDataClass.getAnnotation(Bean.class) != null) {
        return BEANS.get(contextDataClass);
      }
      return null;
    });
    return contextDataClass.cast(contextData);
  }

  /**
   * Put given global context data.
   */
  public DoStructureMigrationContext putGlobal(IDoStructureMigrationGlobalContextData contextData) {
    assertNotNull(contextData, "contextData is required");
    m_globalContextDataMap.put(contextData.getIdentifierClass(), contextData);
    return this;
  }

  /**
   * @return Value for given locale context data class.
   */
  public <T extends IDoStructureMigrationLocalContextData> T get(Class<T> contextDataClass) {
    assertNotNull(contextDataClass, "contextDataClass is required");

    Deque<IDoStructureMigrationLocalContextData> deque = m_localContextDataMap.get(contextDataClass);
    if (deque == null || deque.isEmpty()) {
      return null;
    }
    return contextDataClass.cast(deque.peek());
  }

  /**
   * Internal usage only.
   * <p>
   * Assign given context data. Usage:
   *
   * <pre>
   * ctx.push(contextData);
   * try {
   *   // migrate data object
   * }
   * finally {
   *   ctx.remove(contextData);
   * }
   * </pre>
   */
  protected DoStructureMigrationContext push(IDoStructureMigrationLocalContextData contextData) {
    assertNotNull(contextData, "contextData is required");
    Deque<IDoStructureMigrationLocalContextData> deque = m_localContextDataMap.computeIfAbsent(contextData.getIdentifierClass(), k -> new ArrayDeque<>());
    deque.push(contextData);
    return this;
  }

  /**
   * Internal usage only.
   * <p>
   * Remove context data from context (instance must be the same as used for
   * {@link #push(IDoStructureMigrationLocalContextData)}.
   */
  protected void remove(IDoStructureMigrationLocalContextData contextData) {
    assertNotNull(contextData, "contextData is required");
    Deque<IDoStructureMigrationLocalContextData> deque = m_localContextDataMap.get(contextData.getIdentifierClass());
    assertNotNull(deque, "no context data found for {}", contextData.getIdentifierClass());
    IDoStructureMigrationLocalContextData dequeElement = deque.peek(); // implementation detail: first peek only and check if same instance, then remove
    assertTrue(contextData == dequeElement, "last element in deque is not element to remove: remove '{}', deque: '{}'", contextData, dequeElement);
    deque.pop();
    if (deque.isEmpty()) {
      // remove deque if empty
      m_localContextDataMap.remove(contextData.getIdentifierClass());
    }
  }

  /**
   * Convenience method to access global logger.
   */
  public IDoStructureMigrationLogger getLogger() {
    return getGlobal(IDoStructureMigrationLogger.class);
  }

  /**
   * Convenience method to access global stats.
   */
  public DoStructureMigrationStatsContextData getStats() {
    return getGlobal(DoStructureMigrationStatsContextData.class);
  }
}

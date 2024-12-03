/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Represents a map which holds lists of {@link HybridActionContextElement}s indexed by a string key. The value of the
 * map is always a list, even when only a single context element is expected. Several convenience methods to add or
 * retrieve context elements are provided, though the actual map can also be accessed via {@link #getMap()}.
 */
@Bean
public class HybridActionContextElements {

  private final Map<String, List<HybridActionContextElement>> m_map = new LinkedHashMap<>();

  /**
   * @return whether the internal map is empty
   */
  public boolean isEmpty() {
    return m_map.isEmpty();
  }

  /**
   * @return the internal map (live), never {@code null}
   */
  public Map<String, List<HybridActionContextElement>> getMap() {
    return m_map;
  }

  /**
   * Returns a list of context elements for the given key. If the given key does not exist, an
   * {@link AssertionException} is thrown. Use {@link #optList(String)} to return {@code null} instead. Use
   * {@link #getSingle(String)} if only a single context element is expected.
   */
  public List<HybridActionContextElement> getList(String key) {
    List<HybridActionContextElement> contextElements = optList(key);
    return Assertions.assertNotNull(contextElements, "Missing context elements with key '{}'", key);
  }

  /**
   * Returns a list of context elements for the given key, or {@code null} if the given key does not exist.
   */
  public List<HybridActionContextElement> optList(String key) {
    return m_map.get(key);
  }

  /**
   * Returns the context element for the given key. If the given key does not exist, an {@link AssertionException} is
   * thrown. Use {@link #optSingle(String)} to return {@code null} instead. Use {@link #getList(String)} if more than
   * one context element is expected.
   */
  public HybridActionContextElement getSingle(String key) {
    HybridActionContextElement contextElement = optSingle(key);
    return Assertions.assertNotNull(contextElement, "Missing context element with key '{}'", key);
  }

  /**
   * Returns the context element for the given key, or {@code null} if the given key does not exist. Use
   * {@link #optList(String)} if more than one context element is expected.
   */
  public HybridActionContextElement optSingle(String key) {
    return CollectionUtility.firstElement(optList(key));
  }

  // ------------------------------

  /**
   * Stores the given map of context elements in the internal map. Nothing happens if the argument is {@code null} or
   * empty.
   */
  public HybridActionContextElements withMap(Map<String, List<HybridActionContextElement>> map) {
    if (map != null) {
      m_map.putAll(map);
    }
    return this;
  }

  /**
   * Stores the given list of context elements in the internal map under the given key. Existing elements for the same
   * key are overwritten.
   *
   * @throws AssertionException
   *     if any of the arguments is {@code null}
   */
  public HybridActionContextElements withElements(String key, List<HybridActionContextElement> contextElements) {
    Assertions.assertNotNull(key);
    Assertions.assertNotNull(contextElements);
    m_map.put(key, contextElements);
    return this;
  }

  /**
   * Stores the given context element in the internal map under the given key. Existing elements for the same key are
   * overwritten.
   *
   * @throws AssertionException
   *     if any of the arguments is {@code null}
   */
  public HybridActionContextElements withElement(String key, HybridActionContextElement contextElement) {
    Assertions.assertNotNull(key);
    m_map.put(key, CollectionUtility.arrayList(contextElement));
    return this;
  }

  /**
   * Creates a {@link HybridActionContextElement} and stores it in the internal map under the given key. Existing
   * elements for the same key are overwritten.
   *
   * @throws AssertionException
   *     if any of the arguments is {@code null}
   */
  public HybridActionContextElements withElement(String key, IWidget widget) {
    return withElement(key, HybridActionContextElement.of(widget));
  }

  /**
   * Creates a {@link HybridActionContextElement} and stores it in the internal map under the given key. Existing
   * elements for the same key are overwritten.
   *
   * @throws AssertionException
   *     if {@code key} or {@code widget} is {@code null}
   */
  public HybridActionContextElements withElement(String key, IWidget widget, Object element) {
    return withElement(key, HybridActionContextElement.of(widget, element));
  }
}

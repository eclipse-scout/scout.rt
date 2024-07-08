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
import java.util.Map;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public class HybridActionContextElements {

  private final Map<String, HybridActionContextElement> m_map = new LinkedHashMap<>();

  public Map<String, HybridActionContextElement> get() {
    return m_map;
  }

  public HybridActionContextElements withElements(Map<String, HybridActionContextElement> contextElements) {
    m_map.putAll(contextElements);
    return this;
  }

  public HybridActionContextElements withElement(String key, HybridActionContextElement contextElement) {
    m_map.put(key, contextElement);
    return this;
  }

  public HybridActionContextElements withElement(String key, IWidget widget) {
    m_map.put(key, HybridActionContextElement.of(widget));
    return this;
  }

  public HybridActionContextElements withElement(String key, IWidget widget, Object element) {
    m_map.put(key, HybridActionContextElement.of(widget, element));
    return this;
  }
}

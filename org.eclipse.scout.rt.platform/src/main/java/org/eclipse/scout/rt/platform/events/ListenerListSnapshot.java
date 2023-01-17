/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.events;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ListenerListSnapshot {
  private final Map<IListenerListWithManagement, Map<String/* listenerType + propertyName or eventType */, List<Object/* listener*/>>> m_map;

  public ListenerListSnapshot() {
    m_map = new IdentityHashMap<>();
  }

  /**
   * @return listenerList -&gt; context/type -&gt; list of listeners
   */
  public Map<IListenerListWithManagement, Map<String, List<Object>>> getData() {
    return m_map;
  }

  protected void add(IListenerListWithManagement listenerList, String context, Object listener) {
    if (listener == null) {
      return;
    }
    if (context == null) {
      context = "*";
    }
    m_map
        .computeIfAbsent(listenerList, listenerList2 -> new TreeMap<>())
        .computeIfAbsent(context, context2 -> new ArrayList<>())
        .add(listener);
  }

  public void dump() {
    try (PrintWriter out = new PrintWriter(System.out, true)) {
      dump(out);
    }
  }

  public void dump(PrintWriter out) {
    m_map.forEach((listenerList, map2) -> {
      out.println("LIST " + listenerList.getClass().getName() + " " + map2.values().stream().mapToInt(List::size).sum());
      map2.forEach((context, listeners) -> {
        out.println("  TYPE " + context + " " + listeners.size());
        listeners
            .stream()
            .collect(Collectors.groupingBy(name -> name))
            .forEach((name, elements) -> out.println("    " + name + ": " + elements.size()));
      });
      out.println();
    });
  }
}

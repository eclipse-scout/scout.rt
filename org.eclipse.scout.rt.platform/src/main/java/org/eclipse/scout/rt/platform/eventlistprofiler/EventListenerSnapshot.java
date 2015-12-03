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
package org.eclipse.scout.rt.platform.eventlistprofiler;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EventListenerSnapshot implements IEventListenerSnapshot {
  private HashMap<String/* listenerType,context */, List<Object/* listeners */>> m_map;

  public EventListenerSnapshot() {
    m_map = new HashMap<String, List<Object>>();
  }

  @Override
  public void add(Class<?> listenerType, String context, Object listener) {
    if (listener == null) {
      return;
    }
    String key = listenerType.getName();
    if (context != null) {
      key += "#" + context;
    }
    List<Object> list = m_map.get(key);
    if (list == null) {
      list = new ArrayList<Object>();
      m_map.put(key, list);
    }
    list.add(listener);
  }

  public void dump(PrintWriter out) {
    out.println("DUMP AT " + new Date());
    for (Map.Entry<String, List<Object>> e : m_map.entrySet()) {
      String key = e.getKey();
      List<Object> list = e.getValue();
      out.println("TYPE " + key + " " + list.size());
      TreeMap<String, Integer> types = new TreeMap<String, Integer>();
      for (Object listener : list) {
        String c = listener.getClass().getName();
        Integer i = types.get(c);
        if (i == null) {
          i = 0;
        }
        types.put(c, i + 1);
      }
      for (Map.Entry<String, Integer> entry : types.entrySet()) {
        out.println("  " + entry.getKey() + ": " + entry.getValue());
      }
    }
    out.println();
  }

}

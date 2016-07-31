/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JsonAdapterReferenceTracker {

  private final Map<Object, Set<Object>> m_usageMap;
  private final Set<IJsonAdapter<?>> m_unreferencedAdapters;

  public JsonAdapterReferenceTracker() {
    m_usageMap = new HashMap<>();
    m_unreferencedAdapters = new HashSet<IJsonAdapter<?>>();
  }

  public void put(IJsonAdapter jsonAdapter, IJsonAdapter parent) {
    Set<Object> parents = new HashSet<>();
    if (parent != null) {
      parents.add(parent);
    }
    m_usageMap.put(jsonAdapter, parents);
    m_unreferencedAdapters.remove(jsonAdapter);
  }

  public void remove(IJsonAdapter jsonAdapter, IJsonAdapter parent) {
    Set<Object> parents = m_usageMap.get(jsonAdapter);
    if (parents != null) {
      if (parent == null) {
        parents.clear();
      }
      else {
        parents.remove(parent);
      }
    }
    if (parents == null || parents.size() == 0) {
      m_unreferencedAdapters.add(jsonAdapter);
    }
  }

  public Set<IJsonAdapter<?>> unreferencedAdapters() {
    return m_unreferencedAdapters;
  }
}

/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;

/**
 * This class is a per session registry of IJsonAdapter instances.
 */
class JsonAdapterRegistry {

  private static class P_RegistryValue {

    private final String m_id;

    private final IJsonAdapter<?> m_jsonAdapter;

    private final Object m_model;

    P_RegistryValue(String id, Object model, IJsonAdapter<?> jsonAdapter) {
      m_id = id;
      m_model = model;
      m_jsonAdapter = jsonAdapter;
    }

  }

  /**
   * Maps the JsonAdapter ID to an IJsonAdapter instance (wrapped by a composite).
   */
  private final Map<String, P_RegistryValue> m_idAdapterMap;

  /**
   * Maps a Scout model instance to an IJsonAdapter instance (wrapped by a composite).
   */
  private final Map<Object, P_RegistryValue> m_modelAdapterMap;

  JsonAdapterRegistry() {
    m_idAdapterMap = new HashMap<>();
    m_modelAdapterMap = new HashMap<>();
  }

  void addJsonAdapter(IJsonAdapter jsonAdapter) {
    String id = jsonAdapter.getId();
    Object model = jsonAdapter.getModel();

    P_RegistryValue value = new P_RegistryValue(id, model, jsonAdapter);
    m_idAdapterMap.put(id, value);
    m_modelAdapterMap.put(model, value);
  }

  void removeJsonAdapter(String id) {
    P_RegistryValue value = m_idAdapterMap.remove(id);
    if (value != null) {
      //FIXME CGU not null check necessary for tests, check with awe
      m_modelAdapterMap.remove(value.m_model);
    }
  }

  IJsonAdapter<?> getJsonAdapter(String id) {
    return m_idAdapterMap.get(id).m_jsonAdapter;
  }

  IJsonAdapter<?> getJsonAdapter(Object model) {
    P_RegistryValue value = m_modelAdapterMap.get(model);
    if (value == null) {
      return null;
    }
    else {
      return value.m_jsonAdapter;
    }
  }

  void dispose() {
    for (P_RegistryValue value : CollectionUtility.arrayList(m_idAdapterMap.values())) {
      IJsonAdapter<?> jsonAdapter = value.m_jsonAdapter;
      jsonAdapter.dispose();
      removeJsonAdapter(jsonAdapter.getId());
    }
    assert m_idAdapterMap.isEmpty();
    assert m_modelAdapterMap.isEmpty();
  }

}

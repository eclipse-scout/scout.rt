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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;

/**
 * This class is a per session registry of IJsonAdapter instances.
 */
public class JsonAdapterRegistry {

  /**
   * Maps the JsonAdapter ID to an IJsonAdapter instance (wrapped by a composite).
   */
  private final Map<String, P_RegistryValue> m_idAdapterMap;

  /**
   * Maps Scout model instance to an IJsonAdapter instance (wrapped by a composite), grouped by parents
   */
  private final Map<IJsonAdapter<?>, Map<Object, P_RegistryValue>> m_parentAdapterMap;

  public JsonAdapterRegistry() {
    m_idAdapterMap = new HashMap<>();
    m_parentAdapterMap = new HashMap<>();
  }

  protected Map<String, P_RegistryValue> idAdapterMap() {
    return m_idAdapterMap;
  }

  protected Map<IJsonAdapter<?>, Map<Object, P_RegistryValue>> parentAdapterMap() {
    return m_parentAdapterMap;
  }

  public void addJsonAdapter(IJsonAdapter jsonAdapter, IJsonAdapter<?> parent) {
    String id = jsonAdapter.getId();
    Object model = jsonAdapter.getModel();

    P_RegistryValue value = new P_RegistryValue(model, jsonAdapter);
    m_idAdapterMap.put(id, value);
    Map<Object, P_RegistryValue> modelAdapterMap = m_parentAdapterMap.get(parent);
    if (modelAdapterMap == null) {
      modelAdapterMap = new HashMap<Object, JsonAdapterRegistry.P_RegistryValue>();
      m_parentAdapterMap.put(parent, modelAdapterMap);
    }
    modelAdapterMap.put(model, value);
  }

  public void removeJsonAdapter(String id) {
    P_RegistryValue value = m_idAdapterMap.remove(id);
    for (Iterator<Map<Object, P_RegistryValue>> it = m_parentAdapterMap.values().iterator(); it.hasNext();) {
      Map<Object, P_RegistryValue> modelAdapterMap = it.next();
      modelAdapterMap.remove(value.getModel());
      // Cleanup parentAdapterMap
      if (modelAdapterMap.isEmpty()) {
        it.remove();
      }
    }
  }

  public IJsonAdapter<?> getJsonAdapter(String id) {
    P_RegistryValue entry = m_idAdapterMap.get(id);
    return entry != null ? entry.getJsonAdapter() : null;
  }

  public <M, A extends IJsonAdapter<? super M>> A getJsonAdapter(M model, IJsonAdapter<?> parent) {
    Map<Object, P_RegistryValue> modelAdapterMap = m_parentAdapterMap.get(parent);
    if (modelAdapterMap == null) {
      return null;
    }
    P_RegistryValue value = modelAdapterMap.get(model);
    if (value == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    A result = (A) value.getJsonAdapter();
    return result;
  }

  public List<IJsonAdapter<?>> getJsonAdapters(IJsonAdapter<?> parent) {
    List<IJsonAdapter<?>> childAdapters = new LinkedList<IJsonAdapter<?>>();
    Map<Object, P_RegistryValue> modelAdapterMap = m_parentAdapterMap.get(parent);
    if (modelAdapterMap == null) {
      return childAdapters;
    }
    for (P_RegistryValue value : modelAdapterMap.values()) {
      childAdapters.add(value.getJsonAdapter());
    }
    return childAdapters;
  }

  public long getJsonAdapterCount() {
    long size = 0;
    for (Map<Object, P_RegistryValue> modelAdapterMap : m_parentAdapterMap.values()) {
      size += modelAdapterMap.size();
    }
    return size;
  }

  public void disposeAllJsonAdapters() {
    for (String key : CollectionUtility.arrayList(m_idAdapterMap.keySet())) {
      P_RegistryValue value = m_idAdapterMap.get(key);
      if (value != null) { // Check if still in registry (it might already have been removed by a parent adapter)
        IJsonAdapter<?> jsonAdapter = value.getJsonAdapter();
        jsonAdapter.dispose();
      }
    }
  }

  public boolean isEmpty() {
    return (m_idAdapterMap.isEmpty() && m_parentAdapterMap.isEmpty());
  }

  protected static class P_RegistryValue {

    private final IJsonAdapter<?> m_jsonAdapter;
    private final Object m_model;

    protected P_RegistryValue(Object model, IJsonAdapter<?> jsonAdapter) {
      m_model = model;
      m_jsonAdapter = jsonAdapter;
    }

    protected IJsonAdapter<?> getJsonAdapter() {
      return m_jsonAdapter;
    }

    protected Object getModel() {
      return m_model;
    }
  }
}

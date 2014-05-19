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
 * This class is a per session registry of IJsonRenderer instances.
 */
class JsonRendererRegistry {

  private static class P_RegistryValue {

    private final String m_id;

    private final IJsonRenderer<?> m_jsonRenderer;

    private final Object m_modelObject;

    P_RegistryValue(String id, Object modelObject, IJsonRenderer<?> jsonRenderer) {
      m_id = id;
      m_modelObject = modelObject;
      m_jsonRenderer = jsonRenderer;
    }

  }

  /**
   * Maps the JsonRenderer ID to an IJsonRenderer instance (wrapped by a composite).
   */
  private final Map<String, P_RegistryValue> m_idRendererMap;

  /**
   * Maps a Scout model instance to an IJsonRenderer instance (wrapped by a composite).
   */
  private final Map<Object, P_RegistryValue> m_modelRendererMap;

  JsonRendererRegistry() {
    m_idRendererMap = new HashMap<>();
    m_modelRendererMap = new HashMap<>();
  }

  void addJsonRenderer(String id, Object modelObject, IJsonRenderer jsonRenderer) {
    P_RegistryValue value = new P_RegistryValue(id, modelObject, jsonRenderer);
    m_idRendererMap.put(id, value);
    m_modelRendererMap.put(modelObject, value);
  }

  private void removeJsonRenderer(String id) {
    P_RegistryValue value = m_idRendererMap.remove(id);
    m_modelRendererMap.remove(value.m_modelObject);
  }

  IJsonRenderer<?> getJsonRenderer(String id) {
    return m_idRendererMap.get(id).m_jsonRenderer;
  }

  IJsonRenderer<?> getJsonRenderer(Object modelObject) {
    P_RegistryValue value = m_modelRendererMap.get(modelObject);
    if (value == null) {
      return null;
    }
    else {
      return value.m_jsonRenderer;
    }
  }

  void dispose() {
    for (P_RegistryValue value : CollectionUtility.arrayList(m_idRendererMap.values())) {
      IJsonRenderer<?> jsonRenderer = value.m_jsonRenderer;
      jsonRenderer.dispose();
      removeJsonRenderer(jsonRenderer.getId());
    }
    assert m_idRendererMap.isEmpty();
    assert m_modelRendererMap.isEmpty();
  }

}

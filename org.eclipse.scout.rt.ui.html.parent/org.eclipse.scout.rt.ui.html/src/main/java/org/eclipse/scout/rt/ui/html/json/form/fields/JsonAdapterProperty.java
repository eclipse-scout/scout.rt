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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

public abstract class JsonAdapterProperty<T> extends JsonProperty<T> {
  private IJsonSession m_jsonSession;
  private boolean m_global;
  private IFilter<Object> m_filter;
  private Set<IJsonAdapter> m_ownedAdapters = new HashSet<IJsonAdapter>();

  public JsonAdapterProperty(String propertyName, T model, IJsonSession session) {
    this(propertyName, model, session, false, null);
  }

  public JsonAdapterProperty(String propertyName, T model, IJsonSession session, boolean global, IFilter<Object> filter) {
    super(propertyName, model);
    m_jsonSession = session;
    m_global = global;
    m_filter = filter;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  @Override
  public Object valueToJsonOnPropertyChange(Object oldValue, Object newValue) {
    if (!m_global) {
      disposeAdapters(newValue);
    }
    createAdapters(newValue);
    return super.valueToJsonOnPropertyChange(oldValue, newValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object prepareValueForToJson(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Collection) {
      return JsonAdapterUtility.getAdapterIdsForModel(getJsonSession(), (Collection<Object>) value, getParentJsonAdapter(), m_filter);
    }
    return JsonAdapterUtility.getAdapterIdForModel(getJsonSession(), value, getParentJsonAdapter(), m_filter);
  }

  public void createAdapters() {
    createAdapters(modelValue());
  }

  protected void createAdapters(Object modelValue) {
    if (modelValue == null) {
      return;
    }
    if (modelValue instanceof Collection) {
      for (Object model : (Collection) modelValue) {
        createAdapter(model);
      }
    }
    else {
      createAdapter(modelValue);
    }
  }

  protected void createAdapter(Object model) {
    if (m_global) {
      m_jsonSession.getRootJsonAdapter().attachAdapter(model, m_filter);
    }
    else {
      IJsonAdapter<?> adapter = getParentJsonAdapter().attachAdapter(model, m_filter);

      // Only track owned adapters, only those may be disposed
      if (getParentJsonAdapter() == adapter.getParent()) {
        m_ownedAdapters.add(adapter);
      }
    }
  }

  /**
   * If the property is a collection: Disposes every old model if the new list does not contain it.
   * If the property is not a collection: Dispose the old model.
   */
  protected void disposeAdapters(Object newModels) {
    Set<IJsonAdapter> attachedAdapters = new HashSet<IJsonAdapter>(m_ownedAdapters);
    for (IJsonAdapter<?> adapter : attachedAdapters) {
      if (newModels instanceof Collection) {
        // Dispose adapter only if's model is not part of the new models
        if (!((Collection) newModels).contains(adapter.getModel())) {
          adapter.dispose();
          m_ownedAdapters.remove(adapter);
        }
      }
      else {
        adapter.dispose();
        m_ownedAdapters.remove(adapter);
      }
    }
  }

  @Override
  public void attachChildAdapters() {
    createAdapters();
  }

}

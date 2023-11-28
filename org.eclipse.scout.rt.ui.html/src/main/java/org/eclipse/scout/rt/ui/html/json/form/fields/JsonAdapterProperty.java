/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

@SuppressWarnings("squid:S00118")
public abstract class JsonAdapterProperty<MODEL_ELEMENT> extends JsonProperty<MODEL_ELEMENT> {
  private final IUiSession m_uiSession;
  private final Predicate<Object> m_global;
  private final boolean m_disposeOnChange;
  private final Predicate<Object> m_filter;
  private final Set<IJsonAdapter<?>> m_ownedAdapters = new HashSet<>();

  public JsonAdapterProperty(String propertyName, MODEL_ELEMENT model, IUiSession session) {
    super(propertyName, model);
    m_uiSession = session;
    JsonAdapterPropertyConfig config = createConfig();
    m_global = config.getGlobal();
    m_disposeOnChange = config.isDisposeOnChange();
    m_filter = config.getFilter();
  }

  protected JsonAdapterPropertyConfig createConfig() {
    return JsonAdapterPropertyConfigBuilder.defaultConfig();
  }

  protected IUiSession getUiSession() {
    return m_uiSession;
  }

  protected boolean isGlobal() {
    return isGlobal(null);
  }

  /**
   * @return true whether a global adapter should be created for the given model.
   */
  protected boolean isGlobal(Object model) {
    return m_global.test(model);
  }

  public boolean isDisposeOnChange() {
    return m_disposeOnChange;
  }

  protected Predicate<Object> getFilter() {
    return m_filter;
  }

  protected Set<IJsonAdapter<?>> getOwnedAdapters() {
    return m_ownedAdapters;
  }

  @Override
  public void handlePropertyChange(Object oldValue, Object newValue) {
    if (m_disposeOnChange) {
      disposeObsoleteAdapters(newValue);
    }
    createAdapters(newValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object prepareValueForToJson(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Collection) {
      return JsonAdapterUtility.getAdapterIdsForModel(getUiSession(), (Collection<Object>) value, getParentJsonAdapter(), m_filter);
    }
    return JsonAdapterUtility.getAdapterIdForModel(getUiSession(), value, getParentJsonAdapter(), m_filter);
  }

  public void createAdapters() {
    createAdapters(modelValue());
  }

  protected void createAdapters(Object modelValue) {
    if (modelValue == null) {
      return;
    }
    if (modelValue instanceof Collection) {
      for (Object model : (Iterable<?>) modelValue) {
        createAdapter(model);
      }
    }
    else {
      createAdapter(modelValue);
    }
  }

  protected void createAdapter(Object model) {
    if (!accept()) {
      return;
    }
    if (isGlobal(model)) {
      m_uiSession.getRootJsonAdapter().attachAdapter(model, m_filter);
    }
    else {
      IJsonAdapter<?> adapter = getParentJsonAdapter().attachAdapter(model, m_filter); // result may be null due to filter
      // Only track owned adapters, only those may be disposed
      if (m_disposeOnChange && adapter != null && adapter.getParent() == getParentJsonAdapter()) {
        m_ownedAdapters.add(adapter);
      }
    }
  }

  /**
   * If the new value is a collection:<br>
   * Disposes every owned old adapter if the new list does not contain it.
   * <p>
   * If the new value is <i>not</i> a collection:<br>
   * Dispose all old owned adapters.
   */
  protected void disposeObsoleteAdapters(Object newModels) {
    Set<IJsonAdapter<?>> attachedAdapters = new HashSet<>(m_ownedAdapters);
    for (IJsonAdapter<?> adapter : attachedAdapters) {
      if (newModels instanceof Collection) {
        // Dispose adapter only if's model is not part of the new models
        if (!((Collection<?>) newModels).contains(adapter.getModel())) {
          disposeAdapter(adapter);
        }
      }
      else {
        disposeAdapter(adapter);
      }
    }
  }

  /**
   * Disposes the given adapter and removes it from the set of owned adapters.
   */
  public void disposeAdapter(IJsonAdapter<?> adapter) {
    adapter.dispose();
    m_ownedAdapters.remove(adapter);
  }

  @Override
  public void attachChildAdapters() {
    createAdapters();
  }
}

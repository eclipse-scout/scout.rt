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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractJsonAdapter<T> implements IJsonAdapter<T> {

  private final IJsonSession m_jsonSession;
  private final T m_model;
  private final String m_id;
  private boolean m_attached;

  public AbstractJsonAdapter(T model, IJsonSession jsonSession, String id) {
    if (model == null) {
      throw new IllegalArgumentException("model must not be null");
    }
    m_model = model;
    m_jsonSession = jsonSession;
    m_id = id;
    m_jsonSession.registerJsonAdapter(this);
    init();
  }

  protected void init() {
    attachChildAdapters();
  }

  @Override
  public final String getId() {
    return m_id;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  @Override
  public T getModel() {
    return m_model;
  }

  /**
   * Attach child adapters of this adapter instance here by calling the <code>attachAdapter[s](model[s])</code> methods.
   * This will also <em>create</em> a new JSON adapter instance when the adapter does not yet exist for the given model.
   */
  protected void attachChildAdapters() {
  }

  protected void disposeChildAdapters() {
  }

  @Override
  public final void attach() {
    if (m_attached) {
      throw new IllegalStateException("Adapter already attached");
    }
    attachModel();
    m_attached = true;
  }

  /**
   * Override this method in order to attach listeners on the Scout model object.
   * At this point a JsonAdapter instance has been already created for the model object.
   * The default implementation does nothing.
   */
  protected void attachModel() {
  }

  @Override
  public void dispose() {
    if (m_attached) {
      detachModel();
    }
    disposeChildAdapters();
    m_jsonSession.unregisterJsonAdapter(m_id);
  }

  protected void detachModel() {
  }

  @Override
  public boolean isAttached() {
    return m_attached;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    putProperty(json, "objectType", getObjectType());
    putProperty(json, "id", getId());
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    throw new IllegalStateException("Event not handled. " + event);
  }

  protected final JSONObject putProperty(JSONObject json, String key, Object value) {
    return JsonObjectUtility.putProperty(json, key, value);
  }

  protected final void disposeAdapters(Collection<? extends Object> models) {
    for (Object model : models) {
      disposeAdapter(model);
    }
  }

  protected final void optDisposeAdapter(Object model) {
    if (model != null) {
      disposeAdapter(model);
    }
  }

  protected final void disposeAdapter(Object model) {
    IJsonAdapter<?> jsonAdapter = m_jsonSession.getJsonAdapter(model);
    // on session dispose, the adapters get disposed in random order, so they may already be disposed when calling this method
    if (jsonAdapter != null) {
      jsonAdapter.dispose();
    }
  }

  protected final void optAttachAdapter(Object model) {
    if (model != null) {
      attachAdapter(model);
    }
  }

  protected final IJsonAdapter<?> attachAdapter(Object model) {
    return m_jsonSession.getOrCreateJsonAdapter(model);
  }

  protected final List<IJsonAdapter<?>> attachAdapters(Collection<?> models) {
    List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
    for (Object model : models) {
      adapters.add(attachAdapter(model));
    }
    return adapters;
  }

  /**
   * Returns an existing adapter for the given model. When no adapter is registered for the given model this method will
   * return null. This method is a shortcut for <code>getJsonSession().getJsonAdapter(model)</code>.
   */
  protected final IJsonAdapter<?> getAdapter(Object model) {
    return getJsonSession().getJsonAdapter(model);
  }

  protected final Collection<IJsonAdapter<?>> getAdapters(Collection<?> models) {
    List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
    for (Object model : models) {
      adapters.add(getAdapter(model));
    }
    return adapters;
  }

  /**
   * Returns the ID of the JSON adapter for the given model.
   * This method requires that the adapter has already been created before.
   * The method will never create a new adapter instance.
   */
  protected final String getAdapterIdForModel(Object model) {
    IJsonAdapter<?> adapter = getAdapter(model);
    if (adapter == null) {
      throw new IllegalArgumentException("No adapter registered for model=" + model);
    }
    // can assert already isAttached() here?
    return adapter.getId();
  }

  /**
   * Returns a list of IDs of the JSON adapters for the given models.
   * This method requires that the adapter has already been created before.
   * The method will never create a new adapter instance.
   */
  protected final JSONArray getAdapterIdsForModels(Collection<?> models) {
    return getAdapterIds(getAdapters(models));
  }

  /**
   * Returns a list of IDs of the JSON adapters for the given adapters.
   */
  protected final JSONArray getAdapterIds(Collection<IJsonAdapter<?>> adapters) {
    return JsonObjectUtility.adapterIdsToJson(adapters);
  }

  protected final JSONObject putAdapterIdProperty(JSONObject json, String key, Object model) {
    return JsonObjectUtility.putProperty(json, key, getAdapterIdForModel(model));
  }

  protected final JSONObject optPutAdapterIdProperty(JSONObject json, String key, Object model) {
    if (model == null) {
      return json;
    }
    else {
      return JsonObjectUtility.putProperty(json, key, getAdapterIdForModel(model));
    }
  }

  protected final JSONObject putAdapterIdsProperty(JSONObject json, String key, Collection<?> models) {
    return JsonObjectUtility.putProperty(json, key, getAdapterIdsForModels(models));
  }

  protected final void addActionEvent(String eventName, JSONObject json) {
    getJsonSession().currentJsonResponse().addActionEvent(getId(), eventName, json);
  }

  protected void addPropertyChangeEvent(String propertyName, Object newValue) {
    if (newValue instanceof IJsonAdapter<?>) {
      throw new IllegalArgumentException("Cannot pass an adapter instance to a JSON response");
    }
    getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), propertyName, newValue);
  }

}

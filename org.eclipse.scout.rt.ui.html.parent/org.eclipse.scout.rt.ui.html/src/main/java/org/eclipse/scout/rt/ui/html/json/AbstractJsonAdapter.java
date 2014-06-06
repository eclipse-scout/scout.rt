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

import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractJsonAdapter<T> implements IJsonAdapter<T> {

  private final IJsonSession m_jsonSession;
  private final T m_model;
  private final String m_id;
  private boolean m_initialized;

  public AbstractJsonAdapter(T model, IJsonSession jsonSession, String id) {
    if (model == null) {
      throw new IllegalArgumentException("model must not be null");
    }
    m_model = model;
    m_jsonSession = jsonSession;
    m_id = id;
    m_jsonSession.registerJsonAdapter(this);
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

  @Override
  public final void init() {
    attachModel();
    m_initialized = true;
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
    if (!m_initialized) {
      return; // TODO AWE: (ask C.GU) das wäre auch eher IllegalState, nicht?
    }
    detachModel();
    m_jsonSession.unregisterJsonAdapter(m_id);
  }

  protected void detachModel() {
  }

  public boolean isInitialized() {
    return m_initialized;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    putProperty(json, "objectType", getObjectType());
    putProperty(json, "id", getId());
    return json;
  }

  protected final JSONObject putProperty(JSONObject json, String key, Object value) {
    return JsonObjectUtility.putProperty(json, key, value);
  }

  protected JSONObject newJsonObjectForModel(String propertyName, Object model) {
    return JsonObjectUtility.newJsonObjectForModel(getJsonSession(), propertyName, model);
  }

  protected final JSONObject modelToJson(Object model) {
    return JsonObjectUtility.modelToJson(getJsonSession(), model);
  }

  protected final JSONArray modelsToJson(List<?> models) {
    return JsonObjectUtility.modelsToJson(getJsonSession(), models);
  }

  protected void disposeJsonAdapters(Collection<? extends Object> models) {
    JsonObjectUtility.disposeJsonAdapters(getJsonSession(), models);
  }

  protected void disposeJsonAdapter(Object model) {
    JsonObjectUtility.disposeJsonAdapter(getJsonSession(), model);
  }

}

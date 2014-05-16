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
package org.eclipse.scout.rt.ui.json;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonRenderer<T> implements IJsonRenderer<T> {

  private final IJsonSession m_jsonSession;
  private final T m_modelObject;
  private final String m_id;
  private boolean m_initialized;

  public AbstractJsonRenderer(T modelObject, IJsonSession jsonSession, String id) {
    if (modelObject == null) {
      throw new IllegalArgumentException("modelObject must not be null");
    }
    m_modelObject = modelObject;
    m_jsonSession = jsonSession;
    m_id = id;
  }

  @Override
  public final String getId() {
    return m_id;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  @Override
  public T getModelObject() {
    return m_modelObject;
  }

  @Override
  public final void init() {
    attachModel();
    m_initialized = true;
  }

  /**
   * Override this method in order to attach listeners on the Scout model object.
   * At this point a JsonRenderer instance has been already created for the model object.
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

  /**
   * Adds a property to the given JSON object and deals with exceptions.
   */
  protected final JSONObject putProperty(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  /**
   * Calls <code>jsonSession.getOrCreateJsonRenderer(Object)</code> and returns <code>toJson()</code>.
   */
  protected final JSONObject modelObjectToJson(Object modelObject) {
    IJsonRenderer<?> jsonRenderer = getJsonSession().getOrCreateJsonRenderer(modelObject);
    return jsonRenderer.toJson();
  }

  /**
   * Calls <code>jsonSession.getOrCreateJsonRenderer(Object)</code> for each object in the model objects List and adds
   * the return values of <code>toJson()</code> to the JSONArray.
   */
  protected final JSONArray modelObjectsToJson(List<?> modelObjects) {
    JSONArray array = new JSONArray();
    for (Object modelObject : modelObjects) {
      array.put(modelObjectToJson(modelObject));
    }
    return array;
  }

}

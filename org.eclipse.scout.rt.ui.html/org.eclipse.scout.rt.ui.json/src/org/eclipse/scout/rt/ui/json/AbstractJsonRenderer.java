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

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonRenderer<T> implements IJsonRenderer<T> {
  private final IJsonSession m_jsonSession;
  private final T m_modelObject;
  private final String m_id;
  private boolean m_initialized;

  public AbstractJsonRenderer(T modelObject, IJsonSession jsonSession) {
    this(modelObject, jsonSession, null);
  }

  public AbstractJsonRenderer(T modelObject, IJsonSession jsonSession, String id) {
    if (modelObject == null) {
      throw new IllegalArgumentException("modelObject must not be null");
    }
    m_modelObject = modelObject;
    m_jsonSession = jsonSession;
    if (id == null) {
      id = jsonSession.createUniqueIdFor(this);
    }
    m_id = id;
  }

  @Override
  public String getId() {
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
  public final void init() throws JsonException {
    getJsonSession().registerJsonRenderer(getId(), this);
    attachModel();
    m_initialized = true;
  }

  protected void attachModel() throws JsonException {
  }

  @Override
  public void dispose() throws JsonException {
    if (!m_initialized) {
      return;
    }
    detachModel();
    getJsonSession().unregisterJsonRenderer(getId());
  }

  protected void detachModel() throws JsonException {
  }

  public boolean isInitialized() {
    return m_initialized;
  }

  @Override
  public JSONObject toJson() throws JsonException {
    JSONObject json = new JSONObject();
    try {
      json.put("objectType", getObjectType());
      json.put("id", getId());

      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

}

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

public abstract class AbstractJsonRenderer<T extends Object> implements IJsonRenderer {
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

  protected T getModelObject() {
    return m_modelObject;
  }

  @Override
  public final void init() throws JsonUIException {
    getJsonSession().registerJsonRenderer(getId(), this);
    attachModel();
    m_initialized = true;
  }

  protected void attachModel() throws JsonUIException {
  }

  @Override
  public void dispose() throws JsonUIException {
    if (!m_initialized) {
      return;
    }
    detachModel();
    getJsonSession().unregisterJsonRenderer(getId());
  }

  protected void detachModel() throws JsonUIException {
  }

  public boolean isInitialized() {
    return m_initialized;
  }

}

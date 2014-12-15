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

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

public abstract class JsonAdapterProperty<T> extends JsonProperty<T> {
  private IJsonSession m_jsonSession;
  private boolean m_global;

  public JsonAdapterProperty(String propertyName, T model, IJsonSession session) {
    this(propertyName, model, session, false);
  }

  public JsonAdapterProperty(String propertyName, T model, IJsonSession session, boolean global) {
    super(propertyName, model);
    m_jsonSession = session;
    m_global = global;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  @Override
  public Object valueToJsonOnPropertyChange(Object oldValue, Object newValue) {
    if (!m_global && oldValue != null) {
      disposeAdapter(oldValue);
    }
    createAdapters(newValue);
    return super.valueToJsonOnPropertyChange(oldValue, newValue);
  }

  @Override
  public Object prepareValueForToJson(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Collection) {
      return JsonAdapterUtility.getAdapterIdsForModel(getJsonSession(), (Collection) value, getParentJsonAdapter());
    }
    return JsonAdapterUtility.getAdapterIdForModel(getJsonSession(), value, getParentJsonAdapter());
  }

  public void createAdapters() {
    createAdapters(modelValue());
  }

  private void createAdapters(Object modelValue) {
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

  private void createAdapter(Object model) {
    if (m_global) {
      m_jsonSession.getRootJsonAdapter().attachAdapter(model);
    }
    else {
      getParentJsonAdapter().attachAdapter(model);
    }
  }

  public void disposeAdapters() {
    disposeAdapter(modelValue());
  }

  private void disposeAdapters(Object modelValue) {
    if (modelValue == null) {
      return;
    }
    if (modelValue instanceof Collection) {
      for (Object model : (Collection) modelValue) {
        disposeAdapter(model);
      }
    }
    else {
      disposeAdapter(modelValue);
    }
  }

  protected void disposeAdapter(Object model) {
    IJsonAdapter<Object> jsonAdapter = m_jsonSession.getJsonAdapter(model, getParentJsonAdapter(), false);
    if (jsonAdapter != null) {
      jsonAdapter.dispose();
    }
  }

  @Override
  public void attachChildAdapters() {
    createAdapters();
  }

}

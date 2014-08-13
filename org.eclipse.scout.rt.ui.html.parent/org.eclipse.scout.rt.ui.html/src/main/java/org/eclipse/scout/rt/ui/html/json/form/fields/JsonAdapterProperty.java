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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;

public abstract class JsonAdapterProperty<T> extends JsonProperty<T> {
  private IJsonSession m_jsonSession;

  public JsonAdapterProperty(String propertyName, T model, IJsonSession session) {
    super(propertyName, model);
    m_jsonSession = session;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  @Override
  public Object prepareValueForToJson(Object value) {
    if (value == null) {
      return null;
    }

    if (value instanceof Collection) {
      Collection models = (Collection) value;
      List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
      for (Object model : models) {
        adapters.add(m_jsonSession.getOrCreateJsonAdapter(model));
      }
      return JsonObjectUtility.adapterIdsToJson(adapters);
    }
    else {
      return m_jsonSession.getOrCreateJsonAdapter(value).getId();
    }
  }

  public void attachAdapters() {
    if (modelValue() == null) {
      return;
    }

    if (modelValue() instanceof Collection) {
      for (Object model : (Collection) modelValue()) {
        m_jsonSession.getOrCreateJsonAdapter(model);
      }
    }
    else {
      m_jsonSession.getOrCreateJsonAdapter(modelValue());
    }
  }

  public void disposeAdapters() {
    if (modelValue() == null) {
      return;
    }

    if (modelValue() instanceof Collection) {
      for (Object model : (Collection) modelValue()) {
        disposeAdapter(model);
      }
    }
    else {
      disposeAdapter(modelValue());
    }
  }

  protected void disposeAdapter(Object model) {
    //TODO Same code as in AbstractJsonAdapter, maybe share
    IJsonAdapter<?> jsonAdapter = m_jsonSession.getJsonAdapter(model);
    // on session dispose, the adapters get disposed in random order, so they may already be disposed when calling this method
    if (jsonAdapter != null) {
      jsonAdapter.dispose();
    }
  }
}

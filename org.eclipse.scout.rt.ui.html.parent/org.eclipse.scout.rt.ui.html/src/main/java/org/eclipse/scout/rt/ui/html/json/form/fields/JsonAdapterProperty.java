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

import org.eclipse.scout.rt.ui.html.json.IJsonSession;

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
    return m_jsonSession.getOrCreateJsonAdapter(value).getId();
  }

}

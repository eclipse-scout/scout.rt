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
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;

public abstract class JsonAdapterProperty<T, V> extends JsonProperty<T, V> {
  private IJsonSession m_jsonSession;

  public JsonAdapterProperty(String propertyName, T modelObject, IJsonSession session) {
    super(propertyName, modelObject);
    m_jsonSession = session;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  @Override
  public Object valueToJson(Object value) {
    return JsonObjectUtility.modelObjectToJson(m_jsonSession, value);
  }

}

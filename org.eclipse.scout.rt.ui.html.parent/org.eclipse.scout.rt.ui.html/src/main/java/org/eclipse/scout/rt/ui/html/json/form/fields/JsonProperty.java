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

/**
 * This property class is used to map a model object property to a JSON property and is used to automatically create a
 * JSON object for a model and also to propagate property change events to the browser-side client.
 * 
 * @param <T>
 *          Type of model object
 * @param <V>
 *          Type of value
 */
public abstract class JsonProperty<T, V> {

  private final String m_propertyName;

  private final T m_modelObject;

  public JsonProperty(String propertyName, T modelObject) {
    m_propertyName = propertyName;
    m_modelObject = modelObject;
  }

  public String getPropertyName() {
    return m_propertyName;
  }

  abstract protected V getValueImpl(T modelObject);

  public Object valueToJson(Object value) {
    return value;
  }

  public Object getValueAsJson() {
    return valueToJson(getValueImpl(m_modelObject));
  }

}

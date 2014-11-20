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

/**
 * This property class is used to map a model object property to a JSON property and is used to automatically create a
 * JSON object for a model and also to propagate property change events to the browser-side client.
 *
 * @param <T>
 *          Type of model object
 */
public abstract class JsonProperty<T> {

  private final String m_propertyName;

  private final T m_model;

  public JsonProperty(String propertyName, T model) {
    m_propertyName = propertyName;
    m_model = model;
  }

  public String getPropertyName() {
    return m_propertyName;
  }

  protected T getModel() {
    return m_model;
  }

  abstract protected Object modelValue();

  public Object prepareValueForToJson(Object value) {
    return value;
  }

  public Object valueToJson() {
    return prepareValueForToJson(modelValue());
  }

  @Override
  public String toString() {
    return m_propertyName + ": " + m_model;
  }

}

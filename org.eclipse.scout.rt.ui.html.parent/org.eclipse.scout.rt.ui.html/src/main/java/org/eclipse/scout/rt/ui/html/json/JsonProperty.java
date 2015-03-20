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

import java.util.LinkedList;
import java.util.List;

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
  private IJsonAdapter<?> m_parentJsonAdapter;
  private List<JsonProperty<?>> m_slaveProperties = new LinkedList<JsonProperty<?>>();
  private boolean m_valueSent;

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

  public void setParentJsonAdapter(IJsonAdapter<?> parentJsonAdapter) {
    m_parentJsonAdapter = parentJsonAdapter;
  }

  public IJsonAdapter<?> getParentJsonAdapter() {
    return m_parentJsonAdapter;
  }

  public void addSlaveProperty(JsonProperty<?> property) {
    m_slaveProperties.add(property);
  }

  public List<JsonProperty<?>> getSlaveProperties() {
    return m_slaveProperties;
  }

  public void setValueSent(boolean valueSent) {
    m_valueSent = valueSent;
  }

  public boolean isValueSent() {
    return m_valueSent;
  }

  public boolean accept() {
    return true;
  }

  /**
   * Returns the <i>raw</i> model value.
   * <p>
   * <b>Important:</b> Never convert the model value here! Use {@link #prepareValueForToJson(Object)} instead for that
   * purpose. Otherwise, the conversion will not be applied when the property value changes and the new value is
   * directly passed to {@link #valueToJsonOnPropertyChange(Object, Object)}.
   */
  protected abstract Object modelValue();

  public Object prepareValueForToJson(Object value) {
    // TODO BSH JSON | Check if we can / should add convenience here (e.g. for Date, lists etc.)
    return value;
  }

  public Object valueToJson() {
    return prepareValueForToJson(modelValue());
  }

  public Object valueToJsonOnPropertyChange(Object oldValue, Object newValue) {
    return prepareValueForToJson(newValue);
  }

  public void attachChildAdapters() {
  }

  @Override
  public String toString() {
    return m_propertyName + ": " + m_model;
  }
}

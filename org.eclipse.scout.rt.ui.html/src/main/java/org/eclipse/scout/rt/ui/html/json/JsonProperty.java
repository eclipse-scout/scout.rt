/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
 * @param <MODEL_ELEMENT>
 *          Type of model object
 */
@SuppressWarnings("squid:S00118")
public abstract class JsonProperty<MODEL_ELEMENT> {

  private final String m_propertyName;
  private final MODEL_ELEMENT m_model;
  private IJsonAdapter<?> m_parentJsonAdapter;
  private List<JsonProperty<?>> m_lazyProperties = new LinkedList<JsonProperty<?>>();
  private boolean m_valueSent;

  public JsonProperty(String propertyName, MODEL_ELEMENT model) {
    m_propertyName = propertyName;
    m_model = model;
  }

  public String getPropertyName() {
    return m_propertyName;
  }

  /**
   * Returns the property name to be used in the JSON. By default, the model property name ({@link #getPropertyName()})
   * is used, but implementing classes may choose to overwrite this method to use a different property name in the UI.
   */
  public String jsonPropertyName() {
    return getPropertyName();
  }

  protected MODEL_ELEMENT getModel() {
    return m_model;
  }

  public void setParentJsonAdapter(IJsonAdapter<?> parentJsonAdapter) {
    m_parentJsonAdapter = parentJsonAdapter;
  }

  public IJsonAdapter<?> getParentJsonAdapter() {
    return m_parentJsonAdapter;
  }

  public void addLazyProperty(JsonProperty<?> property) {
    m_lazyProperties.add(property);
  }

  public List<JsonProperty<?>> getLazyProperties() {
    return m_lazyProperties;
  }

  public void setValueSent(boolean valueSent) {
    m_valueSent = valueSent;
  }

  /**
   * Necessary for the lazy loading behavior. Will be used to check whether a lazy property has already been sent.
   *
   * @return true if the value has been sent, false if not
   */
  public boolean isValueSent() {
    return m_valueSent;
  }

  /**
   * Controls the lazy loading behavior. Expects that the property was added using
   * {@link #addLazyProperty(JsonProperty)}.
   *
   * @return true if the value is allowed to be sent, false if not.
   */
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

  /**
   * Returns an Object which is suitable to be used in a JSONObject. The default implementation simply returns the given
   * value. Complex model objects require more sophisticated approaches to transform the model state into JSON.
   */
  public Object prepareValueForToJson(Object value) {
    // TODO [5.2] bsh: JSON | Check if we can / should add convenience here (e.g. for Date, lists etc.)
    return value;
  }

  public Object valueToJson() {
    return prepareValueForToJson(modelValue());
  }

  /**
   * This method is called when the property value changes. Subclasses of JsonProperty may do something in that case.
   * The default implementation does nothing. Note: this method must be always executed, event when the event itself is
   * filtered (which means, it isn't sent to the browser - however, the method may still have an impact on JsonAdapter
   * instances on the client side).
   */
  public void handlePropertyChange(Object oldValue, Object newValue) {
  }

  public void attachChildAdapters() {
  }

  @Override
  public String toString() {
    return m_propertyName + ": " + m_model;
  }
}

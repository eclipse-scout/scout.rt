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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.json.JSONObject;

public abstract class AbstractJsonPropertyObserver<T extends IPropertyObserver> extends AbstractJsonAdapter<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonPropertyObserver.class);

  private P_PropertyChangeListener m_propertyChangeListener;
  private PropertyEventFilter m_propertyEventFilter;
  private boolean m_initializingProperties;

  /**
   * Key = propertyName.
   */
  private Map<String, JsonProperty<?>> m_jsonProperties;

  public AbstractJsonPropertyObserver(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
    m_propertyEventFilter = new PropertyEventFilter();
    m_jsonProperties = new HashMap<>();
  }

  @Override
  public void init() {
    m_initializingProperties = true;
    initJsonProperties(getModel());
    m_initializingProperties = false;
    super.init();
  }

  protected void initJsonProperties(T model) {
  }

  /**
   * Adds a property to the list of JSON properties. These properties are automatically managed by the JsonAdapter,
   * which means they're automatically included in the object returned by the <code>toJson()</code> method and also
   * are propagated to the browser-side client when a property change event occurs.
   */
  @SuppressWarnings("unchecked")
  protected void putJsonProperty(JsonProperty jsonProperty) {
    if (!m_initializingProperties) {
      throw new IllegalStateException("Putting properties is only allowed in initJsonProperties.");
    }
    jsonProperty.setParentJsonAdapter(this);
    m_jsonProperties.put(jsonProperty.getPropertyName(), jsonProperty);
  }

  protected JsonProperty<?> getJsonProperty(String name) {
    return m_jsonProperties.get(name);
  }

  /**
   * Adds a filter condition for the given property and value to the current response. When later in this event
   * handler a property change event occurs with the same value, the event is not sent back to the client (=filtered).
   */
  protected void addPropertyEventFilterCondition(String propertyName, Object value) {
    m_propertyEventFilter.addCondition(new PropertyChangeEventFilterCondition(propertyName, value));
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    for (JsonProperty<?> prop : m_jsonProperties.values()) {
      if (prop.accept()) {
        prop.attachChildAdapters();
      }
    }
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_propertyChangeListener == null) {
      m_propertyChangeListener = new P_PropertyChangeListener();
      getModel().addPropertyChangeListener(m_propertyChangeListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_propertyChangeListener != null) {
      getModel().removePropertyChangeListener(m_propertyChangeListener);
      m_propertyChangeListener = null;
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    for (JsonProperty<?> jsonProperty : m_jsonProperties.values()) {
      if (jsonProperty.accept()) {
        putProperty(json, jsonProperty.getPropertyName(), jsonProperty.valueToJson());
        jsonProperty.setValueSent(true);
      }
    }
    return json;
  }

  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    String propertyName = event.getPropertyName();
    if (m_jsonProperties.containsKey(propertyName)) {
      JsonProperty<?> jsonProperty = m_jsonProperties.get(propertyName);
      handleSlaveJsonProperties(jsonProperty);
      event = m_propertyEventFilter.filter(event);
      if (event != null && jsonProperty.accept()) {
        handleJsonPropertyChange(jsonProperty, event.getOldValue(), event.getNewValue());
      }
    }
    else {
      event = m_propertyEventFilter.filter(event);
      if (event != null) {
        handleModelPropertyChange(propertyName, event.getOldValue(), event.getNewValue());
      }
    }
  }

  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
  }

  private void handleSlaveJsonProperties(JsonProperty<?> masterProperty) {
    for (JsonProperty<?> slave : masterProperty.getSlaveProperties()) {
      Object modelValue = slave.modelValue();
      if (!slave.isValueSent() && modelValue != null) {
        handleJsonPropertyChange(slave, null, modelValue);
      }
    }
  }

  private void handleJsonPropertyChange(JsonProperty<?> jsonProperty, Object oldValue, Object newValue) {
    String propertyName = jsonProperty.getPropertyName();
    if (jsonProperty.accept()) {
      addPropertyChangeEvent(propertyName, jsonProperty.valueToJsonOnPropertyChange(oldValue, newValue));
      jsonProperty.setValueSent(true);
      LOG.debug("Added property change event '" + propertyName + ": " + newValue + "' for " + getObjectType() + " with id " + getId() + ". Model: " + getModel());
    }
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      handleModelPropertyChange(event);
    }
  }

  @Override
  public void cleanUpEventFilters() {
    m_propertyEventFilter.removeAllConditions();
  }

}

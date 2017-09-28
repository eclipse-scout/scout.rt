/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJsonPropertyObserver<T extends IPropertyObserver> extends AbstractJsonAdapter<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonPropertyObserver.class);

  private PropertyChangeListener m_propertyChangeListener;
  private final PropertyEventFilter m_propertyEventFilter;
  private boolean m_initializingProperties;

  /**
   * Key = propertyName.
   */
  private final Map<String, JsonProperty<?>> m_jsonProperties;

  public AbstractJsonPropertyObserver(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
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
   * which means they're automatically included in the object returned by the <code>toJson()</code> method and also are
   * propagated to the browser-side client when a property change event occurs.
   */
  @SuppressWarnings("unchecked")
  protected void putJsonProperty(JsonProperty jsonProperty) {
    if (!m_initializingProperties) {
      throw new IllegalStateException("Putting properties is only allowed in initJsonProperties.");
    }
    jsonProperty.setParentJsonAdapter(this);
    m_jsonProperties.put(jsonProperty.getPropertyName(), jsonProperty);
  }

  protected void removeJsonProperty(String jsonPropertyName) {
    if (!m_initializingProperties) {
      throw new IllegalStateException("Putting properties is only allowed in initJsonProperties.");
    }
    m_jsonProperties.remove(jsonPropertyName);
  }

  protected JsonProperty<?> getJsonProperty(String name) {
    return m_jsonProperties.get(name);
  }

  /**
   * Adds a filter condition for the given property and value to the current response. When later in this event handler
   * a property change event occurs with the same value, the event is not sent back to the client (=filtered).
   */
  protected void addPropertyEventFilterCondition(String propertyName, Object value) {
    addPropertyEventFilterCondition(new PropertyChangeEventFilterCondition(propertyName, value));
  }

  protected void addPropertyEventFilterCondition(IPropertyChangeEventFilterCondition condition) {
    m_propertyEventFilter.addCondition(condition);
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
    if (m_propertyChangeListener != null) {
      throw new IllegalStateException();
    }
    m_propertyChangeListener = new P_PropertyChangeListener();
    getModel().addPropertyChangeListener(m_propertyChangeListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_propertyChangeListener == null) {
      throw new IllegalStateException();
    }
    getModel().removePropertyChangeListener(m_propertyChangeListener);
    m_propertyChangeListener = null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    for (JsonProperty<?> jsonProperty : m_jsonProperties.values()) {
      if (jsonProperty.accept()) {
        putProperty(json, jsonProperty.jsonPropertyName(), jsonProperty.valueToJson());
        jsonProperty.setValueSent(true);
      }
    }
    return json;
  }

  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    String propertyName = event.getPropertyName();
    Object oldValue = event.getOldValue();
    Object newValue = event.getNewValue();

    if (m_jsonProperties.containsKey(propertyName)) {
      JsonProperty<?> jsonProperty = m_jsonProperties.get(propertyName);
      handleLazyJsonProperties(jsonProperty);
      jsonProperty.handlePropertyChange(oldValue, newValue);
      if (!jsonProperty.accept()) {
        return;
      }
      // Check if a property-event-filter prevents the property change to be added to the JSON response
      PropertyChangeEvent filteredEvent = filterPropertyChangeEvent(event);
      if (filteredEvent != null) {
        // TODO [7.0] cgu: we should add a logic to prevent sending a non changed state. Example: Property changes from A to B and back to A -> No property change event necessary
        // We could do this by remembering the old value and if an event occurs with the same value as the stored old one -> removePropertyChangeEvent
        // But: This may not be done for every property! If the property event was fired using setPropertyAlwaysFire it must be always. We should probably mark those events, but how?
        // Maybe we should better generate a global event buffer concept -> buffer every event not just table or tree events. This would solve event race conditions too -> we should guarantee that events fired by the model are sent to the gui in the same order
        addPropertyChangeEvent(jsonProperty, oldValue, newValue);
        return;
      }

      // Event is filtered, but we must check if another event has already added a property change event,
      // in that case we must add the event anyway, because the filter may lead to incorrect behavior
      // on slow connections. SmartField Example:
      //
      // 1 UI: displayText='X' (text is selected)
      // 2 User selects text with Ctrl + A
      // 3 User clicks on a row in the proposal-chooser and presses A immediately
      // 4 two events are sent to the server in the same request:
      //    1. Table, rowClick   -> sets displayText to 'B' (via Java model)
      //    2. StringField, acceptProposal -> sets displayText to 'A'
      //
      // Expected: after all events have been processed, displayText in the UI should be A
      // but without this check below, it would be 'B', because the second event sets a filter
      // for 'A', so when displayText is changed to 'A' it is not sent back to the UI
      // which is wrong in that case.
      if (responseAlreadyContainsPropertyChangeEvent(jsonProperty)) {
        addPropertyChangeEvent(jsonProperty, oldValue, newValue);
      }
    }
    else {
      // No JsonProperty is registered for this property. Subclass must deal itself with the
      // property change event and is responsible for adding a suitable propertyChange event
      // to the JSON response (or not).
      handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }

  protected boolean responseAlreadyContainsPropertyChangeEvent(JsonProperty<?> jsonProperty) {
    return getUiSession().currentJsonResponse().containsPropertyChangeEvent(getId(), jsonProperty.getPropertyName());
  }

  /**
   * Returns null when the given event has been filtered by current event-filters, or the event itself when no filtering
   * has been applied.
   */
  protected PropertyChangeEvent filterPropertyChangeEvent(PropertyChangeEvent event) {
    return m_propertyEventFilter.filter(event);
  }

  protected void handleLazyJsonProperties(JsonProperty<?> masterProperty) {
    for (JsonProperty<?> lazyProperty : masterProperty.getLazyProperties()) {
      Object modelValue = lazyProperty.modelValue();
      lazyProperty.handlePropertyChange(null, modelValue);
      // Note: at this point we don'check if the a property-change-event is filtered or not
      if (modelValue != null && lazyProperty.accept() && !lazyProperty.isValueSent()) {
        addPropertyChangeEvent(lazyProperty, null, modelValue);
      }
    }
  }

  protected void addPropertyChangeEvent(JsonProperty<?> jsonProperty, Object oldValue, Object newValue) {
    String propertyName = jsonProperty.jsonPropertyName();
    addPropertyChangeEvent(propertyName, jsonProperty.prepareValueForToJson(newValue));
    jsonProperty.setValueSent(true);
    LOG.debug("Added property change event '{}: {}' for {} with id {}. Model: {}", propertyName, newValue, getObjectType(), getId(), getModel());
  }

  /**
   * This method is called, when a PropertyChangeEvent from the model occurs and no JsonProperty is registered for the
   * given propertyName. Note that you must check if the property-change-event has been filtered by using the
   * {@link #filterPropertyChangeEvent(PropertyChangeEvent)} method before you add an event to the JSON response. The
   * default implementation does nothing.
   */
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
  }

  @Override
  public void cleanUpEventFilters() {
    m_propertyEventFilter.removeAllConditions();
  }

  protected class P_PropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      ModelJobs.assertModelThread();
      handleModelPropertyChange(event);
    }
  }
}

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
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;
import org.json.JSONObject;

public abstract class AbstractJsonPropertyObserver<T extends IPropertyObserver> extends AbstractJsonAdapter<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractJsonPropertyObserver.class);

  private P_PropertyChangeListener m_propertyChangeListener;
  private PropertyEventFilter m_propertyEventFilter;

  /**
   * Key = propertyName.
   */
  private Map<String, JsonProperty> m_jsonProperties;

  public AbstractJsonPropertyObserver(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
    m_jsonProperties = new HashMap<>();
    m_propertyEventFilter = new PropertyEventFilter(getModel());
  }

  /**
   * Adds a property to the list of JSON properties. These properties are automatically managed by the JsonAdapter,
   * which means they're automatically included in the object returned by the <code>toJson()</code> method and also
   * are propagated to the browser-side client when a property change event occurs.
   */
  protected void putJsonProperty(JsonProperty jsonProperty) {
    m_jsonProperties.put(jsonProperty.getPropertyName(), jsonProperty);
  }

  public PropertyEventFilter getPropertyEventFilter() {
    return m_propertyEventFilter;
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_propertyChangeListener == null) {
      m_propertyChangeListener = new P_PropertyChangeListener();
      getModel().addPropertyChangeListener(m_propertyChangeListener);
    }

    //Attach child adapters
    //FIXME maybe move child adapter creation to constructor. AttachModel should only attach the model (listeners)
    for (JsonProperty<?> prop : m_jsonProperties.values()) {
      if (prop instanceof JsonAdapterProperty) {
        ((JsonAdapterProperty) prop).attachAdapters();
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    //FIXME CGU this is actually wrong. attach adapters does not necessarily create a new adapter (It may if there is non for the given model).
    //Dispose however always disposes. If the model is used elsewhere, it will fail because there is no adapter anymore
    //Possible solutions:
    //- Dispose removes just the owning adapter (this) from a set of references. Only if the list is empty dispose the adapter. BUT: Aussuming the property is set to null before disposing the owning adapter, the child adapter never gets disposed.
    //- Don't create an adapter for each model instance. Instead always create an adapter if the owning adapter needs one (getOrCreate -> create). But: May influence traffic and offline behaviour. Needs to be considered very well.
    for (JsonProperty<?> prop : m_jsonProperties.values()) {
      if (prop instanceof JsonAdapterProperty) {
        ((JsonAdapterProperty) prop).disposeAdapters();
      }
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
    for (JsonProperty<?> prop : m_jsonProperties.values()) {
      putProperty(json, prop.getPropertyName(), prop.valueToJson());
    }
    return json;
  }

  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    if (m_jsonProperties.containsKey(propertyName)) {
      JsonProperty jsonProperty = m_jsonProperties.get(propertyName);
      addPropertyChangeEvent(propertyName, jsonProperty.prepareValueForToJson(newValue));
      LOG.debug("Added property change event '" + propertyName + ": " + newValue + "' for " + getObjectType() + " with id " + getId());
    }
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      event = getPropertyEventFilter().filterIgnorableModelEvent(event);
      if (event == null) {
        return;
      }
      handleModelPropertyChange(event.getPropertyName(), event.getNewValue());
    }
  }

}

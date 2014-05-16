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
package org.eclipse.scout.rt.ui.json;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.ui.json.form.fields.JsonProperty;
import org.json.JSONObject;

public abstract class AbstractJsonPropertyObserverRenderer<T extends IPropertyObserver> extends AbstractJsonRenderer<T> {

  private P_PropertyChangeListener m_propertyChangeListener;

  /**
   * Key = propertyName.
   */
  private Map<String, JsonProperty> m_jsonProperties;

  public AbstractJsonPropertyObserverRenderer(T modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
    m_jsonProperties = new HashMap<>();
  }

  /**
   * Adds a property to the list of JSON properties. These properties are automatically managed by the JsonRenderer,
   * which means they're automatically included in the object returned by the <code>toJson()</code> method and also
   * are propagated to the browser-side client when a property change event occurs.
   */
  protected void putJsonProperty(JsonProperty jsonProperty) {
    m_jsonProperties.put(jsonProperty.getPropertyName(), jsonProperty);
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_propertyChangeListener == null) {
      m_propertyChangeListener = new P_PropertyChangeListener();
      getModelObject().addPropertyChangeListener(m_propertyChangeListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_propertyChangeListener != null) {
      getModelObject().removePropertyChangeListener(m_propertyChangeListener);
      m_propertyChangeListener = null;
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    for (JsonProperty<?, ?> prop : m_jsonProperties.values()) {
      putProperty(json, prop.getPropertyName(), prop.getValueAsJson());
    }
    return json;
  }

  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    if (m_jsonProperties.containsKey(propertyName)) {
      JsonProperty jsonProperty = m_jsonProperties.get(propertyName);
      getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), propertyName, jsonProperty.valueToJson(newValue));
    }
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      handleModelPropertyChange(e.getPropertyName(), e.getNewValue());
    }
  }

}

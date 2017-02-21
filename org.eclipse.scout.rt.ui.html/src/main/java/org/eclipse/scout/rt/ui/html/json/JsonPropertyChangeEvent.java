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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

/**
 * {@link JsonEvent} of type {@link JsonEventType#PROPERTY}. Properties are stored in a structured "properties" map
 * (instead of the finished JSON structure in "data").
 */
public class JsonPropertyChangeEvent extends JsonEvent {

  private final Map<String, Object> m_properties = new HashMap<>();

  /**
   * @param target
   *          Event target (usually, an adapter ID)
   */
  public JsonPropertyChangeEvent(String target) {
    super(target, JsonEventType.PROPERTY.getEventType(), null);
  }

  /**
   * @return always <code>null</code>, use {@link #getProperties()} instead
   */
  @Override
  @SuppressWarnings("squid:S1185") // method is overridden because of JavaDoc
  public JSONObject getData() {
    return super.getData();
  }

  /**
   * @return live map of properties (never <code>null</code>)
   */
  public Map<String, Object> getProperties() {
    return m_properties;
  }

  @Override
  public JSONObject toJson() {
    JSONObject properties = new JSONObject();
    for (Entry<String, Object> entry : m_properties.entrySet()) {
      String propertyName = entry.getKey();
      Object newValue = entry.getValue();
      // If value is not an plain JSON value, convert it to a valid JSON value (e.g. when newValue is of type FilteredJsonAdapterIds)
      if (newValue instanceof IJsonObject) {
        newValue = ((IJsonObject) newValue).toJson();
      }
      // Add special NULL object for null values to preserve them in the resulting JSON string
      properties.put(propertyName, (newValue == null ? JSONObject.NULL : newValue));
    }

    JSONObject json = super.toJson();
    json.put("properties", properties);
    return json;
  }

  @Override
  public String toString() {
    return super.toString() + ". Properties: " + m_properties;
  }
}

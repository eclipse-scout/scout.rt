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

import org.json.JSONObject;

/**
 * This class is a Java wrapper around a <code>JSONObject</code> with properties 'type' and 'id'.
 */
public class JsonEvent implements IJsonObject {

  public static final String TARGET = "target";
  public static final String TYPE = "type";

  private final String m_target;
  private final String m_type;
  private final JSONObject m_data;

  /**
   * @param target
   *          Event target (usually, an adapter ID)
   * @param type
   *          See {@link JsonEventType} enum for a list of often used event types.
   * @param data
   *          Event data (or <code>null</code>). <b><font color=red>Please note:</font></b> Do not use the reserved
   *          property names <code>'target'</code> and <code>'type'</code> in this object, as they will be overridden by
   *          the corresponding first two arguments.
   */
  public JsonEvent(String target, String type, JSONObject data) {
    if (target == null) {
      throw new IllegalArgumentException("Argument 'target' must be null");
    }
    if (type == null) {
      throw new IllegalArgumentException("Argument 'type' must be null");
    }
    if (data == null) {
      data = new JSONObject();
    }
    m_target = target;
    m_type = type;
    m_data = data;
  }

  public String getTarget() {
    return m_target;
  }

  public String getType() {
    return m_type;
  }

  public JSONObject getData() {
    return m_data;
  }

  public static JsonEvent fromJson(JSONObject json) {
    if (json == null) {
      throw new IllegalArgumentException("Argument 'json' must not be null");
    }
    String target = JsonObjectUtility.getString(json, TARGET);
    String type = JsonObjectUtility.getString(json, TYPE);
    // data is a copy of the JSON object but without target and type properties
    JSONObject data = JsonObjectUtility.newJSONObject(json.toString());
    data.remove(TARGET);
    data.remove(TYPE);
    return new JsonEvent(target, type, data);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = JsonObjectUtility.newLinkedJSONObject();
    JsonObjectUtility.putProperty(json, TARGET, m_target);
    JsonObjectUtility.putProperty(json, TYPE, m_type);
    JsonObjectUtility.putProperties(json, m_data);
    return json;
  }

  @Override
  public String toString() {
    return "Target: " + m_target + ". Type: " + m_type + ". Data: " + m_data;
  }
}

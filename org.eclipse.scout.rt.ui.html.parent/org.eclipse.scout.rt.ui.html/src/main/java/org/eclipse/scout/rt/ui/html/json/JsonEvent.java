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
public class JsonEvent implements IJsonMapper {

  public static final String TYPE = "type";

  public static final String ID = "id";

  private final JSONObject m_data;

  private final String m_id;

  private final String m_type;

  /**
   * @param type
   *          See {@link JsonEventType} enum for a list of often used event types.
   * @param id
   *          Adapater ID
   * @param data
   *          Nullable data
   */
  public JsonEvent(String id, String type, JSONObject data) {
    if (id == null || type == null) {
      throw new IllegalArgumentException("id and type cannot be null");
    }
    m_id = id;
    m_type = type;
    if (data == null) {
      m_data = new JSONObject();
    }
    else {
      m_data = data;
    }
  }

  public String getType() {
    return m_type;
  }

  public String getId() {
    return m_id;
  }

  public JSONObject getData() {
    return m_data;
  }

  public static JsonEvent fromJson(JSONObject json) {
    String id = JsonObjectUtility.getString(json, ID);
    String type = JsonObjectUtility.getString(json, TYPE);
    return new JsonEvent(id, type, json);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json;
    if (m_data == null) {
      json = new JSONObject();
    }
    else {
      json = JsonObjectUtility.newJSONObject(m_data.toString());
    }
    JsonObjectUtility.putProperty(json, ID, m_id);
    JsonObjectUtility.putProperty(json, TYPE, m_type);
    return json;
  }

}

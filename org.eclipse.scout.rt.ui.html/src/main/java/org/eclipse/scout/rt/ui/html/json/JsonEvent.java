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

import org.json.JSONObject;

/**
 * This class is a Java wrapper around a <code>JSONObject</code> with properties 'type' and 'id'.
 */
public class JsonEvent implements IJsonObject {

  public static final String TARGET = "target";
  public static final String TYPE = "type";

  private final String m_target;
  private final String m_type;
  private final String m_reference;
  private final JSONObject m_data;
  private volatile boolean m_protected;

  /**
   * @param target
   *          Event target (usually, an adapter ID)
   * @param type
   *          See {@link JsonEventType} enum for a list of often used event types.
   * @param reference
   *          Optional reference value, used to clean up events in the JSON response (see
   *          {@link JsonResponse#removeJsonAdapter(String)}. Should be of the same type as the 'target' attribute.
   * @param data
   *          Event data (or <code>null</code>). <b><font color=red>Important:</font></b> Do not use the reserved
   *          property names <code>'target'</code> and <code>'type'</code> in this object, as they will be overridden by
   *          the corresponding first two arguments.
   */
  public JsonEvent(String target, String type, String reference, JSONObject data) {
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
    m_reference = reference;
    m_data = data;
  }

  /**
   * @param target
   *          Event target (usually, an adapter ID)
   * @param type
   *          See {@link JsonEventType} enum for a list of often used event types. Do not construct a {@link JsonEvent}
   *          with type {@link JsonEventType#PROPERTY} directly, create {@link JsonPropertyChangeEvent} instead.
   * @param data
   *          Event data (or <code>null</code>). <b><font color=red>Important:</font></b> Do not use the reserved
   *          property names <code>'target'</code> and <code>'type'</code> in this object, as they will be overridden by
   *          the corresponding first two arguments.
   */
  public JsonEvent(String target, String type, JSONObject data) {
    this(target, type, null, data);
  }

  public String getTarget() {
    return m_target;
  }

  public String getType() {
    return m_type;
  }

  public String getReference() {
    return m_reference;
  }

  public JSONObject getData() {
    return m_data;
  }

  /**
   * @return true if the event is "protected", i.e. it will not be ignored by {@link JsonResponse#toJson()}.
   */
  public boolean isProtected() {
    return m_protected;
  }

  /**
   * Marks the event as "protected", i.e. it will not be ignored by {@link JsonResponse#toJson()}.
   */
  public void protect() {
    m_protected = true;
  }

  public static JsonEvent fromJson(JSONObject json) {
    if (json == null) {
      throw new IllegalArgumentException("Argument 'json' must not be null");
    }
    String target = json.getString(TARGET);
    String type = json.getString(TYPE);
    // data is a copy of the JSON object but without target and type properties
    JSONObject data = new JSONObject(json.toString());
    data.remove(TARGET);
    data.remove(TYPE);
    return new JsonEvent(target, type, data);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put(TARGET, m_target);
    json.put(TYPE, m_type);
    JsonObjectUtility.putProperties(json, m_data);
    return json;
  }

  @Override
  public String toString() {
    return "Target: " + m_target + ". Type: " + m_type + (m_reference == null ? "" : ". Reference: " + m_reference) + ". Data: " + m_data;
  }

  /**
   * Creates a string similar to {@link #toString()} but instead of using the whole data object only the keys of that
   * data object are used to create the string. This makes sure the string does not contain any sensitive data.
   */
  public String toSafeString() {
    String dataKeys = "";
    if (m_data != null) {
      dataKeys = m_data.keySet().toString();
    }
    return "Target: " + m_target + ". Type: " + m_type + (m_reference == null ? "" : ". Reference: " + m_reference) + ". Data-Keys: " + dataKeys;
  }
}

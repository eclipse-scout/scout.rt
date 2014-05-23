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
 * This class is a Java wrapper around a <code>JSONObject</code> with properties 'type_' and 'id'.
 */
public class JsonEvent {

  public static final String TYPE = "type_";
  public static final String ID = "id";

  private final JSONObject m_json;

  public JsonEvent(JSONObject event) {
    m_json = event;
  }

  public String getType() {
    return JsonObjectUtility.getString(m_json, TYPE);
  }

  public String getId() {
    return JsonObjectUtility.getString(m_json, ID);
  }

  public JSONObject getJsonObject() {
    return m_json;
  }
}

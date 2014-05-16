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

import org.json.JSONObject;

public class JsonEvent {

  public static final String TYPE = "type_";
  public static final String ID = "id";

  private final JSONObject m_event;

  public JsonEvent(JSONObject event) {
    m_event = event;
  }

  public String getEventType() {
    return JsonObjectUtility.getString(m_event, TYPE);
  }

  public String getEventId() {
    return JsonObjectUtility.getString(m_event, ID);
  }

  public JSONObject getEventObject() {
    return m_event;
  }
}

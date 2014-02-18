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

import org.json.JSONException;
import org.json.JSONObject;

public class JsonRequest {
  private final JSONObject m_event;

  public JsonRequest(JSONObject event) {
    m_event = event;
  }

  public String getSessionPartId() {
    try {
      return m_event.getString("sessionPartId");
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
  }

  public String getEventType() {
    try {
      return m_event.getString("type");
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
  }

  public String getEventId() {
    try {
      return m_event.getString("id");
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
  }

  public JSONObject getEventData() {
    try {
      if (m_event.has("data")) {
        return m_event.getJSONObject("data");
      }
      return null;
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
  }
}

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

import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.json.JSONObject;

public class JsonStatus implements IJsonObject {

  private final IStatus m_status;

  public JsonStatus(IStatus status) {
    m_status = status;
  }

  public IStatus getStatus() {
    return m_status;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("message", m_status.getMessage());
    json.put("severity", m_status.getSeverity());
    json.put("iconId", m_status.getIconId());
    if (m_status.isMultiStatus()) {
      for (IStatus cs : ((IMultiStatus) m_status).getChildren()) {
        json.append("children", new JsonStatus(cs).toJson());
      }
    }
    return json;
  }

  public static JSONObject toJson(IStatus status) {
    if (status == null) {
      return null;
    }
    return new JsonStatus(status).toJson();
  }

  public static IStatus toScoutObject(JSONObject jsonStatus) {
    if (jsonStatus == null) {
      return null;
    }
    String message = jsonStatus.optString("message");
    int severity = jsonStatus.getInt("severity");
    String iconId = jsonStatus.optString("iconId");
    Status status = new Status(message, severity);
    if (iconId != null) {
      status.withIconId(iconId);
    }
    return status;
  }

}

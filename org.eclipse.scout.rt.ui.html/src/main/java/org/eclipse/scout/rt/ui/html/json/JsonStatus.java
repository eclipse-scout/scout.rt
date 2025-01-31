/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONObject;

public class JsonStatus implements IJsonObject {

  private final IStatus m_status;

  public JsonStatus(IStatus status) {
    m_status = status;
  }

  public String getObjectType() {
    return "Status";
  }

  public IStatus getStatus() {
    return m_status;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("objectType", getObjectType());
    json.put("message", m_status.getMessage());
    json.put("severity", m_status.getSeverity());
    json.put("iconId", BinaryResourceUrlUtility.createIconUrl(m_status.getIconId()));
    json.put("code", m_status.getCode());
    if (m_status.isMultiStatus()) {
      for (IStatus cs : ((IMultiStatus) m_status).getChildren()) {
        json.append("children", MainJsonObjectFactory.get().createJsonObject(cs).toJson());
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

  @SuppressWarnings("ConstantConditions")
  public static IStatus toScoutObject(JSONObject jsonStatus) {
    if (jsonStatus == null) {
      return null;
    }
    String message = jsonStatus.optString("message");
    int severity = jsonStatus.getInt("severity");
    String iconId = jsonStatus.optString("iconId");
    Integer code = jsonStatus.optInt("code");
    Status status = new Status(message, severity);
    if (iconId != null) {
      status.withIconId(iconId);
    }
    if (code != null) {
      status.withCode(code);
    }
    return status;
  }
}

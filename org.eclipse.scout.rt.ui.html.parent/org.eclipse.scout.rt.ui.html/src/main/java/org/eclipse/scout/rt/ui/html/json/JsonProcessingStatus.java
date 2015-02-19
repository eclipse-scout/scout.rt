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

import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.rt.client.IFieldStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ScoutFieldStatus;
import org.json.JSONObject;

/**
 * @author awe
 */
public class JsonProcessingStatus implements IJsonObject {

  private final IStatus m_status;

  public JsonProcessingStatus(IStatus processingStatus) {
    this.m_status = processingStatus;
  }

  public IStatus getProcessingStatus() {
    return m_status;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "message", getMessage());
    JsonObjectUtility.putProperty(json, "iconName", getIconUrl());
    return json;
  }

  private String getIconUrl() {
    if (m_status instanceof IFieldStatus) {
      return ((IFieldStatus) m_status).getIconId();
    }
    else {
      return ScoutFieldStatus.getIconIdFromSeverity(m_status.getSeverity());
    }
  }

  private String getMessage() {
    return m_status.getMessage();
  }

}

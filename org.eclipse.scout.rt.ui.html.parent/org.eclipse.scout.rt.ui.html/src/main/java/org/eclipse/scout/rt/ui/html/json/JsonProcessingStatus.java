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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.json.JSONObject;

/**
 * @author awe
 */
public class JsonProcessingStatus implements IJsonMapper {

  private final IProcessingStatus m_processingStatus;

  public JsonProcessingStatus(IProcessingStatus processingStatus) {
    this.m_processingStatus = processingStatus;
  }

  public IProcessingStatus getProcessingStatus() {
    return m_processingStatus;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "message", getMessage());
    JsonObjectUtility.putProperty(json, "iconName", getIconUrl());
    return json;
  }

  private String getIconUrl() {
    switch (m_processingStatus.getSeverity()) {
      case IProcessingStatus.FATAL:
      case IProcessingStatus.ERROR:
        return AbstractIcons.StatusError;
      case IProcessingStatus.WARNING:
        return AbstractIcons.StatusWarning;
      default:
        return AbstractIcons.StatusInfo;
    }
  }

  private String getMessage() {
    StringBuilder sb = new StringBuilder();
    if (m_processingStatus.getTitle() != null) {
      sb.append(m_processingStatus.getTitle());
    }
    if (m_processingStatus.getMessage() != null) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(m_processingStatus.getMessage());
    }
    return sb.toString();
  }

}

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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author awe
 */
public class JsonProcessingStatus {

  private final IProcessingStatus processingStatus;

  public JsonProcessingStatus(IProcessingStatus processingStatus) {
    this.processingStatus = processingStatus;
  }

  public IProcessingStatus getProcessingStatus() {
    return processingStatus;
  }

  public JSONObject toJson() {
    try {
      JSONObject json = new JSONObject();
      json.put("message", getMessage());
      json.put("iconName", getIconUrl());
      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  private String getIconUrl() {
    switch (processingStatus.getSeverity()) {
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
    if (processingStatus.getTitle() != null) {
      sb.append(processingStatus.getTitle());
    }
    if (processingStatus.getMessage() != null) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(processingStatus.getMessage());
    }
    return sb.toString();
  }

}

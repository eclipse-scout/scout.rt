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

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.json.JSONObject;

public class JsonGridData implements IJsonObject {

  private final GridData m_gridData;

  public JsonGridData(GridData gridData) {
    m_gridData = gridData;
  }

  @Override
  public JSONObject toJson() {
    if (m_gridData == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("fillHorizontal", m_gridData.fillHorizontal);
    json.put("fillVertical", m_gridData.fillVertical);
    json.put("h", m_gridData.h);
    json.put("heightInPixel", m_gridData.heightInPixel);
    json.put("horizontalAlignment", m_gridData.horizontalAlignment);
    json.put("useUiHeight", m_gridData.useUiHeight);
    json.put("useUiWidth", m_gridData.useUiWidth);
    json.put("verticalAlignment", m_gridData.verticalAlignment);
    json.put("w", m_gridData.w);
    json.put("weightX", m_gridData.weightX);
    json.put("weightY", m_gridData.weightY);
    json.put("widthInPixel", m_gridData.widthInPixel);
    json.put("x", m_gridData.x);
    json.put("y", m_gridData.y);
    return json;
  }

  public static JSONObject toJson(GridData gridData) {
    if (gridData == null) {
      return null;
    }
    return new JsonGridData(gridData).toJson();
  }
}

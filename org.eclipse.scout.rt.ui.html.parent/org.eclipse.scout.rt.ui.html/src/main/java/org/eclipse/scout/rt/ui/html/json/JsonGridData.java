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

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.json.JSONObject;

public class JsonGridData implements IJsonMapper {

  private GridData m_gridData;

  public JsonGridData(GridData gridData) {
    m_gridData = gridData;
  }

  // TODO AWE: (layout) wahrscheinlich müssen nicht alle properties gesendet werden, vielleicht nur die,
  // die != default wert sind?
  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "fillHorizontal", m_gridData.fillHorizontal);
    JsonObjectUtility.putProperty(json, "fillVertical", m_gridData.fillVertical);
    JsonObjectUtility.putProperty(json, "h", m_gridData.h);
    JsonObjectUtility.putProperty(json, "heightInPixel", m_gridData.heightInPixel);
    JsonObjectUtility.putProperty(json, "horizontalAlignment", m_gridData.horizontalAlignment);
    JsonObjectUtility.putProperty(json, "useUiHeight", m_gridData.useUiHeight);
    JsonObjectUtility.putProperty(json, "useUiWidth", m_gridData.useUiWidth);
    JsonObjectUtility.putProperty(json, "verticalAlignment", m_gridData.verticalAlignment);
    JsonObjectUtility.putProperty(json, "w", m_gridData.w);
    JsonObjectUtility.putProperty(json, "weightX", m_gridData.weightX);
    JsonObjectUtility.putProperty(json, "weightY", m_gridData.weightY);
    JsonObjectUtility.putProperty(json, "widthInPixel", m_gridData.widthInPixel);
    JsonObjectUtility.putProperty(json, "x", m_gridData.x);
    JsonObjectUtility.putProperty(json, "y", m_gridData.y);
    return json;
  }

  // TODO AWE: (json) klassen wie diese sollten statisch sein, oder evtl. in eine factory verschoben werden?
  // siehe auch JsonProcessingStatus

}

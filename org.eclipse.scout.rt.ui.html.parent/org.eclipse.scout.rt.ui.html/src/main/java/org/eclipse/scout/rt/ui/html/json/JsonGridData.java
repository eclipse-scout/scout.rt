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

import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.putProperty;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.json.JSONObject;

public class JsonGridData implements IJsonMapper {

  private GridData m_gridData;

  public JsonGridData(GridData gridData) {
    m_gridData = gridData;
  }

  // TODO AWE: (layout) wahrscheinlich müssen nicht alle properties gesendet werden, vielleicht nur die,
  // die != default wert sind? Möglicherweise sind auch gar nicht alle relevant fürs Html UI.
  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    putProperty(json, "fillHorizontal", m_gridData.fillHorizontal);
    putProperty(json, "fillVertical", m_gridData.fillVertical);
    putProperty(json, "h", m_gridData.h);
    putProperty(json, "heightInPixel", m_gridData.heightInPixel);
    putProperty(json, "horizontalAlignment", m_gridData.horizontalAlignment);
    putProperty(json, "useUiHeight", m_gridData.useUiHeight);
    putProperty(json, "useUiWidth", m_gridData.useUiWidth);
    putProperty(json, "verticalAlignment", m_gridData.verticalAlignment);
    putProperty(json, "w", m_gridData.w);
    putProperty(json, "weightX", m_gridData.weightX);
    putProperty(json, "weightY", m_gridData.weightY);
    putProperty(json, "widthInPixel", m_gridData.widthInPixel);
    putProperty(json, "x", m_gridData.x);
    putProperty(json, "y", m_gridData.y);
    return json;
  }

  // TODO AWE: (json) klassen wie diese sollten statisch sein, oder evtl. in eine factory verschoben werden?
  // siehe auch JsonProcessingStatus

}

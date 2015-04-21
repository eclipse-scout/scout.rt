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
package org.eclipse.scout.rt.ui.html.json.basic.activitymap;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.ui.html.json.IIdProvider;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonActivityRow<RI, AI> implements IJsonObject {
  private RI m_resourceId;
  private final IIdProvider<ActivityCell<RI, AI>> m_cellIdProvider;

  private List<ActivityCell<RI, AI>> m_cells;

  public JsonActivityRow(RI resourceId, List<ActivityCell<RI, AI>> cells, IIdProvider<ActivityCell<RI, AI>> cellIdProvider) {
    m_resourceId = resourceId;
    m_cells = cells;
    m_cellIdProvider = cellIdProvider;
  }

  @Override
  public Object toJson() {
    JSONObject jsonRow = new JSONObject();
    //FIXME CGU resourceId? probably create unique id
    jsonRow.put("resourceId", m_resourceId);
    jsonRow.put("cells", cellsToJson());
    JsonObjectUtility.filterDefaultValues(jsonRow, "ActivityRow");
    return jsonRow;
  }

  protected JSONArray cellsToJson() {
    JSONArray jsonCells = new JSONArray();
    for (ActivityCell<RI, AI> cell : m_cells) {
      JsonActivityCell<RI, AI> jsonCell = new JsonActivityCell<RI, AI>(cell, m_cellIdProvider);
      jsonCells.put(jsonCell.toJson());
    }
    return jsonCells;
  }
}

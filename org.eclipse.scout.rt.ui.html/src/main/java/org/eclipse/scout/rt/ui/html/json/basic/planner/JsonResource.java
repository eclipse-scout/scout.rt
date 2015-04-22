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
package org.eclipse.scout.rt.ui.html.json.basic.planner;

import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.Resource;
import org.eclipse.scout.rt.ui.html.json.IIdProvider;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonResource implements IJsonObject {
  private Resource m_resource;
  private final IIdProvider<Activity<?, ?>> m_cellIdProvider;

  public JsonResource(Resource resource, IIdProvider<Activity<?, ?>> cellIdProvider) {
    m_resource = resource;
    m_cellIdProvider = cellIdProvider;
  }

  @Override
  public Object toJson() {
    JSONObject jsonRow = new JSONObject();
    //FIXME CGU resourceId? probably create unique id
    jsonRow.put("resourceCell", new JsonCell(m_resource.getCell()).toJson());
    jsonRow.put("cells", cellsToJson());
    JsonObjectUtility.filterDefaultValues(jsonRow, "ActivityRow");
    return jsonRow;
  }

  protected JSONArray cellsToJson() {
    JSONArray jsonCells = new JSONArray();
    for (Activity<?, ?> cell : m_resource.getActivities()) {
      JsonActivityCell jsonCell = new JsonActivityCell(cell, m_cellIdProvider);
      jsonCells.put(jsonCell.toJson());
    }
    return jsonCells;
  }
}

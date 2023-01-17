/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.basic.planner;

import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.ui.html.json.IIdProvider;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonResource implements IJsonObject {
  private final Resource<?> m_resource;
  private final IIdProvider<Resource<?>> m_resourceIdProvider;
  private final IIdProvider<Activity<?, ?>> m_cellIdProvider;
  private final IJsonAdapter<?> m_parentAdapter;

  public JsonResource(Resource<?> resource, IJsonAdapter<?> parentAdapter, IIdProvider<Resource<?>> resourceIdProvider, IIdProvider<Activity<?, ?>> cellIdProvider) {
    m_resource = resource;
    m_parentAdapter = parentAdapter;
    m_resourceIdProvider = resourceIdProvider;
    m_cellIdProvider = cellIdProvider;
  }

  @Override
  public Object toJson() {
    JSONObject jsonRow = new JSONObject();
    jsonRow.put("id", m_resourceIdProvider.getId(m_resource));
    jsonRow.put("resourceCell", new JsonCell(m_resource.getCell(), m_parentAdapter).toJson());
    jsonRow.put("activities", cellsToJson());
    JsonObjectUtility.filterDefaultValues(jsonRow, "Resource");
    return jsonRow;
  }

  protected JSONArray cellsToJson() {
    JSONArray jsonCells = new JSONArray();
    for (Activity<?, ?> cell : m_resource.getActivities()) {
      JsonActivity jsonCell = new JsonActivity(cell, m_cellIdProvider);
      jsonCells.put(jsonCell.toJson());
    }
    return jsonCells;
  }
}

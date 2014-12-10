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
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.desktop.IJsonOutlineAdapter;
import org.json.JSONObject;

public class JsonOutlineTable<T extends ITable> extends JsonTable<T> {

  private final IJsonOutlineAdapter m_jsonOutline;

  public JsonOutlineTable(T model, IJsonSession jsonSession, String id, IJsonOutlineAdapter jsonOutline, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
    m_jsonOutline = jsonOutline;
  }

  @Override
  protected JSONObject tableRowToJson(ITableRow row) {
    JSONObject json = super.tableRowToJson(row);
    putProperty(json, "nodeId", m_jsonOutline.getNodeId(row));
    return json;
  }

}

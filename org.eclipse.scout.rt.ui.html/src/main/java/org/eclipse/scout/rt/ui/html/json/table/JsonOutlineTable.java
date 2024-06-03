/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutline;
import org.json.JSONObject;

public class JsonOutlineTable<T extends ITable> extends JsonTable<T> {
  public static final String PROP_PAGE = "ui:page";

  private final IPage<?> m_page;

  public JsonOutlineTable(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent, IPage<?> page) {
    super(model, uiSession, id, parent);
    m_page = page;
  }

  @Override
  protected JSONObject tableRowToJson(ITableRow row) {
    JSONObject json = super.tableRowToJson(row);
    ITreeNode treeNode = m_page.getTreeNodeFor(row);
    JsonOutline<IOutline> jsonOutline = getGlobalAdapter(m_page.getOutline());
    String nodeId = jsonOutline.getOrCreateNodeId(treeNode);
    putProperty(json, "nodeId", nodeId);
    BEANS.all(IOutlineTableRowToJsonContributor.class).forEach(c -> c.contribute(json, m_page, row));
    return json;
  }
}

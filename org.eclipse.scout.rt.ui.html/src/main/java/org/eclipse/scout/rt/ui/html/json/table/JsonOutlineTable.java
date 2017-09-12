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
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutline;
import org.json.JSONObject;

public class JsonOutlineTable<T extends ITable> extends JsonTable<T> {
  public static final String PROP_PAGE = "ui:page";

  private final IPage m_page;

  public JsonOutlineTable(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent, IPage page) {
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
    return json;
  }

}

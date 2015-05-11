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
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.table.JsonOutlineTable;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONObject;

public class JsonOutline<T extends IOutline> extends JsonTree<T> {

  private static final String PROP_DETAIL_FORM = IOutline.PROP_DETAIL_FORM;
  private static final String PROP_DETAIL_TABLE = IOutline.PROP_DETAIL_TABLE;
  private static final String PROP_DETAIL_FORM_VISIBLE = "detailFormVisible";
  private static final String PROP_DETAIL_TABLE_VISIBLE = "detailTableVisible";

  private Set<IJsonAdapter<?>> m_jsonDetailTables = new HashSet<IJsonAdapter<?>>();

  public JsonOutline(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Outline";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "defaultDetailForm", getModel().getDefaultDetailForm());
    return json;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getDefaultDetailForm());
  }

  @Override
  protected void attachNode(ITreeNode node, boolean attachChildren) {
    if (attachChildren) {
      attachNodes(node.getChildNodes(), attachChildren);
    }
    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected page.");
    }
    IPage<?> page = (IPage) node;
    if (page.isDetailFormVisible()) {
      attachGlobalAdapter(page.getDetailForm());
    }
    if (page.isTableVisible()) {
      attachDetailTable(page);
    }
  }

  @Override
  protected JSONObject treeNodeToJson(ITreeNode node) {
    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    IPage page = (IPage) node;
    JSONObject json = super.treeNodeToJson(node);
    putDetailFormAndTable(json, page);
    return json;
  }

  protected void putDetailFormAndTable(JSONObject json, IPage page) {
    putProperty(json, PROP_DETAIL_FORM_VISIBLE, page.isDetailFormVisible());
    if (page.isDetailFormVisible()) {
      putAdapterIdProperty(json, PROP_DETAIL_FORM, page.getDetailForm());
    }
    putProperty(json, PROP_DETAIL_TABLE_VISIBLE, page.isTableVisible());
    if (page.isTableVisible()) {
      putAdapterIdProperty(json, PROP_DETAIL_TABLE, page.getTable());
    }
  }

  @Override
  protected void disposeNode(ITreeNode node, boolean disposeChildren) {
    super.disposeNode(node, disposeChildren);

    IPage page = (IPage) node;
    ITable table = page.getTable();
    if (table != null) {
      IJsonAdapter<?> jsonTableAdapter = getGlobalAdapter(table);
      if (jsonTableAdapter != null) {
        // Detail tables are global to make them reusable by other components (e.g. table field)
        // Therefore we have to dispose them by our own
        m_jsonDetailTables.remove(jsonTableAdapter);
        if (!jsonTableAdapter.isDisposed()) {
          jsonTableAdapter.dispose();
        }
      }
    }

    // No need to dispose detail form (it will be disposed automatically when it is closed)
  }

  protected IJsonAdapter<?> attachDetailTable(IPage page) {
    page.getTable().setProperty(JsonOutlineTable.PROP_PAGE, page);
    IJsonAdapter<?> detailTableAdapter = attachGlobalAdapter(page.getTable());
    m_jsonDetailTables.add(detailTableAdapter);
    return detailTableAdapter;
  }

  @Override
  protected void handleModelOtherTreeEvent(TreeEvent event) {
    switch (event.getType()) {
      case OutlineEvent.TYPE_PAGE_CHANGED:
        handleModelPageChanged((OutlineEvent) event);
        break;
      default:
        //NOP
    }
  }

  protected void handleModelPageChanged(OutlineEvent event) {
    IPage page = (IPage) event.getNode();
    attachNode(page, false);
    JSONObject jsonEvent = JsonObjectUtility.newOrderedJSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(page));
    putDetailFormAndTable(jsonEvent, page);
    replaceActionEvent("pageChanged", jsonEvent);
  }
}

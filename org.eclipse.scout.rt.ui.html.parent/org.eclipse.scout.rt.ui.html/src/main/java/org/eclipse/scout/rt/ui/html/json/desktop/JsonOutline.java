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

import java.util.Collection;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONObject;

public class JsonOutline extends JsonTree<IOutline> {

  public JsonOutline(IOutline model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "Outline";
  }

  @Override
  protected void attachTreeNode(ITreeNode node) {
    super.attachTreeNode(node);

    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected page.");
    }
    IPage page = (IPage) node;

    optAttachAdapter(page.getDetailForm());
    if (page instanceof IPageWithTable) {
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
      attachAdapter(pageWithTable.getTable());
    }
  }

  @Override
  protected JSONObject treeNodeToJson(ITreeNode node) {
    JSONObject json = super.treeNodeToJson(node);

    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected page.");
    }
    IPage page = (IPage) node;

    optPutAdapterIdProperty(json, "detailForm", page.getDetailForm());

    String pageType = "";
    if (page instanceof IPageWithTable) {
      pageType = "table";
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
      putAdapterIdProperty(json, "table", pageWithTable.getTable());
    }
    else {
      pageType = "node";
      //FIXME send internal table and ignore on gui? or better modify model? -> maybe best to make it configurable on nodepage
//        IPageWithNodes pageWithNodes = (IPageWithNodes) page;
//        ITable table = pageWithNodes.getInternalTable();
//        if (table != null) {
//          JsonDesktopTable jsonTable = m_jsonTables.get(table);
//          if (jsonTable == null) {
//            jsonTable = new JsonDesktopTable(table, getJsonSession());
//            jsonTable.init();
//            m_jsonTables.put(table, jsonTable);
//          }
//          json.put("table", m_jsonTables.get(table).toJson());
//        }
    }
    putProperty(json, "type", pageType);

    return json;
  }

  @Override
  protected void handleModelNodesDeleted(TreeEvent event) {
    super.handleModelNodesDeleted(event);

    Collection<ITreeNode> nodes = event.getNodes();
    for (ITreeNode node : nodes) {
      //FIXME CGU really dispose? Or better keep for offline? Memory issue?
      if (node instanceof IPageWithTable) {
        IPageWithTable<?> pageWithTable = (IPageWithTable<?>) node;
        JsonTable table = (JsonTable) getJsonSession().getJsonAdapter(pageWithTable.getTable());
        if (table != null) {
          table.dispose();
        }
      }
    }
  }

  protected void handleModelDetailFormChanged(IForm detailForm) {
    JSONObject jsonEvent = new JSONObject();
    ITreeNode selectedNode = getModel().getSelectedNode();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(selectedNode));
    // TODO AWE: (json) überprüfen, ob das hier stimmt. Jetzt ist der zeitliche ablauf wohl etwas anders
    // als früher, würde man m_treeNodeIds.get(selectedNode) aufrufen, käme hier "null" zurück.
    // Evtl. muss das in den anderen handleXYZ() methoden auch so gelöst werden?
    if (detailForm == null) {
      putProperty(jsonEvent, "detailForm", null);
    }
    else {
      IJsonAdapter<?> detailFormAdapter = attachAdapter(detailForm);
      putProperty(jsonEvent, "detailForm", detailFormAdapter.getId());
    }
    addActionEvent("detailFormChanged", jsonEvent);
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    if (IOutline.PROP_DETAIL_FORM.equals(propertyName)) {
      handleModelDetailFormChanged((IForm) newValue);
    }
    else {
      super.handleModelPropertyChange(propertyName, newValue);
    }
  }
}

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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
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
  protected void createChildAdapters() {
    super.createChildAdapters();
    if (getModel().isRootNodeVisible()) {
      attachPage(getModel().getRootPage());
    }
    else {
      for (IPage page : getModel().getRootPage().getChildPages()) {
        attachPage(page);
      }
    }
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    if (getModel().isRootNodeVisible()) {
      disposePage(getModel().getRootPage());
    }
    else {
      for (IPage page : getModel().getRootPage().getChildPages()) {
        disposePage(page);
      }
    }
  }

  protected void attachPage(IPage page) {
    for (IPage childPage : page.getChildPages()) {
      attachPage(childPage);
    }

    optAttachAdapter(page.getDetailForm());
    if (page instanceof IPageWithTable) {
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
      attachAdapter(pageWithTable.getTable());
    }
    else if (page instanceof IPageWithNodes) {
      IPageWithNodes pageWithNodes = (IPageWithNodes) page;
      attachAdapter(pageWithNodes.getInternalTable());
    }
  }

  protected void disposePage(IPage page) {
    for (IPage childPage : page.getChildPages()) {
      disposePage(childPage);
    }

    optDisposeAdapter(page.getDetailForm());
    if (page instanceof IPageWithTable) {
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
      disposeAdapter(pageWithTable.getTable());
    }
  }

  @Override
  protected JSONObject treeNodeToJson(ITreeNode node) {
    JSONObject json = super.treeNodeToJson(node);

    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    IPage page = (IPage) node;

    optPutAdapterIdProperty(json, IOutline.PROP_DETAIL_FORM, page.getDetailForm());

    String pageType = "";
    if (page instanceof IPageWithTable) {
      pageType = IOutline.PROP_DETAIL_TABLE;
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
      putAdapterIdProperty(json, IOutline.PROP_DETAIL_TABLE, pageWithTable.getTable());
    }
    else if (page instanceof IPageWithNodes) {
      pageType = "node";
      //FIXME send internal table and ignore on gui? or better modify model? -> maybe best to make it configurable on nodepage
      IPageWithNodes pageWithNodes = (IPageWithNodes) page;
      ITable table = pageWithNodes.getInternalTable();
      putAdapterIdProperty(json, IOutline.PROP_DETAIL_TABLE, table);
    }
    //FIXME CGU virtual node?
    putProperty(json, "type", pageType);

    return json;
  }

  @Override
  protected void handleModelNodesInserted(TreeEvent event) {
    for (ITreeNode node : event.getNodes()) {
      if (!(node instanceof IPage)) {
        throw new IllegalArgumentException("Expected page.");
      }
      IPage page = (IPage) node;
      attachPage(page);
    }

    super.handleModelNodesInserted(event);
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
      putProperty(jsonEvent, IOutline.PROP_DETAIL_FORM, null);
    }
    else {
      IJsonAdapter<?> detailFormAdapter = attachAdapter(detailForm);
      putProperty(jsonEvent, IOutline.PROP_DETAIL_FORM, detailFormAdapter.getId());
    }
    System.out.println(IOutline.PROP_DETAIL_FORM + detailForm);
    addActionEvent("detailFormChanged", jsonEvent);
  }

  protected void handleModelDetailTableChanged(ITable detailTable) {
    JSONObject jsonEvent = new JSONObject();
    ITreeNode selectedNode = getModel().getSelectedNode();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(selectedNode));
    if (detailTable == null) {
      putProperty(jsonEvent, IOutline.PROP_DETAIL_TABLE, null);
    }
    else {
      IJsonAdapter<?> detailFormAdapter = attachAdapter(detailTable);
      putProperty(jsonEvent, IOutline.PROP_DETAIL_TABLE, detailFormAdapter.getId());
    }
    addActionEvent("detailTableChanged", jsonEvent);
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    if (IOutline.PROP_DETAIL_FORM.equals(propertyName)) {
      handleModelDetailFormChanged((IForm) newValue);
    }
    else if (IOutline.PROP_DETAIL_TABLE.equals(propertyName)) {
      handleModelDetailTableChanged((ITable) newValue);
    }
    else {
      super.handleModelPropertyChange(propertyName, newValue);
    }
  }
}

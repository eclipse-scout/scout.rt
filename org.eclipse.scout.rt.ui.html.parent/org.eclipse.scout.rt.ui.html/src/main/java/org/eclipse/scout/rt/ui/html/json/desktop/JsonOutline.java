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
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline5;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage5;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONObject;

public class JsonOutline<T extends IOutline> extends JsonTree<T> {

  private static final String EVENT_PAGE_CHANGED = "pageChanged";

  public JsonOutline(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "Outline";
  }

  @Override
  protected void attachModel() {
    super.attachModel();

    if (getModel() instanceof IOutline5) {
      optAttachAdapter(((IOutline5) getModel()).getDefaultDetailForm());
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();

    if (getModel() instanceof IOutline5) {
      disposeAdapter(((IOutline5) getModel()).getDefaultDetailForm());
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();

    if (getModel() instanceof IOutline5) {
      optPutAdapterIdProperty(json, "defaultDetailForm", ((IOutline5) getModel()).getDefaultDetailForm());
    }
    return json;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
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
    //FIXME CGU What if there is a detailform AND a table? We should give the possibility on the gui to show both. Currently one of them overlaps the other
    if (page.isTableVisible()) {
      if (page instanceof IPageWithTable) {
        IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
        attachAdapter(pageWithTable.getTable());
      }
      else if (page instanceof IPageWithNodes) {
        IPageWithNodes pageWithNodes = (IPageWithNodes) page;
        attachAdapter(pageWithNodes.getInternalTable());
      }
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
    else if (page instanceof IPageWithNodes) {
      IPageWithNodes pageWithNodes = (IPageWithNodes) page;
      disposeAdapter(pageWithNodes.getInternalTable());
    }
  }

  // TODO AWE: (scout) remove these two methods when x5-classes are merged into scout RT
  private boolean getDetailFormVisible(IPage page) {
    if (page instanceof IPage5) {
      return ((IPage5) page).isDetailFormVisible();
    }
    else {
      return true;
    }
  }

  private void setDetailFormVisible(IPage page, boolean visible) {
    if (page instanceof IPage5) {
      ((IPage5) page).setDetailFormVisible(visible);
    }
  }

  @Override
  protected JSONObject treeNodeToJson(ITreeNode node) {
    JSONObject json = super.treeNodeToJson(node);

    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    IPage page = (IPage) node;

    // TODO AWE: (menu-navi) discuss with C.GU - müsste man hier nicht IPage.PROP_DETAIL_FORM verwenden?
    // müsste die Methode nicht pageToJson heissen?
    optPutAdapterIdProperty(json, IOutline.PROP_DETAIL_FORM, page.getDetailForm());
    putProperty(json, IPage5.PROP_DETAIL_FORM_VISIBLE, getDetailFormVisible(page));

    String pageType = "";
    if (page instanceof IPageWithTable) {
      pageType = "table";
      if (page.isTableVisible()) {
        IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
        putAdapterIdProperty(json, IOutline.PROP_DETAIL_TABLE, pageWithTable.getTable());
      }
    }
    else if (page instanceof IPageWithNodes) {
      pageType = "node";
      if (page.isTableVisible()) {
        IPageWithNodes pageWithNodes = (IPageWithNodes) page;
        ITable table = pageWithNodes.getInternalTable();
        putAdapterIdProperty(json, IOutline.PROP_DETAIL_TABLE, table);
      }
    }
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
    if (detailForm == null) {
      putProperty(jsonEvent, IOutline.PROP_DETAIL_FORM, null);
    }
    else {
      IJsonAdapter<?> detailFormAdapter = attachAdapter(detailForm);
      putProperty(jsonEvent, IOutline.PROP_DETAIL_FORM, detailFormAdapter.getId());
    }
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
  protected void handleOtherTreeEvent(TreeEvent event) {
    switch (event.getType()) {
      case OutlineEvent.TYPE_PAGE_CHANGED:
        handleModelPageChanged(event);
        break;
      default:
        //NOP
    }
  }

  private void handleModelPageChanged(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    IPage page = (IPage) event.getNode();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(page));
    putProperty(jsonEvent, IPage5.PROP_DETAIL_FORM_VISIBLE, getDetailFormVisible(page));
    addActionEvent("pageChanged", jsonEvent);
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

  private void handleUiPageChanged(JsonEvent event) {
    JSONObject data = event.getData();
    IPage page = (IPage) getTreeNodeForNodeId(JsonObjectUtility.getString(data, PROP_NODE_ID));
    boolean detailFormVisible = JsonObjectUtility.getBoolean(data, IPage5.PROP_DETAIL_FORM_VISIBLE);
    setDetailFormVisible(page, detailFormVisible);
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_PAGE_CHANGED.equals(event.getType())) {
      handleUiPageChanged(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

}

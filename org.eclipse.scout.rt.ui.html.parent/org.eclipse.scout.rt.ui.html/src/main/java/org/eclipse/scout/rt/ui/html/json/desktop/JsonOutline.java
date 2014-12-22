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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline5;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapterFactory;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.table.JsonOutlineTable;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONObject;

public class JsonOutline<T extends IOutline> extends JsonTree<T> {

  private static final String EVENT_PAGE_CHANGED = "pageChanged";
  private static final String PROP_DETAIL_FORM = IOutline.PROP_DETAIL_FORM;
  private static final String PROP_DETAIL_TABLE = "detailTable";
  private static final String PROP_DETAIL_FORM_VISIBLE = "detailFormVisible";
  private Set<IJsonAdapter<?>> m_jsonDetailTables = new HashSet<IJsonAdapter<?>>();

  public JsonOutline(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Outline";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    if (getModel() instanceof IOutline5) {
      putAdapterIdProperty(json, "defaultDetailForm", ((IOutline5) getModel()).getDefaultDetailForm());
    }
    return json;
  }

  // The Outline does not show any menus because the detail form or table does -> don't send them
  @Override
  protected void putContextMenu(JSONObject json) {
    // nop
  }

  @Override
  protected void attachContextMenu() {
    // nop
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    // nop
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    if (getModel() instanceof IOutline5) {
      attachAdapter(((IOutline5) getModel()).getDefaultDetailForm());
    }
    if (getModel().isRootNodeVisible()) {
      attachPage(getModel().getRootPage());
    }
    else {
      for (IPage<?> page : getModel().getRootPage().getChildPages()) {
        attachPage(page);
      }
    }
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    // Detail tables are global to make them reusable by other components (e.g. table field)
    // Therefore we have to dispose them by our own (although this is not very likely because outlines don't die before a session disposal)
    for (IJsonAdapter<?> childAdapter : m_jsonDetailTables) {
      childAdapter.dispose();
    }
  }

  protected void attachPage(IPage<?> page) {
    for (IPage<?> childPage : page.getChildPages()) {
      attachPage(childPage);
    }
    if (page.isDetailFormVisible()) {
      attachGlobalAdapter(page.getDetailForm());
    }
    if (page.isTableVisible()) {
      // Create 'outline' variant for table
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
    putAdapterIdProperty(json, PROP_DETAIL_FORM, page.getDetailForm());
    putProperty(json, PROP_DETAIL_FORM_VISIBLE, page.isDetailFormVisible());
    if (page.isTableVisible()) {
      putAdapterIdProperty(json, PROP_DETAIL_TABLE, page.getTable());
    }
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
      IPage page = (IPage) node;
      ITable table = page.getTable();
      if (table != null) {
        IJsonAdapter<?> jsonAdapter = getGlobalAdapter(table);
        if (jsonAdapter != null) {
          m_jsonDetailTables.remove(jsonAdapter);
          jsonAdapter.dispose();
        }
      }
    }
  }

  protected void handleModelDetailFormChanged(IForm detailForm) {
    JSONObject jsonEvent = new JSONObject();
    ITreeNode selectedNode = getModel().getSelectedNode();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(selectedNode));
    if (detailForm == null) {
      putProperty(jsonEvent, PROP_DETAIL_FORM, null);
    }
    else {
      IJsonAdapter<?> detailFormAdapter = attachGlobalAdapter(detailForm);
      putProperty(jsonEvent, PROP_DETAIL_FORM, detailFormAdapter.getId());
    }
    addActionEvent("detailFormChanged", jsonEvent);
  }

  protected void handleModelDetailTableChanged(ITable detailTable) {
    JSONObject jsonEvent = new JSONObject();
    ITreeNode selectedNode = getModel().getSelectedNode();
    IPage page = (IPage) selectedNode;
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(selectedNode));
    if (detailTable == null) {
      putProperty(jsonEvent, PROP_DETAIL_TABLE, null);
    }
    else {
      IJsonAdapter<?> detailTableAdapter = attachDetailTable(page);
      putProperty(jsonEvent, PROP_DETAIL_TABLE, detailTableAdapter.getId());
    }
    addActionEvent("detailTableChanged", jsonEvent);
  }

  protected IJsonAdapter<?> attachDetailTable(IPage page) {
    IJsonAdapter<?> detailTableAdapter = attachGlobalAdapter(page.getTable(), new P_JsonOutlineTableFactory(page));
    m_jsonDetailTables.add(detailTableAdapter);
    return detailTableAdapter;
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
    IPage page = (IPage) event.getNode();
    if (page.isDetailFormVisible()) {
      attachGlobalAdapter(page.getDetailForm());
    }
    if (page.isTableVisible()) {
      attachDetailTable(page);
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(page));
    putProperty(jsonEvent, PROP_DETAIL_FORM_VISIBLE, page.isDetailFormVisible());
    addActionEvent("pageChanged", jsonEvent);
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (PROP_DETAIL_FORM.equals(propertyName)) {
      handleModelDetailFormChanged((IForm) newValue);
    }
    else if (PROP_DETAIL_TABLE.equals(propertyName)) {
      handleModelDetailTableChanged((ITable) newValue);
    }
    else {
      super.handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }

  private void handleUiPageChanged(JsonEvent event) {
    JSONObject data = event.getData();
    IPage page = (IPage) getTreeNodeForNodeId(JsonObjectUtility.getString(data, PROP_NODE_ID));
    boolean detailFormVisible = JsonObjectUtility.getBoolean(data, PROP_DETAIL_FORM_VISIBLE);
    page.setDetailFormVisible(detailFormVisible);
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

  private class P_JsonOutlineAdapter implements IJsonOutlineAdapter {

    private final IPage m_page;

    private P_JsonOutlineAdapter(IPage page) {
      m_page = page;
    }

    @Override
    public String getNodeId(ITableRow tableRow) {
      ITreeNode treeNode = m_page.getTreeNodeFor(tableRow);
      return JsonOutline.this.getOrCreateNodeId(treeNode);
    }

  }

  private class P_JsonOutlineTableFactory implements IJsonAdapterFactory {

    private final IPage m_page;

    private P_JsonOutlineTableFactory(IPage page) {
      m_page = page;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IJsonAdapter<?> createJsonAdapter(Object model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
      return new JsonOutlineTable((ITable) model, jsonSession, id, new P_JsonOutlineAdapter(m_page), parent);
    }

  }

}

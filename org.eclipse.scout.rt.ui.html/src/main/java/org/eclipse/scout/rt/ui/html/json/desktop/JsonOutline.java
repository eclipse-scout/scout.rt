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
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.IVirtualTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeUtility;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.InspectorInfo;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.table.JsonOutlineTable;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONObject;

public class JsonOutline<OUTLINE extends IOutline> extends JsonTree<OUTLINE> {

  private static final String PROP_DETAIL_FORM = IOutline.PROP_DETAIL_FORM;
  private static final String PROP_DETAIL_TABLE = IOutline.PROP_DETAIL_TABLE;
  private static final String PROP_DETAIL_FORM_VISIBLE = "detailFormVisible";
  private static final String PROP_DETAIL_TABLE_VISIBLE = "detailTableVisible";

  private Set<IJsonAdapter<?>> m_jsonDetailTables = new HashSet<IJsonAdapter<?>>();
  private final IDesktop m_desktop;

  public JsonOutline(OUTLINE outline, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(outline, uiSession, id, parent);
    m_desktop = uiSession.getClientSession().getDesktop();
  }

  @Override
  public String getObjectType() {
    return "Outline";
  }

  @Override
  protected void initJsonProperties(OUTLINE model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<OUTLINE>(IOutline.PROP_BREADCRUMB_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isBreadcrumbEnabled();
      }
    });
    putJsonProperty(new JsonProperty<OUTLINE>(IOutline.PROP_NAVIGATE_BUTTONS_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isNavigateButtonsVisible();
      }
    });
    putJsonProperty(new JsonAdapterProperty<OUTLINE>(IOutline.PROP_DEFAULT_DETAIL_FORM, model, getUiSession()) {
      @Override
      protected IForm modelValue() {
        return getModel().getDefaultDetailForm();
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().disposeOnChange(false).build();
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdsProperty(json, "views", m_desktop.getViews(getModel()));
    putAdapterIdsProperty(json, "dialogs", m_desktop.getDialogs(getModel(), false));
    putAdapterIdsProperty(json, "messageBoxes", m_desktop.getMessageBoxes(getModel()));
    putAdapterIdsProperty(json, "fileChoosers", m_desktop.getFileChoosers(getModel()));
    return json;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getDefaultDetailForm());

    attachGlobalAdapters(m_desktop.getViews(getModel()));
    attachGlobalAdapters(m_desktop.getDialogs(getModel(), false));
    attachGlobalAdapters(m_desktop.getMessageBoxes(getModel()));
    attachGlobalAdapters(m_desktop.getFileChoosers(getModel()));
  }

  @Override
  protected void attachNode(ITreeNode node, boolean attachChildren) {
    // Don't attach virtual nodes if they are already resolved
    node = TreeUtility.unwrapResolvedNode(node);

    if (attachChildren) {
      attachNodes(node.getChildNodes(), attachChildren);
    }
    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    IPage<?> page = (IPage) node;
    if (hasDetailForm(page)) {
      attachGlobalAdapter(page.getDetailForm());
    }
    if (page.isTableVisible()) {
      attachDetailTable(page);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    super.handleUiEvent(event);
    if ("breadcrumbEnabled".equals(event.getType())) {
      handleUiBreadcrumbEnabled(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiBreadcrumbEnabled(JsonEvent event) {
    getModel().setBreadcrumbEnabled(event.getData().getBoolean("breadcrumbEnabled"));
  }

  @Override
  protected void handleModelTreeEvent(TreeEvent event) {
    super.handleModelTreeEvent(event);

    // When nodes are deleted, immediately detach the detail table from the deleted nodes. If we would do
    // this later when the event buffer is processed, there could have been other events in the meantime
    // (e.g. table events) which fail when the nodes (and everything that is attached to them, namely detail
    // tables) are still existing. (Disposing the detail table right away is correct, because no matter what
    // the event buffer does, the nodes are definitively deleted.)
    if (CompareUtility.isOneOf(event.getType(), TreeEvent.TYPE_NODES_DELETED, TreeEvent.TYPE_ALL_CHILD_NODES_DELETED)) {
      detachDetailTables(event.getNodes(), true);
    }
  }

  @Override
  protected JSONObject treeNodeToJson(ITreeNode node) {
    // Virtual and resolved nodes are equal in maps, but they don't behave the same. For example, a
    // a virtual page does not return a detail table, while the resolved node does. Therefore, we
    // want to always use the resolved node, if it exists.
    node = TreeUtility.unwrapResolvedNode(node);

    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    IPage page = (IPage) node;
    JSONObject json = super.treeNodeToJson(node);
    putDetailFormAndTable(json, page);
    putNodeType(json, node);
    BEANS.get(InspectorInfo.class).put(getUiSession(), json, page);
    return json;
  }

  protected void putNodeType(JSONObject json, ITreeNode node) {
    String nodeType = null;
    if (node instanceof IPageWithNodes) {
      nodeType = "nodes";
    }
    else if (node instanceof IPageWithTable) {
      nodeType = "table";
    }
    else if (node instanceof IVirtualTreeNode) {
      nodeType = "virtual";
    }
    if (nodeType != null) {
      putProperty(json, "nodeType", nodeType);
    }
  }

  protected void putDetailFormAndTable(JSONObject json, IPage page) {
    putProperty(json, PROP_DETAIL_FORM_VISIBLE, page.isDetailFormVisible());
    if (page.isDetailFormVisible() && hasDetailForm(page)) {
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
    detachDetailTable(node, false);
    // No need to dispose detail form (it will be disposed automatically when it is closed)
  }

  protected IJsonAdapter<?> attachDetailTable(IPage page) {
    page.getTable().setProperty(JsonOutlineTable.PROP_PAGE, page);
    IJsonAdapter<?> detailTableAdapter = attachGlobalAdapter(page.getTable());
    m_jsonDetailTables.add(detailTableAdapter);
    return detailTableAdapter;
  }

  protected void detachDetailTable(IPage page) {
    ITable table = page.getTable();
    if (table != null) {
      table.setProperty(JsonOutlineTable.PROP_PAGE, null);
      IJsonAdapter<?> jsonTableAdapter = getGlobalAdapter(table);
      if (jsonTableAdapter != null) {
        m_jsonDetailTables.remove(jsonTableAdapter);
        if (!jsonTableAdapter.isDisposed()) {
          jsonTableAdapter.dispose();
        }
      }
    }
  }

  protected void detachDetailTable(ITreeNode node, boolean disposeChildren) {
    if (disposeChildren) {
      detachDetailTables(node.getChildNodes(), disposeChildren);
    }

    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    detachDetailTable((IPage) node);
  }

  protected void detachDetailTables(Collection<ITreeNode> nodes, boolean disposeChildren) {
    for (ITreeNode node : nodes) {
      detachDetailTable(node, disposeChildren);
    }
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

    if (page.isStatusDeleted() || !page.isFilterAccepted()) { // Ignore deleted or filtered nodes, because for the UI, they don't exist
      return;
    }
    String nodeId = optNodeId(page);
    if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(page));
    putDetailFormAndTable(jsonEvent, page);
    addActionEvent("pageChanged", jsonEvent);
  }

  @Override
  protected void putUpdatedPropertiesForResolvedNode(JSONObject jsonNode, String nodeId, ITreeNode node, IVirtualTreeNode virtualNode) {
    super.putUpdatedPropertiesForResolvedNode(jsonNode, nodeId, node, virtualNode);
    putNodeType(jsonNode, node);
    BEANS.get(InspectorInfo.class).put(getUiSession(), jsonNode, node);
  }

  /**
   * @return <code>true</code> if the page has a detail form that is not closed, <code>false</code> otherwise (closed
   *         forms should not be attached, because the close event causes the JSON adapter to be disposed)
   */
  protected boolean hasDetailForm(IPage page) {
    return (page.getDetailForm() != null && !page.getDetailForm().isFormClosed());
  }
}

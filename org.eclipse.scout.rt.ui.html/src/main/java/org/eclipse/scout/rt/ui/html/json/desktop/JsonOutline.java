/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.js.IJsPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.InspectorInfo;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.table.JsonOutlineTable;
import org.eclipse.scout.rt.ui.html.json.tree.IChildNodeIndexLookup;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONObject;

public class JsonOutline<OUTLINE extends IOutline> extends JsonTree<OUTLINE> {

  private static final String PROP_DETAIL_FORM = IOutline.PROP_DETAIL_FORM;
  private static final String PROP_DETAIL_TABLE = IOutline.PROP_DETAIL_TABLE;
  private static final String PROP_DETAIL_FORM_VISIBLE = "detailFormVisible";
  private static final String PROP_NAVIGATE_BUTTONS_VISIBLE = "navigateButtonsVisible";
  private static final String PROP_DETAIL_TABLE_VISIBLE = "detailTableVisible";
  private static final String PROP_OVERVIEW_ICON_ID = "overviewIconId";
  private static final String PROP_SHOW_TILE_OVERVIEW = "showTileOverview";
  private static final String PROP_COMPACT_ROOT = "compactRoot";

  private final IDesktop m_desktop;
  private final LazyValue<JsonDataObjectHelper> m_jsonDoHelper = new LazyValue<>(() -> BEANS.get(JsonDataObjectHelper.class)); // cached instance

  public JsonOutline(OUTLINE outline, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(outline, uiSession, id, parent);
    m_desktop = uiSession.getClientSession().getDesktop();
  }

  @Override
  public String getObjectType() {
    return "Outline";
  }

  protected JsonDataObjectHelper jsonDoHelper() {
    return m_jsonDoHelper.get();
  }

  @Override
  protected void initJsonProperties(OUTLINE model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<>(IOutline.PROP_NAVIGATE_BUTTONS_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isNavigateButtonsVisible();
      }
    });
    putJsonProperty(new JsonAdapterProperty<>(IOutline.PROP_DEFAULT_DETAIL_FORM, model, getUiSession()) {
      @Override
      protected IForm modelValue() {
        return getModel().getDefaultDetailForm();
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return JsonAdapterPropertyConfigBuilder.globalConfig();
      }
    });
    putJsonProperty(new JsonProperty<>(IOutline.PROP_OUTLINE_OVERVIEW_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isOutlineOverviewVisible();
      }
    });

  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdsProperty(json, "views", m_desktop.getViews(getModel()));
    if (!m_desktop.getSelectedViews(getModel()).isEmpty()) {
      putAdapterIdsProperty(json, "selectedViewTabs", m_desktop.getSelectedViews(getModel()));
    }
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
    attachGlobalAdapters(m_desktop.getSelectedViews(getModel()));
    attachGlobalAdapters(m_desktop.getDialogs(getModel(), false));
    attachGlobalAdapters(m_desktop.getMessageBoxes(getModel()));
    attachGlobalAdapters(m_desktop.getFileChoosers(getModel()));
  }

  @Override
  protected JsonContextMenu<IContextMenu> createJsonContextMenu() {
    return new JsonContextMenu<>(getModel().getContextMenu(), this, new OutlineMenuFilter<>());
  }

  @Override
  protected void attachNode(ITreeNode node, boolean attachChildren) {
    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    super.attachNode(node, attachChildren);
    IPage<?> page = (IPage<?>) node;
    if (hasDetailForm(page)) {
      attachGlobalAdapter(page.getDetailForm());
    }
    if (page.isTableVisible()) {
      attachDetailTable(page);
    }
  }

  @Override
  protected void handleModelTreeEvent(TreeEvent event) {
    if (!acceptModelTreeEvent(event)) {
      return;
    }

    super.handleModelTreeEvent(event);

    // When nodes are deleted, immediately detach the detail table from the deleted nodes. If we would do
    // this later when the event buffer is processed, there could have been other events in the meantime
    // (e.g. table events) which fail when the nodes (and everything that is attached to them, namely detail
    // tables) are still existing. (Disposing the detail table right away is correct, because no matter what
    // the event buffer does, the nodes are definitively deleted.)
    if (ObjectUtility.isOneOf(event.getType(), TreeEvent.TYPE_NODES_DELETED, TreeEvent.TYPE_ALL_CHILD_NODES_DELETED)) {
      detachDetailTables(event.getNodes(), true);
    }
  }

  @SuppressWarnings("RedundantIfStatement")
  protected boolean acceptModelTreeEvent(TreeEvent event) {
    // Don't fill the event buffer with events that are currently not relevant for the UI
    if (event instanceof OutlineEvent && ObjectUtility.isOneOf(event.getType(),
        OutlineEvent.TYPE_PAGE_BEFORE_DATA_LOADED,
        OutlineEvent.TYPE_PAGE_AFTER_DATA_LOADED,
        OutlineEvent.TYPE_PAGE_AFTER_TABLE_INIT,
        OutlineEvent.TYPE_PAGE_AFTER_PAGE_INIT,
        OutlineEvent.TYPE_PAGE_AFTER_SEARCH_FORM_START,
        OutlineEvent.TYPE_PAGE_AFTER_DISPOSE,
        OutlineEvent.TYPE_PAGE_ACTIVATED)) {
      return false;
    }
    return true;
  }

  @Override
  protected void putCellProperties(JSONObject json, ITreeNode node) {
    if (node instanceof IJsPage) {
      // Send text because it might come from a summary column from a parent PageWithTable
      json.put("text", node.getCell().getText());
    }
    else {
      super.putCellProperties(json, node);
    }
  }

  @Override
  protected JSONObject treeNodeToJson(ITreeNode node, IChildNodeIndexLookup childIndexes, Set<ITreeNode> acceptedNodes) {
    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    IPage<?> page = (IPage<?>) node;
    JSONObject json = super.treeNodeToJson(node, childIndexes, acceptedNodes);
    putNodeType(json, node);
    if (node instanceof IJsPage) {
      putJsPageObjectTypeAndModel(json, (IJsPage) node);
    }
    else {
      putDetailFormAndTable(json, page);
      putProperty(json, PROP_NAVIGATE_BUTTONS_VISIBLE, page.isNavigateButtonsVisible());
      json.put(PROP_OVERVIEW_ICON_ID, page.getOverviewIconId());
      json.put(PROP_SHOW_TILE_OVERVIEW, page.isShowTileOverview());
      json.put(PROP_COMPACT_ROOT, page.isCompactRoot());
    }
    BEANS.get(InspectorInfo.class).put(getUiSession().currentHttpRequest(), json, page, p -> INSPECTOR_ID_PROVIDER.get().getIdForPage(p));
    BEANS.all(IPageToJsonContributor.class).forEach(c -> c.contribute(json, page));
    JsonObjectUtility.filterDefaultValues(json, "Page");
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
    else if (node instanceof IJsPage) {
      nodeType = "jsPage";
    }
    if (nodeType != null) {
      putProperty(json, "nodeType", nodeType);
    }
  }

  protected void putDetailFormAndTable(JSONObject json, IPage<?> page) {
    putProperty(json, PROP_DETAIL_FORM_VISIBLE, page.isDetailFormVisible());
    if (page.isDetailFormVisible() && hasDetailForm(page)) {
      putAdapterIdProperty(json, PROP_DETAIL_FORM, page.getDetailForm());
    }
    putProperty(json, PROP_DETAIL_TABLE_VISIBLE, page.isTableVisible());
    if (page.isTableVisible()) {
      ITable table = page.getTable(false);
      if (table != null) {
        putAdapterIdProperty(json, PROP_DETAIL_TABLE, table);
      }
    }
  }

  protected void putJsPageObjectTypeAndModel(JSONObject json, IJsPage jsPage) {
    putProperty(json, IJsPage.PROP_JS_PAGE_OBJECT_TYPE, jsPage.getJsPageObjectType());
    putProperty(json, IJsPage.PROP_JS_PAGE_MODEL, jsonDoHelper().dataObjectToJson(jsPage.getJsPageModel()));
  }

  @Override
  protected void disposeNode(ITreeNode node, boolean disposeChildren, Set<ITreeNode> disposedNodes) {
    detachDetailTable(node, false);
    super.disposeNode(node, disposeChildren, disposedNodes);
    // No need to dispose detail form (it will be disposed automatically when it is closed)
  }

  protected void attachDetailTable(IPage<?> page) {
    ITable table = page.getTable(false);
    if (table == null) {
      return;
    }
    table.setProperty(JsonOutlineTable.PROP_PAGE, page);
    attachGlobalAdapter(table);
  }

  protected void detachDetailTable(IPage<?> page) {
    ITable table = page.getTable(false);
    if (table != null) {
      table.setProperty(JsonOutlineTable.PROP_PAGE, null);
      IJsonAdapter<?> jsonTableAdapter = getGlobalAdapter(table);
      if (jsonTableAdapter != null && !jsonTableAdapter.isDisposed()) {
        jsonTableAdapter.dispose();
      }
    }
  }

  protected void detachDetailTable(ITreeNode node, boolean disposeChildren) {
    if (disposeChildren) {
      detachDetailTables(getChildNodes(node), true);
    }

    if (!(node instanceof IPage)) {
      throw new IllegalArgumentException("Expected node to be a page. " + node);
    }
    detachDetailTable((IPage<?>) node);
  }

  protected void detachDetailTables(Collection<ITreeNode> nodes, boolean disposeChildren) {
    for (ITreeNode node : nodes) {
      detachDetailTable(node, disposeChildren);
    }
  }

  @SuppressWarnings("SwitchStatementWithTooFewBranches")
  @Override
  protected void handleModelOtherTreeEvent(TreeEvent event) {
    switch (event.getType()) {
      case OutlineEvent.TYPE_PAGE_CHANGED:
        handleModelPageChanged((OutlineEvent) event);
        break;
      // Note: Check acceptModelTreeEvent() before adding new cases here
      default:
        //NOP
    }
  }

  protected void handleModelPageChanged(OutlineEvent event) {
    IPage<?> page = (IPage<?>) event.getNode();

    if (!isNodeAccepted(page)) {
      return;
    }

    attachNode(page, false);
    String nodeId = optNodeId(page);
    if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(page));
    putDetailFormAndTable(jsonEvent, page);
    putProperty(jsonEvent, PROP_NAVIGATE_BUTTONS_VISIBLE, page.isNavigateButtonsVisible());
    jsonEvent.put(PROP_OVERVIEW_ICON_ID, page.getOverviewIconId());
    addActionEvent("pageChanged", jsonEvent);
  }

  /**
   * @return <code>true</code> if the page has a detail form that is not closed, <code>false</code> otherwise (closed
   *         forms should not be attached, because the close event causes the JSON adapter to be disposed)
   */
  protected boolean hasDetailForm(IPage<?> page) {
    return (page.getDetailForm() != null && !page.getDetailForm().isFormClosed());
  }
}

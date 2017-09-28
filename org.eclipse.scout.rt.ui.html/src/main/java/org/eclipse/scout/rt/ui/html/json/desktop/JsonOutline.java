/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.InspectorInfo;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.table.JsonOutlineTable;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.json.JSONObject;

public class JsonOutline<OUTLINE extends IOutline> extends JsonTree<OUTLINE> {

  private static final String PROP_DETAIL_FORM = IOutline.PROP_DETAIL_FORM;
  private static final String PROP_DETAIL_TABLE = IOutline.PROP_DETAIL_TABLE;
  private static final String PROP_DETAIL_FORM_VISIBLE = "detailFormVisible";
  private static final String PROP_DETAIL_TABLE_VISIBLE = "detailTableVisible";

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
        return JsonAdapterPropertyConfigBuilder.globalConfig();
      }
    });
    putJsonProperty(new JsonProperty<OUTLINE>(IOutline.PROP_OUTLINE_OVERVIEW_VISIBLE, model) {
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
  protected void attachNodeInternal(ITreeNode node) {
    super.attachNodeInternal(node);

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
  protected JSONObject treeNodeToJson(ITreeNode node) {
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
      ITable table = page.getTable(false);
      if (table != null) {
        putAdapterIdProperty(json, PROP_DETAIL_TABLE, table);
      }
    }
  }

  @Override
  protected void disposeNode(ITreeNode node, boolean disposeChildren) {
    super.disposeNode(node, disposeChildren);
    detachDetailTable(node, false);
    // No need to dispose detail form (it will be disposed automatically when it is closed)
  }

  protected void attachDetailTable(IPage page) {
    ITable table = page.getTable(false);
    if (table == null) {
      return;
    }
    table.setProperty(JsonOutlineTable.PROP_PAGE, page);
    attachGlobalAdapter(table);
  }

  protected void detachDetailTable(IPage page) {
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
      // Note: Check acceptModelTreeEvent() before adding new cases here
      default:
        //NOP
    }
  }

  protected void handleModelPageChanged(OutlineEvent event) {
    IPage page = (IPage) event.getNode();

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
    addActionEvent("pageChanged", jsonEvent);
  }

  /**
   * @return <code>true</code> if the page has a detail form that is not closed, <code>false</code> otherwise (closed
   *         forms should not be attached, because the close event causes the JSON adapter to be disposed)
   */
  protected boolean hasDetailForm(IPage page) {
    return (page.getDetailForm() != null && !page.getDetailForm().isFormClosed());
  }
}

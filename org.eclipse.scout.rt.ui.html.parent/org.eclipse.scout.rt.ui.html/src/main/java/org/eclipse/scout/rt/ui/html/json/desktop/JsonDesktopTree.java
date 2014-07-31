package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.menu.IContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.tree.TreeEventFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonDesktopTree extends AbstractJsonPropertyObserver<IOutline> implements IContextMenuOwner {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktopTree.class);
  public static final String EVENT_NODES_SELECTED = "nodesSelected";
  public static final String EVENT_NODE_EXPANDED = "nodeExpanded";
  public static final String EVENT_NODES_DELETED = "nodesDeleted";
  public static final String EVENT_ALL_NODES_DELETED = "allNodesDeleted";
  public static final String PROP_NODE_ID = "nodeId";
  public static final String PROP_NODE_IDS = "nodeIds";
  public static final String PROP_COMMON_PARENT_NODE_ID = "commonParentNodeId";
  public static final String PROP_MENUS = "menus";
  public static final String PROP_NODES = "nodes";
  public static final String PROP_SELECTED_NODE_IDS = "selectedNodeIds";

  private P_ModelTreeListener m_modelTreeListener;
  private Map<String, ITreeNode> m_treeNodes;
  private Map<ITreeNode, String> m_treeNodeIds;
  private TreeEventFilter m_treeEventFilter;

  public JsonDesktopTree(IOutline model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
    m_treeNodes = new HashMap<>();
    m_treeNodeIds = new HashMap<>();
    m_treeEventFilter = new TreeEventFilter(getModel());
  }

  @Override
  public String getObjectType() {
    return "DesktopTree";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_modelTreeListener == null) { //FIXME CGU illegal state when null
      m_modelTreeListener = new P_ModelTreeListener();
      getModel().addUITreeListener(m_modelTreeListener);
    }
    attachAdapter(getModel().getContextMenu());
    attachAdapters(getModel().getMenus());
    if (getModel().isRootNodeVisible()) {
      attachPage(getModel().getRootPage());
    }
    else {
      for (IPage childPage : getModel().getRootPage().getChildPages()) {
        attachPage(childPage);
      }
    }
  }

  private void attachPage(IPage page) {
    if (page.getChildNodeCount() > 0) {
      for (IPage childPage : page.getChildPages()) {
        attachPage(childPage);
      }
    }
    optAttachAdapter(page.getDetailForm());
    if (page instanceof IPageWithTable) {
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
      attachAdapter(pageWithTable.getTable());
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_modelTreeListener != null) {
      getModel().removeTreeListener(m_modelTreeListener);
      m_modelTreeListener = null;
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    m_treeNodeIds.clear();
    m_treeNodes.clear();
  }

  public TreeEventFilter getTreeEventFilter() {
    return m_treeEventFilter;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JSONArray jsonPages = new JSONArray();
    if (getModel().isRootNodeVisible()) {
      IPage rootPage = getModel().getRootPage();
      jsonPages.put(pageToJson(rootPage));
    }
    else {
      for (IPage childPage : getModel().getRootPage().getChildPages()) {
        jsonPages.put(pageToJson(childPage));
      }
    }
    putProperty(json, PROP_NODES, jsonPages);
    putProperty(json, PROP_SELECTED_NODE_IDS, nodeIdsToJson(getModel().getSelectedNodes()));
    putAdapterIdsProperty(json, PROP_MENUS, getModel().getMenus());
    return json;
  }

  protected void handleModelTreeEvent(TreeEvent event) {
    event = getTreeEventFilter().filterIgnorableModelEvent(event);
    if (event == null) {
      return;
    }
    switch (event.getType()) {
      case TreeEvent.TYPE_NODES_INSERTED: {
        handleModelNodesInserted(event);
        break;
      }
      case TreeEvent.TYPE_NODES_DELETED: {
        handleModelNodesDeleted(event);
        break;
      }
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_COLLAPSED: {
        if (!getModel().isRootNodeVisible() && getModel().getRootNode() == event.getNode()) {
          //Not necessary to send events for invisible root node
          return;
        }
        handleModelNodeExpanded(event.getNode());
        break;
      }
      case TreeEvent.TYPE_NODES_SELECTED: {
        handleModelNodesSelected(event.getNodes());
        break;
      }
    }
  }

  protected void handleModelNodeExpanded(ITreeNode modelNode) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, m_treeNodeIds.get(modelNode));
    putProperty(jsonEvent, "expanded", modelNode.isExpanded());
    addActionEvent("nodeExpanded", jsonEvent);
  }

  protected void handleModelNodesInserted(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray jsonPages = new JSONArray();
    for (ITreeNode node : event.getNodes()) {
      IPage page = (IPage) node;
      attachPage(page);
      jsonPages.put(pageToJson(page));
    }
    putProperty(jsonEvent, "nodes", jsonPages);
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getOrCreateNodeId(event.getCommonParentNode()));
    addActionEvent("nodesInserted", jsonEvent);
  }

  protected void handleModelNodesDeleted(TreeEvent event) {
    Collection<ITreeNode> nodes = event.getNodes();
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, m_treeNodeIds.get(event.getCommonParentNode()));

    if (event.getCommonParentNode().getChildNodes().size() == 0) {
      addActionEvent(EVENT_ALL_NODES_DELETED, jsonEvent);
    }
    else {
      putProperty(jsonEvent, PROP_NODE_IDS, nodeIdsToJson(event.getNodes()));
      addActionEvent(EVENT_NODES_DELETED, jsonEvent);
    }

    for (ITreeNode node : nodes) {
      String nodeId = m_treeNodeIds.get(node);
      m_treeNodeIds.remove(node);
      m_treeNodes.remove(nodeId);

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

  protected void handleModelNodesSelected(Collection<ITreeNode> modelNodes) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_IDS, nodeIdsToJson(modelNodes));
    addActionEvent(EVENT_NODES_SELECTED, jsonEvent);
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

  protected JSONArray nodeIdsToJson(Collection<ITreeNode> modelNodes) {
    JSONArray jsonNodeIds = new JSONArray();
    for (ITreeNode node : modelNodes) {
      jsonNodeIds.put(m_treeNodeIds.get(node));
    }
    return jsonNodeIds;
  }

  protected void handleModelTreeEventBatch(List<? extends TreeEvent> events) {
    for (TreeEvent event : events) {
      handleModelTreeEvent(event);
    }
  }

  @Override
  public void handleModelContextMenuChanged(ContextMenuEvent event) {
    List<IJsonAdapter<?>> menuAdapters = attachAdapters(getModel().getMenus());
    addPropertyChangeEvent(PROP_MENUS, getAdapterIds(menuAdapters));
  }

  protected String getOrCreateNodeId(ITreeNode node) {
    String id = m_treeNodeIds.get(node);
    if (id == null) {
      id = getJsonSession().createUniqueIdFor(null);
      m_treeNodes.put(id, node);
      m_treeNodeIds.put(node, id);
    }
    return id;
  }

  protected JSONObject pageToJson(IPage page) {
    String id = getOrCreateNodeId(page);
    JSONObject json = new JSONObject();
    putProperty(json, "id", id);
    putProperty(json, "text", page.getCell().getText());
    putProperty(json, "expanded", page.isExpanded());
    putProperty(json, "leaf", page.isLeaf());
    JSONArray jsonChildPages = new JSONArray();
    if (page.getChildNodeCount() > 0) {
      for (IPage childPage : page.getChildPages()) {
        jsonChildPages.put(pageToJson(childPage));
      }
    }
    putProperty(json, "childNodes", jsonChildPages);
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

  public ITreeNode getTreeNodeForNodeId(String nodeId) {
    ITreeNode node = m_treeNodes.get(nodeId);
    if (node == null) {
      throw new JsonException("No node found for id " + nodeId);
    }
    return node;
  }

  public List<ITreeNode> extractTreeNodes(JSONObject json) {
    return jsonToTreeNodes(JsonObjectUtility.getJSONArray(json, PROP_NODE_IDS));
  }

  public List<ITreeNode> jsonToTreeNodes(JSONArray nodeIds) {
    List<ITreeNode> nodes = new ArrayList<>(nodeIds.length());
    for (int i = 0; i < nodeIds.length(); i++) {
      nodes.add(m_treeNodes.get(JsonObjectUtility.get(nodeIds, i)));
    }
    return nodes;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("nodeClicked".equals(event.getType())) {
      handleUiNodeClick(event, res);
    }
    else if (EVENT_NODES_SELECTED.equals(event.getType())) {
      handleUiNodesSelected(event, res);
    }
    else if (EVENT_NODE_EXPANDED.equals(event.getType())) {
      handleUiNodeExpanded(event, res);
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  protected void handleUiNodeClick(JsonEvent event, JsonResponse res) {
    final ITreeNode node = getTreeNodeForNodeId(JsonObjectUtility.getString(event.getJsonObject(), PROP_NODE_ID));
    getModel().getUIFacade().fireNodeClickFromUI(node);
  }

  protected void handleUiNodesSelected(JsonEvent event, JsonResponse res) {
    final List<ITreeNode> nodes = extractTreeNodes(event.getJsonObject());
    TreeEvent treeEvent = new TreeEvent(getModel(), TreeEvent.TYPE_NODES_SELECTED, nodes);
    getTreeEventFilter().addIgnorableModelEvent(treeEvent);
    try {
      getModel().getUIFacade().setNodesSelectedFromUI(nodes);
    }
    finally {
      getTreeEventFilter().removeIgnorableModelEvent(treeEvent);
    }
  }

  protected void handleUiNodeExpanded(JsonEvent event, JsonResponse res) {
    final ITreeNode node = getTreeNodeForNodeId(JsonObjectUtility.getString(event.getJsonObject(), PROP_NODE_ID));
    final boolean expanded = JsonObjectUtility.getBoolean(event.getJsonObject(), "expanded");
    if (node.isExpanded() == expanded) {
      return;
    }
    TreeEvent treeEvent = new TreeEvent(getModel(), TreeEvent.TYPE_NODE_EXPANDED, node);
    getTreeEventFilter().addIgnorableModelEvent(treeEvent);
    try {
      getModel().getUIFacade().setNodeExpandedFromUI(node, expanded);
    }
    finally {
      getTreeEventFilter().removeIgnorableModelEvent(treeEvent);
    }
  }

  private class P_ModelTreeListener implements TreeListener {
    @Override
    public void treeChanged(final TreeEvent e) {
      handleModelTreeEvent(e);
    }

    @Override
    public void treeChangedBatch(List<? extends TreeEvent> events) {
      handleModelTreeEventBatch(events);
    }
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

package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.get;
import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.getBoolean;
import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.getJSONArray;
import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.getString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuEvent;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.html.client.ext.IPage2;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserverAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.menu.IContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.tree.TreeEventFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonDesktopTree extends AbstractJsonPropertyObserverAdapter<IOutline> implements IContextMenuOwner {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonDesktopTree.class);
  public static final String EVENT_NODES_SELECTED = "nodesSelected";
  public static final String EVENT_NODE_EXPANDED = "nodeExpanded";
  public static final String PROP_NODE_ID = "nodeId";
  public static final String PROP_NODE_IDS = "nodeIds";
  public static final String PROP_MENUS = "menus";
  public static final String PROP_NODES = "nodes";
  public static final String PROP_SELECTED_NODE_IDS = "selectedNodeIds";

  private P_ModelTreeListener m_modelTreeListener;
  private Map<String, ITreeNode> m_treeNodes;
  private Map<ITreeNode, String> m_treeNodeIds;
  private TreeEventFilter m_treeEventFilter;

  public JsonDesktopTree(IOutline modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
    m_treeNodes = new HashMap<>();
    m_treeNodeIds = new HashMap<>();
    m_treeEventFilter = new TreeEventFilter(getModelObject());
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
      getModelObject().addUITreeListener(m_modelTreeListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_modelTreeListener != null) {
      getModelObject().removeTreeListener(m_modelTreeListener);
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
    if (getModelObject().isRootNodeVisible()) {
      IPage rootPage = getModelObject().getRootPage();
      jsonPages.put(pageToJson(rootPage));
    }
    else {
      for (IPage childPage : getModelObject().getRootPage().getChildPages()) {
        jsonPages.put(pageToJson(childPage));
      }
    }
    putProperty(json, PROP_NODES, jsonPages);
    putProperty(json, PROP_SELECTED_NODE_IDS, nodeIdsToJson(getModelObject().getSelectedNodes()));

    putProperty(json, PROP_MENUS, modelObjectsToJson(getModelObject().getMenus()));

    //FIXME cgu refactor
    modelObjectToJson(getModelObject().getContextMenu());
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
        handleModelNodesDeleted(event.getNodes());
        break;
      }
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_COLLAPSED: {
        if (!getModelObject().isRootNodeVisible() && getModelObject().getRootNode() == event.getNode()) {
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
    getJsonSession().currentJsonResponse().addActionEvent("nodeExpanded", getId(), jsonEvent);
  }

  protected void handleModelNodesInserted(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray jsonPages = new JSONArray();
    for (ITreeNode node : event.getNodes()) {
      JSONObject jsonPage = pageToJson((IPage) node);
      jsonPages.put(jsonPage);
    }
    putProperty(jsonEvent, "nodes", jsonPages);
    putProperty(jsonEvent, "commonParentNodeId", m_treeNodeIds.get(event.getCommonParentNode()));
    getJsonSession().currentJsonResponse().addActionEvent("nodesInserted", getId(), jsonEvent);
  }

  protected void handleModelNodesDeleted(Collection<ITreeNode> modelNodes) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_IDS, nodeIdsToJson(modelNodes));
    JSONArray jsonNodeIds = new JSONArray();
    for (ITreeNode node : modelNodes) {
      String nodeId = m_treeNodeIds.get(node);
      jsonNodeIds.put(nodeId);
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
    getJsonSession().currentJsonResponse().addActionEvent("nodesDeleted", getId(), jsonEvent);
  }

  protected void handleModelNodesSelected(Collection<ITreeNode> modelNodes) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_IDS, nodeIdsToJson(modelNodes));
    getJsonSession().currentJsonResponse().addActionEvent(EVENT_NODES_SELECTED, getId(), jsonEvent);
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
    getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), PROP_MENUS, modelObjectsToJson(getModelObject().getMenus()));
  }

  protected String getOrCreatedNodeId(ITreeNode node) {
    String id = m_treeNodeIds.get(node);
    if (id == null) {
      id = getJsonSession().createUniqueIdFor(null);
      m_treeNodes.put(id, node);
      m_treeNodeIds.put(node, id);
    }
    return id;
  }

  protected JSONObject pageToJson(IPage page) {
    String id = getOrCreatedNodeId(page);
    String parentNodeId = getOrCreatedNodeId(page.getParentPage());
    JSONObject json = new JSONObject();
    putProperty(json, "id", id);
    putProperty(json, "parentNodeId", parentNodeId);
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

    String pageType = "";
    if (page instanceof IPageWithTable) {
      pageType = "table";
      IPageWithTable<?> pageWithTable = (IPageWithTable<?>) page;
      ITable table = pageWithTable.getTable();
      putProperty(json, "table", modelObjectToJson(table));

      if (page instanceof IPage2) {
        putProperty(json, "tableControls", modelObjectsToJson(((IPage2) page).getTableControls()));
      }

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
    return jsonToTreeNodes(getJSONArray(json, PROP_NODE_IDS));
  }

  public List<ITreeNode> jsonToTreeNodes(JSONArray nodeIds) {
    List<ITreeNode> nodes = new ArrayList<>(nodeIds.length());
    for (int i = 0; i < nodeIds.length(); i++) {
      nodes.add(m_treeNodes.get(get(nodeIds, i)));
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
    final ITreeNode node = getTreeNodeForNodeId(getString(event.getJsonObject(), PROP_NODE_ID));
    getModelObject().getUIFacade().fireNodeClickFromUI(node);
  }

  protected void handleUiNodesSelected(JsonEvent event, JsonResponse res) {
    final List<ITreeNode> nodes = extractTreeNodes(event.getJsonObject());
    TreeEvent treeEvent = new TreeEvent(getModelObject(), TreeEvent.TYPE_NODES_SELECTED, nodes);
    getTreeEventFilter().addIgnorableModelEvent(treeEvent);

    try {
      getModelObject().getUIFacade().setNodesSelectedFromUI(nodes);
    }
    finally {
      getTreeEventFilter().removeIgnorableModelEvent(treeEvent);
    }
  }

  protected void handleUiNodeExpanded(JsonEvent event, JsonResponse res) {
    final ITreeNode node = getTreeNodeForNodeId(getString(event.getJsonObject(), PROP_NODE_ID));
    final boolean expanded = getBoolean(event.getJsonObject(), "expanded");
    if (node.isExpanded() == expanded) {
      return;
    }
    TreeEvent treeEvent = new TreeEvent(getModelObject(), TreeEvent.TYPE_NODE_EXPANDED, node);
    getTreeEventFilter().addIgnorableModelEvent(treeEvent);
    try {
      getModelObject().getUIFacade().setNodeExpandedFromUI(node, expanded);
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

}

package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.root.ContextMenuEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.menu.IContextMenuOwner;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonTree<T extends ITree> extends AbstractJsonPropertyObserver<T> implements IContextMenuOwner {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonTree.class);
  public static final String EVENT_NODE_CLICKED = "nodeClicked";
  public static final String EVENT_NODE_ACTION = "nodeAction";
  public static final String EVENT_NODES_SELECTED = "nodesSelected";
  public static final String EVENT_NODE_EXPANDED = "nodeExpanded";
  public static final String EVENT_NODES_DELETED = "nodesDeleted";
  public static final String EVENT_NODES_INSERTED = "nodesInserted";
  public static final String EVENT_ALL_NODES_DELETED = "allNodesDeleted";
  public static final String PROP_NODE_ID = "nodeId";
  public static final String PROP_NODE_IDS = "nodeIds";
  public static final String PROP_COMMON_PARENT_NODE_ID = "commonParentNodeId";
  public static final String PROP_NODES = "nodes";
  public static final String PROP_SELECTED_NODE_IDS = "selectedNodeIds";

  private P_ModelTreeListener m_modelTreeListener;
  private Map<String, ITreeNode> m_treeNodes;
  private Map<ITreeNode, String> m_treeNodeIds;
  private TreeEventFilter m_treeEventFilter;

  public JsonTree(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
    m_treeNodes = new HashMap<>();
    m_treeNodeIds = new HashMap<>();
    m_treeEventFilter = new TreeEventFilter(getModel());
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(ITree.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "Tree";
  }

  @Override
  protected void createChildAdapters() {
    super.createChildAdapters();
    attachAdapter(getModel().getContextMenu());
    attachAdapters(getModel().getMenus());
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    disposeAdapter(getModel().getContextMenu());
    disposeAdapters(getModel().getMenus());
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_modelTreeListener == null) { //FIXME CGU illegal state when null
      m_modelTreeListener = new P_ModelTreeListener();
      getModel().addUITreeListener(m_modelTreeListener);
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
    JSONArray jsonNodes = new JSONArray();
    if (getModel().isRootNodeVisible()) {
      ITreeNode rootNode = getModel().getRootNode();
      jsonNodes.put(treeNodeToJson(rootNode));
    }
    else {
      for (ITreeNode childNode : getModel().getRootNode().getChildNodes()) {
        jsonNodes.put(treeNodeToJson(childNode));
      }
    }
    putProperty(json, PROP_NODES, jsonNodes);
    putProperty(json, PROP_SELECTED_NODE_IDS, nodeIdsToJson(getModel().getSelectedNodes()));
    putAdapterIdsProperty(json, PROP_MENUS, getModel().getMenus());
    return json;
  }

  protected void handleModelTreeEvent(TreeEvent event) {
    event = getTreeEventFilter().filter(event);
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
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(modelNode));
    putProperty(jsonEvent, "expanded", modelNode.isExpanded());
    addActionEvent(EVENT_NODE_EXPANDED, jsonEvent);
  }

  protected void handleModelNodesInserted(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray jsonNodes = new JSONArray();
    for (ITreeNode node : event.getNodes()) {
      jsonNodes.put(treeNodeToJson(node));
    }
    putProperty(jsonEvent, PROP_NODES, jsonNodes);
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getOrCreateNodeId(event.getCommonParentNode()));
    addActionEvent(EVENT_NODES_INSERTED, jsonEvent);
  }

  protected void handleModelNodesDeleted(TreeEvent event) {
    Collection<ITreeNode> nodes = event.getNodes();
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getOrCreateNodeId(event.getCommonParentNode()));

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
    }
  }

  protected void handleModelNodesSelected(Collection<ITreeNode> modelNodes) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_IDS, nodeIdsToJson(modelNodes));
    addActionEvent(EVENT_NODES_SELECTED, jsonEvent);
  }

  protected JSONArray nodeIdsToJson(Collection<ITreeNode> modelNodes) {
    JSONArray jsonNodeIds = new JSONArray();
    for (ITreeNode node : modelNodes) {
      jsonNodeIds.put(getOrCreateNodeId(node));
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
    //FIXME dispose former menus? Keep list of menu adapters? Currently switching a page always creates new menus.
    List<IJsonAdapter<?>> menuAdapters = attachAdapters(getModel().getMenus());
    addPropertyChangeEvent(PROP_MENUS, getAdapterIds(menuAdapters));
  }

  protected String getOrCreateNodeId(ITreeNode node) {
    if (node == null) {
      return null;
    }

    String id = m_treeNodeIds.get(node);
    if (id == null) {
      id = getJsonSession().createUniqueIdFor(null);
      m_treeNodes.put(id, node);
      m_treeNodeIds.put(node, id);
    }
    return id;
  }

  protected JSONObject treeNodeToJson(ITreeNode node) {
    String id = getOrCreateNodeId(node);
    JSONObject json = new JSONObject();
    putProperty(json, "id", id);
    putProperty(json, "text", node.getCell().getText());
    putProperty(json, "expanded", node.isExpanded());
    putProperty(json, "leaf", node.isLeaf());
    JSONArray jsonChildNodes = new JSONArray();
    if (node.getChildNodeCount() > 0) {
      for (ITreeNode childNode : node.getChildNodes()) {
        jsonChildNodes.put(treeNodeToJson(childNode));
      }
    }
    putProperty(json, "childNodes", jsonChildNodes);
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
    if (EVENT_NODE_CLICKED.equals(event.getType())) {
      handleUiNodeClicked(event, res);
    }
    else if (EVENT_NODE_ACTION.equals(event.getType())) {
      handleUiNodeAction(event, res);
    }
    else if (EVENT_NODES_SELECTED.equals(event.getType())) {
      handleUiNodesSelected(event, res);
    }
    else if (EVENT_NODE_EXPANDED.equals(event.getType())) {
      handleUiNodeExpanded(event, res);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  protected void handleUiNodeClicked(JsonEvent event, JsonResponse res) {
    final ITreeNode node = getTreeNodeForNodeId(JsonObjectUtility.getString(event.getData(), PROP_NODE_ID));
    getModel().getUIFacade().fireNodeClickFromUI(node);
  }

  protected void handleUiNodeAction(JsonEvent event, JsonResponse res) {
    final ITreeNode node = getTreeNodeForNodeId(JsonObjectUtility.getString(event.getData(), PROP_NODE_ID));
    getModel().getUIFacade().fireNodeActionFromUI(node);
  }

  protected void handleUiNodesSelected(JsonEvent event, JsonResponse res) {
    final List<ITreeNode> nodes = extractTreeNodes(event.getData());
    TreeEventFilterCondition condition = new TreeEventFilterCondition(TreeEvent.TYPE_NODES_SELECTED, nodes);
    getTreeEventFilter().addCondition(condition);
    try {
      getModel().getUIFacade().setNodesSelectedFromUI(nodes);
    }
    finally {
      getTreeEventFilter().removeCondition(condition);
    }
  }

  protected void handleUiNodeExpanded(JsonEvent event, JsonResponse res) {
    ITreeNode node = getTreeNodeForNodeId(JsonObjectUtility.getString(event.getData(), PROP_NODE_ID));
    boolean expanded = JsonObjectUtility.getBoolean(event.getData(), "expanded");
    int eventType = TreeEvent.TYPE_NODE_EXPANDED;
    if (!expanded) {
      eventType = TreeEvent.TYPE_NODE_COLLAPSED;
    }
    TreeEventFilterCondition condition = new TreeEventFilterCondition(eventType, node);
    getTreeEventFilter().addCondition(condition);
    try {
      getModel().getUIFacade().setNodeExpandedFromUI(node, expanded);
    }
    finally {
      getTreeEventFilter().removeCondition(condition);
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

package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree5;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.menu.IContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonTree<T extends ITree> extends AbstractJsonPropertyObserver<T> implements IContextMenuOwner {

  public static final String EVENT_NODES_INSERTED = "nodesInserted";
  public static final String EVENT_NODES_UPDATED = "nodesUpdated";
  public static final String EVENT_NODES_DELETED = "nodesDeleted";
  public static final String EVENT_ALL_NODES_DELETED = "allNodesDeleted";
  public static final String EVENT_NODES_SELECTED = "nodesSelected";
  public static final String EVENT_NODE_CLICKED = "nodeClicked";
  public static final String EVENT_NODE_ACTION = "nodeAction";
  public static final String EVENT_NODE_EXPANDED = "nodeExpanded";
  public static final String EVENT_NODE_CHANGED = "nodeChanged";
  public static final String EVENT_CHILD_NODE_ORDER_CHANGED = "childNodeOrderChanged";
  public static final String EVENT_NODES_CHECKED = "nodesChecked";

  public static final String PROP_NODE_ID = "nodeId";
  public static final String PROP_NODE_IDS = "nodeIds";
  public static final String PROP_COMMON_PARENT_NODE_ID = "commonParentNodeId";
  public static final String PROP_NODE = "node";
  public static final String PROP_NODES = "nodes";
  public static final String PROP_EXPANDED = "expanded";
  public static final String PROP_SELECTED_NODE_IDS = "selectedNodeIds";

  private TreeListener m_treeListener;
  private Map<String, ITreeNode> m_treeNodes;
  private Map<ITreeNode, String> m_treeNodeIds;
  private TreeEventFilter m_treeEventFilter;

  public JsonTree(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
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
    putJsonProperty(new JsonProperty<T>(ITree5.PROP_FILTER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        if (getModel() instanceof ITree5) {
          return ((ITree5) getModel()).isFilterEnabled();
        }
        else {
          return false;
        }
      }
    });
    putJsonProperty(new JsonProperty<T>(ITree.PROP_CHECKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCheckable();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITree.PROP_MULTI_CHECK, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultiCheck();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITree.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITree.PROP_AUTO_CHECK_CHILDREN, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoCheckChildNodes();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "Tree";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachContextMenu();
    attachNodes(getTopLevelNodes(true), true);
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    disposeNodes(getTopLevelNodes(true), true);

    // "Leak detection". Assert that after disposing the child adapters,
    // all nodes have been disposed (they will remove themselves in their
    // dispose() method).
    if (!m_treeNodeIds.isEmpty() || !m_treeNodes.isEmpty()) {
      throw new IllegalStateException("Not all nodes have been disposed! TreeNodeIds: " + m_treeNodeIds + " TreeNodes: " + m_treeNodes);
    }
  }

  protected void attachContextMenu() {
    attachAdapter(getModel().getContextMenu());
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_treeListener != null) {
      throw new IllegalStateException();
    }
    m_treeListener = new P_TreeListener();
    getModel().addUITreeListener(m_treeListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_treeListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeTreeListener(m_treeListener);
    m_treeListener = null;
  }

  protected void attachNode(ITreeNode node, boolean attachChildren) {
  }

  protected void attachNodes(Collection<ITreeNode> nodes, boolean attachChildren) {
    for (ITreeNode node : nodes) {
      attachNode(node, attachChildren);
    }
  }

  protected void disposeNode(ITreeNode node, boolean disposeChildren) {
    if (disposeChildren) {
      disposeNodes(node.getChildNodes(), disposeChildren);
    }
    String nodeId = m_treeNodeIds.get(node);
    m_treeNodeIds.remove(node);
    m_treeNodes.remove(nodeId);
  }

  protected void disposeNodes(Collection<ITreeNode> nodes, boolean disposeChildren) {
    for (ITreeNode node : nodes) {
      disposeNode(node, disposeChildren);
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JSONArray jsonNodes = new JSONArray();
    for (ITreeNode childNode : getTopLevelNodes(true)) {
      jsonNodes.put(treeNodeToJson(childNode));
    }
    putProperty(json, PROP_NODES, jsonNodes);
    putProperty(json, PROP_SELECTED_NODE_IDS, nodeIdsToJson(getModel().getSelectedNodes()));
    putContextMenu(json);
    return json;
  }

  protected void putContextMenu(JSONObject json) {
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      JsonObjectUtility.putProperty(json, PROP_MENUS, JsonObjectUtility.adapterIdsToJson(jsonContextMenu.getJsonChildActions()));
    }
  }

  protected void handleModelTreeEvent(TreeEvent event) {
    event = m_treeEventFilter.filter(event);
    if (event == null) {
      return;
    }
    switch (event.getType()) {
      case TreeEvent.TYPE_NODES_INSERTED:
        handleModelNodesInserted(event);
        break;
      case TreeEvent.TYPE_NODES_UPDATED:
        handleModelNodesUpdated(event);
        break;
      case TreeEvent.TYPE_NODES_DELETED:
        handleModelNodesDeleted(event);
        break;
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_COLLAPSED:
        if (!isInvisibleRootNode(event.getNode())) { // Not necessary to send events for invisible root node
          handleModelNodeExpanded(event.getNode());
        }
        break;
      case TreeEvent.TYPE_NODES_SELECTED:
        handleModelNodesSelected(event.getNodes());
        break;
      case TreeEvent.TYPE_NODES_CHECKED:
        handleModelNodesChecked(event.getNodes());
        break;
      case TreeEvent.TYPE_NODE_CHANGED:
        handleModelNodeChanged(event.getNode());
        break;
      case TreeEvent.TYPE_NODE_FILTER_CHANGED:
        handleModelNodeFilterChanged();
        break;
      case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED:
        handleModelChildNodeOrderChanged(event);
        break;
      default:
        handleOtherTreeEvent(event);
        break;
    }
    // TODO Tree | Events not yet implemented:
    // - TYPE_BEFORE_NODES_SELECTED
    // - TYPE_NODE_REQUEST_FOCUS
    // - TYPE_NODE_ENSURE_VISIBLE
    // - TYPE_REQUEST_FOCUS
    // - TYPE_SCROLL_TO_SELECTION
    // Probabely not needed:
    // - TYPE_NODE_ACTION
    // - TYPE_NODES_DRAG_REQUEST
    // - TYPE_DRAG_FINISHED
    // - TYPE_NODE_DROP_ACTION
    // - TYPE_NODE_CLICK
    // - TYPE_NODE_DROP_TARGET_CHANGED
  }

  /**
   * Default impl. does nothing. Override this method to handle custom tree-events.
   */
  protected void handleOtherTreeEvent(TreeEvent event) {
  }

  protected void handleModelNodeExpanded(ITreeNode modelNode) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(modelNode));
    putProperty(jsonEvent, PROP_EXPANDED, modelNode.isExpanded());
    addActionEvent(EVENT_NODE_EXPANDED, jsonEvent);
  }

  protected void handleModelNodesInserted(TreeEvent event) {
    addNodesInsertedEvent(event.getCommonParentNode(), event.getNodes());
  }

  protected void addNodesInsertedEvent(ITreeNode commonParentNode, Collection<ITreeNode> nodes) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray jsonNodes = new JSONArray();
    attachNodes(nodes, true);
    for (ITreeNode node : nodes) {
      jsonNodes.put(treeNodeToJson(node));
    }
    putProperty(jsonEvent, PROP_NODES, jsonNodes);
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getOrCreateNodeId(commonParentNode));
    addActionEvent(EVENT_NODES_INSERTED, jsonEvent);
  }

  protected void handleModelNodesUpdated(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    for (ITreeNode node : event.getNodes()) {
      // Only send _some_ of the properties. Everything else will be handled with separate events.
      // --> See also: Tree.js/_onNodesUpdated()
      JSONObject jsonNode = new JSONObject();
      putProperty(jsonNode, "id", getOrCreateNodeId(node));
      putProperty(jsonNode, "leaf", node.isLeaf());
      JsonObjectUtility.append(jsonEvent, PROP_NODES, jsonNode);
    }
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getOrCreateNodeId(event.getCommonParentNode()));
    addActionEvent(EVENT_NODES_UPDATED, jsonEvent);
  }

  protected void handleModelNodesDeleted(TreeEvent event) {
    Collection<ITreeNode> nodes = event.getNodes();
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getOrCreateNodeId(event.getCommonParentNode()));
    // If no nodes remain, just send "all" instead of every single nodeId. However, the nodes must be disposed individually.
    if (event.getCommonParentNode().getFilteredChildNodes().size() == 0) {
      addActionEvent(EVENT_ALL_NODES_DELETED, jsonEvent);
    }
    else {
      putProperty(jsonEvent, PROP_NODE_IDS, nodeIdsToJson(nodes));
      addActionEvent(EVENT_NODES_DELETED, jsonEvent);
    }
    disposeNodes(nodes, true);
  }

  protected void handleModelNodesSelected(Collection<ITreeNode> modelNodes) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_IDS, nodeIdsToJson(modelNodes));
    addActionEvent(EVENT_NODES_SELECTED, jsonEvent);
  }

  protected void handleModelNodesChecked(Collection<ITreeNode> modelNodes) {
    JSONObject jsonEvent = new JSONObject();
    JSONArray jsonNodes = new JSONArray();
    for (ITreeNode node : modelNodes) {
      String id = getOrCreateNodeId(node);
      JSONObject json = new JSONObject();
      putProperty(json, "id", id);
      putProperty(json, "checked", node.isChecked());
      JsonObjectUtility.filterDefaultValues(json, "TreeNode");
      jsonNodes.put(json);
    }
    putProperty(jsonEvent, PROP_NODES, (jsonNodes));
    addActionEvent(EVENT_NODES_CHECKED, jsonEvent);
  }

  protected void handleModelNodeChanged(ITreeNode modelNode) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, getOrCreateNodeId(modelNode));
    putCellProperties(jsonEvent, modelNode.getCell());
    addActionEvent(EVENT_NODE_CHANGED, jsonEvent);
  }

  protected void handleModelNodeFilterChanged() {
    // Delete all nodes
    disposeNodes(getTopLevelNodes(false), true);
    addActionEvent(EVENT_ALL_NODES_DELETED, new JSONObject());

    // Insert the remaining nodes again (re-attaching them automatically)
    addNodesInsertedEvent(null, getTopLevelNodes(true));
  }

  protected void handleModelChildNodeOrderChanged(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    JsonObjectUtility.putProperty(jsonEvent, "parentNodeId", getOrCreateNodeId(event.getCommonParentNode()));
    for (ITreeNode childNode : event.getChildNodes()) {
      JsonObjectUtility.append(jsonEvent, "childNodeIds", getOrCreateNodeId(childNode));
    }
    addActionEvent(EVENT_CHILD_NODE_ORDER_CHANGED, jsonEvent);
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  protected JSONArray nodeIdsToJson(Collection<ITreeNode> modelNodes) {
    JSONArray jsonNodeIds = new JSONArray();
    for (ITreeNode node : modelNodes) {
      String nodeId = getOrCreateNodeId(node);
      //May be null if its the invisible root node
      if (nodeId != null) {
        jsonNodeIds.put(nodeId);
      }
    }
    return jsonNodeIds;
  }

  protected String getOrCreateNodeId(ITreeNode node) {
    if (node == null) {
      return null;
    }
    if (isInvisibleRootNode(node)) {
      return null;
    }
    String id = getNodeId(node);
    if (id != null) {
      return id;
    }
    id = getJsonSession().createUniqueIdFor(null);
    m_treeNodes.put(id, node);
    m_treeNodeIds.put(node, id);
    return id;
  }

  protected String getNodeId(ITreeNode node) {
    return m_treeNodeIds.get(node);
  }

  protected ITreeNode getNode(String nodeId) {
    return m_treeNodes.get(nodeId);
  }

  protected boolean isInvisibleRootNode(ITreeNode node) {
    return !getModel().isRootNodeVisible() && getModel().getRootNode() == node;
  }

  protected List<ITreeNode> getTopLevelNodes(boolean filteredOnly) {
    ITreeNode rootNode = getModel().getRootNode();
    if (getModel().isRootNodeVisible()) {
      return CollectionUtility.arrayList(rootNode);
    }
    return (filteredOnly ? rootNode.getFilteredChildNodes() : rootNode.getChildNodes());
  }

  protected void putCellProperties(JSONObject json, ICell cell) {
    // We deliberately don't use JsonCell here, because most properties are not supported in a tree anyway
    JsonObjectUtility.putProperty(json, "text", cell.getText());
    JsonObjectUtility.putProperty(json, "iconId", cell.getIconId());
    JsonObjectUtility.putProperty(json, "tooltipText", cell.getTooltipText());
    JsonObjectUtility.putProperty(json, "foregroundColor", cell.getForegroundColor());
    JsonObjectUtility.putProperty(json, "backgroundColor", cell.getBackgroundColor());
    JsonObjectUtility.putProperty(json, "font", (cell.getFont() == null ? null : cell.getFont().toPattern()));
    // TODO BSH Tree | Maybe add "cssStyle"?
  }

  protected JSONObject treeNodeToJson(ITreeNode node) {
    JSONObject json = new JSONObject();
    putProperty(json, "id", getOrCreateNodeId(node));
    putProperty(json, "expanded", node.isExpanded());
    putProperty(json, "leaf", node.isLeaf());
    putProperty(json, "checked", node.isChecked());
    putProperty(json, "enabled", node.isEnabled());
    putCellProperties(json, node.getCell());
    JSONArray jsonChildNodes = new JSONArray();
    if (node.getChildNodeCount() > 0) {
      for (ITreeNode childNode : node.getFilteredChildNodes()) {
        jsonChildNodes.put(treeNodeToJson(childNode));
      }
    }
    putProperty(json, "childNodes", jsonChildNodes);
    JsonObjectUtility.filterDefaultValues(json, "TreeNode");
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
      ITreeNode node = getNode(JsonObjectUtility.getString(nodeIds, i));
      if (node != null) {
        nodes.add(node);
      }
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
    else if (EVENT_NODES_CHECKED.equals(event.getType())) {
      handleUiNodesChecked(event, res);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  protected void handleUiNodesChecked(JsonEvent event, JsonResponse res) {
    CheckedInfo treeNodesChecked = jsonToCheckedInfo(event.getData());
    addTreeEventFilterCondition(TreeEvent.TYPE_NODES_CHECKED, treeNodesChecked.getAllNodes());
    if (treeNodesChecked.getCheckedNodes().size() > 0) {
      getModel().getUIFacade().setNodesCheckedFromUI(treeNodesChecked.getCheckedNodes(), true);
    }
    if (treeNodesChecked.getUncheckedNodes().size() > 0) {
      getModel().getUIFacade().setNodesCheckedFromUI(treeNodesChecked.getUncheckedNodes(), false);
    }
  }

  protected void handleUiNodeClicked(JsonEvent event, JsonResponse res) {
    final ITreeNode node = getTreeNodeForNodeId(JsonObjectUtility.getString(event.getData(), PROP_NODE_ID));
    getModel().getUIFacade().fireNodeClickFromUI(node, MouseButton.Left);
  }

  protected void handleUiNodeAction(JsonEvent event, JsonResponse res) {
    final ITreeNode node = getTreeNodeForNodeId(JsonObjectUtility.getString(event.getData(), PROP_NODE_ID));
    getModel().getUIFacade().fireNodeActionFromUI(node);
  }

  protected void handleUiNodesSelected(JsonEvent event, JsonResponse res) {
    final List<ITreeNode> nodes = extractTreeNodes(event.getData());
    addTreeEventFilterCondition(TreeEvent.TYPE_NODES_SELECTED, nodes);
    getModel().getUIFacade().setNodesSelectedFromUI(nodes);
  }

  protected void handleUiNodeExpanded(JsonEvent event, JsonResponse res) {
    ITreeNode node = getTreeNodeForNodeId(JsonObjectUtility.getString(event.getData(), PROP_NODE_ID));
    boolean expanded = JsonObjectUtility.getBoolean(event.getData(), PROP_EXPANDED);
    int eventType = expanded ? TreeEvent.TYPE_NODE_EXPANDED : TreeEvent.TYPE_NODE_COLLAPSED;
    addTreeEventFilterCondition(eventType, CollectionUtility.arrayList(node));
    getModel().getUIFacade().setNodeExpandedFromUI(node, expanded);
  }

  protected final TreeEventFilter getTreeEventFilter() {
    return m_treeEventFilter;
  }

  protected void addTreeEventFilterCondition(int type, List<ITreeNode> nodes) {
    m_treeEventFilter.addCondition(new TreeEventFilterCondition(type, nodes));
  }

  @Override
  public void cleanUpEventFilters() {
    super.cleanUpEventFilters();
    m_treeEventFilter.removeAllConditions();
  }

  protected CheckedInfo jsonToCheckedInfo(JSONObject data) {
    JSONArray jsonNodes = data.optJSONArray("nodes");
    CheckedInfo checkInfo = new CheckedInfo();
    for (int i = 0; i < jsonNodes.length(); i++) {
      JSONObject jsonObject = jsonNodes.optJSONObject(i);
      ITreeNode row = m_treeNodes.get(jsonObject.optString("nodeId"));
      checkInfo.getAllNodes().add(row);
      if (jsonObject.optBoolean("checked")) {
        checkInfo.getCheckedNodes().add(row);
      }
      else {
        checkInfo.getUncheckedNodes().add(row);
      }
    }
    return checkInfo;
  }

  protected static class CheckedInfo {
    private final List<ITreeNode> m_allNodes = new ArrayList<ITreeNode>();
    private final List<ITreeNode> m_checkedNodes = new ArrayList<ITreeNode>();
    private final List<ITreeNode> m_uncheckedNodes = new ArrayList<ITreeNode>();

    public CheckedInfo() {
    }

    public List<ITreeNode> getAllNodes() {
      return m_allNodes;
    }

    public List<ITreeNode> getCheckedNodes() {
      return m_checkedNodes;
    }

    public List<ITreeNode> getUncheckedNodes() {
      return m_uncheckedNodes;
    }
  }

  private class P_TreeListener extends TreeAdapter {

    @Override
    public void treeChanged(final TreeEvent e) {
      handleModelTreeEvent(e);
    }
  }
}

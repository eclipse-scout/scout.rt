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
package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.IVirtualTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeUtility;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceConsumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTree<TREE extends ITree> extends AbstractJsonPropertyObserver<TREE> implements IJsonContextMenuOwner, IBinaryResourceConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(JsonTree.class);

  public static final String EVENT_NODES_INSERTED = "nodesInserted";
  public static final String EVENT_NODES_UPDATED = "nodesUpdated";
  public static final String EVENT_NODES_DELETED = "nodesDeleted";
  public static final String EVENT_ALL_CHILD_NODES_DELETED = "allChildNodesDeleted";
  public static final String EVENT_NODES_SELECTED = "nodesSelected";
  public static final String EVENT_NODE_CLICKED = "nodeClicked";
  public static final String EVENT_NODE_ACTION = "nodeAction";
  public static final String EVENT_NODE_EXPANDED = "nodeExpanded";
  public static final String EVENT_NODE_CHANGED = "nodeChanged";
  public static final String EVENT_CHILD_NODE_ORDER_CHANGED = "childNodeOrderChanged";
  public static final String EVENT_NODES_CHECKED = "nodesChecked";
  public static final String EVENT_REQUEST_FOCUS = "requestFocus";
  public static final String EVENT_SCROLL_TO_SELECTION = "scrollToSelection";

  public static final String PROP_NODE_ID = "nodeId";
  public static final String PROP_NODE_IDS = "nodeIds";
  public static final String PROP_COMMON_PARENT_NODE_ID = "commonParentNodeId";
  public static final String PROP_NODE = "node";
  public static final String PROP_NODES = "nodes";
  public static final String PROP_EXPANDED = "expanded";
  public static final String PROP_EXPANDED_LAZY = "expandedLazy";
  public static final String PROP_SELECTED_NODES = "selectedNodes";

  private TreeListener m_treeListener;
  private final Map<String, ITreeNode> m_treeNodes;
  private final Map<ITreeNode, String> m_treeNodeIds;
  private final TreeEventFilter m_treeEventFilter;
  private final AbstractEventBuffer<TreeEvent> m_eventBuffer;

  public JsonTree(TREE model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_treeNodes = new HashMap<>();
    m_treeNodeIds = new HashMap<>();
    m_treeEventFilter = new TreeEventFilter(getModel());
    m_eventBuffer = model.createEventBuffer();
  }

  @Override
  public String getObjectType() {
    return "Tree";
  }

  @Override
  protected void initJsonProperties(TREE model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<TREE>(ITree.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<TREE>(ITree.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }
    });
    putJsonProperty(new JsonProperty<TREE>(ITree.PROP_CHECKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCheckable();
      }
    });
    putJsonProperty(new JsonProperty<TREE>(ITree.PROP_MULTI_CHECK, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultiCheck();
      }
    });
    putJsonProperty(new JsonProperty<TREE>(ITree.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<TREE>(ITree.PROP_AUTO_CHECK_CHILDREN, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoCheckChildNodes();
      }
    });
    putJsonProperty(new JsonProperty<ITree>(ITree.PROP_SCROLL_TO_SELECTION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollToSelection();
      }
    });
    putJsonProperty(new JsonProperty<ITree>(ITree.PROP_DROP_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDropType();
      }
    });
    putJsonProperty(new JsonProperty<ITree>(ITree.PROP_DROP_MAXIMUM_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getDropMaximumSize();
      }
    });
    putJsonProperty(new JsonAdapterProperty<ITree>(ITable.PROP_KEY_STROKES, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<IAction>()).build();
      }

      @Override
      protected List<IKeyStroke> modelValue() {
        return getModel().getKeyStrokes();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getContextMenu(), new DisplayableActionFilter<IMenu>());
    attachNodes(getTopLevelNodes(), true);
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    disposeAllNodes();
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
      if (isNodeAccepted(node)) {
        attachNode(node, attachChildren);
      }
    }
  }

  /**
   * Removes all node mappings without querying the model.
   */
  protected void disposeAllNodes() {
    m_treeNodeIds.clear();
    m_treeNodes.clear();
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
    for (ITreeNode childNode : getTopLevelNodes()) {
      if (!isNodeAccepted(childNode)) {
        continue;
      }
      jsonNodes.put(treeNodeToJson(childNode));
    }
    putProperty(json, PROP_NODES, jsonNodes);
    putProperty(json, PROP_SELECTED_NODES, nodeIdsToJson(getModel().getSelectedNodes(), true, true));
    putContextMenu(json);
    return json;
  }

  protected void putContextMenu(JSONObject json) {
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      json.put(PROP_MENUS, jsonContextMenu.childActionsToJson());
    }
  }

  protected void handleModelTreeEvent(TreeEvent event) {
    event = m_treeEventFilter.filter(event);
    if (event == null) {
      return;
    }
    // Add event to buffer instead of handling it immediately. (This allows coalescing the events at JSON response level.)
    bufferModelEvent(event);
    registerAsBufferedEventsAdapter();
  }

  protected void bufferModelEvent(final TreeEvent event) {
    switch (event.getType()) {
      case TreeEvent.TYPE_NODE_FILTER_CHANGED: {
        // Convert the "filter changed" event to a NODES_DELETED and a NODES_INSERTED event. This prevents sending unnecessary
        // data to the UI. We convert the event before adding it to the event buffer to allow coalescing on UI-level.
        // NOTE: This may lead to a temporary inconsistent situation, where node events exist in the buffer after the
        // node itself is deleted. This is because the node is not really deleted from the model. However, when processing
        // the buffered events, the "wrong" events will be ignored and everything is fixed again.
        applyFilterChangedEventToUiRec(Collections.singletonList(getModel().getRootNode()));
        break;
      }
      default: {
        m_eventBuffer.add(event);
      }
    }
  }

  /**
   * Recursively traverses through the given nodes (and its child nodes) and checks which of the model nodes are hidden
   * by tree filters.
   * <ul>
   * <li>For every newly hidden node (i.e. a node that is currently visible on the UI) a NODES_DELETED event is created.
   * <li>For every newly visible node (i.e. a node that is currently invisible on the UI) a NODES_INSERTED event is
   * created.
   * </ul>
   * All new events are added to the event buffer, where they might be coalesced later.
   */
  protected void applyFilterChangedEventToUiRec(List<ITreeNode> nodes) {
    for (ITreeNode node : nodes) {
      boolean processChildNodes = true;

      if (!isInvisibleRootNode(node) && node.getTree() != null) {
        String existingNodeId = optNodeId(node);
        if (node.isFilterAccepted()) {
          if (existingNodeId == null) {
            // Node is not filtered but JsonTree does not know it yet --> handle as insertion event
            m_eventBuffer.add(new TreeEvent(node.getTree(), TreeEvent.TYPE_NODES_INSERTED, node));
            // Stop recursion, because this node (including its child nodes) is already inserted
            processChildNodes = false;
          }
        }
        else if (!node.isRejectedByUser()) {
          if (existingNodeId != null) {
            // Node is filtered, but JsonTree has it in its list --> handle as deletion event
            m_eventBuffer.add(new TreeEvent(node.getTree(), TreeEvent.TYPE_NODES_DELETED, node));
          }
          // Stop recursion, because this node (including its child nodes) is already deleted
          processChildNodes = false;
        }
      }

      // Recursion
      if (processChildNodes) {
        applyFilterChangedEventToUiRec(node.getChildNodes());
      }
    }
  }

  @Override
  public void processBufferedEvents() {
    if (m_eventBuffer.isEmpty()) {
      return;
    }
    List<TreeEvent> coalescedEvents = m_eventBuffer.consumeAndCoalesceEvents();
    for (TreeEvent event : coalescedEvents) {
      processBufferedEvent(event);
    }
  }

  protected void processBufferedEvent(TreeEvent event) {
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
      case TreeEvent.TYPE_ALL_CHILD_NODES_DELETED:
        handleModelAllChildNodesDeleted(event);
        break;
      case TreeEvent.TYPE_NODE_EXPANDED:
      case TreeEvent.TYPE_NODE_COLLAPSED:
        if (!isInvisibleRootNode(event.getNode())) { // Not necessary to send events for invisible root node
          handleModelNodeExpanded(event.getNode(), false);
        }
        break;
      case TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE:
      case TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE:
        if (isInvisibleRootNode(event.getNode())) { // Send event for all child nodes
          for (ITreeNode childNode : event.getNode().getChildNodes()) {
            handleModelNodeExpanded(childNode, true);
          }
        }
        else {
          handleModelNodeExpanded(event.getNode(), true);
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
        // See special handling in bufferModelEvent()
        throw new IllegalStateException("Unsupported event type: " + event);
      case TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED:
        handleModelChildNodeOrderChanged(event);
        break;
      case TreeEvent.TYPE_REQUEST_FOCUS:
        handleModelRequestFocus(event);
        break;
      case TreeEvent.TYPE_SCROLL_TO_SELECTION:
        handleModelScrollToSelection(event);
        break;
      default:
        handleModelOtherTreeEvent(event);
        break;
    }
    // TODO [5.2] bsh: Tree | Events not yet implemented:
    // - TYPE_NODE_REQUEST_FOCUS
    // - TYPE_NODE_ENSURE_VISIBLE what is the difference to scroll_to_selection? delete in treeevent
    // - TYPE_NODES_DRAG_REQUEST
    // - TYPE_DRAG_FINISHED
    // - TYPE_NODE_DROP_ACTION, partly implemented with consumeBinaryResource(...)
    // - TYPE_NODE_DROP_TARGET_CHANGED
  }

  /**
   * Default impl. does nothing. Override this method to handle custom tree-events.
   */
  protected void handleModelOtherTreeEvent(TreeEvent event) {
  }

  protected void handleModelNodeExpanded(ITreeNode modelNode, boolean recursive) {
    if (!isNodeAccepted(modelNode)) {
      return;
    }
    String nodeId = optNodeId(modelNode);
    if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, nodeId);
    putProperty(jsonEvent, PROP_EXPANDED, modelNode.isExpanded());
    putProperty(jsonEvent, PROP_EXPANDED_LAZY, modelNode.isExpandedLazy());
    putProperty(jsonEvent, "recursive", recursive);
    addActionEvent(EVENT_NODE_EXPANDED, jsonEvent);
  }

  protected void handleModelNodesInserted(TreeEvent event) {
    JSONArray jsonNodes = new JSONArray();
    attachNodes(event.getNodes(), true); // FIXME cgu: why not inside loop? attaching for rejected nodes?
    for (ITreeNode node : event.getNodes()) {
      if (isNodeAccepted(node)) {
        jsonNodes.put(treeNodeToJson(node));
      }
    }
    if (jsonNodes.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODES, jsonNodes);
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getOrCreateNodeId(event.getCommonParentNode()));
    addActionEvent(EVENT_NODES_INSERTED, jsonEvent);
  }

  protected void handleModelNodesUpdated(TreeEvent event) {
    JSONArray jsonNodes = new JSONArray();
    for (ITreeNode node : event.getNodes()) {
      if (!isNodeAccepted(node)) {
        continue;
      }
      String nodeId = optNodeId(node);
      if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
        continue;
      }
      JSONObject jsonNode = new JSONObject();
      putProperty(jsonNode, "id", nodeId);
      // Only send _some_ of the properties. Everything else (e.g. "checked", "expanded") will be handled with separate events.
      // --> See also: Tree.js/_onNodesUpdated()
      putProperty(jsonNode, "leaf", node.isLeaf());
      putProperty(jsonNode, "enabled", node.isEnabled());
      putProperty(jsonNode, "lazyExpandingEnabled", node.isLazyExpandingEnabled());

      // Check for virtual nodes that were replaces with real nodes (this will have triggered an NODES_UPDATED event).
      // This would not really be necessary, as both nodes are considered "equal" (see implementation of VirtualTreeNode),
      // but some properties have to be updated in the UI, therefore we replace the nodes in our internal maps.
      ITreeNode cachedNode = optTreeNodeForNodeId(nodeId);
      if (cachedNode instanceof IVirtualTreeNode && cachedNode != node) {
        m_treeNodeIds.put(node, nodeId);
        m_treeNodes.put(nodeId, node);
        putUpdatedPropertiesForResolvedNode(jsonNode, nodeId, node, (IVirtualTreeNode) cachedNode);
      }

      jsonNodes.put(jsonNode);
    }
    if (jsonNodes.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODES, jsonNodes);
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, optNodeId(event.getCommonParentNode()));
    addActionEvent(EVENT_NODES_UPDATED, jsonEvent);
  }

  protected void handleModelNodesDeleted(TreeEvent event) {
    Collection<ITreeNode> nodes = event.getNodes();
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, optNodeId(event.getCommonParentNode()));
    // Small optimization: If no nodes remain, just
    // send "all" instead of every single nodeId. (However, the nodes must be disposed individually.)
    // Caveat: This can only be optimized when no nodes were inserted again in the same "tree changing" scope.
    if (event.getCommonParentNode() != null && getFilteredNodeCount(event.getCommonParentNode()) == 0) {
      addActionEvent(EVENT_ALL_CHILD_NODES_DELETED, jsonEvent);
    }
    else {
      JSONArray jsonNodeIds = nodeIdsToJson(nodes, false, false);
      if (jsonNodeIds.length() > 0) {
        putProperty(jsonEvent, PROP_NODE_IDS, jsonNodeIds);
        addActionEvent(EVENT_NODES_DELETED, jsonEvent);
      }
    }
    disposeNodes(nodes, true);
  }

  protected void handleModelAllChildNodesDeleted(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getNodeId(event.getCommonParentNode()));
    addActionEvent(EVENT_ALL_CHILD_NODES_DELETED, jsonEvent);
    // Read the removed nodes from the event, because they are no longer contained in the model
    disposeNodes(event.getChildNodes(), true);
  }

  protected void handleModelNodesSelected(Collection<ITreeNode> modelNodes) {
    JSONArray jsonNodeIds = nodeIdsToJson(modelNodes, true, false);
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_IDS, jsonNodeIds);
    addActionEvent(EVENT_NODES_SELECTED, jsonEvent);
  }

  protected void handleModelNodesChecked(Collection<ITreeNode> modelNodes) {
    JSONArray jsonNodes = new JSONArray();
    for (ITreeNode node : modelNodes) {
      if (!isNodeAccepted(node)) {
        continue;
      }
      String nodeId = optNodeId(node);
      if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
        continue;
      }
      JSONObject json = new JSONObject();
      putProperty(json, "id", nodeId);
      putProperty(json, "checked", node.isChecked());
      jsonNodes.put(json);
    }
    if (jsonNodes.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODES, (jsonNodes));
    addActionEvent(EVENT_NODES_CHECKED, jsonEvent);
  }

  protected void handleModelNodeChanged(ITreeNode modelNode) {
    if (!isNodeAccepted(modelNode)) {
      return;
    }
    String nodeId = optNodeId(modelNode);
    if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODE_ID, nodeId);
    putCellProperties(jsonEvent, modelNode.getCell());
    addActionEvent(EVENT_NODE_CHANGED, jsonEvent);
  }

  protected void handleModelChildNodeOrderChanged(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put("parentNodeId", getNodeId(event.getCommonParentNode()));
    boolean hasNodeIds = false;
    for (ITreeNode childNode : event.getChildNodes()) {
      if (!isNodeAccepted(childNode)) {
        continue;
      }
      String childNodeId = optNodeId(childNode);
      if (childNodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
        continue;
      }
      jsonEvent.append("childNodeIds", childNodeId);
      hasNodeIds = true;
    }
    if (hasNodeIds) {
      addActionEvent(EVENT_CHILD_NODE_ORDER_CHANGED, jsonEvent);
    }
  }

  protected void handleModelRequestFocus(TreeEvent event) {
    addActionEvent(EVENT_REQUEST_FOCUS);
  }

  protected void handleModelScrollToSelection(TreeEvent event) {
    addActionEvent(EVENT_SCROLL_TO_SELECTION);
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  @Override
  public void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties) {
    if ((getModel().getDropType() & IDNDSupport.TYPE_FILE_TRANSFER) == IDNDSupport.TYPE_FILE_TRANSFER) {
      ResourceListTransferObject transferObject = new ResourceListTransferObject(binaryResources);
      ITreeNode node = null;
      if (uploadProperties != null && uploadProperties.containsKey("nodeId")) {
        String nodeId = uploadProperties.get("nodeId");
        if (!StringUtility.isNullOrEmpty(nodeId)) {
          node = getTreeNodeForNodeId(nodeId);
        }
      }
      getModel().getUIFacade().fireNodeDropActionFromUI(node, transferObject);
    }
  }

  @Override
  public long getMaximumBinaryResourceUploadSize() {
    return getModel().getDropMaximumSize();
  }

  protected JSONArray nodeIdsToJson(Collection<ITreeNode> modelNodes, boolean autoCreateNodeId) {
    return nodeIdsToJson(modelNodes, true, autoCreateNodeId);
  }

  protected JSONArray nodeIdsToJson(Collection<ITreeNode> modelNodes, boolean checkNodeAccepted, boolean autoCreateNodeId) {
    JSONArray jsonNodeIds = new JSONArray();
    for (ITreeNode node : modelNodes) {
      if (checkNodeAccepted && !isNodeAccepted(node)) {
        continue;
      }
      String nodeId;
      if (autoCreateNodeId) {
        nodeId = getOrCreateNodeId(node);
      }
      else {
        nodeId = optNodeId(node);
        if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
          continue;
        }
      }
      // May be null if its the invisible root node
      if (nodeId != null) {
        jsonNodeIds.put(nodeId);
      }
    }
    return jsonNodeIds;
  }

  public String getOrCreateNodeId(ITreeNode node) {
    if (node == null) {
      return null;
    }
    if (isInvisibleRootNode(node)) {
      return null;
    }
    String id = m_treeNodeIds.get(node);
    if (id != null) {
      return id;
    }
    id = getUiSession().createUniqueId();
    m_treeNodes.put(id, node);
    m_treeNodeIds.put(node, id);
    return id;
  }

  /**
   * @return the nodeIdfor the given node. Returns <code>null</code> if the node is the invisible root node or
   *         <code>null</code> itself. Use {@link #optNodeId(ITreeNode)} to prevent an exception when no nodeId could be
   *         found.
   * @throws UiException
   *           when no nodeId is found for the given node
   */
  protected String getNodeId(ITreeNode node) {
    if (node == null) {
      return null;
    }
    if (isInvisibleRootNode(node)) {
      return null;
    }
    String nodeId = m_treeNodeIds.get(node);
    if (nodeId == null) {
      throw new UiException("Unknown node: " + node);
    }
    return nodeId;
  }

  /**
   * @return the nodeId for the given node or <code>null</code> if the node has no nodeId assigned. Also returns
   *         <code>null</code> if the node is the invisible root node or <code>null</code> itself.
   */
  protected String optNodeId(ITreeNode node) {
    if (node == null) {
      return null;
    }
    if (isInvisibleRootNode(node)) {
      return null;
    }
    return m_treeNodeIds.get(node);
  }

  protected boolean isInvisibleRootNode(ITreeNode node) {
    if (!getModel().isRootNodeVisible()) {
      return (node == getModel().getRootNode());
    }
    return false;
  }

  protected List<ITreeNode> getTopLevelNodes() {
    ITreeNode rootNode = getModel().getRootNode();
    if (getModel().isRootNodeVisible()) {
      return CollectionUtility.arrayList(rootNode);
    }
    return rootNode.getChildNodes();
  }

  protected void putCellProperties(JSONObject json, ICell cell) {
    // We deliberately don't use JsonCell here, because most properties are not supported in a tree anyway
    json.put("text", cell.getText());
    json.put("iconId", BinaryResourceUrlUtility.createIconUrl(cell.getIconId()));
    json.put("cssClass", (cell.getCssClass()));
    json.put("tooltipText", cell.getTooltipText());
    json.put("foregroundColor", cell.getForegroundColor());
    json.put("backgroundColor", cell.getBackgroundColor());
    json.put("font", (cell.getFont() == null ? null : cell.getFont().toPattern()));
    json.put("htmlEnabled", cell.isHtmlEnabled());
  }

  /**
   * If the given node has a parent node, the value for the property "childNodeIndex" is calculated and added to the
   * given JSON object.
   * <p>
   * Note that the calculated value may differ from the model's {@link ITreeNode#getChildNodeIndex()} value! This is
   * because not all model nodes are sent to the UI. The calculated value only counts nodes sent to the UI, e.g. a node
   * with childNodeIndex=50 may result in "childNodeIndex: 3" if 47 of the preceding nodes are filtered.
   */
  protected void putChildNodeIndex(JSONObject json, ITreeNode node) {
    if (node.getParentNode() != null && node.getParentNode().getChildNodeCount() > 0) {
      int childNodeIndex = 0;
      // Find the node in the parents childNodes list (skipping non-accepted nodes)
      for (ITreeNode childNode : node.getParentNode().getChildNodes()) {
        childNode = TreeUtility.unwrapResolvedNode(childNode);
        // Only count accepted nodes
        if (isNodeAccepted(childNode)) {
          if (childNode == node) {
            // We have found our node!
            break;
          }
          childNodeIndex++;
        }
      }
      putProperty(json, "childNodeIndex", childNodeIndex);
    }
  }

  protected JSONObject treeNodeToJson(ITreeNode node) {
    // Virtual and resolved nodes are equal in maps, but they don't behave the same. For example, a
    // a virtual node does not return child nodes, while the resolved node does. Therefore, we
    // want to always use the resolved node, if it exists.
    node = TreeUtility.unwrapResolvedNode(node);

    JSONObject json = new JSONObject();
    putProperty(json, "id", getOrCreateNodeId(node));
    putProperty(json, "expanded", node.isExpanded());
    putProperty(json, "expandedLazy", node.isExpandedLazy());
    putProperty(json, "lazyExpandingEnabled", node.isLazyExpandingEnabled());
    putProperty(json, "leaf", node.isLeaf());
    putProperty(json, "checked", node.isChecked());
    putProperty(json, "enabled", node.isEnabled());
    putProperty(json, "iconId", BinaryResourceUrlUtility.createIconUrl(node.getCell().getIconId()));
    putProperty(json, "initialExpanded", node.isInitialExpanded());
    putChildNodeIndex(json, node);
    putCellProperties(json, node.getCell());
    JSONArray jsonChildNodes = new JSONArray();
    if (node.getChildNodeCount() > 0) {
      for (ITreeNode childNode : node.getChildNodes()) {
        if (!isNodeAccepted(childNode)) {
          continue;
        }
        jsonChildNodes.put(treeNodeToJson(childNode));
      }
    }
    putProperty(json, "childNodes", jsonChildNodes);
    JsonObjectUtility.filterDefaultValues(json, "TreeNode");
    return json;
  }

  /**
   * Returns a treeNode for the given nodeId, or null when no node is found for the given nodeId.
   */
  public ITreeNode optTreeNodeForNodeId(String nodeId) {
    return m_treeNodes.get(nodeId);
  }

  /**
   * Returns a treeNode for the given nodeId.
   *
   * @throws UiException
   *           when no node is found for the given nodeId
   */
  public ITreeNode getTreeNodeForNodeId(String nodeId) {
    ITreeNode node = optTreeNodeForNodeId(nodeId);
    if (node == null) {
      throw new UiException("No node found for id " + nodeId);
    }
    return node;
  }

  public List<ITreeNode> extractTreeNodes(JSONObject json) {
    JSONArray nodeIds = json.getJSONArray(PROP_NODE_IDS);
    List<ITreeNode> nodes = new ArrayList<>(nodeIds.length());
    for (int i = 0; i < nodeIds.length(); i++) {
      ITreeNode node = optTreeNodeForNodeId(nodeIds.getString(i));
      if (node != null) {
        nodes.add(node);
      }
    }
    return nodes;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_NODE_CLICKED.equals(event.getType())) {
      handleUiNodeClicked(event);
    }
    else if (EVENT_NODE_ACTION.equals(event.getType())) {
      handleUiNodeAction(event);
    }
    else if (EVENT_NODES_SELECTED.equals(event.getType())) {
      handleUiNodesSelected(event);
    }
    else if (EVENT_NODE_EXPANDED.equals(event.getType())) {
      handleUiNodeExpanded(event);
    }
    else if (EVENT_NODES_CHECKED.equals(event.getType())) {
      handleUiNodesChecked(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiNodesChecked(JsonEvent event) {
    CheckedInfo treeNodesChecked = jsonToCheckedInfo(event.getData());
    addTreeEventFilterCondition(TreeEvent.TYPE_NODES_CHECKED, treeNodesChecked.getAllNodes());
    if (treeNodesChecked.getCheckedNodes().size() > 0) {
      getModel().getUIFacade().setNodesCheckedFromUI(treeNodesChecked.getCheckedNodes(), true);
    }
    if (treeNodesChecked.getUncheckedNodes().size() > 0) {
      getModel().getUIFacade().setNodesCheckedFromUI(treeNodesChecked.getUncheckedNodes(), false);
    }
  }

  protected void handleUiNodeClicked(JsonEvent event) {
    String nodeId = event.getData().getString(PROP_NODE_ID);
    ITreeNode node = optTreeNodeForNodeId(nodeId);
    if (node == null) {
      LOG.warn("Requested tree-node with ID {} doesn't exist. Skip nodeClicked event", nodeId);
      return;
    }
    getModel().getUIFacade().fireNodeClickFromUI(node, MouseButton.Left);
  }

  protected void handleUiNodeAction(JsonEvent event) {
    String nodeId = event.getData().getString(PROP_NODE_ID);
    ITreeNode node = optTreeNodeForNodeId(nodeId);
    if (node == null) {
      LOG.warn("Requested tree-node with ID {} doesn't exist. Skip nodeAction event", nodeId);
      return;
    }
    getModel().getUIFacade().fireNodeActionFromUI(node);
  }

  protected void handleUiNodesSelected(JsonEvent event) {
    final List<ITreeNode> nodes = extractTreeNodes(event.getData());
    addTreeEventFilterCondition(TreeEvent.TYPE_NODES_SELECTED, nodes);
    getModel().getUIFacade().setNodesSelectedFromUI(nodes);
  }

  protected void handleUiNodeExpanded(JsonEvent event) {
    String nodeId = event.getData().getString(PROP_NODE_ID);
    ITreeNode node = optTreeNodeForNodeId(nodeId);
    if (node == null) {
      LOG.warn("Requested tree-node with ID {} doesn't exist. Skip nodeExpanded event", nodeId);
      return;
    }
    boolean expanded = event.getData().getBoolean(PROP_EXPANDED);
    boolean lazy = event.getData().getBoolean(PROP_EXPANDED_LAZY);
    int eventType = expanded ? TreeEvent.TYPE_NODE_EXPANDED : TreeEvent.TYPE_NODE_COLLAPSED;
    addTreeEventFilterCondition(eventType, CollectionUtility.arrayList(node));
    getModel().getUIFacade().setNodeExpandedFromUI(node, expanded, lazy);
  }

  /**
   * Ignore deleted or filtered nodes, because for the UI, they don't exist
   */
  protected boolean isNodeAccepted(ITreeNode node) {
    if (node.isStatusDeleted()) {
      return false;
    }
    if (node.isFilterAccepted()) {
      return true;
    }
    // Accept if rejected by user row filter because gui is and should be aware of that row
    return node.isRejectedByUser();
  }

  /**
   * @return the filtered node count excluding nodes filtered by the user
   */
  protected int getFilteredNodeCount(ITreeNode parentNode) {
    if (getModel().getNodeFilters().size() == 0) {
      return parentNode.getChildNodeCount();
    }
    int filteredNodeCount = 0;
    for (ITreeNode node : parentNode.getChildNodes()) {
      if (node.isFilterAccepted() || node.isRejectedByUser()) {
        filteredNodeCount++;
      }
    }
    return filteredNodeCount;
  }

  protected AbstractEventBuffer<TreeEvent> eventBuffer() {
    return m_eventBuffer;
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
      ITreeNode row = m_treeNodes.get(jsonObject.getString("nodeId"));
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

  /**
   * Called by {@link #handleModelNodesUpdated(TreeEvent)} when it has detected that a virtual tree node was resolved to
   * a real node. Subclasses may override this method to put any updated properties to the JSON response.
   *
   * @param jsonNode
   *          {@link JSONObject} sent to the UI for the resolved node. Updated properties may be put in here.
   * @param nodeId
   *          The ID of the resolved node.
   * @param node
   *          The new, resolved node
   * @param cachedNode
   *          The old, virtual node
   */
  protected void putUpdatedPropertiesForResolvedNode(JSONObject jsonNode, String nodeId, ITreeNode node, IVirtualTreeNode virtualNode) {
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

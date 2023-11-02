/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.tree.AutoCheckStyle;
import org.eclipse.scout.rt.client.ui.basic.tree.CheckableStyle;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeUtility;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.visitor.DepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;
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

public class JsonTree<TREE extends ITree> extends AbstractJsonWidget<TREE> implements IJsonContextMenuOwner, IBinaryResourceConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(JsonTree.class);

  public static final String EVENT_NODES_INSERTED = "nodesInserted";
  public static final String EVENT_NODES_UPDATED = "nodesUpdated";
  public static final String EVENT_NODES_DELETED = "nodesDeleted";
  public static final String EVENT_ALL_CHILD_NODES_DELETED = "allChildNodesDeleted";
  public static final String EVENT_NODES_SELECTED = "nodesSelected";
  public static final String EVENT_NODE_CLICK = "nodeClick";
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

  /**
   * Keep the parent/child hierarchy to that nodes may be disposed properly. In case of delete events the model is
   * already updated, so it is not always possible anymore to visit all the child nodes.
   */
  private final Map<ITreeNode, Set<ITreeNode>> m_childNodes;
  private final Map<ITreeNode, ITreeNode> m_parentNodes;

  private final TreeEventFilter m_treeEventFilter;
  private final AbstractEventBuffer<TreeEvent> m_eventBuffer;
  private JsonContextMenu<IContextMenu> m_jsonContextMenu;
  private final JsonTreeListeners m_listeners = new JsonTreeListeners();

  public JsonTree(TREE model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_treeNodes = new HashMap<>();
    m_treeNodeIds = new HashMap<>();
    m_childNodes = new HashMap<>();
    m_parentNodes = new HashMap<>();
    m_treeEventFilter = new TreeEventFilter(this);
    m_eventBuffer = model.createEventBuffer();
  }

  @Override
  public String getObjectType() {
    return "Tree";
  }

  public JsonContextMenu<IContextMenu> getJsonContextMenu() {
    return m_jsonContextMenu;
  }

  @Override
  public void init() {
    super.init();

    // Replay missed events
    IEventHistory<TreeEvent> eventHistory = getModel().getEventHistory();
    if (eventHistory != null) {
      for (TreeEvent event : eventHistory.getRecentEvents()) {
        // Immediately execute events (no buffering), because this method is not called
        // from the model but from the JSON layer. If Response.toJson() is in progress,
        // adding this adapter to the list of buffered event providers would cause
        // an exception.
        processBufferedEvent(event);
      }
    }
  }

  @Override
  protected void initJsonProperties(TREE model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<>(ITree.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_CHECKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCheckable();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_MULTI_CHECK, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultiCheck();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_LAZY_EXPANDING_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLazyExpandingEnabled();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_AUTO_CHECK_CHILDREN, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoCheckChildNodes();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_AUTO_CHECK_STYLE, model) {
      @Override
      protected AutoCheckStyle modelValue() {
        return getModel().getAutoCheckStyle();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return ((AutoCheckStyle) value).name().toLowerCase();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_SCROLL_TO_SELECTION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollToSelection();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_DROP_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDropType();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_DROP_MAXIMUM_SIZE, model) {
      @Override
      protected Long modelValue() {
        return getModel().getDropMaximumSize();
      }
    });
    putJsonProperty(new JsonAdapterProperty<>(ITree.PROP_KEY_STROKES, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<>()).build();
      }

      @Override
      protected List<IKeyStroke> modelValue() {
        return getModel().getKeyStrokes();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_DISPLAY_STYLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayStyle();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_TOGGLE_BREADCRUMB_STYLE_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isToggleBreadcrumbStyleEnabled();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_CHECKABLE_STYLE, model) {
      @Override
      protected CheckableStyle modelValue() {
        return getModel().getCheckableStyle();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return ((CheckableStyle) value).name().toLowerCase();
      }
    });
    putJsonProperty(new JsonProperty<>(ITree.PROP_TEXT_FILTER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTextFilterEnabled();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    m_jsonContextMenu = createJsonContextMenu();
    m_jsonContextMenu.init();
    attachNodes(getTopLevelNodes(), true);
  }

  protected JsonContextMenu<IContextMenu> createJsonContextMenu() {
    return new JsonContextMenu<>(getModel().getContextMenu(), this);
  }

  @Override
  protected void disposeChildAdapters() {
    disposeAllNodes();
    m_jsonContextMenu.dispose();
    super.disposeChildAdapters();
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

  protected void attachNodeInternal(ITreeNode node) {
    // We create a node id because it can happen that we handle events
    // concerning nodes which have not yet been assigned a node id.
    // Rather than requiring callers to ensure that the nodes on which
    // their events operate exist, we create them here ourselves.
    getOrCreateNodeId(node);

    Set<ITreeNode> children = getChildNodes(node.getParentNode());
    children.add(node);
    m_childNodes.put(node.getParentNode(), children);
    m_parentNodes.put(node, node.getParentNode());
  }

  protected void attachNode(ITreeNode node, boolean attachChildren) {
    if (!isNodeAccepted(node)) {
      return;
    }

    attachNodeInternal(node);

    if (attachChildren) {
      attachNodes(node.getChildNodes(), true);
    }
  }

  protected void attachNodes(Collection<ITreeNode> nodes, boolean attachChildren) {
    for (ITreeNode node : nodes) {
      attachNode(node, attachChildren);
    }
  }

  /**
   * Removes all node mappings without querying the model.
   */
  protected void disposeAllNodes() {
    m_treeNodeIds.clear();
    m_treeNodes.clear();
    m_childNodes.clear();
    m_parentNodes.clear();
  }

  protected void disposeNode(ITreeNode node, boolean disposeChildren, Set<ITreeNode> disposedNodes) {
    if (disposeChildren) {
      disposeNodes(getChildNodes(node), true, disposedNodes);
    }
    String nodeId = m_treeNodeIds.get(node);
    m_treeNodeIds.remove(node);
    m_treeNodes.remove(nodeId);

    // Remove node from parent/child hierarchy maps.
    // The node will be removed from its parent childNodes list later in unlinkFromParentNode
    m_childNodes.remove(node);
    m_parentNodes.remove(node);
    disposedNodes.add(node);
  }

  /**
   * @return the child nodes of the given nodes which are kept by the map {@link #m_childNodes}. This method is
   *         typically used on delete operations because {@link ITreeNode#getChildNodes()} may not contain the deleted
   *         nodes anymore.
   * @see #m_childNodes
   */
  protected Set<ITreeNode> getChildNodes(ITreeNode node) {
    Set<ITreeNode> children = m_childNodes.get(node);
    if (children == null) {
      return new HashSet<>();
    }
    return children;
  }

  protected ITreeNode getParentNode(ITreeNode node) {
    return m_parentNodes.get(node);
  }

  /**
   * Removes the given node from the child list of the parent node ({@link #m_childNodes}). Does not remove it from the
   * {@link #m_parentNodes} list because it is not necessary as it will be done in
   * {@link #disposeNode(ITreeNode, boolean, Set)}.
   */
  protected void unlinkFromParentNode(ITreeNode node) {
    ITreeNode parentNode = getParentNode(node);
    Set<ITreeNode> childrenOfParent = getChildNodes(parentNode);
    childrenOfParent.remove(node);
  }

  protected void disposeNodes(Collection<ITreeNode> nodes, boolean disposeChildren, Set<ITreeNode> disposedNodes) {
    for (ITreeNode node : nodes) {
      disposeNode(node, disposeChildren, disposedNodes);
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    IChildNodeIndexLookup childIndexes = createChildNodeIndexLookup();
    JSONArray jsonNodes = treeNodesToJson(getTopLevelNodes(), childIndexes);
    putProperty(json, PROP_NODES, jsonNodes);
    putProperty(json, PROP_SELECTED_NODES, nodeIdsToJson(getModel().getSelectedNodes(), true, true));
    putProperty(json, PROP_MENUS, getJsonContextMenu().childActionsToJson());
    return json;
  }

  /**
   * If the given node has a parent node, the value for the property "childNodeIndex" is calculated and added to the
   * given JSON object.
   * <p>
   * Note that the calculated value may differ from the model's {@link ITreeNode#getChildNodeIndex()} value! This is
   * because not all model nodes are sent to the UI. The calculated value only counts nodes sent to the UI, e.g. a node
   * with childNodeIndex=50 may result in "childNodeIndex: 3" if 47 of the preceding nodes are filtered.
   */
  protected IChildNodeIndexLookup createChildNodeIndexLookup() {
    IdentityHashMap<ITreeNode, Integer> indexMap = new IdentityHashMap<>();
    return node -> {
      ITreeNode parentNode = node.getParentNode();
      if (parentNode == null) {
        return -1;
      }
      //probe cache
      Integer indexOrNull = indexMap.get(node);
      if (indexOrNull != null) {
        return indexOrNull;
      }
      //fill cache
      int childNodeIndex = 0;
      // Find the node in the parents childNodes list (skipping non-accepted nodes)
      for (ITreeNode childNode : parentNode.getChildNodes()) {
        // Only count accepted nodes
        if (isNodeAccepted(childNode)) {
          indexMap.put(childNode, childNodeIndex);
          childNodeIndex++;
        }
      }
      return ObjectUtility.nvl(indexMap.get(node), -1);
    };
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

  @SuppressWarnings("SwitchStatementWithTooFewBranches")
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
   * Applies node filters to the UI by converting them to NODES_DELETED and NODES_INSERTED events, respectively:
   * <ul>
   * <li>For every newly hidden node (i.e. a node that is currently visible on the UI) a NODES_DELETED event is created.
   * <li>For every newly visible node (i.e. a node that is currently invisible on the UI) a NODES_INSERTED event is
   * created.
   * </ul>
   * All new events are already grouped by the common parent node and are added to the event buffer, where they might be
   * further coalesced.
   */
  protected void applyFilterChangedEventToUiRec(List<ITreeNode> nodes) {
    Map<ITreeNode, List<ITreeNode>> nodesToInsertByParent = new HashMap<>();
    Map<ITreeNode, List<ITreeNode>> nodesToDeleteByParent = new HashMap<>();
    processFilterChangedEventForUiRec(Collections.singletonList(getModel().getRootNode()), nodesToInsertByParent, nodesToDeleteByParent);
    nodesToDeleteByParent.forEach((key, value) -> m_eventBuffer.add(new TreeEvent(getModel(), TreeEvent.TYPE_NODES_DELETED, key, value)));
    nodesToInsertByParent.forEach((key, value) -> m_eventBuffer.add(new TreeEvent(getModel(), TreeEvent.TYPE_NODES_INSERTED, key, value)));
  }

  /**
   * Recursively traverses through the given nodes (and its child nodes) and checks which of the model nodes are hidden
   * by tree filters.
   * <ul>
   * <li>newly hidden nodes (i.e. a node that is currently visible on the UI) are collected in the
   * {@code nodesToDeleteByParent} map.
   * <li>newly visible nodes (i.e. a node that is currently invisible on the UI) are collected in the
   * {@code nodesToInsertByParent} map.
   * </ul>
   * All affected nodes are grouped by parent node and event type. All new events are added to the event buffer, where
   * they might be coalesced later.
   */
  protected void processFilterChangedEventForUiRec(List<ITreeNode> nodes, Map<ITreeNode, List<ITreeNode>> nodesToInsertByParent, Map<ITreeNode, List<ITreeNode>> nodesToDeleteByParent) {
    for (ITreeNode node : nodes) {
      boolean processChildNodes = true;

      if (!isInvisibleRootNode(node) && node.getTree() != null) {
        String existingNodeId = optNodeId(node);
        if (node.isFilterAccepted()) {
          if (existingNodeId == null) {
            // Node is not filtered but JsonTree does not know it yet --> handle as insertion event
            nodesToInsertByParent.computeIfAbsent(node.getParentNode(), k -> new LinkedList<>()).add(node);
            // Stop recursion, because this node (including its child nodes) is already inserted
            processChildNodes = false;
          }
        }
        else if (!node.isRejectedByUser()) {
          if (existingNodeId != null) {
            // Node is filtered, but JsonTree has it in its list --> handle as deletion event
            nodesToDeleteByParent.computeIfAbsent(node.getParentNode(), k -> new LinkedList<>()).add(node);
          }
          // Stop recursion, because this node (including its child nodes) is already deleted
          processChildNodes = false;
        }
      }

      // Recursion
      if (processChildNodes) {
        processFilterChangedEventForUiRec(node.getChildNodes(), nodesToInsertByParent, nodesToDeleteByParent);
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
    // TODO [7.0] bsh: Tree | Events not yet implemented:
    // - TYPE_NODE_REQUEST_FOCUS
    // - TYPE_NODE_ENSURE_VISIBLE what is the difference to scroll_to_selection? delete in TreeEvent
    // - TYPE_NODES_DRAG_REQUEST
    // - TYPE_DRAG_FINISHED
    // - TYPE_NODE_DROP_ACTION, partly implemented with consumeBinaryResource(...)
    // - TYPE_NODE_DROP_TARGET_CHANGED
  }

  /**
   * Default impl. does nothing. Override this method to handle custom tree-events.
   */
  protected void handleModelOtherTreeEvent(TreeEvent event) {
    // empty default implementation
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
    Set<ITreeNode> acceptedNodes = new HashSet<>();
    attachNodes(event.getNodes(), true); // TODO [7.0] cgu: why not inside loop? attaching for rejected nodes?
    IChildNodeIndexLookup childIndexes = createChildNodeIndexLookup();
    JSONArray jsonNodes = treeNodesToJson(event.getNodes(), childIndexes, acceptedNodes);
    if (jsonNodes.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODES, jsonNodes);
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getOrCreateNodeId(event.getCommonParentNode()));
    addActionEvent(EVENT_NODES_INSERTED, jsonEvent);
    m_listeners.fireEvent(new JsonTreeEvent(this, JsonTreeEvent.TYPE_NODES_INSERTED, acceptedNodes));
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

    for (ITreeNode node : nodes) {
      unlinkFromParentNode(node);
    }
    Set<ITreeNode> disposedNodes = new HashSet<>();
    disposeNodes(nodes, true, disposedNodes);
    m_listeners.fireEvent(new JsonTreeEvent(this, JsonTreeEvent.TYPE_NODES_DELETED, disposedNodes));
  }

  protected void handleModelAllChildNodesDeleted(TreeEvent event) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_COMMON_PARENT_NODE_ID, getNodeId(event.getCommonParentNode()));
    addActionEvent(EVENT_ALL_CHILD_NODES_DELETED, jsonEvent);
    // Read the removed nodes from the event, because they are no longer contained in the model
    for (ITreeNode node : event.getChildNodes()) {
      unlinkFromParentNode(node);
    }
    Set<ITreeNode> disposedNodes = new HashSet<>();
    disposeNodes(event.getChildNodes(), true, disposedNodes);
    m_listeners.fireEvent(new JsonTreeEvent(this, JsonTreeEvent.TYPE_NODES_DELETED, disposedNodes));
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
      addJsonNodesChecked(jsonNodes, node);

      if (getModel().isAutoCheckChildNodes()) {
        for (ITreeNode childNode : collectChildNodesCheckedRec(node)) {
          addJsonNodesChecked(jsonNodes, childNode);
        }
      }
    }
    if (jsonNodes.length() == 0) {
      return;
    }

    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, PROP_NODES, jsonNodes);
    addActionEvent(EVENT_NODES_CHECKED, jsonEvent);
  }

  protected void addJsonNodesChecked(JSONArray jsonNodes, ITreeNode node) {
    if (!isNodeAccepted(node)) {
      return;
    }
    String nodeId = optNodeId(node);
    if (nodeId == null) { // Ignore nodes that are not yet sent to the UI (may happen due to asynchronous event processing)
      return;
    }
    jsonNodes.put(nodeCheckedToJson(nodeId, node));
  }

  protected JSONObject nodeCheckedToJson(String nodeId, ITreeNode node) {
    JSONObject json = new JSONObject();
    putProperty(json, "id", nodeId);
    putProperty(json, "checked", node.isChecked());
    return json;
  }

  protected Collection<ITreeNode> collectChildNodesCheckedRec(ITreeNode node) {
    P_ChildNodesVisitor visitor = new P_ChildNodesVisitor();
    TreeUtility.visitNodes(node.getChildNodes(), visitor);
    return visitor.getNodes();
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
    addActionEvent(EVENT_REQUEST_FOCUS).protect();
  }

  protected void handleModelScrollToSelection(TreeEvent event) {
    addActionEvent(EVENT_SCROLL_TO_SELECTION).protect();
  }

  @Override
  public void handleModelContextMenuChanged(FilteredJsonAdapterIds<?> filteredAdapters) {
    addPropertyChangeEvent(PROP_MENUS, filteredAdapters);
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
  public long getMaximumUploadSize() {
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
      // May be null if it's the invisible root node
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
   * @return the nodeId for the given node. Returns <code>null</code> if the node is the invisible root node or
   *         <code>null</code> itself. Use {@link #optNodeId(ITreeNode)} to prevent an exception when no nodeId could be
   *         found.
   * @throws UiException
   *           when no nodeId is found for the given node
   */
  public String getNodeId(ITreeNode node) {
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
  public String optNodeId(ITreeNode node) {
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

  protected void putChildNodeIndex(JSONObject json, ITreeNode node, IChildNodeIndexLookup childIndexes) {
    int childNodeIndex = childIndexes.childNodeIndexOf(node);
    if (childNodeIndex >= 0) {
      putProperty(json, "childNodeIndex", childNodeIndex);
    }
  }

  protected JSONArray treeNodesToJson(Collection<ITreeNode> nodes, IChildNodeIndexLookup childIndexes) {
    return treeNodesToJson(nodes, childIndexes, new HashSet<>());
  }

  protected JSONArray treeNodesToJson(Collection<ITreeNode> nodes, IChildNodeIndexLookup childIndexes, Set<ITreeNode> acceptedNodes) {
    JSONArray jsonNodes = new JSONArray();
    for (ITreeNode node : nodes) {
      if (isNodeAccepted(node)) {
        jsonNodes.put(treeNodeToJson(node, childIndexes, acceptedNodes));
        acceptedNodes.add(node);
      }
    }
    return jsonNodes;
  }

  protected JSONObject treeNodeToJson(ITreeNode node, IChildNodeIndexLookup childIndexes, Set<ITreeNode> acceptedNodes) {
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
    putChildNodeIndex(json, node, childIndexes);
    putCellProperties(json, node.getCell());
    JSONArray jsonChildNodes = new JSONArray();
    if (node.getChildNodeCount() > 0) {
      for (ITreeNode childNode : node.getChildNodes()) {
        if (!isNodeAccepted(childNode)) {
          continue;
        }
        acceptedNodes.add(childNode);
        jsonChildNodes.put(treeNodeToJson(childNode, childIndexes, acceptedNodes));
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
    return extractTreeNodes(nodeIds);
  }

  public List<ITreeNode> extractTreeNodes(JSONArray nodeIds) {
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
    if (EVENT_NODE_CLICK.equals(event.getType())) {
      handleUiNodeClick(event);
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
    CheckedInfo checkedInfo = jsonToCheckedInfo(event.getData());
    addTreeEventFilterCondition(TreeEvent.TYPE_NODES_CHECKED).setCheckedNodes(checkedInfo.getCheckedNodes(), checkedInfo.getUncheckedNodes());
    if (!checkedInfo.getCheckedNodes().isEmpty()) {
      getModel().getUIFacade().setNodesCheckedFromUI(checkedInfo.getCheckedNodes(), true);
    }
    if (!checkedInfo.getUncheckedNodes().isEmpty()) {
      getModel().getUIFacade().setNodesCheckedFromUI(checkedInfo.getUncheckedNodes(), false);
    }
  }

  protected void handleUiNodeClick(JsonEvent event) {
    String nodeId = event.getData().getString(PROP_NODE_ID);
    ITreeNode node = optTreeNodeForNodeId(nodeId);
    if (node == null) {
      LOG.info("Requested tree-node with ID {} doesn't exist. Skip nodeClicked event", nodeId);
      return;
    }
    getModel().getUIFacade().fireNodeClickFromUI(node, MouseButton.Left);
  }

  protected void handleUiNodeAction(JsonEvent event) {
    String nodeId = event.getData().getString(PROP_NODE_ID);
    ITreeNode node = optTreeNodeForNodeId(nodeId);
    if (node == null) {
      LOG.info("Requested tree-node with ID {} doesn't exist. Skip nodeAction event", nodeId);
      return;
    }
    getModel().getUIFacade().fireNodeActionFromUI(node);
  }

  protected void handleUiNodesSelected(JsonEvent event) {
    final JSONArray nodeIds = event.getData().getJSONArray(PROP_NODE_IDS);
    final List<ITreeNode> nodes = extractTreeNodes(nodeIds);
    if (nodes.isEmpty() && nodeIds.length() > 0) {
      // Ignore inconsistent selections from UI (probably an obsolete cached event)
      return;
    }
    if (nodes.size() == nodeIds.length()) {
      addTreeEventFilterCondition(TreeEvent.TYPE_NODES_SELECTED).setNodes(nodes);
    }
    getModel().getUIFacade().setNodesSelectedFromUI(nodes);
  }

  protected void handleUiNodeExpanded(JsonEvent event) {
    String nodeId = event.getData().getString(PROP_NODE_ID);
    ITreeNode node = optTreeNodeForNodeId(nodeId);
    if (node == null) {
      LOG.info("Requested tree-node with ID {} doesn't exist. Skip nodeExpanded event", nodeId);
      return;
    }
    boolean expanded = event.getData().getBoolean(PROP_EXPANDED);
    boolean lazy = event.getData().getBoolean(PROP_EXPANDED_LAZY);
    int eventType = expanded ? TreeEvent.TYPE_NODE_EXPANDED : TreeEvent.TYPE_NODE_COLLAPSED;
    addTreeEventFilterCondition(eventType).setNodes(CollectionUtility.arrayList(node));
    getModel().getUIFacade().setNodeExpandedFromUI(node, expanded, lazy);
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (ITree.PROP_DISPLAY_STYLE.equals(propertyName)) {
      String displayStyle = data.getString(propertyName);
      addPropertyEventFilterCondition(propertyName, displayStyle);
      getModel().getUIFacade().setDisplayStyleFromUI(displayStyle);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  /**
   * Ignore deleted or filtered nodes, because for the UI, they don't exist
   */
  public boolean isNodeAccepted(ITreeNode node) {
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
    if (getModel().getNodeFilters().isEmpty()) {
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

  protected TreeEventFilterCondition addTreeEventFilterCondition(int treeEventType) {
    TreeEventFilterCondition condition = new TreeEventFilterCondition(treeEventType);
    m_treeEventFilter.addCondition(condition);
    return condition;
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

  protected static class CheckedInfo {
    private final List<ITreeNode> m_allNodes = new ArrayList<>();
    private final List<ITreeNode> m_checkedNodes = new ArrayList<>();
    private final List<ITreeNode> m_uncheckedNodes = new ArrayList<>();

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

  public JsonTreeListeners listeners() {
    return m_listeners;
  }

  public void addListener(JsonTreeListener listener, Integer... eventTypes) {
    listeners().add(listener, false, eventTypes);
  }

  public void removeListener(JsonTreeListener listener, Integer... eventTypes) {
    listeners().remove(listener, eventTypes);
  }

  protected class P_TreeListener extends TreeAdapter {

    @Override
    public void treeChanged(final TreeEvent e) {
      ModelJobs.assertModelThread();
      handleModelTreeEvent(e);
    }
  }

  protected class P_ChildNodesVisitor extends DepthFirstTreeVisitor<ITreeNode> {

    private final Set<ITreeNode> m_nodes = new HashSet<>();

    @Override
    public TreeVisitResult preVisit(ITreeNode node, int level, int index) {
      m_nodes.add(node);
      return TreeVisitResult.CONTINUE;
    }

    public Set<ITreeNode> getNodes() {
      return m_nodes;
    }
  }
}

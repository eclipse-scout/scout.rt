/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  App, arrays, CellModel, ChildModelOf, defaultValues, Event, ModelAdapter, objects, RemoteEvent, scout, Tree, TreeDropEvent, TreeNode, TreeNodeActionEvent, TreeNodeClickEvent, TreeNodeExpandedEvent, TreeNodeModel, TreeNodesCheckedEvent,
  TreeNodesSelectedEvent
} from '../index';

export class TreeAdapter extends ModelAdapter {
  declare widget: Tree;

  constructor() {
    super();
    this._addRemoteProperties(['displayStyle']);
  }

  override _postCreateWidget() {
    super._postCreateWidget();
    this._autoCheckChildNodes();
  }

  protected _autoCheckChildNodes() {
    if (!this.widget.autoCheckChildren) {
      return;
    }

    // When the Java model provides a checked parent node having non-checked child nodes
    // then we want the child nodes to be auto checked.
    Tree.visitNodes((node: TreeNode, parentNode: TreeNode) => {
      if (node.checked) {
        this.widget.checkNodes(node, {checked: true});
        return true; // Skip subtree
      }
    }, this.widget.nodes, null);
  }

  protected _sendNodesSelected(nodeIds: string[], debounceSend: boolean) {
    let eventData = {
      nodeIds: nodeIds
    };

    // send delayed to avoid a lot of requests while selecting
    // coalesce: only send the latest selection changed event for a field
    this._send('nodesSelected', eventData, {
      delay: (debounceSend ? 250 : 0),
      coalesce: function(previous: RemoteEvent) {
        return this.target === previous.target && this.type === previous.type;
      }
    });
  }

  protected _onWidgetNodeClick(event: TreeNodeClickEvent) {
    this._send('nodeClick', {
      nodeId: event.node.id
    });
  }

  protected _onWidgetNodeAction(event: TreeNodeActionEvent) {
    this._send('nodeAction', {
      nodeId: event.node.id
    });
  }

  protected _onWidgetNodesSelected(event: TreeNodesSelectedEvent) {
    let nodeIds = this.widget.nodesToIds(this.widget.selectedNodes);
    this._sendNodesSelected(nodeIds, event.debounce);
  }

  protected _onWidgetNodeExpanded(event: TreeNodeExpandedEvent) {
    this._send('nodeExpanded', {
      nodeId: event.node.id,
      expanded: event.expanded,
      expandedLazy: event.expandedLazy
    });
  }

  protected _onWidgetNodesChecked(event: TreeNodesCheckedEvent) {
    this._sendNodesChecked(event.nodes);
  }

  protected _sendNodesChecked(nodes: TreeNode[]) {
    let data = {
      nodes: []
    };

    for (let i = 0; i < nodes.length; i++) {
      data.nodes.push({
        nodeId: nodes[i].id,
        checked: nodes[i].checked
      });
    }

    this._send('nodesChecked', data);
  }

  protected override _onWidgetEvent(event: Event<Tree>) {
    if (event.type === 'nodesSelected') {
      this._onWidgetNodesSelected(event as TreeNodesSelectedEvent);
    } else if (event.type === 'nodeClick') {
      this._onWidgetNodeClick(event as TreeNodeClickEvent);
    } else if (event.type === 'nodeAction') {
      this._onWidgetNodeAction(event as TreeNodeActionEvent);
    } else if (event.type === 'nodeExpanded') {
      this._onWidgetNodeExpanded(event as TreeNodeExpandedEvent);
    } else if (event.type === 'nodesChecked') {
      this._onWidgetNodesChecked(event as TreeNodesCheckedEvent);
    } else if (event.type === 'drop' && this.widget.dragAndDropHandler) {
      this.widget.dragAndDropHandler.uploadFiles(event as TreeDropEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'nodesInserted') {
      this._onNodesInserted(event.nodes, event.commonParentNodeId);
    } else if (event.type === 'nodesUpdated') {
      this._onNodesUpdated(event.nodes);
    } else if (event.type === 'nodesDeleted') {
      this._onNodesDeleted(event.nodeIds, event.commonParentNodeId);
    } else if (event.type === 'allChildNodesDeleted') {
      this._onAllChildNodesDeleted(event.commonParentNodeId);
    } else if (event.type === 'nodesSelected') {
      this._onNodesSelected(event.nodeIds);
    } else if (event.type === 'nodeExpanded') {
      this._onNodeExpanded(event.nodeId, event as any);
    } else if (event.type === 'nodeChanged') {
      this._onNodeChanged(event.nodeId, event as any);
    } else if (event.type === 'nodesChecked') {
      this._onNodesChecked(event.nodes);
    } else if (event.type === 'childNodeOrderChanged') {
      this._onChildNodeOrderChanged(event.childNodeIds, event.parentNodeId);
    } else if (event.type === 'requestFocus') {
      this._onRequestFocus();
    } else if (event.type === 'scrollToSelection') {
      this._onScrollToSelection();
    } else {
      super.onModelAction(event);
    }
  }

  protected _onNodesInserted(nodes: TreeNode[] | TreeNode, parentNodeId: string) {
    let parentNode: TreeNode;
    if (parentNodeId !== null && parentNodeId !== undefined) {
      parentNode = this.widget.nodesMap[parentNodeId];
      if (!parentNode) {
        throw new Error('Parent node could not be found. Id: ' + parentNodeId);
      }
    }
    this.widget.insertNodes(nodes, parentNode);
  }

  protected _onNodesUpdated(nodes: TreeNode | TreeNode[]) {
    this.widget.updateNodes(nodes);
  }

  protected _onNodesDeleted(nodeIds: string[], parentNodeId: string) {
    // noinspection DuplicatedCode
    let parentNode: TreeNode;
    if (parentNodeId !== null && parentNodeId !== undefined) {
      parentNode = this.widget.nodesMap[parentNodeId];
      if (!parentNode) {
        throw new Error('Parent node could not be found. Id: ' + parentNodeId);
      }
    }
    this.addFilterForWidgetEventType('nodesSelected');
    this.addFilterForWidgetEventType('nodesChecked');
    let nodes = this.widget.nodesByIds(nodeIds);
    this.widget.deleteNodes(nodes, parentNode);
  }

  protected _onAllChildNodesDeleted(parentNodeId: string) {
    // noinspection DuplicatedCode
    let parentNode: TreeNode;
    if (parentNodeId !== null && parentNodeId !== undefined) {
      parentNode = this.widget.nodesMap[parentNodeId];
      if (!parentNode) {
        throw new Error('Parent node could not be found. Id: ' + parentNodeId);
      }
    }
    this.addFilterForWidgetEventType('nodesSelected');
    this.addFilterForWidgetEventType('nodesChecked');
    this.widget.deleteAllChildNodes(parentNode);
  }

  protected _onNodesSelected(nodeIds: string[]) {
    this.addFilterForWidgetEvent(widgetEvent => widgetEvent.type === 'nodesSelected' && arrays.equals(nodeIds, this.widget.nodesToIds(this.widget.selectedNodes)));
    let nodes = this.widget.nodesByIds(nodeIds);
    this.widget.selectNodes(nodes);
  }

  /**
   * @param event.expanded true, to expand the node
   * @param event.expandedLazy true, to expand the nodes lazily
   * @param event.recursive true, to expand the descendant nodes as well
   */
  protected _onNodeExpanded(nodeId: string, event: RemoteEvent & { expanded: boolean; expandedLazy: boolean; recursive?: boolean }) {
    let node = this.widget.nodesMap[nodeId],
      options = {
        lazy: event.expandedLazy
      };

    let affectedNodesMap = objects.createMap() as Record<string, boolean>;
    affectedNodesMap[nodeId] = true;
    if (event.recursive) {
      Tree.visitNodes(n => {
        affectedNodesMap[n.id] = true;
      }, node.childNodes);
    }
    this.addFilterForWidgetEvent((widgetEvent: TreeNodeExpandedEvent) => {
      return widgetEvent.type === 'nodeExpanded' &&
        affectedNodesMap[widgetEvent.node.id] &&
        event.expanded === widgetEvent.expanded &&
        event.expandedLazy === widgetEvent.expandedLazy;
    });

    this.widget.setNodeExpanded(node, event.expanded, options);
    if (event.recursive) {
      this.widget.setNodeExpandedRecursive(node.childNodes, event.expanded, options);
    }
  }

  protected _onNodeChanged(nodeId: string, cell: RemoteEvent & CellModel) {
    let node = this.widget.nodesMap[nodeId];

    defaultValues.applyTo(cell, 'TreeNode');
    node.text = cell.text;
    node.cssClass = cell.cssClass;
    node.iconId = cell.iconId;
    node.tooltipText = cell.tooltipText;
    node.foregroundColor = cell.foregroundColor;
    node.backgroundColor = cell.backgroundColor;
    node.font = cell.font;
    node.htmlEnabled = cell.htmlEnabled;

    this.widget.changeNode(node);
  }

  protected _onNodesChecked(nodes: { id: string; checked: boolean }[]) {
    let checkedNodes: TreeNode[] = [],
      uncheckedNodes: TreeNode[] = [];

    nodes.forEach(nodeData => {
      let node = this.widget.nodeById(nodeData.id);
      if (nodeData.checked) {
        checkedNodes.push(node);
      } else {
        uncheckedNodes.push(node);
      }
    });

    this.widget.checkNodes(checkedNodes, {
      checked: true,
      checkOnlyEnabled: false
    });
    this.widget.uncheckNodes(uncheckedNodes, {
      checkOnlyEnabled: false
    });
  }

  protected _onChildNodeOrderChanged(childNodeIds: string[], parentNodeId: string) {
    let parentNode = this.widget.nodeById(parentNodeId);
    let nodes = this.widget.nodesByIds(childNodeIds);
    this.widget.updateNodeOrder(nodes, parentNode);
  }

  protected _onRequestFocus() {
    this.widget.focus();
  }

  protected _onScrollToSelection() {
    this.widget.revealSelection();
  }

  protected _initNodeModel(nodeModel?: TreeNodeModel): ChildModelOf<TreeNode> {
    nodeModel = nodeModel || {};
    nodeModel.objectType = scout.nvl(nodeModel.objectType, this._getDefaultNodeObjectType());
    nodeModel.remote = true; // mark nodes that have a server representation (useful to skip JS logic that is already done on the server)
    defaultValues.applyTo(nodeModel);
    return nodeModel as ChildModelOf<TreeNode>;
  }

  protected _getDefaultNodeObjectType(): string {
    return 'TreeNode';
  }

  protected static _updateMarkChildrenCheckedRemote(this: Tree & { modelAdapter: TreeAdapter; _updateMarkChildrenCheckedOrig }, node: TreeNode) {
    // In autoCheckChildren mode, don't change the checked state of parent nodes while the tree is initializing if nodes come from Java.
    // This is necessary to set the checked state of the child nodes correctly, see _autoCheckChildNodes.
    if (this.modelAdapter && this.autoCheckChildren && !this.initialized) {
      return;
    }
    return this._updateMarkChildrenCheckedOrig(node);
  }

  /**
   * 'this' in this function refers to the Tree
   */
  protected static _createTreeNodeRemote(this: Tree & { modelAdapter: TreeAdapter; _createTreeNodeOrig }, nodeModel: TreeNodeModel) {
    if (this.modelAdapter) {
      nodeModel = this.modelAdapter._initNodeModel(nodeModel);
    }
    return this._createTreeNodeOrig(nodeModel);
  }

  protected static resolveTextKeysRemote(this: TreeNode & { remote: boolean; resolveTextKeysOrig }, properties: string[]) {
    if (this.remote) {
      // Never resolve '${textKey:...}' references in texts from the server
      return;
    }
    return this.resolveTextKeysOrig(properties);
  }

  /**
   * Static method to modify the prototype of Tree.
   */
  static modifyTreePrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(Tree, '_createTreeNode', TreeAdapter._createTreeNodeRemote, true);
    objects.replacePrototypeFunction(Tree, '_updateMarkChildrenChecked', TreeAdapter._updateMarkChildrenCheckedRemote, true);

    objects.replacePrototypeFunction(TreeNode, 'resolveTextKeys', TreeAdapter.resolveTextKeysRemote, true);
  }
}

App.addListener('bootstrap', TreeAdapter.modifyTreePrototype);

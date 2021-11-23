/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, defaultValues, ModelAdapter, objects, Tree} from '../index';

export default class TreeAdapter extends ModelAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['displayStyle']);
  }

  _sendNodesSelected(nodeIds, debounceSend) {
    let eventData = {
      nodeIds: nodeIds
    };

    // send delayed to avoid a lot of requests while selecting
    // coalesce: only send the latest selection changed event for a field
    this._send('nodesSelected', eventData, {
      delay: (debounceSend ? 250 : 0),
      coalesce: function(previous) {
        return this.target === previous.target && this.type === previous.type;
      }
    });
  }

  _onWidgetNodeClick(event) {
    this._send('nodeClick', {
      nodeId: event.node.id
    });
  }

  _onWidgetNodeAction(event) {
    this._send('nodeAction', {
      nodeId: event.node.id
    });
  }

  _onWidgetNodesSelected(event) {
    let nodeIds = this.widget._nodesToIds(this.widget.selectedNodes);
    this._sendNodesSelected(nodeIds, event.debounce);
  }

  _onWidgetNodeExpanded(event) {
    this._send('nodeExpanded', {
      nodeId: event.node.id,
      expanded: event.expanded,
      expandedLazy: event.expandedLazy
    });
  }

  _onWidgetNodesChecked(event) {
    this._sendNodesChecked(event.nodes);
  }

  _sendNodesChecked(nodes) {
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

  _onWidgetEvent(event) {
    if (event.type === 'nodesSelected') {
      this._onWidgetNodesSelected(event);
    } else if (event.type === 'nodeClick') {
      this._onWidgetNodeClick(event);
    } else if (event.type === 'nodeAction') {
      this._onWidgetNodeAction(event);
    } else if (event.type === 'nodeExpanded') {
      this._onWidgetNodeExpanded(event);
    } else if (event.type === 'nodesChecked') {
      this._onWidgetNodesChecked(event);
    } else if (event.type === 'drop' && this.widget.dragAndDropHandler) {
      this.widget.dragAndDropHandler.uploadFiles(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  onModelAction(event) {
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
      this._onNodeExpanded(event.nodeId, event);
    } else if (event.type === 'nodeChanged') {
      this._onNodeChanged(event.nodeId, event);
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

  _onNodesInserted(nodes, parentNodeId) {
    let parentNode;
    if (parentNodeId !== null && parentNodeId !== undefined) {
      parentNode = this.widget.nodesMap[parentNodeId];
      if (!parentNode) {
        throw new Error('Parent node could not be found. Id: ' + parentNodeId);
      }
    }
    this.widget.insertNodes(nodes, parentNode);
  }

  _onNodesUpdated(nodes) {
    this.widget.updateNodes(nodes);
  }

  _onNodesDeleted(nodeIds, parentNodeId) {
    let parentNode;
    if (parentNodeId !== null && parentNodeId !== undefined) {
      parentNode = this.widget.nodesMap[parentNodeId];
      if (!parentNode) {
        throw new Error('Parent node could not be found. Id: ' + parentNodeId);
      }
    }
    this.addFilterForWidgetEventType('nodesSelected');
    this.addFilterForWidgetEventType('nodesChecked');
    let nodes = this.widget._nodesByIds(nodeIds);
    this.widget.deleteNodes(nodes, parentNode);
  }

  _onAllChildNodesDeleted(parentNodeId) {
    let parentNode;
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

  _onNodesSelected(nodeIds) {
    this.addFilterForWidgetEvent(widgetEvent => {
      return widgetEvent.type === 'nodesSelected' &&
        arrays.equals(nodeIds, this.widget._nodesToIds(this.widget.selectedNodes));
    });
    let nodes = this.widget._nodesByIds(nodeIds);
    this.widget.selectNodes(nodes);
  }

  /**
   * @parem event.expanded true, to expand the node
   * @param event.expandedLazy true, to expand the nodes lazily
   * @param event.recursive true, to expand the descendant nodes as well
   */
  _onNodeExpanded(nodeId, event) {
    let node = this.widget.nodesMap[nodeId],
      options = {
        lazy: event.expandedLazy
      };

    let affectedNodesMap = objects.createMap();
    affectedNodesMap[nodeId] = true;
    if (event.recursive) {
      Tree.visitNodes(n => {
        affectedNodesMap[n.id] = true;
      }, node.childNodes);
    }
    this.addFilterForWidgetEvent(widgetEvent => {
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

  // noinspection DuplicatedCode
  _onNodeChanged(nodeId, cell) {
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

  _onNodesChecked(nodes) {
    let checkedNodes = [],
      uncheckedNodes = [];

    nodes.forEach(function(nodeData) {
      let node = this.widget._nodeById(nodeData.id);
      if (nodeData.checked) {
        checkedNodes.push(node);
      } else {
        uncheckedNodes.push(node);
      }
    }, this);

    this.addFilterForWidgetEventType('nodesChecked');

    this.widget.checkNodes(checkedNodes, {
      checked: true,
      checkChildren: false,
      checkOnlyEnabled: false
    });
    this.widget.uncheckNodes(uncheckedNodes, {
      checkChildren: false,
      checkOnlyEnabled: false
    });
  }

  _onChildNodeOrderChanged(childNodeIds, parentNodeId) {
    let parentNode = this.widget._nodeById([parentNodeId]);
    let nodes = this.widget._nodesByIds(childNodeIds);
    this.widget.updateNodeOrder(nodes, parentNode);
  }

  _onRequestFocus() {
    this.widget.focus();
  }

  _onScrollToSelection() {
    this.widget.revealSelection();
  }
}

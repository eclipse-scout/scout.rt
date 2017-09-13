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
scout.TreeAdapter = function() {
  scout.TreeAdapter.parent.call(this);
  this._addAdapterProperties(['menus', 'keyStrokes']);
  this._addRemoteProperties(['displayStyle']);
};
scout.inherits(scout.TreeAdapter, scout.ModelAdapter);

scout.TreeAdapter.prototype._sendNodesSelected = function(nodeIds, debounceSend) {
  var eventData = {
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
};

scout.TreeAdapter.prototype._onWidgetNodeClicked = function(event) {
  this._send('nodeClicked', {
    nodeId: event.node.id
  });
};

scout.TreeAdapter.prototype._onWidgetNodeAction = function(event) {
  this._send('nodeAction', {
    nodeId: event.node.id
  });
};

scout.TreeAdapter.prototype._onWidgetNodesSelected = function(event) {
  var nodeIds = this.widget._nodesToIds(this.widget.selectedNodes);
  this._sendNodesSelected(nodeIds, event.debounce);
};

scout.TreeAdapter.prototype._onWidgetNodeExpanded = function(event) {
  this._send('nodeExpanded', {
    nodeId: event.node.id,
    expanded: event.expanded,
    expandedLazy: event.expandedLazy
  });
};

scout.TreeAdapter.prototype._onWidgetNodesChecked = function(event) {
  this._sendNodesChecked(event.nodes);
};

scout.TreeAdapter.prototype._sendNodesChecked = function(nodes) {
  var data = {
    nodes: []
  };

  for (var i = 0; i < nodes.length; i++) {
    data.nodes.push({
      nodeId: nodes[i].id,
      checked: nodes[i].checked
    });
  }

  this._send('nodesChecked', data);
};

scout.TreeAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'nodesSelected') {
    this._onWidgetNodesSelected(event);
  } else if (event.type === 'nodeClicked') {
    this._onWidgetNodeClicked(event);
  } else if (event.type === 'nodeAction') {
    this._onWidgetNodeAction(event);
  } else if (event.type === 'nodeExpanded') {
    this._onWidgetNodeExpanded(event);
  } else if (event.type === 'nodesChecked') {
    this._onWidgetNodesChecked(event);
  } else {
    scout.TreeAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.TreeAdapter.prototype.onModelAction = function(event) {
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
    scout.TreeAdapter.parent.prototype.onModelAction.call(this, event);
  }
};

scout.TreeAdapter.prototype._onNodesInserted = function(nodes, parentNodeId) {
  var parentNode;
  if (parentNodeId !== null && parentNodeId !== undefined) {
    parentNode = this.widget.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  this.widget.insertNodes(nodes, parentNode);
};

scout.TreeAdapter.prototype._onNodesUpdated = function(nodes) {
  this.widget.updateNodes(nodes);
};

scout.TreeAdapter.prototype._onNodesDeleted = function(nodeIds, parentNodeId) {
  var parentNode;
  if (parentNodeId !== null && parentNodeId !== undefined) {
    parentNode = this.widget.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  var nodes = this.widget._nodesByIds(nodeIds);
  this.widget.deleteNodes(nodes, parentNode);
};

scout.TreeAdapter.prototype._onAllChildNodesDeleted = function(parentNodeId) {
  var parentNode;
  if (parentNodeId !== null && parentNodeId !== undefined) {
    parentNode = this.widget.nodesMap[parentNodeId];
    if (!parentNode) {
      throw new Error('Parent node could not be found. Id: ' + parentNodeId);
    }
  }
  this.widget.deleteAllChildNodes(parentNode);
};

scout.TreeAdapter.prototype._onNodesSelected = function(nodeIds) {
  this.addFilterForWidgetEvent(function(widgetEvent) {
    return widgetEvent.type === 'nodesSelected' &&
      scout.arrays.equals(nodeIds, this.widget._nodesToIds(this.widget.selectedNodes));
  }.bind(this));
  var nodes = this.widget._nodesByIds(nodeIds);
  this.widget.selectNodes(nodes);
};

scout.TreeAdapter.prototype._onNodeExpanded = function(nodeId, event) {
  var node = this.widget.nodesMap[nodeId],
    options = {
      lazy: event.expandedLazy
    };
  this.addFilterForWidgetEvent(function(widgetEvent) {
    return widgetEvent.type === 'nodeExpanded' &&
      nodeId === widgetEvent.node.id &&
      event.expanded === widgetEvent.expanded &&
      event.expandedLazy === widgetEvent.expandedLazy;
  }.bind(this));
  this.widget.setNodeExpanded(node, event.expanded, options);
  if (event.recursive) {
    this.widget.setNodeExpandedRecursive(node.childNodes, event.expanded, options);
  }
};

scout.TreeAdapter.prototype._onNodeChanged = function(nodeId, cell) {
  var node = this.widget.nodesMap[nodeId];

  scout.defaultValues.applyTo(cell, 'TreeNode');
  node.text = cell.text;
  node.cssClass = cell.cssClass;
  node.iconId = cell.iconId;
  node.tooltipText = cell.tooltipText;
  node.foregroundColor = cell.foregroundColor;
  node.backgroundColor = cell.backgroundColor;
  node.font = cell.font;

  this.widget.changeNode(node);
};

scout.TreeAdapter.prototype._onNodesChecked = function(nodes) {
  var checkedNodes = [],
    uncheckedNodes = [];

  nodes.forEach(function(nodeData) {
    var node = this.widget._nodeById(nodeData.id);
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
};

scout.TreeAdapter.prototype._onChildNodeOrderChanged = function(childNodeIds, parentNodeId) {
  var parentNode = this.widget._nodeById([parentNodeId]);
  var nodes = this.widget._nodesByIds(childNodeIds);
  this.widget.updateNodeOrder(nodes, parentNode);
};

scout.TreeAdapter.prototype._onRequestFocus = function() {
  this.widget.requestFocus();
};

scout.TreeAdapter.prototype._onScrollToSelection = function() {
  this.widget.revealSelection();
};

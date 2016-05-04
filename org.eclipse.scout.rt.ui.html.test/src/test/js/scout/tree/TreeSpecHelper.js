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
var TreeSpecHelper = function(session) {
  this.session = session;
};

TreeSpecHelper.prototype.createModel = function(nodes) {
  var model = createSimpleModel('Tree', this.session);

  if (nodes) {
    model.nodes = nodes;
  }
  model.enabled = true;
  return model;
};

TreeSpecHelper.prototype.createModelFixture = function(nodeCount, depth, expanded) {
  return this.createModel(this.createModelNodes(nodeCount, depth, expanded));
};

TreeSpecHelper.prototype.createModelNode = function(id, text, position) {
  return {
    id: id + '' || scout.objectFactory.createUniqueId(),
    text: text,
    childNodeIndex: position ? position : 0,
    enabled: true,
    checked: false
  };
};

TreeSpecHelper.prototype.createModelNodes = function(nodeCount, depth, expanded) {
  return this.createModelNodesInternal(nodeCount, depth, expanded);
};

TreeSpecHelper.prototype.createModelNodesInternal = function(nodeCount, depth, expanded, parentNode) {
  if (!nodeCount) {
    return;
  }

  var nodes = [],
    nodeId;
  if (!depth) {
    depth = 0;
  }
  for (var i = 0; i < nodeCount; i++) {
    nodeId = i;
    if (parentNode) {
      nodeId = parentNode.id + '_' + nodeId;
    }
    nodes[i] = this.createModelNode(nodeId, 'node ' + nodeId);
    nodes[i].expanded = expanded;
    if (depth > 0) {
      nodes[i].childNodes = this.createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
    }
  }
  return nodes;
};

TreeSpecHelper.prototype.createTree = function(model) {
  var tree = new scout.Tree();
  tree.init(model);
  return tree;
};

TreeSpecHelper.prototype.createCompactTree = function(model) {
  var tree = new scout.CompactTree();
  tree.init(model);
  return tree;
};

TreeSpecHelper.prototype.findAllNodes = function(tree) {
  return tree.$container.find('.tree-node');
};

TreeSpecHelper.prototype.createNodeExpandedEvent = function(model, nodeId, expanded) {
  return {
    target: model.id,
    nodeId: nodeId,
    expanded: expanded,
    type: 'nodeExpanded'
  };
};

TreeSpecHelper.prototype.selectNodesAndAssert = function(tree, nodes) {
  tree.selectNodes(nodes);
  this.assertSelection(tree, nodes);
};

TreeSpecHelper.prototype.assertSelection = function(tree, nodes) {
  var $selectedNodes = tree.$selectedNodes();
  expect($selectedNodes.length).toBe(nodes.length);

  var selectedNodes = [];
  $selectedNodes.each(function() {
    selectedNodes.push($(this).data('node'));
  });

  expect(scout.arrays.equalsIgnoreOrder(nodes, selectedNodes)).toBeTruthy();
  expect(scout.arrays.equalsIgnoreOrder(nodes, tree.selectedNodes)).toBeTruthy();
};

TreeSpecHelper.prototype.createNodesSelectedEvent = function(model, nodeIds) {
  return {
    target: model.id,
    nodeIds: nodeIds,
    type: 'nodesSelected'
  };
};

TreeSpecHelper.prototype.createNodesInsertedEvent = function(model, nodes, commonParentNodeId) {
  return {
    target: model.id,
    commonParentNodeId: commonParentNodeId,
    nodes: nodes,
    type: 'nodesInserted'
  };
};

TreeSpecHelper.prototype.createNodesInsertedEventTopNode = function(model, nodes) {
  return {
    target: model.id,
    nodes: nodes,
    type: 'nodesInserted'
  };
};

TreeSpecHelper.prototype.createNodesDeletedEvent = function(model, nodeIds, commonParentNodeId) {
  return {
    target: model.id,
    commonParentNodeId: commonParentNodeId,
    nodeIds: nodeIds,
    type: 'nodesDeleted'
  };
};

TreeSpecHelper.prototype.createAllChildNodesDeletedEvent = function(model, commonParentNodeId) {
  return {
    target: model.id,
    commonParentNodeId: commonParentNodeId,
    type: 'allChildNodesDeleted'
  };
};

TreeSpecHelper.prototype.createNodeChangedEvent = function(model, nodeId) {
  return {
    target: model.id,
    nodeId: nodeId,
    type: 'nodeChanged'
  };
};

TreeSpecHelper.prototype.createNodesUpdatedEvent = function(model, nodes) {
  return {
    target: model.id,
    nodes: nodes,
    type: 'nodesUpdated'
  };
};

TreeSpecHelper.prototype.createTreeEnabledEvent = function(model, enabled) {
  return {
    target: model.id,
    type: 'property',
    properties: {
      enabled: enabled
    }
  };
};

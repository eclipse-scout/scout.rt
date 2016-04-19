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
scout.OutlineSpecHelper = function(session) {
  this.session = session;
};

scout.OutlineSpecHelper.prototype.createModelFixture = function(nodeCount, depth, expanded) {
  return this.createModel(this.createModelNodes(nodeCount, depth, expanded));
};

scout.OutlineSpecHelper.prototype.createModel = function(nodes) {
  var model = createSimpleModel('Outline', this.session);

  if (nodes) {
    model.nodes = nodes;
  }

  return model;
};

scout.OutlineSpecHelper.prototype.createModelNode = function (id, text) {
  return {
    "id": id,
    "text": text
  };
};

scout.OutlineSpecHelper.prototype.createModelNodes = function (nodeCount, depth, expanded) {
  return this.createModelNodesInternal(nodeCount, depth, expanded);
};

scout.OutlineSpecHelper.prototype.createModelNodesInternal = function(nodeCount, depth, expanded, parentNode) {
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
    nodes[i] = this.createModelNode(nodeId, 'node ' + i);
    nodes[i].expanded = expanded;
    if (depth > 0) {
      nodes[i].childNodes = this.createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
    }
  }
  return nodes;
};

scout.OutlineSpecHelper.prototype.createOutline = function(model) {
  var tree = new scout.Outline();
  tree.init(model);
  return tree;
};

/**
 * Creates an outline with 3 nodes, the first node has a visible detail form
 */
scout.OutlineSpecHelper.prototype.createOutlineWithOneDetailForm = function() {
  var model = this.createModelFixture(3, 2, true);
  var outline = this.createOutline(model);
  var node = model.nodes[0];
  node.detailForm = new scout.FormSpecHelper(this.session).createFormWithOneField();
  node.detailFormVisible = true;
  return outline;
};

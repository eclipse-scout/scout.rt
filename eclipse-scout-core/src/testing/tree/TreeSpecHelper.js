/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, CompactTree, ObjectFactory, Tree, TreeAdapter} from '../../index';
import $ from 'jquery';

export default class TreeSpecHelper {

  constructor(session) {
    this.session = session;
  }

  createModel(nodes) {
    let model = createSimpleModel('Tree', this.session);

    if (nodes) {
      model.nodes = nodes;
    }
    model.enabled = true;
    return model;
  }

  createModelFixture(nodeCount, depth, expanded) {
    return this.createModel(this.createModelNodes(nodeCount, depth, expanded));
  }

  createModelNode(id, text, position) {
    return {
      id: id + '' || ObjectFactory.get().createUniqueId(),
      text: text,
      childNodeIndex: position ? position : 0,
      enabled: true,
      checked: false
    };
  }

  createModelNodes(nodeCount, depth, expanded) {
    return this.createModelNodesInternal(nodeCount, depth, expanded);
  }

  createModelNodesInternal(nodeCount, depth, expanded, parentNode) {
    if (!nodeCount) {
      return;
    }

    let nodes = [],
      nodeId;
    if (!depth) {
      depth = 0;
    }
    for (let i = 0; i < nodeCount; i++) {
      nodeId = i;
      if (parentNode) {
        nodeId = parentNode.id + '_' + nodeId;
      }
      nodes[i] = this.createModelNode(nodeId, 'node ' + nodeId, i);
      nodes[i].expanded = expanded;
      if (depth > 0) {
        nodes[i].childNodes = this.createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
      }
    }
    return nodes;
  }

  createTree(model) {
    let defaults = {
      parent: this.session.desktop
    };
    model = $.extend({}, defaults, model);
    let tree = new Tree();
    tree.init(model);
    return tree;
  }

  createTreeAdapter(model) {
    let adapter = new TreeAdapter();
    adapter.init(model);
    return adapter;
  }

  createCompactTree(model) {
    let tree = new CompactTree();
    tree.init(model);
    return tree;
  }

  createCompactTreeAdapter(model) {
    model.objectType = 'Tree:Compact';
    let tree = new TreeAdapter();
    tree.init(model);
    return tree;
  }

  findAllNodes(tree) {
    return tree.$container.find('.tree-node');
  }

  createNodeExpandedEvent(model, nodeId, expanded) {
    return {
      target: model.id,
      nodeId: nodeId,
      expanded: expanded,
      type: 'nodeExpanded'
    };
  }

  selectNodesAndAssert(tree, nodes) {
    tree.selectNodes(nodes);
    this.assertSelection(tree, nodes);
  }

  assertSelection(tree, nodes) {
    let $selectedNodes = tree.$selectedNodes();
    expect($selectedNodes.length).toBe(nodes.length);

    let selectedNodes = [];
    $selectedNodes.each(function() {
      selectedNodes.push($(this).data('node'));
    });

    expect(arrays.equalsIgnoreOrder(nodes, selectedNodes)).toBeTruthy();
    expect(arrays.equalsIgnoreOrder(nodes, tree.selectedNodes)).toBeTruthy();
  }

  createNodesSelectedEvent(model, nodeIds) {
    return {
      target: model.id,
      nodeIds: nodeIds,
      type: 'nodesSelected'
    };
  }

  createNodesInsertedEvent(model, nodes, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      nodes: nodes,
      type: 'nodesInserted'
    };
  }

  createNodesInsertedEventTopNode(model, nodes) {
    return {
      target: model.id,
      nodes: nodes,
      type: 'nodesInserted'
    };
  }

  createNodesDeletedEvent(model, nodeIds, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      nodeIds: nodeIds,
      type: 'nodesDeleted'
    };
  }

  createAllChildNodesDeletedEvent(model, commonParentNodeId) {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      type: 'allChildNodesDeleted'
    };
  }

  createNodeChangedEvent(model, nodeId) {
    return {
      target: model.id,
      nodeId: nodeId,
      type: 'nodeChanged'
    };
  }

  createNodesUpdatedEvent(model, nodes) {
    return {
      target: model.id,
      nodes: nodes,
      type: 'nodesUpdated'
    };
  }

  createChildNodeOrderChangedEvent(model, childNodeIds, parentNodeId) {
    return {
      target: model.id,
      parentNodeId: parentNodeId,
      childNodeIds: childNodeIds,
      type: 'childNodeOrderChanged'
    };
  }

  createTreeEnabledEvent(model, enabled) {
    return {
      target: model.id,
      type: 'property',
      properties: {
        enabled: enabled
      }
    };
  }
}

/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, CompactTree, CompactTreeAdapter, InitModelOf, ModelAdapter, ObjectIdProvider, ObjectType, ObjectWithType, Range, RemoteEvent, Session, SomeRequired, Tree, TreeAdapter, TreeModel, TreeNode, TreeNodeModel, Widget
} from '../../index';
import {SpecTree} from '../index';
import $ from 'jquery';

export class TreeSpecHelper {
  session: Session;

  constructor(session: Session) {
    this.session = session;
  }

  createModel(nodes: TreeNodeModel[]): SpecTreeModel {
    let model = createSimpleModel('Tree', this.session) as TreeModel & { objectType: ObjectType<Tree> };

    if (nodes) {
      model.nodes = nodes;
    }
    return model as TreeModel & { id: string; objectType: string; parent: Widget; session: Session };
  }

  createModelFixture(nodeCount?: number, depth?: number, expanded?: boolean): SpecTreeModel {
    return this.createModel(this.createModelNodes(nodeCount, depth, {expanded: expanded}));
  }

  createModelNode(id?: string, text?: string, model?: TreeNodeModel): TreeNodeModel {
    return $.extend({
      id: id || ObjectIdProvider.get().createUiSeqId(),
      text: text
    }, model);
  }

  createModelNodes(nodeCount?: number, depth?: number, model?: TreeNodeModel): TreeNodeModel[] {
    return this.createModelNodesInternal(nodeCount, depth, null, model);
  }

  createModelNodesInternal(nodeCount: number, depth?: number, parentNode?: TreeNodeModel, model?: TreeNodeModel): TreeNodeModel[] {
    if (!nodeCount) {
      return;
    }

    let nodes = [];
    if (!depth) {
      depth = 0;
    }
    model = model || {};
    for (let i = 0; i < nodeCount; i++) {
      let nodeId = i + '';
      if (parentNode) {
        nodeId = parentNode.id + '_' + nodeId;
      }
      nodes[i] = this.createModelNode(nodeId, 'node ' + nodeId, model);
      if (depth > 0) {
        nodes[i].childNodes = this.createModelNodesInternal(nodeCount, depth - 1, nodes[i], model);
      }
    }
    return nodes;
  }

  createTree(model: TreeModel): SpecTree {
    let defaults = {
      parent: this.session.desktop
    };
    model = $.extend({}, defaults, model);
    let tree = new SpecTree();
    tree.init(model as InitModelOf<Tree>);
    return tree;
  }

  createTreeAdapter(model: InitModelOf<ModelAdapter> | SpecTreeModel): TreeAdapter {
    let adapter = new TreeAdapter();
    adapter.init(model);
    return adapter;
  }

  createCompactTree(model: InitModelOf<Tree>): CompactTree & SpecTree {
    let tree = new CompactTree();
    tree.init(model);
    return tree as CompactTree & SpecTree;
  }

  createCompactTreeAdapter(model: InitModelOf<TreeAdapter> | SpecTreeModel): TreeAdapter {
    model.objectType = 'Tree:Compact';
    let tree = new CompactTreeAdapter();
    tree.init(model);
    return tree;
  }

  findAllNodes(tree: Tree): JQuery {
    return tree.$container.find('.tree-node');
  }

  createNodeExpandedEvent(model: { id: string }, nodeId: string, expanded: boolean): RemoteEvent {
    return {
      target: model.id,
      nodeId: nodeId,
      expanded: expanded,
      type: 'nodeExpanded'
    };
  }

  selectNodesAndAssert(tree: Tree, nodes: TreeNode[]) {
    tree.selectNodes(nodes);
    this.assertSelection(tree, nodes);
  }

  assertSelection(tree: Tree, nodes: TreeNode[]) {
    let $selectedNodes = tree.$selectedNodes();
    expect($selectedNodes.length).toBe(nodes.length);

    let selectedNodes = [];
    $selectedNodes.each(function() {
      selectedNodes.push($(this).data('node'));
    });

    expect(arrays.equalsIgnoreOrder(nodes, selectedNodes)).toBeTruthy();
    expect(arrays.equalsIgnoreOrder(nodes, tree.selectedNodes)).toBeTruthy();
  }

  createNodesSelectedEvent(model: { id: string }, nodeIds: string[]): RemoteEvent {
    return {
      target: model.id,
      nodeIds: nodeIds,
      type: 'nodesSelected'
    };
  }

  createNodesInsertedEvent(model: { id: string }, nodes: TreeNodeModel[], commonParentNodeId?: string): RemoteEvent {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      nodes: nodes,
      type: 'nodesInserted'
    };
  }

  createNodesInsertedEventTopNode(model: { id: string }, nodes: TreeNodeModel[]): RemoteEvent {
    return {
      target: model.id,
      nodes: nodes,
      type: 'nodesInserted'
    };
  }

  createNodesDeletedEvent(model: { id: string }, nodeIds: string[], commonParentNodeId: string): RemoteEvent {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      nodeIds: nodeIds,
      type: 'nodesDeleted'
    };
  }

  createAllChildNodesDeletedEvent(model: { id: string }, commonParentNodeId?: string): RemoteEvent {
    return {
      target: model.id,
      commonParentNodeId: commonParentNodeId,
      type: 'allChildNodesDeleted'
    };
  }

  createNodeChangedEvent(model: { id: string }, nodeId: string): RemoteEvent {
    return {
      target: model.id,
      nodeId: nodeId,
      type: 'nodeChanged'
    };
  }

  createNodesUpdatedEvent(model: { id: string }, nodes: TreeNodeModel[]): RemoteEvent {
    return {
      target: model.id,
      nodes: nodes,
      type: 'nodesUpdated'
    };
  }

  createChildNodeOrderChangedEvent(model: { id: string }, childNodeIds: string[], parentNodeId: string): RemoteEvent {
    return {
      target: model.id,
      parentNodeId: parentNodeId,
      childNodeIds: childNodeIds,
      type: 'childNodeOrderChanged'
    };
  }

  createTreeEnabledEvent(model: { id: string }, enabled: boolean): RemoteEvent {
    return {
      target: model.id,
      type: 'property',
      properties: {
        enabled: enabled
      }
    };
  }

  assertAriaPosInSetOnNodes(nodes: TreeNode[], start: number, size: number) {
    nodes = nodes.filter(node => node.rendered && node.attached);
    expect(nodes.length).toBeGreaterThan(0);
    for (let i = 0; i < nodes.length; i++) {
      const node = nodes[i];
      expect(node.$node.attr('aria-setsize')).withContext(`node ${i}`).toBe(String(size));
      expect(node.$node.attr('aria-posinset')).withContext(`node ${i}`).toBe(`${start + i + 1}`);
    }
  }
}

export type SpecTreeModel = SomeRequired<TreeModel, 'id' | 'session' | 'parent' | 'objectType'> & ObjectWithType;

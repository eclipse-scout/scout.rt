/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TreeNode} from './TreeNode';
import {arrays} from '../util/arrays';

/**
 * This object is needed during an update in the tree. It
 * holds the updated nodes to be processed at the end of the update.
 *
 * This class holds two lists of {@link TreeNode}:
 * - TreeNodes which have to be rendered
 * - TreeNodes which triggered an update event
 */
export class TreeCheckNodesResult {
  protected _requireRenderTreeNodes: TreeNode[];
  protected _requireTriggerEventNodes: TreeNode[];

  constructor() {
    this._requireRenderTreeNodes = [];
    this._requireTriggerEventNodes = [];
  }

  addNodeForRendering(...nodes: TreeNode[]) {
    arrays.removeAll(nodes, this._requireRenderTreeNodes);
    this._requireRenderTreeNodes.push(...nodes);
  }

  addNodeForEventTrigger(...nodes: TreeNode[]) {
    arrays.removeAll(nodes, this._requireTriggerEventNodes);
    this._requireTriggerEventNodes.push(...nodes);
  }

  addNodeForRenderingAndEventTrigger(...nodes: TreeNode[]) {
    this.addNodeForRendering(...nodes);
    this.addNodeForEventTrigger(...nodes);
  }

  getNodesForRendering(): TreeNode[] {
    return this._requireRenderTreeNodes;
  }

  getNodesForEventTrigger(): TreeNode[] {
    return this._requireTriggerEventNodes;
  }

  removeNode(node: TreeNode) {
    arrays.remove(this._requireRenderTreeNodes, node);
    arrays.remove(this._requireTriggerEventNodes, node);
  }

  add(treeNodeUpdate: TreeCheckNodesResult) {
    this.addNodeForRendering(...treeNodeUpdate.getNodesForRendering());
    this.addNodeForEventTrigger(...treeNodeUpdate.getNodesForEventTrigger());
  }
}

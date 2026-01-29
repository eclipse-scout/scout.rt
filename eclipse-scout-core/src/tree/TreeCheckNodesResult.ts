/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TreeNode} from './../index';

/**
 * This object is needed during an update in the tree. It
 * holds the updated nodes to be processed at the end of the update.
 *
 * This class holds two lists of {@link TreeNode}:
 * - TreeNodes which have to be rendered
 * - TreeNodes which triggered an update event
 */
export class TreeCheckNodesResult {
  requireRenderTreeNodes: Set<TreeNode>;
  requireTriggerEventNodes: Set<TreeNode>;

  constructor() {
    this.requireRenderTreeNodes = new Set();
    this.requireTriggerEventNodes = new Set();
  }

  addNodeForRendering(node: TreeNode) {
    this.requireRenderTreeNodes.add(node);
  }

  addNodeForEventTrigger(node: TreeNode) {
    this.requireTriggerEventNodes.add(node);
  }

  addNodeForRenderingAndEventTrigger(node: TreeNode) {
    this.addNodeForRendering(node);
    this.addNodeForEventTrigger(node);
  }

  add(treeNodeUpdate: TreeCheckNodesResult) {
    treeNodeUpdate.requireRenderTreeNodes.forEach(node => this.requireRenderTreeNodes.add(node));
    treeNodeUpdate.requireTriggerEventNodes.forEach(node => this.requireTriggerEventNodes.add(node));
  }
}

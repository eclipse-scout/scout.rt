/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTreeNavigationKeyStroke, arrays, keys, ScoutKeyboardEvent, Tree, TreeEventCurrentNode, TreeNode} from '../../index';

export class TreeNavigationUpKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree: Tree, modifierBitMask: number) {
    super(tree, modifierBitMask);
    this.which = [keys.UP];
    this.renderingHints.text = '↑';
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & TreeEventCurrentNode) => {
      let newSelectedNode = this._computeNewSelection(event._treeCurrentNode);
      if (newSelectedNode) {
        return newSelectedNode.$node;
      }
    };
  }

  protected override _computeNewSelection(currentNode: TreeNode): TreeNode {
    let nodes = this.field.visibleNodesFlat;
    if (nodes.length === 0) {
      return;
    }
    if (!currentNode) {
      return arrays.last(nodes);
    }
    return nodes[nodes.indexOf(currentNode) - 1];
  }
}

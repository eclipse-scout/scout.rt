/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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

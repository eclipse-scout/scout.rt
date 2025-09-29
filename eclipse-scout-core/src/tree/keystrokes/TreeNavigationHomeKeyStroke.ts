/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTreeNavigationKeyStroke, arrays, keys, Tree, TreeEventCurrentNode, TreeNode} from '../../index';

export class TreeNavigationHomeKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree: Tree, modifierBitMask: number) {
    super(tree, modifierBitMask);
    this.which = [keys.HOME];
  }

  override handle(event: JQuery.KeyboardEventBase & TreeEventCurrentNode) {
    let newSelection = this._computeNewSelection(event._treeCurrentNode);
    if (newSelection) {
      this.selectNodesAndReveal(newSelection);
    }
  }

  protected override _computeNewSelection(currentNode: TreeNode): TreeNode {
    return arrays.first(this.field.visibleNodesFlat);
  }
}

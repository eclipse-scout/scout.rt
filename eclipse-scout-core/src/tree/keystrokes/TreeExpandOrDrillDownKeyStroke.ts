/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTreeNavigationKeyStroke, ScoutKeyboardEvent, Tree, TreeEventCurrentNode, TreeNode} from '../../index';

export class TreeExpandOrDrillDownKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree: Tree, modifierBitMask: number, key: number, displayText: string) {
    super(tree, modifierBitMask);
    this.which = [key];
    this.renderingHints.text = displayText;
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & TreeEventCurrentNode) => {
      let currentNode = event._treeCurrentNode;
      if (this.isNodeExpandable(currentNode)) {
        return currentNode.$node;
      } else if (currentNode.childNodes.length > 0) {
        return currentNode.childNodes[0].$node;
      }
    };
  }

  protected override _accept(event: ScoutKeyboardEvent & TreeEventCurrentNode): boolean {
    let accepted = super._accept(event);
    let currentNode = event._treeCurrentNode;
    return accepted && currentNode && (this.isNodeExpandable(currentNode) || currentNode.childNodes.length > 0);
  }

  isNodeExpandable(node: TreeNode): boolean {
    return !node.expanded && !node.leaf;
  }

  override handle(event: JQuery.KeyboardEventBase & TreeEventCurrentNode) {
    let currentNode = event._treeCurrentNode;
    if (this.isNodeExpandable(currentNode)) {
      this.field.expandNode(currentNode, {
        lazy: false // always show all nodes on node double click
      });
    } else {
      let visibleChildNodes = currentNode.childNodes.filter(function(node) {
        // Filter using isFilterAccepted does not work because node.filterAccepted is wrong for visible child nodes of a lazy expanded node
        return this.field.visibleNodesFlat.indexOf(node) > -1;
      }, this);
      if (visibleChildNodes.length > 0) {
        this.selectNodesAndReveal(visibleChildNodes[0], true);
      }
    }
  }
}

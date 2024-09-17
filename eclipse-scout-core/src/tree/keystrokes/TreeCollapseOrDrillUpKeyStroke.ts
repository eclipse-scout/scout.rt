/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTreeNavigationKeyStroke, ScoutKeyboardEvent, Tree, TreeEventCurrentNode} from '../../index';

export class TreeCollapseOrDrillUpKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree: Tree, modifierBitMask: number, key: number, displayText: string) {
    super(tree, modifierBitMask);
    this.which = [key];
    this.renderingHints.text = displayText;
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & TreeEventCurrentNode) => {
      let currentNode = event._treeCurrentNode;
      if (currentNode.expanded) {
        return currentNode.$node;
      } else if (currentNode.parentNode) {
        return currentNode.parentNode.$node;
      }
    };
  }

  protected override _accept(event: ScoutKeyboardEvent & TreeEventCurrentNode): boolean {
    let accepted = super._accept(event);
    let currentNode = event._treeCurrentNode;
    return accepted && !!currentNode && (currentNode.expanded || !!currentNode.parentNode);
  }

  override handle(event: JQuery.KeyboardEventBase & TreeEventCurrentNode) {
    let currentNode = event._treeCurrentNode;
    if (currentNode.expanded && currentNode.childNodes.length) {
      this.field.collapseNode(currentNode);
    } else if (currentNode.parentNode) {
      this.selectNodesAndReveal(currentNode.parentNode, true);
    }
  }
}

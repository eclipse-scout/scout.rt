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
import {AbstractTreeNavigationKeyStroke, keys, ScoutKeyboardEvent, Tree, TreeEventCurrentNode} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TreeCollapseOrDrillUpKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree: Tree, modifierBitMask: number) {
    super(tree, modifierBitMask);
    this.which = [keys.SUBTRACT];
    this.renderingHints.text = '-';
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

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement> & TreeEventCurrentNode) {
    let currentNode = event._treeCurrentNode;
    if (currentNode.expanded) {
      this.field.collapseNode(currentNode);
    } else if (currentNode.parentNode) {
      this.selectNodesAndReveal(currentNode.parentNode, true);
    }
  }
}

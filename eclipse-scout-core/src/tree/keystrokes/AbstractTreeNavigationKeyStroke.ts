/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, HAlign, KeyStroke, keyStrokeModifier, ScoutKeyboardEvent, Tree, TreeNode} from '../../index';

export type TreeEventCurrentNode = {
  _treeCurrentNode?: TreeNode;
  _$treeCurrentNode?: JQuery;
};

export class AbstractTreeNavigationKeyStroke extends KeyStroke {
  declare field: Tree;
  nodesFocusable: boolean;

  constructor(tree: Tree, modifierBitMask?: number) {
    super();
    this.field = tree;
    this.repeatable = true;
    this.stopPropagation = true;
    this.renderingHints.hAlign = HAlign.RIGHT;

    this.ctrl = keyStrokeModifier.isCtrl(modifierBitMask);
    this.shift = keyStrokeModifier.isShift(modifierBitMask);
    this.alt = keyStrokeModifier.isAlt(modifierBitMask);

    this.keyStrokeMode = KeyStroke.Mode.DOWN;
    this.inheritAccessibility = false;
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & TreeEventCurrentNode) => {
      return this._computeNewSelection(event._treeCurrentNode)?.$node;
    };
  }

  protected override _accept(event: ScoutKeyboardEvent & TreeEventCurrentNode): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    event._treeCurrentNode = this.nodesFocusable && this.field.focusedNode || this.field.selectedNodes?.[0];
    event._$treeCurrentNode = event._treeCurrentNode?.$node;
    this.field.get$Focusable().addClass('keyboard-navigation');
    return true;
  }

  override handle(event: JQuery.KeyboardEventBase & TreeEventCurrentNode) {
    let newSelection = this._computeNewSelection(event._treeCurrentNode);
    if (newSelection) {
      this.selectNodesAndReveal(newSelection, true);
    }
  }

  protected _computeNewSelection(currentNode: TreeNode): TreeNode {
    return null;
  }

  selectNodesAndReveal(newSelection: TreeNode | TreeNode[], debounceSend?: boolean) {
    newSelection = arrays.ensure(newSelection);
    if (this.nodesFocusable) {
      this.field.setFocusedNode(newSelection[0]);
      this.field.scrollTo(newSelection[0]);
    } else {
      this.field.selectNodes(newSelection, debounceSend);
      this.field.revealSelection();
    }
    if (!this.field.isFocused()) {
      this.field.focus();
    }
  }

  setNodesFocusable(nodesFocusable: boolean) {
    this.nodesFocusable = nodesFocusable;
  }
}

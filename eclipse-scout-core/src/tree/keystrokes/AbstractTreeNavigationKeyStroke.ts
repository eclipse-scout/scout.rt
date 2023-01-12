/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HAlign, KeyStroke, keyStrokeModifier, ScoutKeyboardEvent, Tree, TreeNode} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export type TreeEventCurrentNode = {
  _treeCurrentNode?: TreeNode;
  _$treeCurrentNode?: JQuery;
};

export class AbstractTreeNavigationKeyStroke extends KeyStroke {
  declare field: Tree;

  constructor(tree: Tree, modifierBitMask: number) {
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
  }

  protected override _accept(event: ScoutKeyboardEvent & TreeEventCurrentNode): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    if (this.field.selectedNodes.length > 0) {
      event._treeCurrentNode = this.field.selectedNodes[0];
      event._$treeCurrentNode = event._treeCurrentNode.$node;
    }
    this.field.$container.addClass('keyboard-navigation');
    return true;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement> & TreeEventCurrentNode) {
    let newSelection = this._computeNewSelection(event._treeCurrentNode);
    if (newSelection) {
      this.selectNodesAndReveal(newSelection, true);
    }
  }

  protected _computeNewSelection(currentNode: TreeNode): TreeNode | TreeNode[] {
    return [];
  }

  selectNodesAndReveal(newSelection: TreeNode | TreeNode[], debounceSend?: boolean) {
    this.field.selectNodes(newSelection, debounceSend);
    this.field.revealSelection();
    if (!this.field.isFocused()) {
      this.field.focus();
    }
  }
}

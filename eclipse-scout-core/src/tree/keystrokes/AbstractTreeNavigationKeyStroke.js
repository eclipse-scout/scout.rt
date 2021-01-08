/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HAlign, KeyStroke, keyStrokeModifier} from '../../index';

export default class AbstractTreeNavigationKeyStroke extends KeyStroke {

  constructor(tree, modifierBitMask) {
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

  _accept(event) {
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

  handle(event) {
    let newSelection = this._computeNewSelection(event._treeCurrentNode);
    if (newSelection) {
      this.selectNodesAndReveal(newSelection, true);
    }
  }

  _computeNewSelection(currentNode) {
    return [];
  }

  selectNodesAndReveal(newSelection, debounceSend) {
    this.field.selectNodes(newSelection, debounceSend);
    this.field.revealSelection();
    if (!this.field.isFocused()) {
      this.field.focus();
    }
  }
}

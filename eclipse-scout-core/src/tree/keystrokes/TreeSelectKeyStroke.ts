/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AbstractTreeNavigationKeyStroke, HAlign, keys, ScoutKeyboardEvent, Tree, TreeEventCurrentNode} from '../..';

export class TreeSelectKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree: Tree) {
    super(tree);
    this.which = [keys.SPACE, keys.ENTER];
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & TreeEventCurrentNode) => {
      return this.field.focusedNode?.$node;
    };
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.stopPropagation = true;
  }

  protected override _accept(event: ScoutKeyboardEvent & TreeEventCurrentNode): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    return this.field.focusedNode && !this.field.isNodeSelected(this.field.focusedNode);
  }

  override handle(event: JQuery.KeyboardEventBase & TreeEventCurrentNode) {
    this.field.selectNodes(this.field.focusedNode);
    if (event.which !== keys.SPACE) {
      // immediatePropagation is allowed for space to propagate it to TreeCheckKeyStroke so pressing space selects and checks at once
      event.stopImmediatePropagation();
    }
  }
}

/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, Tree} from '../../index';

export class TreeSpaceKeyStroke extends KeyStroke {
  declare field: Tree;

  constructor(tree: Tree) {
    super();
    this.field = tree;
    this.which = [keys.SPACE];
    this.renderingHints.render = false;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    return accepted && this.field.checkable && this.field.selectedNodes.length > 0;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.toggleChecked(this.field.selectedNodes);
  }
}

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
import {keys, KeyStroke, ScoutKeyboardEvent, Tree} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class TreeSpaceKeyStroke extends KeyStroke {
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

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    let selectedNodes = this.field.selectedNodes.filter(node => node.enabled);
    // Toggle checked state to 'true', except if every node is already checked
    let checked = selectedNodes.some(node => !node.checked);
    selectedNodes.forEach(node => this.field.checkNode(node, checked));
  }
}

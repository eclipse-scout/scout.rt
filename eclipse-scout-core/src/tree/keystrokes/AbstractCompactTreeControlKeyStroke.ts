/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CompactTree, CompactTreeNode, KeyStroke, ScoutKeyboardEvent} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export type CompactTreeEventNode = {
  _nextNode?: CompactTreeNode;
};

export class AbstractCompactTreeControlKeyStroke extends KeyStroke {
  declare field: CompactTree;

  constructor(compactProcessTree: CompactTree) {
    super();
    this.repeatable = true;
    this.field = compactProcessTree;
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
  }

  protected override _accept(event: ScoutKeyboardEvent & CompactTreeEventNode): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    if (!this.field.nodes || !this.field.nodes.length) {
      return false;
    }

    let $currentNode = this.field.$nodesContainer.find('.section-node.selected'),
      currentNode = $currentNode.data('node') as CompactTreeNode;

    let nextNode = this._findNextNode($currentNode, currentNode);
    if (nextNode) {
      event._nextNode = nextNode;
      return true;
    }
    return false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement> & CompactTreeEventNode) {
    this.field.selectNodes(event._nextNode);
    this.field.checkNode(event._nextNode, true);
  }

  protected _findNextNode($currentNode: JQuery, currentNode: CompactTreeNode): CompactTreeNode {
    throw new Error('method must be overwritten by subclass');
  }
}

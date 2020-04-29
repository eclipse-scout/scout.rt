/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {KeyStroke} from '../../index';

export default class AbstractCompactTreeControlKeyStroke extends KeyStroke {

  constructor(compactProcessTree) {
    super();
    this.repeatable = true;
    this.field = compactProcessTree;
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
  }

  _accept(event) {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    if (!this.field.nodes || !this.field.nodes.length) {
      return false;
    }

    let $currentNode = this.field.$nodesContainer.find('.section-node.selected'),
      currentNode = $currentNode.data('node');

    let nextNode = this._findNextNode($currentNode, currentNode);
    if (nextNode) {
      event._nextNode = nextNode;
      return true;
    }
    return false;
  }

  handle(event) {
    this.field.selectNodes(event._nextNode);
    this.field.checkNode(event._nextNode, true);
  }

  _findNextNode($currentNode, currentNode) {
    throw new Error('method must be overwritten by subclass');
  }
}

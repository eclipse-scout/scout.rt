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
import {keys, KeyStroke} from '../../index';

export default class TreeSpaceKeyStroke extends KeyStroke {

  constructor(tree) {
    super();
    this.field = tree;

    this.which = [keys.SPACE];
    this.renderingHints.render = false;
  }

  _accept(event) {
    let accepted = super._accept(event);
    return accepted &&
      this.field.checkable &&
      this.field.selectedNodes.length;
  }

  handle(event) {
    let selectedNodes = this.field.selectedNodes.filter(node => {
      return node.enabled;
    });
    // Toggle checked state to 'true', except if every node is already checked
    let checked = selectedNodes.some(node => {
      return !node.checked;
    });
    selectedNodes.forEach(function(node) {
      this.field.checkNode(node, checked);
    }, this);
  }
}

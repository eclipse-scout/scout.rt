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
import {AbstractTreeNavigationKeyStroke, arrays, keys} from '../../index';

export default class TreeNavigationEndKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree, modifierBitMask) {
    super(tree, modifierBitMask);
    this.which = [keys.END];
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let newSelectedNode = this._computeNewSelection(event._treeCurrentNode);
      if (newSelectedNode) {
        return newSelectedNode.$node;
      }
    };
  }

  handle(event) {
    let newSelection = this._computeNewSelection(event._treeCurrentNode);
    if (newSelection) {
      this.selectNodesAndReveal(newSelection);
    }
  }

  _computeNewSelection(currentNode) {
    let nodes = this.field.visibleNodesFlat;
    if (nodes.length === 0) {
      return;
    }
    return arrays.last(nodes);
  }
}

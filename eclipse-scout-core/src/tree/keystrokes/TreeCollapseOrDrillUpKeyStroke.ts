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
import {AbstractTreeNavigationKeyStroke, keys} from '../../index';

export default class TreeCollapseOrDrillUpKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree, modifierBitMask) {
    super(tree, modifierBitMask);
    this.which = [keys.SUBTRACT];
    this.renderingHints.text = '-';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let currentNode = event._treeCurrentNode;
      if (currentNode.expanded) {
        return currentNode.$node;
      } else if (currentNode.parentNode) {
        return currentNode.parentNode.$node;
      }
    };
  }

  _accept(event) {
    let accepted = super._accept(event);
    let currentNode = event._treeCurrentNode;
    return accepted && currentNode && (currentNode.expanded || currentNode.parentNode);
  }

  handle(event) {
    let currentNode = event._treeCurrentNode;
    if (currentNode.expanded) {
      this.field.collapseNode(currentNode);
    } else if (currentNode.parentNode) {
      this.selectNodesAndReveal(currentNode.parentNode, true);
    }
  }
}

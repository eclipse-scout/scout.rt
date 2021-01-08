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

export default class TreeExpandOrDrillDownKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree, modifierBitMask) {
    super(tree, modifierBitMask);
    this.which = [keys.ADD];
    this.renderingHints.text = '+';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let currentNode = event._treeCurrentNode;
      if (this.isNodeExpandable(currentNode)) {
        return currentNode.$node;
      } else if (currentNode.childNodes.length > 0) {
        return currentNode.childNodes[0].$node;
      }
    };
  }

  _accept(event) {
    let accepted = super._accept(event);
    let currentNode = event._treeCurrentNode;
    return accepted && currentNode && (this.isNodeExpandable(currentNode) || currentNode.childNodes.length > 0);
  }

  isNodeExpandable(node) {
    return !node.expanded && !node.leaf;
  }

  handle(event) {
    let currentNode = event._treeCurrentNode;
    if (this.isNodeExpandable(currentNode)) {
      this.field.expandNode(currentNode, {
        lazy: false // always show all nodes on node double click
      });
    } else {
      let visibleChildNodes = currentNode.childNodes.filter(function(node) {
        // Filter using isFilterAccepted does not work because node.filterAccepted is wrong for visible child nodes of a lazy expanded node
        return this.field.visibleNodesFlat.indexOf(node) > -1;
      }, this);
      if (visibleChildNodes.length > 0) {
        this.selectNodesAndReveal(visibleChildNodes[0], true);
      }
    }
  }
}

/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TreeNavigationUpKeyStroke = function(tree, modifierBitMask) {
  scout.TreeNavigationUpKeyStroke.parent.call(this, tree, modifierBitMask);
  this.which = [scout.keys.UP];
  this.renderingHints.text = '↑';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var $currentNode = event._$treeCurrentNode;
    if (!$currentNode) {
      return this.field.$nodes().last();
    }
    return $currentNode.prevAll('.tree-node:not(.hidden):first');
  }.bind(this);
};
scout.inherits(scout.TreeNavigationUpKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeNavigationUpKeyStroke.prototype._computeNewSelection = function(currentNode) {
  var nodes = this.field.visibleNodesFlat;
  if (nodes.length === 0) {
    return;
  }
  if (!currentNode) {
    return scout.arrays.last(nodes);
  }
  return nodes[nodes.indexOf(currentNode) - 1];
};

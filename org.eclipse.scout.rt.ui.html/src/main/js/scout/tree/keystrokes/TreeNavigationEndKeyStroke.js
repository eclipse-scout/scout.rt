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
scout.TreeNavigationEndKeyStroke = function(tree, modifierBitMask) {
  scout.TreeNavigationEndKeyStroke.parent.call(this, tree, modifierBitMask);
  this.which = [scout.keys.END];
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var newSelectedNode = this._computeNewSelection(event._treeCurrentNode);
    if (newSelectedNode) {
      return newSelectedNode.$node;
    }
  }.bind(this);
};
scout.inherits(scout.TreeNavigationEndKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeNavigationEndKeyStroke.prototype.handle = function(event) {
  var newSelection = this._computeNewSelection(event._treeCurrentNode);
  if (newSelection) {
    this.selectNodesAndReveal(newSelection);
  }
};

scout.TreeNavigationEndKeyStroke.prototype._computeNewSelection = function(currentNode) {
  var nodes = this.field.visibleNodesFlat;
  if (nodes.length === 0) {
    return;
  }
  return scout.arrays.last(nodes);
};

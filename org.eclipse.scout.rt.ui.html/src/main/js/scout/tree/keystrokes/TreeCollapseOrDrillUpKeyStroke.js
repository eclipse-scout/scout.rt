/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TreeCollapseOrDrillUpKeyStroke = function(tree, modifierBitMask) {
  scout.TreeCollapseOrDrillUpKeyStroke.parent.call(this, tree, modifierBitMask);
  this.which = [scout.keys.SUBTRACT];
  this.renderingHints.text = '-';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var currentNode = event._treeCurrentNode;
    if (currentNode.expanded) {
      return currentNode.$node;
    } else if (currentNode.parentNode) {
      return currentNode.parentNode.$node;
    }
  }.bind(this);
};
scout.inherits(scout.TreeCollapseOrDrillUpKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeCollapseOrDrillUpKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TreeCollapseOrDrillUpKeyStroke.parent.prototype._accept.call(this, event);
  var currentNode = event._treeCurrentNode;
  return accepted && currentNode && (currentNode.expanded || currentNode.parentNode);
};

scout.TreeCollapseOrDrillUpKeyStroke.prototype.handle = function(event) {
  var currentNode = event._treeCurrentNode;
  if (currentNode.expanded) {
    this.field.collapseNode(currentNode);
  } else if (currentNode.parentNode) {
    this.selectNodesAndReveal(currentNode.parentNode, true);
  }
};

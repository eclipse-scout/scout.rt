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
scout.OutlineExpandOrDrillDownKeyStroke = function(tree) {
  scout.OutlineExpandOrDrillDownKeyStroke.parent.call(this, tree);
  this.which = [scout.keys.ADD];
  this.renderingHints.text = '+';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return (!event._treeCurrentNode.expanded ? event._$treeCurrentNode : null);
  }.bind(this);
};
scout.inherits(scout.OutlineExpandOrDrillDownKeyStroke, scout.AbstractOutlineNavigationKeyStroke);

scout.OutlineExpandOrDrillDownKeyStroke.prototype._accept = function(event) {
  var accepted = scout.OutlineExpandOrDrillDownKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && event._treeCurrentNode;
};

scout.OutlineExpandOrDrillDownKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if (!currentNode.expanded && !currentNode.leaf) {
    this.field.expandNode(currentNode, {
      lazy: false // always show all nodes on node double click
    });
    return null;
  } else if (currentNode.childNodes.length > 0) {
    return currentNode.childNodes[0];
  }
};

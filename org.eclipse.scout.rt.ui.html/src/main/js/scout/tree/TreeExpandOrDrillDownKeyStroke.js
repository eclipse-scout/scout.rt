scout.TreeExpandOrDrillDownKeyStroke = function(tree, modifierBitMask) {
  scout.TreeExpandOrDrillDownKeyStroke.parent.call(this, tree, modifierBitMask);
  this.which = [scout.keys.ADD];
  this.renderingHints.text = '+';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return (!event._treeCurrentNode.expanded ? event._$treeCurrentNode : null);
  }.bind(this);
};
scout.inherits(scout.TreeExpandOrDrillDownKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeExpandOrDrillDownKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TreeExpandOrDrillDownKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && event._treeCurrentNode;
};

scout.TreeExpandOrDrillDownKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if (!currentNode.expanded && !currentNode.leaf) {
    this.field.setNodeExpanded(currentNode, true);
    return null;
  } else if (currentNode.childNodes.length > 0) {
    return currentNode.childNodes[0];
  }
};

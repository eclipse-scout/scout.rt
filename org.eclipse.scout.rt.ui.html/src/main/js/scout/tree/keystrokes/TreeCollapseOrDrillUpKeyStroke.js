scout.TreeCollapseOrDrillUpKeyStroke = function(tree, modifierBitMask) {
  scout.TreeCollapseOrDrillUpKeyStroke.parent.call(this, tree, modifierBitMask);
  this.which = [scout.keys.SUBTRACT];
  this.renderingHints.text = '-';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return (event._treeCurrentNode.expanded ? event._$treeCurrentNode : null);
  }.bind(this);
};
scout.inherits(scout.TreeCollapseOrDrillUpKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeCollapseOrDrillUpKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TreeCollapseOrDrillUpKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && event._treeCurrentNode;
};

scout.TreeCollapseOrDrillUpKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if (currentNode.expanded) {
    this.field.collapseNode(currentNode);
    return null;
  } else if (currentNode.parentNode) {
    return currentNode.parentNode;
  }
};

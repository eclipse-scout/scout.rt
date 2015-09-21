scout.OutlineTreeExpandOrDrillDownKeyStroke = function(tree) {
  scout.OutlineTreeExpandOrDrillDownKeyStroke.parent.call(this, tree);
  this.which = [scout.keys.ADD];
  this.renderingHints.text = '+';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return (!event._treeCurrentNode.expanded ? event._$treeCurrentNode : null);
  }.bind(this);
};
scout.inherits(scout.OutlineTreeExpandOrDrillDownKeyStroke, scout.AbstractOutlineTreeNavigationKeyStroke);

scout.OutlineTreeExpandOrDrillDownKeyStroke.prototype._accept = function(event) {
  var accepted = scout.OutlineTreeExpandOrDrillDownKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && event._treeCurrentNode;
};

scout.OutlineTreeExpandOrDrillDownKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if (!currentNode.expanded && !currentNode.leaf) {
    this.field.expandNode(currentNode);
    return null;
  } else if (currentNode.childNodes.length > 0) {
    return currentNode.childNodes[0];
  }
};

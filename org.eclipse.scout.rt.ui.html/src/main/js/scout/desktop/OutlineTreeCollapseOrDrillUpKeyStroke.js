scout.OutlineTreeCollapseOrDrillUpKeyStroke = function(tree) {
  scout.OutlineTreeCollapseOrDrillUpKeyStroke.parent.call(this, tree);
  this.which = [scout.keys.SUBTRACT];
  this.renderingHints.text = '-';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return (event._treeCurrentNode.expanded ? event._$treeCurrentNode : null);
  }.bind(this);
};
scout.inherits(scout.OutlineTreeCollapseOrDrillUpKeyStroke, scout.AbstractOutlineTreeNavigationKeyStroke);

scout.OutlineTreeCollapseOrDrillUpKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TreeCollapseOrDrillUpKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && event._treeCurrentNode;
};

scout.OutlineTreeCollapseOrDrillUpKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if (currentNode.expanded) {
    this.field.setNodeExpanded(currentNode, false);
    return null;
  } else if (currentNode.parentNode) {
    return currentNode.parentNode;
  }
};

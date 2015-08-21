scout.TreeNavigationUpKeyStroke = function(tree, modifierBitMask) {
  scout.TreeNavigationUpKeyStroke.parent.call(this, tree, modifierBitMask);
  this.which = [scout.keys.UP];
  this.renderingHints.text = '↑';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var $currentNode = event._$treeCurrentNode;
    return ($currentNode.length ? $currentNode.prev('.tree-node') : this.field.$nodes().last());
  }.bind(this);
};
scout.inherits(scout.TreeNavigationUpKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeNavigationUpKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if ($currentNode.length === 0) {
    return this.field.$nodes().last().data('node');
  } else {
    return $currentNode.prev('.tree-node').data('node');
  }
};

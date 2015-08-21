scout.TreeNavigationDownKeyStroke = function(tree, modifierBitMask) {
  scout.TreeNavigationDownKeyStroke.parent.call(this, tree, modifierBitMask);
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var $currentNode = event._$treeCurrentNode;
    return ($currentNode.length ? $currentNode.next('.tree-node') : this.field.$nodes().first());
  }.bind(this);
};
scout.inherits(scout.TreeNavigationDownKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeNavigationDownKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if ($currentNode.length === 0) {
    return this.field.$nodes().first().data('node');
  } else {
    return $currentNode.next('.tree-node').data('node');
  }
};

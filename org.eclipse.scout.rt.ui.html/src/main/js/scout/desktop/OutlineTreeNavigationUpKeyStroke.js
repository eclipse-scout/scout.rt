scout.OutlineTreeNavigationUpKeyStroke = function(tree) {
  scout.OutlineTreeNavigationUpKeyStroke.parent.call(this, tree);
  this.which = [scout.keys.UP];
  this.renderingHints.text = '↑';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var $currentNode = event._$treeCurrentNode;
    if ($currentNode.length === 0) {
      return this.field.$nodes().last();
    }
    return $currentNode.prevAll('.tree-node:not(.hidden):first');
  }.bind(this);
};
scout.inherits(scout.OutlineTreeNavigationUpKeyStroke, scout.AbstractOutlineTreeNavigationKeyStroke);

scout.OutlineTreeNavigationUpKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if ($currentNode.length === 0) {
    return this.field.$nodes().last().data('node');
  }
  return $currentNode.prevAll('.tree-node:not(.hidden):first').data('node');
};

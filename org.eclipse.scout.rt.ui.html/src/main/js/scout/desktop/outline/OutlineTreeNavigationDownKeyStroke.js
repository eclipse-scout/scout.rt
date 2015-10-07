scout.OutlineTreeNavigationDownKeyStroke = function(tree) {
  scout.OutlineTreeNavigationDownKeyStroke.parent.call(this, tree);
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var $currentNode = event._$treeCurrentNode;
    if ($currentNode.length === 0) {
      return this.field.$nodes().first();
    }
    return $currentNode.nextAll('.tree-node:not(.hidden):first');
  }.bind(this);
};
scout.inherits(scout.OutlineTreeNavigationDownKeyStroke, scout.AbstractOutlineTreeNavigationKeyStroke);

scout.OutlineTreeNavigationDownKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  if ($currentNode.length === 0) {
    return this.field.$nodes().first().data('node');
  }
  return $currentNode.nextAll('.tree-node:not(.hidden):first').data('node');
};

scout.TreeCollapseAllKeyStroke = function(tree, keyStrokeModifier) {
  scout.TreeCollapseAllKeyStroke.parent.call(this, tree, keyStrokeModifier);
  this.which = [scout.keys.HOME];
  this.renderingHints.hAlign = scout.hAlign.LEFT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$data;
  }.bind(this);
};
scout.inherits(scout.TreeCollapseAllKeyStroke, scout.AbstractTreeNavigationKeyStroke);

scout.TreeCollapseAllKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  this.field.collapseAll();
  return this.field.$nodes().first().data('node');
};

scout.TreeCollapseAllKeyStroke.prototype._accept = function(event) {
  return scout.TreeCollapseAllKeyStroke.parent.prototype._accept.call(this, event);
};

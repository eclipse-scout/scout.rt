scout.TreeSpaceKeyStroke = function(tree) {
  scout.TreeSpaceKeyStroke.parent.call(this);
  this.field = tree;
  this.which = [scout.keys.SPACE];
  this.renderingHints.render = false;
};
scout.inherits(scout.TreeSpaceKeyStroke, scout.KeyStroke);

scout.TreeSpaceKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TreeSpaceKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && this.field.checkable;
};

scout.TreeSpaceKeyStroke.prototype.handle = function(event) {
  var $currentNode = this.field.$selectedNodes().eq(0);
  if ($currentNode.length === 0) {
    return;
  }

  var check = !$($currentNode[0]).data('node').checked;
  for (var j = 0; j < $currentNode.length; j++) {
    var node = $($currentNode[j]).data('node');
    this.field.checkNode(node, check);
  }
};

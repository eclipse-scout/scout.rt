scout.AbstractCompactTreeControlKeyStroke = function(compactProcessTree) {
  scout.AbstractCompactTreeControlKeyStroke.parent.call(this);
  this.field = compactProcessTree;
  this.keyStrokeMode = scout.keyStrokeMode.DOWN;
};
scout.inherits(scout.AbstractCompactTreeControlKeyStroke, scout.KeyStroke);

scout.AbstractCompactTreeControlKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AbstractCompactTreeControlKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  if (!this.field.nodes || !this.field.nodes.length) {
    return false;
  }

  var $currentNode = this.field.$nodesContainer.find('.section-node.selected'),
    currentNode = $currentNode.data('node');

  var nextNode = this._findNextNode($currentNode, currentNode);
  if (nextNode) {
    event._nextNode = nextNode;
    return true;
  } else {
    return false;
  }
};

scout.AbstractCompactTreeControlKeyStroke.prototype.handle = function(event) {
  this.field.selectNodes(event._nextNode);
  this.field.checkNode(event._nextNode, true);
};

scout.AbstractCompactTreeControlKeyStroke.prototype._findNextNode = function($currentNode, currentNode) {
  throw new Error('method must be overwritten by subclass');
};

scout.AbstractOutlineTreeNavigationKeyStroke = function(tree) {
  scout.AbstractOutlineTreeNavigationKeyStroke.parent.call(this);
  this.field = tree;
  this.stopPropagation = true;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;

  this.ctrl = true;
  this.shift = true;

  this.keyStrokeMode = scout.keyStrokeMode.DOWN;
};
scout.inherits(scout.AbstractOutlineTreeNavigationKeyStroke, scout.KeyStroke);

scout.AbstractOutlineTreeNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AbstractOutlineTreeNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var $currentNode = this.field.$selectedNodes().eq(0);
  event._$treeCurrentNode = $currentNode;
  event._treeCurrentNode = $currentNode.data('node');
  return true;
};

scout.AbstractOutlineTreeNavigationKeyStroke.prototype.handle = function(event) {
  var newNodeSelection = this._handleInternal(event._$treeCurrentNode, event._treeCurrentNode);
  if (newNodeSelection) {
    this.field.selectNodes(newNodeSelection);
    this.field.scrollTo(newNodeSelection);
    this.field.handleOutlineContent(true);
  }
};

scout.AbstractOutlineTreeNavigationKeyStroke.prototype._handleInternal = function($currentNode, currentNode) {
  throw new Error('method must be overwritten by subclass');
};

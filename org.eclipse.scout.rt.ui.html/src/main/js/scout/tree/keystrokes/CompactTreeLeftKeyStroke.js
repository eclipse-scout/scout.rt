scout.CompactTreeLeftKeyStroke = function(compactProcessTree) {
  scout.CompactTreeLeftKeyStroke.parent.call(this, compactProcessTree);
  this.renderingHints.text = '←';
  this.which = [scout.keys.LEFT];
};
scout.inherits(scout.CompactTreeLeftKeyStroke, scout.AbstractCompactTreeControlKeyStroke);

scout.CompactTreeLeftKeyStroke.prototype._findNextNode = function($currentNode, currentNode) {
  // Find first process node of previous section, or first process node.
  return $currentNode.parent().prev('.section').children('.section-node').first().data('node') || $currentNode.parent().children('.section-node').not($currentNode).first().data('node');
};

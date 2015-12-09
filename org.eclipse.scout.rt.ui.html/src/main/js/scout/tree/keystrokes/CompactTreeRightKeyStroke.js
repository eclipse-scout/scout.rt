scout.CompactTreeRightKeyStroke = function(compactProcessTree) {
  scout.CompactTreeRightKeyStroke.parent.call(this, compactProcessTree);
  this.renderingHints.text = '→';
  this.which = [scout.keys.RIGHT];
};
scout.inherits(scout.CompactTreeRightKeyStroke, scout.AbstractCompactTreeControlKeyStroke);

scout.CompactTreeRightKeyStroke.prototype._findNextNode = function($currentNode, currentNode) {
  // Find first process node of next section.
  return $currentNode.parent().next('.section').children('.section-node').first().data('node');
};

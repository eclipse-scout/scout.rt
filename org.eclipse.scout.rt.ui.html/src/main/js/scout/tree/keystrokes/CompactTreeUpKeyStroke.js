scout.CompactTreeUpKeyStroke = function(compactProcessTree) {
  scout.CompactTreeUpKeyStroke.parent.call(this, compactProcessTree);
  this.which = [scout.keys.UP];
  this.renderingHints.text = '↑';
};
scout.inherits(scout.CompactTreeUpKeyStroke, scout.AbstractCompactTreeControlKeyStroke);

scout.CompactTreeUpKeyStroke.prototype._findNextNode = function($currentNode, currentNode) {
  if (currentNode) {
    // Find last process node, or last process node in previous section.
    return $currentNode.prev('.section-node').data('node') || $currentNode.parent().prev('.section').children('.section-node').last().data('node');
  } else {
    // Find last process node.
    return scout.arrays.last(scout.arrays.last(this.field.nodes).childNodes);
  }
};

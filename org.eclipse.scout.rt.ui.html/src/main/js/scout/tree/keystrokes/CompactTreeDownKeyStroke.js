scout.CompactTreeDownKeyStroke = function(compactProcessTree) {
  scout.CompactTreeDownKeyStroke.parent.call(this, compactProcessTree);
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
};
scout.inherits(scout.CompactTreeDownKeyStroke, scout.AbstractCompactTreeControlKeyStroke);

scout.CompactTreeDownKeyStroke.prototype._findNextNode = function($currentNode, currentNode) {
  if (currentNode) {
    // Find first process node, or first process node in next section.
    return $currentNode.next('.section-node').data('node') || $currentNode.parent().next('.section').children('.section-node').first().data('node');
  } else {
    // Find first process node.
    return scout.arrays.first(scout.arrays.first(this.field.nodes).childNodes);
  }
};

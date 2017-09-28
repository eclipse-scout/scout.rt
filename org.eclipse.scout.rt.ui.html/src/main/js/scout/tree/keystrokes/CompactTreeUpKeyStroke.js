/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

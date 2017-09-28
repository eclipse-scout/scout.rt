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

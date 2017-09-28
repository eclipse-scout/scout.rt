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

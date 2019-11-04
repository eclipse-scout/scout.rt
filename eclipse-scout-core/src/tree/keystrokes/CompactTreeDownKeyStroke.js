/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractCompactTreeControlKeyStroke, arrays, keys} from '../../index';

export default class CompactTreeDownKeyStroke extends AbstractCompactTreeControlKeyStroke {

  constructor(compactProcessTree) {
    super(compactProcessTree);
    this.which = [keys.DOWN];
    this.renderingHints.text = '↓';
  }

  _findNextNode($currentNode, currentNode) {
    if (currentNode) {
      // Find first process node, or first process node in next section.
      return $currentNode.next('.section-node').data('node') || $currentNode.parent().next('.section').children('.section-node').first().data('node');
    }
    // Find first process node.
    return arrays.first(arrays.first(this.field.nodes).childNodes);
  }
}

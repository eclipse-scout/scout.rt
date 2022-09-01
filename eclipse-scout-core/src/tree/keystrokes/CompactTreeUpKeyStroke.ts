/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractCompactTreeControlKeyStroke, arrays, CompactTree, CompactTreeNode, keys} from '../../index';

export default class CompactTreeUpKeyStroke extends AbstractCompactTreeControlKeyStroke {

  constructor(compactProcessTree: CompactTree) {
    super(compactProcessTree);
    this.which = [keys.UP];
    this.renderingHints.text = '↑';
  }

  protected override _findNextNode($currentNode: JQuery, currentNode: CompactTreeNode): CompactTreeNode {
    if (currentNode) {
      // Find last process node, or last process node in previous section.
      return $currentNode.prev('.section-node').data('node') || $currentNode.parent().prev('.section').children('.section-node').last().data('node');
    }
    // Find last process node.
    return arrays.last(arrays.last(this.field.nodes).childNodes) as CompactTreeNode;
  }
}

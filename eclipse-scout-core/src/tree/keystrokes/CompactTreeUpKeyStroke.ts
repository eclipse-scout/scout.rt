/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractCompactTreeControlKeyStroke, arrays, CompactTree, CompactTreeNode, keys} from '../../index';

export class CompactTreeUpKeyStroke extends AbstractCompactTreeControlKeyStroke {

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

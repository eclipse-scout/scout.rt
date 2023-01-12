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

export class CompactTreeDownKeyStroke extends AbstractCompactTreeControlKeyStroke {

  constructor(compactProcessTree: CompactTree) {
    super(compactProcessTree);
    this.which = [keys.DOWN];
    this.renderingHints.text = '↓';
  }

  protected override _findNextNode($currentNode: JQuery, currentNode: CompactTreeNode): CompactTreeNode {
    if (currentNode) {
      // Find first process node, or first process node in next section.
      return $currentNode.next('.section-node').data('node') || $currentNode.parent().next('.section').children('.section-node').first().data('node');
    }
    // Find first process node.
    return arrays.first(arrays.first(this.field.nodes).childNodes) as CompactTreeNode;
  }
}

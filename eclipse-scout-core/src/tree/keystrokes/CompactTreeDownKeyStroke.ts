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

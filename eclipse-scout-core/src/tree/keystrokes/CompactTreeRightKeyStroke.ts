/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractCompactTreeControlKeyStroke, CompactTree, CompactTreeNode, keys} from '../../index';

export class CompactTreeRightKeyStroke extends AbstractCompactTreeControlKeyStroke {

  constructor(compactProcessTree: CompactTree) {
    super(compactProcessTree);
    this.renderingHints.text = '→';
    this.which = [keys.RIGHT];
  }

  protected override _findNextNode($currentNode: JQuery, currentNode: CompactTreeNode): CompactTreeNode {
    // Find first process node of next section.
    return $currentNode.parent().next('.section').children('.section-node').first().data('node');
  }
}

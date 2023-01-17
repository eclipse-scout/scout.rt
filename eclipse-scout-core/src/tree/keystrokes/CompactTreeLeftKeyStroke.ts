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

export class CompactTreeLeftKeyStroke extends AbstractCompactTreeControlKeyStroke {

  constructor(compactProcessTree: CompactTree) {
    super(compactProcessTree);
    this.renderingHints.text = '←';
    this.which = [keys.LEFT];
  }

  protected override _findNextNode($currentNode: JQuery, currentNode: CompactTreeNode): CompactTreeNode {
    // Find first process node of previous section, or first process node.
    return $currentNode.parent().prev('.section').children('.section-node').first().data('node') || $currentNode.parent().children('.section-node').not($currentNode).first().data('node');
  }
}

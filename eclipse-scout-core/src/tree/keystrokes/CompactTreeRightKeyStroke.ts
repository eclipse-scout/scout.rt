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

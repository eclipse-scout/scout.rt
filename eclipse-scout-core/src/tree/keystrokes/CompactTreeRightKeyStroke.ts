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
import {AbstractCompactTreeControlKeyStroke, keys} from '../../index';

export default class CompactTreeRightKeyStroke extends AbstractCompactTreeControlKeyStroke {

  constructor(compactProcessTree) {
    super(compactProcessTree);
    this.renderingHints.text = '→';
    this.which = [keys.RIGHT];
  }

  _findNextNode($currentNode, currentNode) {
    // Find first process node of next section.
    return $currentNode.parent().next('.section').children('.section-node').first().data('node');
  }
}

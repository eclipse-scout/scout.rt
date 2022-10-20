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

export default class CompactTreeLeftKeyStroke extends AbstractCompactTreeControlKeyStroke {

  constructor(compactProcessTree) {
    super(compactProcessTree);
    this.renderingHints.text = '←';
    this.which = [keys.LEFT];
  }

  _findNextNode($currentNode, currentNode) {
    // Find first process node of previous section, or first process node.
    return $currentNode.parent().prev('.section').children('.section-node').first().data('node') || $currentNode.parent().children('.section-node').not($currentNode).first().data('node');
  }
}

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
import {AbstractTreeNavigationKeyStroke, HAlign, keys} from '../../index';

export default class TreeCollapseAllKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree, keyStrokeModifier) {
    super(tree, keyStrokeModifier);
    this.which = [keys.HOME];
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      if (this.field.visibleNodesFlat.length > 0) {
        return this.field.visibleNodesFlat[0].$node;
      }
    };
  }

  handle(event) {
    this.field.collapseAll();
    if (this.field.visibleNodesFlat.length > 0) {
      this.selectNodesAndReveal(this.field.visibleNodesFlat[0]);
    }
  }
}

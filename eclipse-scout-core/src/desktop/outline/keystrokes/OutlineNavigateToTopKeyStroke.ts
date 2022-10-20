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
import {AbstractTreeNavigationKeyStroke, HAlign, keys} from '../../../index';

export default class OutlineNavigateToTopKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree, modifierBitMask) {
    super(tree, modifierBitMask);
    this.which = [keys.HOME];
    this.renderingHints.hAlign = HAlign.RIGHT;

    this.renderingHints.$drawingArea = function($drawingArea, event) {
      return this.field.$title || this.field.$data;
    }.bind(this);
  }

  handle(event) {
    this.field.navigateToTop();
  }
}

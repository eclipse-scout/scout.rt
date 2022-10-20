/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HAlign, keys, KeyStroke} from '../../../index';

export default class CheckBoxToggleKeyStroke extends KeyStroke {

  constructor(checkbox) {
    super();
    this.field = checkbox;
    this.which = [keys.SPACE];
    this.stopPropagation = true;
    this.stopImmediatePropagation = true;

    this.renderingHints.hAlign = HAlign.LEFT;
    this.renderingHints.$drawingArea = function($drawingArea, event) {
      return this.field.$fieldContainer;
    }.bind(this);
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this.field.toggleChecked();
  }
}

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
import {keys, KeyStroke} from '../index';

export default class MenuExecKeyStroke extends KeyStroke {

  constructor(menu) {
    super();
    this.field = menu;
    this.which = [keys.SPACE, keys.ENTER];
    this.stopPropagation = true;

    this.renderingHints.offset = 16;
    this.renderingHints.$drawingArea = function($drawingArea, event) {
      return this.field.$container;
    }.bind(this);
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this.field.doAction();
  }
}

/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {KeyStroke} from '../index';

export default class ClickActiveElementKeyStroke extends KeyStroke {

  constructor(field, which) {
    super();
    this.field = field;
    this.which = which;
    this.stopPropagation = true;
    this.renderingHints.render = true;
    this.renderingHints.$drawingArea = ($drawingArea, event) => event._$activeElement;
  }

  _accept(event) {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    event._$activeElement = this.field.$container.activeElement();
    return true;
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    event._$activeElement.trigger({
      type: 'click',
      which: 1
    });
  }
}

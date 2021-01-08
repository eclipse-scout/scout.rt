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
import {HAlign, KeyStroke} from '../../../index';

export default class ButtonKeyStroke extends KeyStroke {

  constructor(button, keyStroke) {
    super();
    this.field = button;
    this.parseAndSetKeyStroke(keyStroke);
    this.stopPropagation = true;
    this.stopImmediatePropagation = true;

    this.renderingHints.hAlign = HAlign.RIGHT;
  }

  /**
   * @override KeyStroke.js
   */
  _accept(event) {
    let accepted = super._accept(event);
    return accepted && this.field.$field.isAttached();
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this.field.doAction();
  }
}

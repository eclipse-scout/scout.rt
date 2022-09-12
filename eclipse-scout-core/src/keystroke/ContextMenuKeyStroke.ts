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

export default class ContextMenuKeyStroke extends KeyStroke {

  constructor(field, contextFunction, bindObject) {
    super();
    this._contextFunction = contextFunction;
    this._bindObject = bindObject || this;

    this.field = field;
    this.renderingHints.render = false;

    this.which = [keys.SELECT]; // = "Menu" key
    this.ctrl = false;
    this.shift = false;
    this.stopPropagation = true;
    this.inheritAccessibility = false;
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this._contextFunction.call(this._bindObject, event);
  }
}

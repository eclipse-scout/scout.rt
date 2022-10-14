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
import {keys, KeyStroke, Widget} from '../index';

export default class ContextMenuKeyStroke extends KeyStroke {

  protected _contextFunction: (event: JQuery.KeyboardEventBase) => void;
  protected _bindObject: any;

  constructor(field: Widget, contextFunction: (event: JQuery.KeyboardEventBase) => void, bindObject?: any) {
    super();
    this._contextFunction = contextFunction;
    this._bindObject = bindObject || this;

    this.field = field;
    this.renderingHints.render = false;

    this.which = [keys.SELECT];
    this.ctrl = false;
    this.shift = false;
    this.stopPropagation = true;
    this.inheritAccessibility = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this._contextFunction.call(this._bindObject, event);
  }
}

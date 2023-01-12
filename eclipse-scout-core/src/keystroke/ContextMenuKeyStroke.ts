/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, Widget} from '../index';

export class ContextMenuKeyStroke extends KeyStroke {

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

/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, keys, KeyStroke} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class ActionExecKeyStroke extends KeyStroke {
  declare field: Action;

  constructor(action: Action) {
    super();
    this.field = action;
    this.which = [keys.SPACE, keys.ENTER];
    this.stopPropagation = true;
  }

  override handle(event: KeyboardEventBase) {
    this.field.doAction();
  }
}

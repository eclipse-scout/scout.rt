/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, keys, KeyStroke} from '../index';

export class ActionExecKeyStroke extends KeyStroke {
  declare field: Action;

  constructor(action: Action) {
    super();
    this.field = action;
    this.which = [keys.SPACE, keys.ENTER];
    this.stopPropagation = true;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.doAction();
  }
}

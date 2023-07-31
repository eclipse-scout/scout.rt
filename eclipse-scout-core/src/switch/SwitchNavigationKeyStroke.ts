/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, Switch} from '../index';

export class SwitchNavigationKeyStroke extends KeyStroke {
  declare field: Switch;

  constructor(field: Switch) {
    super();
    this.field = field;
    this.which = [keys.LEFT, keys.RIGHT];
    this.stopPropagation = true;
    this.stopImmediatePropagation = true;
    this.renderingHints.render = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.toggleSwitch(event, event.which !== keys.LEFT);
  }
}

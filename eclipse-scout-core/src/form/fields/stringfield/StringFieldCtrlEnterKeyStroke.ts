/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, StringField} from '../../../index';

export class StringFieldCtrlEnterKeyStroke extends KeyStroke {
  declare field: StringField;

  constructor(stringField: StringField) {
    super();
    this.field = stringField;
    this.which = [keys.ENTER];
    this.ctrl = true;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    return accepted && this.field.hasAction;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field._onIconClick();
  }
}

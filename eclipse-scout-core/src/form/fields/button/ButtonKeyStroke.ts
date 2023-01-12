/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Button, HAlign, KeyStroke, ScoutKeyboardEvent} from '../../../index';

export class ButtonKeyStroke extends KeyStroke {
  declare field: Button;

  constructor(button: Button, keyStroke: string) {
    super();
    this.field = button;
    this.parseAndSetKeyStroke(keyStroke);
    this.stopPropagation = true;
    this.stopImmediatePropagation = true;
    this.renderingHints.hAlign = HAlign.RIGHT;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    return accepted && this.field.$field.isAttached();
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.doAction();
  }
}

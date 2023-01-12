/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ButtonKeyStroke, HAlign, RadioButton, ScoutKeyboardEvent} from '../../../index';

export class RadioButtonKeyStroke extends ButtonKeyStroke {
  declare field: RadioButton<any>;

  constructor(button: RadioButton<any>, keyStroke: string) {
    super(button, keyStroke);
    this.renderingHints.hAlign = HAlign.LEFT;
  }

  /**
   * To not prevent a parent key stroke context from execution of the event, the key stroke event is only accepted if the radio button is not selected.
   */
  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    return accepted && !this.field.selected;
  }
}

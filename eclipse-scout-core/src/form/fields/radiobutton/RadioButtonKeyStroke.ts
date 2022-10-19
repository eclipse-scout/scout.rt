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
import {ButtonKeyStroke, HAlign, RadioButton, ScoutKeyboardEvent} from '../../../index';

export default class RadioButtonKeyStroke extends ButtonKeyStroke {
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

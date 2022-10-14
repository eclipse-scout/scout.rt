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
import {Button, HAlign, KeyStroke, ScoutKeyboardEvent} from '../../../index';

export default class ButtonKeyStroke extends KeyStroke {
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

  override handle(event: JQuery.KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.doAction();
  }
}

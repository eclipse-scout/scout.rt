/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, KeyStroke} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class ActionKeyStroke extends KeyStroke {
  declare field: Action;

  constructor(action: Action) {
    super();
    this.field = action;
    this.parseAndSetKeyStroke(action.keyStroke);
    this.keyStrokeFirePolicy = action.keyStrokeFirePolicy;

    // If one action is executed, don't execute other actions by default
    this.stopPropagation = true;
    this.stopImmediatePropagation = true;
  }

  protected override _isEnabled(): boolean {
    if (!this.which.length) {
      return false; // actions without a keystroke are not enabled.
    }
    return super._isEnabled();
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.doAction();
  }
}

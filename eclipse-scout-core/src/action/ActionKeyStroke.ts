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
import {Action, KeyStroke} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class ActionKeyStroke extends KeyStroke {
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

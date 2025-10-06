/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DesktopTab, keys, KeyStroke, ScoutKeyboardEvent} from '../..';

export class DesktopTabCloseKeyStroke extends KeyStroke {
  declare field: DesktopTab;

  constructor(tab: DesktopTab) {
    super();
    this.field = tab;
    this.which = [keys.ESC];
    this.stopPropagation = true;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    if (!super._accept(event)) {
      return false;
    }
    return this.field.closable;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.view?.abort();
  }
}

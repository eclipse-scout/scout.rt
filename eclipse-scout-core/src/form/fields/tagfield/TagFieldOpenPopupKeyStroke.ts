/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, TagField} from '../../../index';

export class TagFieldOpenPopupKeyStroke extends KeyStroke {
  declare field: TagField;

  constructor(tagField: TagField) {
    super();
    this.field = tagField;
    this.which = [keys.ENTER, keys.SPACE];
    this.renderingHints.render = false;
    this.preventDefault = false;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    return this.field.tagBar && this.field.tagBar.isOverflowIconFocused();
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.tagBar.openOverflowPopup();
  }
}

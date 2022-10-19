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
import {keys, KeyStroke, ScoutKeyboardEvent, TagField} from '../../../index';

export default class TagFieldOpenPopupKeyStroke extends KeyStroke {
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

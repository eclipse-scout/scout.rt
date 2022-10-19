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
import {keys, KeyStroke, ScoutKeyboardEvent, strings, TagField} from '../../../index';

export default class TagFieldEnterKeyStroke extends KeyStroke {
  declare field: TagField;

  constructor(tagField: TagField) {
    super();
    this.field = tagField;
    this.which = [keys.ENTER];
    this.renderingHints.render = false;
    this.preventDefault = false;
    this.stopPropagation = true;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    // set the stopPropagation flag dynamically. While the user is typing we only want the field
    // to apply the current displayText as tag, when the user presses ENTER. But when the displayText
    // is empty, the ENTER key should propagate up to the form.
    this.stopPropagation = strings.hasText(this.field._readDisplayText());
    return this.field.isInputFocused();
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.acceptInput();
  }
}

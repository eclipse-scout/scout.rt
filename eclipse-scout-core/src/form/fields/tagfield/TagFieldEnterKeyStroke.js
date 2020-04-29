/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke, strings} from '../../../index';

export default class TagFieldEnterKeyStroke extends KeyStroke {

  constructor(tagField) {
    super();
    this.field = tagField;
    this.which = [keys.ENTER];
    this.renderingHints.render = false;
    this.preventDefault = false;
    this.stopPropagation = true;
  }

  _accept(event) {
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

  handle(event) {
    this.field.acceptInput();
  }
}

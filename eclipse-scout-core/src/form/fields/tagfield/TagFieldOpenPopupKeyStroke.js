/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../../../index';

export default class TagFieldOpenPopupKeyStroke extends KeyStroke {

  constructor(tagField) {
    super();
    this.field = tagField;
    this.which = [keys.ENTER, keys.SPACE];
    this.renderingHints.render = false;
    this.preventDefault = false;
  }

  _accept(event) {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    return this.field.tagBar && this.field.tagBar.isOverflowIconFocused();
  }

  handle(event) {
    this.field.tagBar.openOverflowPopup();
  }
}

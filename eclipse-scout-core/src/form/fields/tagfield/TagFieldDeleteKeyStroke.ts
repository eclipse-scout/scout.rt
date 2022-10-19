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
import {keys, KeyStroke, TagBar} from '../../../index';

/**
 * @param fieldAdapter acts as an interface so we can use the same key-stroke for TagField and TagBarOverflowPopup.
 *
 */
export default class TagFieldDeleteKeyStroke extends KeyStroke {

  constructor(fieldAdapter) { // FIXME TS: use TagFieldKeyStrokeAdapter as input
    super();
    this.fieldAdapter = fieldAdapter;
    this.which = [keys.DELETE];
    this.renderingHints.render = false;
    this.preventDefault = false;
  }

  _accept(event) {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    return this._$focusedTag().length > 0;
  }

  handle(event) {
    let $tag = this._$focusedTag();
    let tag = TagBar.getTagData($tag);
    this.fieldAdapter.removeTag(tag);
  }

  _$focusedTag() {
    return TagBar.findFocusedTagElement(this.fieldAdapter.$container());
  }
}

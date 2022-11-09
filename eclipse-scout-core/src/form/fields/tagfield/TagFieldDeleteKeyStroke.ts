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
import {keys, KeyStroke, ScoutKeyboardEvent, TagBar, TagFieldKeyStrokeAdapter} from '../../../index';

/**
 * @param fieldAdapter acts as an interface so we can use the same key-stroke for TagField and TagBarOverflowPopup.
 *
 */
export class TagFieldDeleteKeyStroke extends KeyStroke {
  fieldAdapter: TagFieldKeyStrokeAdapter;

  constructor(fieldAdapter: TagFieldKeyStrokeAdapter) {
    super();
    this.fieldAdapter = fieldAdapter;
    this.which = [keys.DELETE];
    this.renderingHints.render = false;
    this.preventDefault = false;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    return this._$focusedTag().length > 0;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let $tag = this._$focusedTag();
    let tag = TagBar.getTagData($tag);
    this.fieldAdapter.removeTag(tag);
  }

  protected _$focusedTag(): JQuery {
    return TagBar.findFocusedTagElement(this.fieldAdapter.$container());
  }
}

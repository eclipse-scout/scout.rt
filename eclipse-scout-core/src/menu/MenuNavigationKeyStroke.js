/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {KeyStroke} from '../index';

export default class MenuNavigationKeyStroke extends KeyStroke {

  constructor(popup) {
    super();
    this.field = popup;
    this.inheritAccessibility = false;
  }

  _accept(event) {
    let accepted = super._accept(event);
    if (!accepted || this.field.bodyAnimating) {
      return false;
    }
    return accepted;
  }

  _changeSelection($oldItem, $newItem) {
    if ($newItem.length === 0) {
      // do not change selection
      return;
    }
    $newItem.select(true).focus();
    if (this.field.updateNextToSelected) {
      this.field.updateNextToSelected(this._menuItemClass, $newItem);
    }
    if ($oldItem.length > 0) {
      $oldItem.select(false);
    }
  }
}

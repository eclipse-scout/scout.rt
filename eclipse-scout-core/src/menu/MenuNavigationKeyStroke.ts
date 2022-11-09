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
import {ContextMenuPopup, KeyStroke, ScoutKeyboardEvent} from '../index';

export class MenuNavigationKeyStroke extends KeyStroke {
  declare field: ContextMenuPopup;

  protected _menuItemClass: string;

  constructor(popup: ContextMenuPopup) {
    super();
    this.field = popup;
    this.inheritAccessibility = false;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted || this.field.bodyAnimating) {
      return false;
    }
    return accepted;
  }

  protected _changeSelection($oldItem: JQuery, $newItem: JQuery) {
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

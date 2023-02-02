/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ContextMenuPopup, keys, MenuNavigationKeyStroke, menuNavigationKeyStrokes} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class MenuNavigationExecKeyStroke extends MenuNavigationKeyStroke {

  constructor(popup: ContextMenuPopup, menuItemClass: string) {
    super(popup);
    this._menuItemClass = menuItemClass;
    this.stopImmediatePropagation = true;
    this.which = [keys.ENTER, keys.SPACE];
    this.renderingHints.render = false;
  }

  override handle(event: KeyboardEventBase) {
    let $menuItem = menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass).$selected;
    if ($menuItem.length > 0) {
      $menuItem.data('widget').doAction();
    }
  }
}

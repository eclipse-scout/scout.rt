/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ContextMenuContainer, keys, MenuNavigationKeyStroke, menuNavigationKeyStrokes} from '../index';

export class MenuNavigationDownKeyStroke extends MenuNavigationKeyStroke {

  constructor(popup: ContextMenuContainer, menuItemClass: string) {
    super(popup);
    this._menuItemClass = menuItemClass;
    this.which = [keys.DOWN];
    this.renderingHints.render = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let menuItems = menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
    if (menuItems.$focused.length > 0) {
      this._changeFocus(menuItems.$focused, menuItems.$focused.nextAll(':visible:not(.disabled)').first());
    } else {
      this._changeFocus(menuItems.$focused, menuItems.$allVisible.first());
    }
  }
}

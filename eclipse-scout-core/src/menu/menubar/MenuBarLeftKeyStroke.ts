/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, MenuBar} from '../../index';

export class MenuBarLeftKeyStroke extends KeyStroke {
  declare field: MenuBar;

  constructor(menuBar: MenuBar) {
    super();
    this.field = menuBar;
    this.which = [keys.LEFT];
    this.renderingHints.render = false;
    this.stopPropagation = true;
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
    this.inheritAccessibility = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let menuItems = this.field.allMenusAsFlatList(),
      $menuItemFocused = this.field.$container.find(':focus'),
      i, menuItem, lastValidItem;

    for (i = 0; i < menuItems.length; i++) {
      menuItem = menuItems[i];
      if ($menuItemFocused[0] === menuItem.$container[0]) {
        if (lastValidItem) {
          this.field.setTabbableMenu(lastValidItem);
          lastValidItem.focus();
        }
        break;
      }
      if (menuItem.isTabTarget()) {
        lastValidItem = menuItem;
      }
    }
  }
}

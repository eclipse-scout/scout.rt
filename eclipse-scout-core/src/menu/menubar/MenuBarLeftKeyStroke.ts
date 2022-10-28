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
import {keys, KeyStroke, MenuBar} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class MenuBarLeftKeyStroke extends KeyStroke {
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

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
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

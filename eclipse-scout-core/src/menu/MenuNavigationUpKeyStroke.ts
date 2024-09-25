/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ContextMenuPopup, keys, MenuNavigationKeyStroke, menuNavigationKeyStrokes} from '../index';

export class MenuNavigationUpKeyStroke extends MenuNavigationKeyStroke {

  constructor(popup: ContextMenuPopup, menuItemClass: string) {
    super(popup);
    this._menuItemClass = menuItemClass;
    this.which = [keys.UP];
    this.renderingHints.render = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let menuItems = menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
    if (menuItems.$selected.length > 0) {
      this._changeSelection(menuItems.$selected, menuItems.$selected.prevAll(':visible').first());
    } else {
      this._changeSelection(menuItems.$selected, menuItems.$allVisible.last());
    }
  }
}

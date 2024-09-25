/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ContextMenuPopup, keys, MenuNavigationExecKeyStroke, menuNavigationKeyStrokes, ScoutKeyboardEvent} from '../index';

export class SubCloseKeyStroke extends MenuNavigationExecKeyStroke {

  constructor(popup: ContextMenuPopup, menuItemClass: string) {
    super(popup, menuItemClass);
    this._menuItemClass = menuItemClass;
    this.which = [keys.BACKSPACE];
    this.renderingHints.render = true;
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & { $menuItem?: JQuery }) => event.$menuItem;
  }

  protected override _accept(event: ScoutKeyboardEvent & { $menuItem?: JQuery }): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    let menuItems = menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass + '.expanded');
    if (menuItems.$all.length > 0) {
      event.$menuItem = menuItems.$all;
      return true;
    }
    return false;
  }

  override handle(event: JQuery.KeyboardEventBase & { $menuItem?: JQuery }) {
    event.$menuItem.data('widget').doAction();
  }
}

/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ContextMenuPopup, HAlign, keys, MenuNavigationExecKeyStroke, menuNavigationKeyStrokes, ScoutKeyboardEvent} from '../index';

export class MenuExecByNumberKeyStroke extends MenuNavigationExecKeyStroke {

  constructor(popup: ContextMenuPopup, menuItemClass: string) {
    super(popup, menuItemClass);
    this._menuItemClass = menuItemClass;
    this.which = [keys[1], keys[2], keys[3], keys[4], keys[5], keys[6], keys[7], keys[8], keys[9]];
    this.renderingHints.render = true;
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & { $menuItem?: JQuery }) => event.$menuItem;
  }

  protected override _accept(event: ScoutKeyboardEvent & { $menuItem?: JQuery }): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    let menuItems = menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
    let index = keys.codesToKeys[event.which];
    event.$menuItem = menuItems.$allVisible.eq(index - 1);
    if (event.$menuItem.length > 0) {
      return true;
    }
    return false;
  }

  override handle(event: JQuery.KeyboardEventBase & { $menuItem?: JQuery }) {
    event.$menuItem.data('widget').doAction();
  }
}

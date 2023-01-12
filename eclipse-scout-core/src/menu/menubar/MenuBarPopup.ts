/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ContextMenuPopup, InitModelOf, Menu, MenuBarPopupModel} from '../../index';

/**
 * The MenuBarPopup is a special Popup that is used in the menu-bar. It is tightly coupled with a menu-item.
 */
export class MenuBarPopup extends ContextMenuPopup implements MenuBarPopupModel {
  declare model: MenuBarPopupModel;

  menu: Menu;

  constructor() {
    super();
    this.menu = null;
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
  }

  protected override _init(options: InitModelOf<this>) {
    options.anchor = options.menu;
    options.closeOnAnchorMouseDown = false;
    super._init(options);
  }

  protected override _render() {
    super._render();
    this.$container.addClass('menu-bar-popup');
  }

  protected override _getMenuItems(): Menu[] {
    return this.menu.childActions;
  }
}

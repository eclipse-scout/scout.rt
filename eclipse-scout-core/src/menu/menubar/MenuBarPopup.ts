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
import {ContextMenuPopup, Menu, MenuBarPopupModel} from '../../index';

/**
 * The MenuBarPopup is a special Popup that is used in the menu-bar. It is tightly coupled with a menu-item.
 */
export default class MenuBarPopup extends ContextMenuPopup implements MenuBarPopupModel {
  declare model: MenuBarPopupModel;

  menu: Menu;

  constructor() {
    super();
    this.menu = null;
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
  }

  protected override _init(options: MenuBarPopupModel) {
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

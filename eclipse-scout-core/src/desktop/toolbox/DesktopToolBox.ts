/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, MenuBox, Popup, strings} from '../../index';

export class DesktopToolBox extends MenuBox {

  constructor() {
    super();
    this.uiMenuCssClass = strings.join(' ', this.uiMenuCssClass, 'desktop-tool-box-item');
  }

  protected override _initMenu(menu: Menu) {
    super._initMenu(menu);
    menu.popupHorizontalAlignment = Popup.Alignment.CENTER;
  }

  protected override _render() {
    super._render();
    this.$container.addClass('desktop-tool-box');
  }
}

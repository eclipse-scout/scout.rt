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

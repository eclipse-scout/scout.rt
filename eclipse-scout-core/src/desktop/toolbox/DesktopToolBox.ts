/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {MenuBox, Popup, strings} from '../../index';

export default class DesktopToolBox extends MenuBox {

  constructor(menuBar) {
    super();
  }

  _init(options) {
    options.uiMenuCssClass = strings.join(' ', options.uiMenuCssClass, 'desktop-tool-box-item');
    super._init(options);
  }

  /**
   * @override
   */
  _initMenu(menu) {
    super._initMenu(menu);
    menu.popupHorizontalAlignment = Popup.Alignment.CENTER;
  }

  /**
   * @override
   */
  _render() {
    super._render();
    this.$container.addClass('desktop-tool-box');
  }
}

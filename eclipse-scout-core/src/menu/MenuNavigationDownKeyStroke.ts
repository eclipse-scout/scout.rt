/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, MenuNavigationKeyStroke, menuNavigationKeyStrokes} from '../index';

export default class MenuNavigationDownKeyStroke extends MenuNavigationKeyStroke {

  constructor(popup, menuItemClass) {
    super(popup);
    this._menuItemClass = menuItemClass;
    this.which = [keys.DOWN];
    this.renderingHints.render = false;
  }

  handle(event) {
    let menuItems = menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
    if (menuItems.$selected.length > 0) {
      this._changeSelection(menuItems.$selected, menuItems.$selected.nextAll(':visible').first());
    } else {
      this._changeSelection(menuItems.$selected, menuItems.$allVisible.first());
    }
  }
}

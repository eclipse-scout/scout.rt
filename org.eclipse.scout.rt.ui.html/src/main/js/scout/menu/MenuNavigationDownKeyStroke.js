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
import {keys} from '../index';
import {MenuNavigationKeyStroke} from '../index';
import {menuNavigationKeyStrokes} from '../index';

export default class MenuNavigationDownKeyStroke extends MenuNavigationKeyStroke {

constructor(popup, menuItemClass) {
  super( popup);
  this._menuItemClass = menuItemClass;
  this.which = [keys.DOWN];
  this.renderingHints.render = false;
}


handle(event) {
  var menuItems = menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  if (menuItems.$selected.length > 0) {
    menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$selected.nextAll(':visible').first());
  } else {
    menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$allVisible.first());
  }
}
}

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
import {keys, MenuNavigationExecKeyStroke, menuNavigationKeyStrokes} from '../index';

export default class SubCloseKeyStroke extends MenuNavigationExecKeyStroke {

  constructor(popup, menuItemClass) {
    super(popup, menuItemClass);
    this._menuItemClass = menuItemClass;
    this.which = [keys.BACKSPACE];
    this.renderingHints.render = true;
    this.renderingHints.$drawingArea = ($drawingArea, event) => event.$menuItem;
  }

  _accept(event) {
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

  handle(event) {
    event.$menuItem.data('widget').doAction();
  }
}

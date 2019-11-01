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
import {HAlign} from '../index';
import {MenuNavigationExecKeyStroke} from '../index';
import {keys} from '../index';
import {menuNavigationKeyStrokes} from '../index';

export default class MenuExecByNumberKeyStroke extends MenuNavigationExecKeyStroke {

constructor(popup, menuItemClass) {
  super( popup, menuItemClass);
  this._menuItemClass = menuItemClass;
  this.which = [keys[1], keys[2], keys[3], keys[4], keys[5], keys[6], keys[7], keys[8], keys[9]];
  this.renderingHints.render = true;
  this.renderingHints.hAlign = HAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event.$menuItem;
  }.bind(this);
}


_accept(event) {
  var accepted = super._accept( event);
  if (!accepted) {
    return false;
  }

  var menuItems = menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  var index = keys.codesToKeys[event.which];
  event.$menuItem = menuItems.$allVisible.eq(index - 1);
  if (event.$menuItem.length > 0) {
    return true;
  }
  return false;
}

handle(event) {
  event.$menuItem.data('widget').doAction();
}
}

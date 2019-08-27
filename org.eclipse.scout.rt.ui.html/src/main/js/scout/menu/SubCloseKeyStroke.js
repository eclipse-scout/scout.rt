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
scout.SubCloseKeyStroke = function(popup, menuItemClass) {
  scout.SubCloseKeyStroke.parent.call(this, popup, menuItemClass);
  this._menuItemClass = menuItemClass;
  this.which = [scout.keys.BACKSPACE];
  this.renderingHints.render = true;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event.$menuItem;
  }.bind(this);
};

scout.inherits(scout.SubCloseKeyStroke, scout.MenuNavigationExecKeyStroke);

scout.SubCloseKeyStroke.prototype._accept = function(event) {
  var accepted = scout.SubCloseKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass + '.expanded');
  if (menuItems.$all.length > 0) {
    event.$menuItem = menuItems.$all;
    return true;
  }
  return false;
};

scout.SubCloseKeyStroke.prototype.handle = function(event) {
  event.$menuItem.data('widget').doAction();
};

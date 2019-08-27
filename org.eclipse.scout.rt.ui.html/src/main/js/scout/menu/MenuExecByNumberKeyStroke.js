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
scout.MenuExecByNumberKeyStroke = function(popup, menuItemClass) {
  scout.MenuExecByNumberKeyStroke.parent.call(this, popup, menuItemClass);
  this._menuItemClass = menuItemClass;
  this.which = [scout.keys[1], scout.keys[2], scout.keys[3], scout.keys[4], scout.keys[5], scout.keys[6], scout.keys[7], scout.keys[8], scout.keys[9]];
  this.renderingHints.render = true;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event.$menuItem;
  }.bind(this);
};
scout.inherits(scout.MenuExecByNumberKeyStroke, scout.MenuNavigationExecKeyStroke);

scout.MenuExecByNumberKeyStroke.prototype._accept = function(event) {
  var accepted = scout.MenuExecByNumberKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  var index = scout.codesToKeys[event.which];
  event.$menuItem = menuItems.$allVisible.eq(index - 1);
  if (event.$menuItem.length > 0) {
    return true;
  }
  return false;
};

scout.MenuExecByNumberKeyStroke.prototype.handle = function(event) {
  event.$menuItem.data('widget').doAction();
};

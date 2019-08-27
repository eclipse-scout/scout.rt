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
scout.MenuNavigationDownKeyStroke = function(popup, menuItemClass) {
  scout.MenuNavigationDownKeyStroke.parent.call(this, popup);
  this._menuItemClass = menuItemClass;
  this.which = [scout.keys.DOWN];
  this.renderingHints.render = false;
};
scout.inherits(scout.MenuNavigationDownKeyStroke, scout.MenuNavigationKeyStroke);

scout.MenuNavigationDownKeyStroke.prototype.handle = function(event) {
  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  if (menuItems.$selected.length > 0) {
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$selected.nextAll(':visible').first());
  } else {
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$allVisible.first());
  }
};

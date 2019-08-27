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
scout.MenuNavigationExecKeyStroke = function(popup, menuItemClass) {
  scout.MenuNavigationExecKeyStroke.parent.call(this, popup);
  this._menuItemClass = menuItemClass;
  this.stopImmediatePropagation = true;
  this.which = [scout.keys.ENTER, scout.keys.SPACE];
  this.renderingHints.render = false;
};
scout.inherits(scout.MenuNavigationExecKeyStroke, scout.MenuNavigationKeyStroke);

scout.MenuNavigationExecKeyStroke.prototype.handle = function(event) {
  var $menuItem = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass).$selected;
  if ($menuItem.length > 0) {
    $menuItem.data('widget').doAction();
  }
};

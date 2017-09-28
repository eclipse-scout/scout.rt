/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MenuBarRightKeyStroke = function(menuBar) {
  scout.MenuBarRightKeyStroke.parent.call(this);
  this.field = menuBar;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.MenuBarRightKeyStroke, scout.KeyStroke);

scout.MenuBarRightKeyStroke.prototype.handle = function(event) {
  var menuItems = this.field.visibleMenuItems,
    $menuItemFocused = this.field.$container.find(':focus'),
    i, menuItem, focusNext = false;

  for (i = 0; i < menuItems.length; i++) {
    menuItem = menuItems[i];
    if (focusNext && menuItem.isTabTarget()) {
      this.field.setTabbableMenu(menuItem);
      menuItem.focus();
      break;
    }
    if ($menuItemFocused[0] === menuItem.$container[0]) {
      focusNext = true;
    }
  }
};

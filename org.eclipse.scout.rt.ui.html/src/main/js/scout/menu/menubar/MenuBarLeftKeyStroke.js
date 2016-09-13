/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MenuBarLeftKeyStroke = function(menuBar) {
  scout.MenuBarLeftKeyStroke.parent.call(this);
  this.field = menuBar;
  this.which = [scout.keys.LEFT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.MenuBarLeftKeyStroke, scout.KeyStroke);

scout.MenuBarLeftKeyStroke.prototype.handle = function(event) {
  var menuItems = this.field.visibleMenuItems,
    $menuItemFocused = this.field.$container.find(':focus'),
    i, menuItem, lastValidItem;

  for (i = 0; i < menuItems.length; i++) {
    menuItem = menuItems[i];
    if ($menuItemFocused[0] === menuItem.$container[0]) {
      if (lastValidItem) {
        this.field.setTabbableMenu(lastValidItem);
        this.field.session.focusManager.requestFocus(lastValidItem.$container);
      }
      break;
    }
    if (menuItem.isTabTarget()) {
      lastValidItem = menuItem;
    }
  }
};

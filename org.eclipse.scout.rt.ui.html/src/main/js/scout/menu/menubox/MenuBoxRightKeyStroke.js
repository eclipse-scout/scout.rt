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
scout.MenuBoxRightKeyStroke = function(menuBox) {
  scout.MenuBoxRightKeyStroke.parent.call(this);
  this.field = menuBox;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.MenuBoxRightKeyStroke, scout.KeyStroke);

scout.MenuBoxRightKeyStroke.prototype.handle = function(event) {
  var menuBox = this.field;

  var currentMenu = menuBox.$container.find(':focus').data('widget');
  if (!currentMenu) {
    return;
  }

  var nextMenu,
    ellipsisMenu = menuBox.htmlComp.layout._ellipsis;
  if (currentMenu !== ellipsisMenu) {
    var visibleMenus = menuBox.visibleMenus();
    var currentMenuIndex = visibleMenus.indexOf(currentMenu);
    if (currentMenuIndex !== -1) {
      // Try next menu
      nextMenu = scout.arrays.find(visibleMenus, function(menu, index) {
        return index > currentMenuIndex && menu.isTabTarget();
      });
      // Try "ellipsis" menu
      if (!nextMenu && ellipsisMenu && ellipsisMenu.isTabTarget()) {
        nextMenu = ellipsisMenu;
      }
    }
  }
  // Try "next" menuBox
  if (!nextMenu && menuBox.nextMenuBox) {
    nextMenu = scout.arrays.find(menuBox.nextMenuBox.visibleMenus(), function(menu) {
      return menu.isTabTarget();
    });
  }

  if (nextMenu) {
    currentMenu.setTabbable(false);
    nextMenu.setTabbable(true);
    menuBox.session.focusManager.requestFocus(nextMenu.$container);
  }
};

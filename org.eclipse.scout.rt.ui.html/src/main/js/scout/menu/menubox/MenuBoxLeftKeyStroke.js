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
scout.MenuBoxLeftKeyStroke = function(menuBox) {
  scout.MenuBoxLeftKeyStroke.parent.call(this);
  this.field = menuBox;
  this.which = [scout.keys.LEFT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.MenuBoxLeftKeyStroke, scout.KeyStroke);

scout.MenuBoxLeftKeyStroke.prototype.handle = function(event) {
  var menuBox = this.field;

  var currentMenu = menuBox.$container.find(':focus').data('widget');
  if (!currentMenu) {
    return;
  }

  var prevMenu,
    ellipsisMenu = menuBox.htmlComp.layout._ellipsis,
    visibleMenus = menuBox.visibleMenus().reverse(), // reversed!
    currentMenuIndex = visibleMenus.indexOf(currentMenu);

  // Try "previous" menu
  prevMenu = scout.arrays.find(visibleMenus, function(menu, index) {
    var leftOfCurrent = (currentMenu === ellipsisMenu || index > currentMenuIndex);
    return leftOfCurrent && menu.isTabTarget();
  });
  // Try "previous" menuBox
  if (!prevMenu && menuBox.prevMenuBox) {
    var prevEllipsisMenu = menuBox.prevMenuBox.htmlComp.layout._ellipsis;
    if (prevEllipsisMenu && prevEllipsisMenu.isTabTarget()) {
      prevMenu = prevEllipsisMenu;
    } else {
      prevMenu = scout.arrays.find(menuBox.prevMenuBox.visibleMenus().reverse(), function(menu) {
        return menu.isTabTarget();
      });
    }
  }

  if (prevMenu) {
    currentMenu.setTabbable(false);
    prevMenu.setTabbable(true);
    menuBox.session.focusManager.requestFocus(prevMenu.$container);
  }
};

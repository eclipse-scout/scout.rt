/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.MenubarBoxLayout = function(menubox) {
  scout.MenubarBoxLayout.parent.call(this);
  this.menubox = menubox;
};
scout.inherits(scout.MenubarBoxLayout, scout.AbstractLayout);

scout.MenubarBoxLayout.prototype.layout = function($container) {
  // void since the menu items are floated inline block.
};

scout.MenubarBoxLayout.prototype.preferredLayoutSize = function($container, options) {
  var menuItemSize = null;

  return this.menubox.menuItems.filter(function(menuItem) {
    return !menuItem.overflown && menuItem.isVisible();
  }).reduce(function(prefSize, menuItem) {
    menuItemSize = menuItem.htmlComp.prefSize({
      useCssSize: true,
      includeMargin: true
    });
    prefSize.height = Math.max(prefSize.height, menuItemSize.height);
    prefSize.width = Math.max(prefSize.width, menuItemSize.width);
    return prefSize;
  }, new scout.Dimension());
};

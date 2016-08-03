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
scout.MenuBoxLayout = function(menuBox) {
  scout.MenuBoxLayout.parent.call(this);
  this.menuBox = menuBox;
};
scout.inherits(scout.MenuBoxLayout, scout.AbstractLayout);

/**
 * @override AbstractLayout.js
 */
scout.MenuBoxLayout.prototype.layout = function($container) {
  this._layout($container);

  this.visibleMenus().forEach(function(menu) {
    // Make sure open popups are at the correct position after layouting
    if (menu.popup) {
      menu.popup.position();
    }
  });
};

scout.MenuBoxLayout.prototype._layout = function($container) {
  var htmlContainer = this.menuBox.htmlComp,
    containerSize = htmlContainer.getSize(),
    menus = this.visibleMenus(),
    menusWidth = 0;

  this.undoCollapse(menus);
  this.undoCompact(menus);
  this.undoShrink(menus);
  menusWidth = this.actualPrefSize(menus).width;
  if (menusWidth <= containerSize.width) {
    // OK, every menu fits into container
    return;
  }

  // Menus don't fit

  // First approach: Set menuBox into compact mode
  this.compact(menus);
  menusWidth = this.actualPrefSize(menus).width;
  if (menusWidth <= containerSize.width) {
    // OK, every menu fits into container
    return;
  }

  // Second approach: Make text invisible and only show the icon (if available)
  this.shrink(menus);
  menusWidth = this.actualPrefSize(menus).width;
  if (menusWidth <= containerSize.width) {
    // OK, every menu fits into container
    return;
  }

  // Third approach: Create ellipsis and move overflown menus into it
  this.collapse(menus, containerSize, menusWidth);
};

scout.MenuBoxLayout.prototype.preferredLayoutSize = function($container) {
  var menus = this.visibleMenus();

  this.undoCollapse(menus);
  this.undoCompact(menus);
  this.undoShrink(menus);

  return this.actualPrefSize();
};

scout.MenuBoxLayout.prototype.compact = function(menus) {
  if (this.menuBox.compactOrig === undefined) {
    this.menuBox.compactOrig = this.compact;
    this.menuBox.htmlComp.suppressInvalidate = true;
    this.menuBox.setCompact(true);
    this.menuBox.htmlComp.suppressInvalidate = false;
  }

  this.compactMenus(menus);
};

scout.MenuBoxLayout.prototype.undoCompact = function(menus) {
  if (this.menuBox.compactOrig !== undefined) {
    this.menuBox.htmlComp.suppressInvalidate = true;
    this.menuBox.setCompact(this.compactOrig);
    this.menuBox.htmlComp.suppressInvalidate = false;
    this.menuBox.compactOrig = undefined;
  }

  this.undoCompactMenus(menus);
};

/**
 * Sets all menus into compact mode.
 */
scout.MenuBoxLayout.prototype.compactMenus = function(menus) {
  menus = menus || this.visibleMenus();
  menus.forEach(function(menu) {
    if (menu.compactOrig !== undefined) {
      // already done
      return;
    }
    menu.compactOrig = menu.compact;
    menu.htmlComp.suppressInvalidate = true;
    menu.setCompact(true);
    menu.htmlComp.suppressInvalidate = false;
  }, this);

  if (this._ellipsis) {
    this._ellipsis.setCompact(true);
  }
};

/**
 * Restores to the previous state of the compact property.
 */
scout.MenuBoxLayout.prototype.undoCompactMenus = function(menus) {
  menus = menus || this.visibleMenus();
  menus.forEach(function(menu) {
    if (menu.compactOrig === undefined) {
      return;
    }
    // Restore old compact state
    menu.htmlComp.suppressInvalidate = true;
    menu.setCompact(menu.compactOrig);
    menu.htmlComp.suppressInvalidate = false;
    menu.compactOrig = undefined;
  }, this);

  if (this._ellipsis) {
    this._ellipsis.setCompact(false);
  }
};

scout.MenuBoxLayout.prototype.shrink = function(menus) {
  this.shrinkMenus(menus);
};

/**
 * Makes the text invisible of all menus with an icon.
 */
scout.MenuBoxLayout.prototype.shrinkMenus = function(menus) {
  menus = menus || this.visibleMenus();
  menus.forEach(function(menu) {
    if (menu.textVisibleOrig !== undefined) {
      // already done
      return;
    }
    if (menu.iconId) {
      menu.textVisibleOrig = menu.textVisible;
      menu.htmlComp.suppressInvalidate = true;
      menu.setTextVisible(false);
      menu.htmlComp.suppressInvalidate = false;
    }
  }, this);
};

scout.MenuBoxLayout.prototype.undoShrink = function(menus) {
  this.undoShrinkMenus(menus);
};

scout.MenuBoxLayout.prototype.undoShrinkMenus = function(menus) {
  menus = menus || this.visibleMenus();
  menus.forEach(function(menu) {
    if (menu.textVisibleOrig === undefined) {
      return;
    }
    // Restore old text visible state
    menu.htmlComp.suppressInvalidate = true;
    menu.setTextVisible(menu.textVisibleOrig);
    menu.htmlComp.suppressInvalidate = false;
    menu.textVisibleOrig = undefined;
  }, this);
};

scout.MenuBoxLayout.prototype.collapse = function(menus, containerSize, menusWidth) {
  this._createAndRenderEllipsis(this.menuBox.$container);
  this._moveOverflowMenusIntoEllipsis(containerSize, menusWidth);
};

/**
 * Undoes the collapsing by removing ellipsis and rendering non rendered menus.
 */
scout.MenuBoxLayout.prototype.undoCollapse = function(menus) {
  menus = menus || this.visibleMenus();
  this._destroyEllipsis();
  this._removeMenusFromEllipsis(menus, this.menuBox.$container);
};

scout.MenuBoxLayout.prototype._createAndRenderEllipsis = function($container) {
  var ellipsis = scout.menus.createEllipsisMenu({
    parent: this.menuBox,
    horizontalAlignment: 1,
    compact: this.menuBox.compact
  });
  ellipsis.uiCssClass = this.menuBox.uiMenuCssClass;
  ellipsis.render($container);
  this._ellipsis = ellipsis;
};

scout.MenuBoxLayout.prototype._destroyEllipsis = function() {
  if (this._ellipsis) {
    this._ellipsis.destroy();
    this._ellipsis = null;
  }
};

/**
 * Moves every menu which doesn't fit into the container into the ellipsis menu
 */
scout.MenuBoxLayout.prototype._moveOverflowMenusIntoEllipsis = function(containerSize, menusWidth) {
  var ellipsisSize = scout.graphics.getSize(this._ellipsis.$container, true);
  menusWidth += ellipsisSize.width;
  this.visibleMenus().slice().reverse().forEach(function(menu) {
    var menuSize;
    if (menusWidth > containerSize.width) {
      // Menu does not fit -> move to ellipsis menu
      menuSize = scout.graphics.getSize(menu.$container, true);
      menusWidth -= menuSize.width;
      scout.menus.moveMenuIntoEllipsis(menu, this._ellipsis);
    }
  }, this);
};

scout.MenuBoxLayout.prototype._removeMenusFromEllipsis = function(menus) {
  menus = menus || this.visibleMenus();
  menus.forEach(function(menu) {
    scout.menus.removeMenuFromEllipsis(menu, this.menuBox.$container);
  }, this);
};

scout.MenuBoxLayout.prototype.actualPrefSize = function(menus) {
  var menusWidth, prefSize;

  menus = menus || this.visibleMenus();
  menusWidth = this._menusWidth(menus);
  prefSize = scout.graphics.prefSize(this.menuBox.$container, {
    includeMargin: true,
    useCssSize: true
  });
  prefSize.width = menusWidth + this.menuBox.htmlComp.getInsets().horizontal();

  return prefSize;
};

/**
 * @return the current width of all menus incl. the ellipsis
 */
scout.MenuBoxLayout.prototype._menusWidth = function(menus) {
  var menusWidth = 0;
  menus = menus || this.visibleMenus();
  menus.forEach(function(menu) {
    if (menu.rendered) {
      menusWidth += menu.$container.outerWidth(true);
    }
  }, this);
  if (this._ellipsis) {
    menusWidth += this._ellipsis.$container.outerWidth(true);
  }
  return menusWidth;
};

scout.MenuBoxLayout.prototype.compactPrefSize = function(menus) {
  menus = menus || this.visibleMenus();

  this.undoCollapse(menus);
  this.undoShrink(menus);
  this.compact(menus);

  return this.actualPrefSize();
};

scout.MenuBoxLayout.prototype.shrinkPrefSize = function(menus) {
  menus = menus || this.visibleMenus();

  this.undoCollapse(menus);
  this.compact(menus);
  this.shrink(menus);

  return this.actualPrefSize();
};

scout.MenuBoxLayout.prototype.visibleMenus = function() {
  return this.menuBox.menus.filter(function(menu) {
    return menu.visible;
  }, this);
};

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
  this.state = {};
  this.menuBox.on('removeAllMenus', this._onMenuBoxAllMenusRemoved.bind(this));
};
scout.inherits(scout.MenuBoxLayout, scout.AbstractLayout);

/**
 * @override AbstractLayout.js
 */
scout.MenuBoxLayout.prototype.layout = function($container) {
  var oldState = this._backupState();

  this.layoutInternal($container);

  if (oldState.compact !== this.state.compact ||
      oldState.shrink !== this.state.shrink ||
      oldState.collapse !== this.state.collapse) {
    this.menuBox.trigger('updateVisibleMenus');
  }
  if (oldState.collapse !== this.state.collapse) {
    this.updateLeftOfButtonMarker();
  }
};

scout.MenuBoxLayout.prototype.layoutInternal = function($container) {
  var htmlContainer = this.menuBox.htmlComp,
    containerSize = htmlContainer.getSize(),
    menus = this.menuBox.visibleMenus(),
    menusWidth = 0;

  // Make sure open popups are at the correct position after layouting
  this.menuBox.session.layoutValidator.schedulePostValidateFunction(function() {
    menus.forEach(function(menu) {
      if (menu.popup) {
        menu.popup.position();
      }
    });
  }.bind(this));

  this.undoCollapse(menus);
  this.undoShrink(menus);
  this.undoCompact(menus);
  this.updateFirstAndLastMenuMarker(menus);

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
  this.collapse(menus, containerSize);
};

scout.MenuBoxLayout.prototype.preferredLayoutSize = function($container) {
  var menus = this.menuBox.visibleMenus();

  var oldState = this._backupState();

  this.undoCollapse(menus);
  this.undoShrink(menus);
  this.undoCompact(menus);
  this.updateFirstAndLastMenuMarker(menus);

  var prefSize = this.actualPrefSize();

  this._restoreState(oldState);
  return prefSize;
};

scout.MenuBoxLayout.prototype.compact = function(menus) {
  if (this.menuBox.compactOrig === undefined) {
    this.menuBox.compactOrig = this.compact;
    this.menuBox.htmlComp.suppressInvalidate = true;
    this.menuBox.setCompact(true);
    this.menuBox.htmlComp.suppressInvalidate = false;
  }

  this.compactMenus(menus);
  this.state.compact = true;
};

scout.MenuBoxLayout.prototype.undoCompact = function(menus) {
  if (this.menuBox.compactOrig !== undefined) {
    this.menuBox.htmlComp.suppressInvalidate = true;
    this.menuBox.setCompact(this.compactOrig);
    this.menuBox.htmlComp.suppressInvalidate = false;
    this.menuBox.compactOrig = undefined;
  }

  this.undoCompactMenus(menus);
  this.state.compact = false;
};

/**
 * Sets all menus into compact mode.
 */
scout.MenuBoxLayout.prototype.compactMenus = function(menus) {
  menus = menus || this.menuBox.visibleMenus();
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
  menus = menus || this.menuBox.visibleMenus();
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
  if (!this.menuBox.shrinkable) {
    return;
  }
  this.shrinkMenus(menus);
  this.state.shrink = true;
};

/**
 * Makes the text invisible of all menus with an icon.
 */
scout.MenuBoxLayout.prototype.shrinkMenus = function(menus) {
  menus = menus || this.menuBox.visibleMenus();
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
  this.state.shrink = false;
};

scout.MenuBoxLayout.prototype.undoShrinkMenus = function(menus) {
  menus = menus || this.menuBox.visibleMenus();
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

scout.MenuBoxLayout.prototype.collapse = function(menus, containerSize) {
  this._createAndRenderEllipsis(this.menuBox.$container);
  var collapsedMenus = this._moveOverflowMenusIntoEllipsis(menus, containerSize);
  this.updateFirstAndLastMenuMarker(collapsedMenus);
  this.state.collapse = true;
  this.state.collapseContainerSize = containerSize;
};

/**
 * Undoes the collapsing by removing ellipsis and rendering non rendered menus.
 */
scout.MenuBoxLayout.prototype.undoCollapse = function(menus) {
  menus = menus || this.menuBox.visibleMenus();
  this._destroyEllipsis();
  this._removeMenusFromEllipsis(menus, this.menuBox.$container);
  this.state.collapse = false;
  this.state.collapseContainerSize = null;
};

scout.MenuBoxLayout.prototype._createAndRenderEllipsis = function($parent) {
  var ellipsis = scout.menus.createEllipsisMenu({
    parent: this.menuBox,
    horizontalAlignment: 1,
    compact: this.menuBox.compact
  });
  ellipsis.uiCssClass = this.menuBox.uiMenuCssClass;
  ellipsis.render($parent);
  this._ellipsis = ellipsis;

  if (this.lastMenu) {
    if (this.lastMenu.rendered) {
      this.lastMenu.$container.removeClass('last');
    }
  }
  this.lastMenu = this._ellipsis;
  this.lastMenu.$container.addClass('last');
};

scout.MenuBoxLayout.prototype._destroyEllipsis = function() {
  if (this._ellipsis) {
    this._ellipsis.destroy();
    this._ellipsis = null;
  }
};

/**
 * Moves every menu which doesn't fit into the container into the ellipsis menu.
 * Returns the list of "surviving" menus (with the ellipsis menu being the last element).
 */
scout.MenuBoxLayout.prototype._moveOverflowMenusIntoEllipsis = function(menus, containerSize) {
  var menusWidth = this.actualPrefSize(menus).width; // already includes width of ellipsis menu

  var collapsedMenus = [this._ellipsis];
  menus.reverse().forEach(function(menu) {
    var menuSize;
    if (menusWidth > containerSize.width) {
      // Menu does not fit -> move to ellipsis menu
      menuSize = scout.graphics.getSize(menu.$container, true);
      menusWidth -= menuSize.width;
      scout.menus.moveMenuIntoEllipsis(menu, this._ellipsis);
    } else {
      collapsedMenus.unshift(menu); // add as first element
    }
  }, this);
  return collapsedMenus;
};

scout.MenuBoxLayout.prototype._removeMenusFromEllipsis = function(menus) {
  menus = menus || this.menuBox.visibleMenus();
  menus.forEach(function(menu) {
    scout.menus.removeMenuFromEllipsis(menu, this.menuBox.$container);
  }, this);
};

scout.MenuBoxLayout.prototype.actualPrefSize = function(menus) {
  var menusWidth, prefSize;

  menus = menus || this.menuBox.visibleMenus();
  menusWidth = this._menusWidth(menus);
  prefSize = scout.graphics.prefSize(this.menuBox.$container, true, {
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
  menus = menus || this.menuBox.visibleMenus();
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
  menus = menus || this.menuBox.visibleMenus();

  var oldState = this._backupState();

  this.undoCollapse(menus);
  this.undoShrink(menus);
  this.compact(menus);
  this.updateFirstAndLastMenuMarker(menus);

  var prefSize = this.actualPrefSize();

  this._restoreState(oldState);
  return prefSize;
};

scout.MenuBoxLayout.prototype.shrinkPrefSize = function(menus) {
  menus = menus || this.menuBox.visibleMenus();

  var oldState = this._backupState();

  this.undoCollapse(menus);
  this.compact(menus);
  this.shrink(menus);
  this.updateFirstAndLastMenuMarker(menus);

  var prefSize = this.actualPrefSize();

  this._restoreState(oldState);
  return prefSize;
};

scout.MenuBoxLayout.prototype.updateFirstAndLastMenuMarker = function(menus) {
  // Find first and last rendered menu
  var firstMenu = null;
  var lastMenu = null;
  (menus || []).forEach(function(menu) {
    if (menu.rendered) {
      if (!firstMenu) {
        firstMenu = menu;
      }
      lastMenu = menu;
    }
  });
  this._markFirstAndLastMenu(firstMenu, lastMenu);
};

scout.MenuBoxLayout.prototype._markFirstAndLastMenu = function(firstMenu, lastMenu) {
  // Check if first or last menu has changed (prevents unnecessary DOM updates)
  if (firstMenu !== this.state.firstMenu || lastMenu !== this.state.lastMenu) {
    // Remove existing markers
    if (this.state.firstMenu && this.state.firstMenu.rendered) {
      this.state.firstMenu.$container.removeClass('first');
    }
    if (this.state.lastMenu && this.state.lastMenu.rendered) {
      this.state.lastMenu.$container.removeClass('last');
    }
    this.state.firstMenu = null;
    this.state.lastMenu = null;
    // Remember menus and add markers (if they are rendered)
    if (firstMenu && firstMenu.rendered) {
      this.state.firstMenu = firstMenu;
      this.state.firstMenu.$container.addClass('first');
    }
    if (lastMenu && lastMenu.rendered) {
      this.state.lastMenu = lastMenu;
      this.state.lastMenu.$container.addClass('last');
    }
  }
};

scout.MenuBoxLayout.prototype._onMenuBoxAllMenusRemoved = function(event) {
  // clear internal state
  this._destroyEllipsis();
  this.state = {};
};

scout.MenuBoxLayout.prototype._backupState = function() {
  var state = {};
  scout.objects.copyOwnProperties(this.state, state);
  return state;
};

scout.MenuBoxLayout.prototype._restoreState = function(oldState) {
  var menus = this.menuBox.visibleMenus();

  if (oldState) {
    if (oldState.compact !== this.state.compact) {
      if (oldState.compact) {
        this.compact(menus);
      } else {
        this.undoCompact(menus);
      }
    }
    if (oldState.shrink !== this.state.shrink) {
      if (oldState.shrink) {
        this.shrink(menus);
      } else {
        this.undoShrink(menus);
      }
    }
    if (oldState.collapse !== this.state.collapse) {
      if (oldState.collapse) {
        this.collapse(menus, oldState.collapseContainerSize);
      } else {
        this.undoCollapse(menus);
      }
    }
    this._markFirstAndLastMenu(oldState.firstMenu, oldState.lastMenu);
  }
};

/**
 * Add class 'left-of-button' to every menu item which is on the left of a button
 */
scout.MenuBoxLayout.prototype.updateLeftOfButtonMarker = function() {
  var items = this.menuBox.visibleMenus(false); // false = exclude unrendered menus
  var prevItem = null;
  for (var i = 0; i < items.length; i++) {
    var item = items[i];
    item.$container.removeClass('left-of-button');
    if (prevItem && item.isButton()) {
      prevItem.$container.addClass('left-of-button');
    }
    prevItem = item;
  }
};

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
scout.MenuBarLayout = function(menuBar) {
  scout.MenuBarLayout.parent.call(this);
  this._menuBar = menuBar;

  this._overflowMenuItems = [];
  this._visibleMenuItems = [];
  this._ellipsis = null;
  this.collapsed = false;
};
scout.inherits(scout.MenuBarLayout, scout.AbstractLayout);

scout.MenuBarLayout.prototype.layout = function($container) {
  var menuItems = this._menuBar.orderedMenuItems.left.concat(this._menuBar.orderedMenuItems.right),
    htmlContainer = scout.HtmlComponent.get($container),
    ellipsis;

  ellipsis = scout.arrays.find(menuItems, function(menuItem) {
    return menuItem.ellipsis;
  });

  this.preferredLayoutSize($container, {
    widthHint: htmlContainer.availableSize().width
  });

  // first set visible to ensure the correct menu gets the tabindex. Therefore the ellipsis visibility is split.
  if (ellipsis && this._overflowMenuItems.length > 0) {
    ellipsis.setHidden(false);
  }
  this._visibleMenuItems.forEach(function(menuItem) {
    menuItem._setOverflown(false);
    menuItem.setParent(this._menuBar);
  }, this);

  this._overflowMenuItems.forEach(function(menuItem) {
    menuItem._setOverflown(true);
  });
  if (ellipsis && this._overflowMenuItems.length === 0) {
    ellipsis.setHidden(true);
  }
  // remove all separators
  this._overflowMenuItems = this._overflowMenuItems.filter(function(menuItem) {
    return !menuItem.separator;
  });

  // set childActions to empty array to prevent the menuItems from calling remove.
  if (ellipsis) {
    ellipsis._closePopup();
    /* workaround to ensure current child action will not be removed when setting the new ones.
     * This workaround and also the setParent on all visible menu items (see above) can be removed
     * with the context menu clean up planned in the UI team.
     */
    ellipsis.childActions = [];
    ellipsis.setChildActions(this._overflowMenuItems);
  }

  // trigger menu items layout
  this._visibleMenuItems.forEach(function(menuItem) {
    menuItem.validateLayout();
  });

  this._visibleMenuItems.forEach(function(menuItem) {
    // Make sure open popups are at the correct position after layouting
    if (menuItem.popup) {
      menuItem.popup.position();
    }
  });
};

scout.MenuBarLayout.prototype.preferredLayoutSize = function($container, options) {
  if (!this._menuBar.isVisible()) {
    return new scout.Dimension(0, 0);
  }

  var htmlContainer = scout.HtmlComponent.get($container),
    menuItems = this._menuBar.orderedMenuItems.all,
    preferredSize = new scout.Dimension(),
    ellipsis,
    ellipsisBoundsGross = new scout.Dimension(),

    _widthHintFilter = function(itemWidth, totalWidth, wHint) {
      // escape truthy
      if (wHint === 0 || wHint) {
        return totalWidth + itemWidth <= wHint;
      }
      return true;
    },
    unorderedVisibleMenus = [];

  if (options.widthHint) {
    options.widthHint = options.widthHint - htmlContainer.insets().horizontal();
  }
  this._visibleMenuItems = [];
  this._overflowMenuItems = [];

  // update first last best guess
  this._bestGuessFirstLast(menuItems);

  this._ensureCachedBounds(menuItems);

  ellipsis = scout.arrays.find(menuItems, function(menuItem) {
    return menuItem.ellipsis;
  });
  if (ellipsis) {
    ellipsisBoundsGross = ellipsis.htmlComp._cachedPrefSize.add(ellipsis.htmlComp._cachedMargins);
    preferredSize.width += ellipsisBoundsGross.width;
  }

  // first all non stackable menus which are always visible
  menuItems.filter(function(menuItem) {
    var prefSizeGross;
    if (!menuItem.isVisible()) {
      return false;
    }
    if (ellipsis && ellipsis === menuItem) {
      return false;
    }
    if (!menuItem.stackable) {
      prefSizeGross = menuItem.htmlComp._cachedPrefSize.add(menuItem.htmlComp._cachedMargins);
      preferredSize.width += prefSizeGross.width;
      preferredSize.height = Math.max(preferredSize.height, prefSizeGross.height);
      unorderedVisibleMenus.push(menuItem);
      return false;
    }
    return true;
  }, this).forEach(function(menuItem, index, items) {
    var prefSizeGross = menuItem.htmlComp._cachedPrefSize.add(menuItem.htmlComp._cachedMargins),
      totalWidth = preferredSize.width;
    if (ellipsis && index === items.length - 1 && this._overflowMenuItems.length === 0) {
      totalWidth -= ellipsisBoundsGross.width;
    }
    if (!this.collapsed && this._overflowMenuItems.length === 0 && _widthHintFilter(prefSizeGross.width, totalWidth, options.widthHint)) {
      preferredSize.width += prefSizeGross.width;
      preferredSize.height = Math.max(preferredSize.height, prefSizeGross.height);
      unorderedVisibleMenus.push(menuItem);
    } else {
      this._overflowMenuItems.push(menuItem);
    }
  }, this);

  if (ellipsis) {
    if (this._overflowMenuItems.length === 0) {
      preferredSize.width -= ellipsisBoundsGross.width;
    } else {
      unorderedVisibleMenus.push(ellipsis);
      preferredSize.height = Math.max(preferredSize.height, ellipsisBoundsGross.height);
    }
  }
  // well order and push to visible menu items
  menuItems.forEach(function(menuItem) {
    if (unorderedVisibleMenus.indexOf(menuItem) >= 0) {
      this._visibleMenuItems.push(menuItem);
    }
  }, this);

  this._updateFirstLastMenuMarker();

  return preferredSize.add(htmlContainer.insets());
};

scout.MenuBarLayout.prototype.invalidate = function() {
  var menuItems = this._menuBar.orderedMenuItems.all;
  menuItems.forEach(function(menuItem) {
    if (menuItem.rendered) {
      menuItem.htmlComp._cachedPrefSize = null;
      menuItem.htmlComp._cachedMargins = null;
    }
  });
};

scout.MenuBarLayout.prototype._ensureCachedBounds = function(menuItems) {
  var classList;
  menuItems.filter(function(menuItem) {
    return menuItem.isVisible();
  }).forEach(function(menuItem) {
    if (!menuItem.htmlComp._cachedPrefSize || !menuItem.htmlComp._cachedMargins) {
      classList = menuItem.$container.attr('class');

      menuItem.$container.removeClass('overflown');
      menuItem.$container.removeClass('hidden');

      menuItem.htmlComp._cachedPrefSize = menuItem.htmlComp.prefSize({
        useCssSize: true,
        exact: true
      });
      menuItem.htmlComp._cachedMargins = scout.graphics.margins(menuItem.$container);
      menuItem.$container.attrOrRemove('class', classList);
    }
  });
};

scout.MenuBarLayout.prototype._bestGuessFirstLast = function(menuItems) {
  var ellipsis;
  menuItems = menuItems.filter(function(menuItem) {
    menuItem.$container.removeClass('first last');
    return menuItem.isVisible();
  });

  scout.arrays.find(menuItems.slice().reverse(), function(menuItem) {
    if (menuItem.ellipsis) {
      ellipsis = menuItem;
      ellipsis.$container.addClass('last');
      return false;
    } else if (ellipsis) {
      menuItem.$container.addClass('last');
      return true;
    } else {
      menuItem.$container.addClass('last');
      return true;
    }
  }, this);

  scout.arrays.first(menuItems).$container.addClass('first');
};

scout.MenuBarLayout.prototype._updateFirstLastMenuMarker = function() {
  this._visibleMenuItems.forEach(function(menuItem, index, arr) {
    if (index === 0) {
      menuItem.$container.addClass('first');
    } else if (index === arr.length - 1) {
      menuItem.$container.addClass('last');
    } else {
      menuItem.$container.removeClass('first last');
    }
  });
  this._overflowMenuItems.forEach(function(menuItem) {
    menuItem.$container.removeClass('first last');
  });
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.MenuBarLayout
 */
scout.MenuBarLayout.size = function(htmlMenuBar, containerSize) {
  var menuBarSize = htmlMenuBar.prefSize();
  menuBarSize.width = containerSize.width;
  menuBarSize = menuBarSize.subtract(htmlMenuBar.margins());
  return menuBarSize;
};

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
scout.MenuBarLayout = function(menuBar) {
  scout.MenuBarLayout.parent.call(this);
  this._menuBar = menuBar;
};
scout.inherits(scout.MenuBarLayout, scout.AbstractLayout);

/**
 * @override AbstractLayout.js
 */
scout.MenuBarLayout.prototype.layout = function($container) {
  // check if all menu items have enough room to be displayed without ellipsis
  this._destroyEllipsis();
  this._menuBar.rebuildItemsInternal();

  // Temporarily add "overflow: hidden" to measure the available size. (The overflow
  // is not hidden in the CSS, otherwise the focus border could get cut off.)
  var oldStyle = $container.attr('style');
  $container.css('overflow', 'hidden');
  $container.css('display', 'inline-block'); // override "display: table"
  var availableWidth = $container.width();
  $container.attrOrRemove('style', oldStyle);

  var leftWidth = this._menuBar.$left.width();
  var rightWidth = this._menuBar.$right.width();

  // use availableWidth + 1 because of rounding problems in JQuery
  if (leftWidth + rightWidth <= availableWidth + 1) {
    // ok, no ellisis required
    this._menuBar.visibleMenuItems = this._menuBar.menuItems;
  } else {
    // create ellipsis menu
    this._createAndRenderEllipsis(this._menuBar.$left, rightWidth === 0);
    var ellipsisSize = scout.graphics.getSize(this._menuBar.ellipsis.$container, true);

    var remainingLeftWidth = Math.min(availableWidth - rightWidth, leftWidth);

    // right-aligned menus are never put into the overflow ellipsis-menu
    // or in other words: they're not responsive.
    // for left-aligned menus: once we notice a menu-item that does not
    // fit into the available space, all following items must also be
    // put into the overflow ellipsis-menu. Otherwise you would put
    // an item with a long text into the ellipsis-menu, but the next
    // icon, with a short text would still be in the menu-bar. Which would
    // be confusing, as it would look like we've changed the order of the
    // menu-items.
    var menuItemsCopy = [];
    var overflown = false;
    var previousMenuItem = null;
    this._menuBar.menuItems.forEach(function(menuItem) {
      if (!menuItem.visible) {
        return;
      }
      if (menuItem.rightAligned) {
        // Always add right-aligned menus
        menuItemsCopy.push(menuItem);
      } else {
        var itemSize = scout.graphics.getSize(menuItem.$container, true);
        remainingLeftWidth -= itemSize.width;
        if (overflown || remainingLeftWidth < 0) {
          // Menu does not fit -> add to ellipsis menu
          if (!overflown) {
            overflown = true;
            // Check if ellipsis menu fits, otherwise the previous menu has to be removed as well
            if (previousMenuItem && remainingLeftWidth + itemSize.width - ellipsisSize.width < 0) {
              this._removeMenuItem(previousMenuItem);
            }
          }
          this._removeMenuItem(menuItem);
        } else {
          // Menu fits, add to normal menu list
          menuItemsCopy.push(menuItem);
        }
        previousMenuItem = menuItem;
      }
    }, this);

    this._addEllipsisToMenuItems(menuItemsCopy);
    this._menuBar.visibleMenuItems = menuItemsCopy;
  }

  this._menuBar.visibleMenuItems.forEach(function(menuItem) {
    // Make sure open popups are at the correct position after layouting
    if (menuItem.popup) {
      menuItem.popup.position();
    }
  });
};

scout.MenuBarLayout.prototype._removeMenuItem = function(menuItem) {
  menuItem.remove();
  menuItem.overflow = true;
  this._menuBar.ellipsis.childActions.push(menuItem);
};

scout.MenuBarLayout.prototype._addEllipsisToMenuItems = function(menuItems) {
  // Add the ellipsis menu to the menu-items list. Order matters because we do not sort
  // menu-items again.
  var insertItemAt = 0;
  menuItems.some(function(menuItem, i) {
    if (menuItem.rightAligned) {
      return true; // break
    }
    insertItemAt = i + 1;
    return false; // keep looking
  });
  scout.arrays.insert(menuItems, this._menuBar.ellipsis, insertItemAt);
};

scout.MenuBarLayout.prototype._createAndRenderEllipsis = function($container, lastMenuInBar) {
  var ellipsis = scout.create('Menu', {
    parent: this._menuBar,
    horizontalAlignment: 1,
    iconId: scout.icons.ELLIPSIS_V,
    tabbable: false
  });
  ellipsis.render($container);
  if (lastMenuInBar) {
    ellipsis.$container.addClass('last');
  }
  this._menuBar.ellipsis = ellipsis;
};

scout.MenuBarLayout.prototype._destroyEllipsis = function() {
  if (this._menuBar.ellipsis) {
    this._menuBar.ellipsis.destroy();
    this._menuBar.ellipsis = null;
  }
};

scout.MenuBarLayout.prototype.preferredLayoutSize = function($container, options) {
  // Menubar has an absolute css height set -> useCssSize = true
  var prefSize = scout.graphics.prefSize($container, {
    //useCssSize: true
    //heightHint: 'css'
    useCssHeight: true
  });
  if (options.widthHint) {
    prefSize.width = Math.min(prefSize.width, options.widthHint);
  }
  return prefSize;
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.MenuBarLayout
 */
scout.MenuBarLayout.size = function(htmlMenuBar, containerSize) {
  var menuBarSize = htmlMenuBar.getPreferredSize();
  menuBarSize.width = containerSize.width;
  menuBarSize = menuBarSize.subtract(htmlMenuBar.getMargins());
  return menuBarSize;
};

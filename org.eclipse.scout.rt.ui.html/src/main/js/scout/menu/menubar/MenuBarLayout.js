/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout} from '../../index';
import {HtmlComponent} from '../../index';
import {MenuBar} from '../../index';
import {scout} from '../../index';
import {Dimension} from '../../index';
import {graphics} from '../../index';
import {arrays} from '../../index';

export default class MenuBarLayout extends AbstractLayout {

constructor(menuBar) {
  super();
  this._menuBar = menuBar;

  this._overflowMenuItems = [];
  this._visibleMenuItems = [];
  this._ellipsis = null;
  this.collapsed = false;
}


layout($container) {
  var menuItems = this._menuBar.orderedMenuItems.left.concat(this._menuBar.orderedMenuItems.right),
    htmlContainer = HtmlComponent.get($container),
    ellipsis;

  this.undoShrink(menuItems);

  ellipsis = arrays.find(menuItems, function(menuItem) {
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
}

preferredLayoutSize($container, options) {
  this._overflowMenuItems = [];
  if (!this._menuBar.isVisible()) {
    return new Dimension(0, 0);
  }
  var visibleMenuItems = this._menuBar.orderedMenuItems.all.filter(function(menuItem) {
      return menuItem.visible;
    }, this),
    overflowMenuItems = visibleMenuItems.filter(function(menuItem) {
      var overflown = menuItem.overflown;
      menuItem._setOverflown(false);
      return overflown;
    }),
    overflowableIndexes = [],
    htmlComp = HtmlComponent.get($container),
    prefSize = new Dimension(0, 0),
    prefWidth = Number.MAX_VALUE;

  // consider avoid falsy 0 in tabboxes a 0 withHint will be used to calculate the minimum width
  if (options.widthHint === 0 || options.widthHint) {
    prefWidth = options.widthHint - htmlComp.insets().horizontal();
  }
  // shortcut for minimum size.
  if (prefWidth <= 0) {
    //reset overflown
    overflowMenuItems.forEach(function(menuItem) {
      menuItem._setOverflown(true);
    });
    return this._minSize(visibleMenuItems);
  }

  prefSize = this._prefSize(visibleMenuItems);
  if (prefSize.width > prefWidth) {
    this.shrink(visibleMenuItems);
  }

  // fill overflowable indexes
  visibleMenuItems.forEach(function(menuItem, index) {
    if (menuItem.stackable) {
      overflowableIndexes.push(index);
    }
  });

  var overflowIndex = -1;
  this._setFirstLastMenuMarker(visibleMenuItems);
  prefSize = this._prefSize(visibleMenuItems);
  while (prefSize.width > prefWidth && overflowableIndexes.length > 0) {
    if (this._menuBar.ellipsisPosition === MenuBar.EllipsisPosition.RIGHT) {
      overflowIndex = overflowableIndexes.splice(-1)[0];
    } else {
      overflowIndex = overflowableIndexes.splice(0, 1)[0] - this._overflowMenuItems.length;
    }
    this._overflowMenuItems.splice(0, 0, visibleMenuItems[overflowIndex]);
    visibleMenuItems.splice(overflowIndex, 1);
    this._setFirstLastMenuMarker(visibleMenuItems);
    prefSize = this._prefSize(visibleMenuItems);
  }

  //reset overflown
  overflowMenuItems.forEach(function(menuItem) {
    menuItem._setOverflown(true);
  });

  this._visibleMenuItems = visibleMenuItems;
  return prefSize.add(htmlComp.insets());
}

_minSize(visibleMenuItems) {
  var prefSize,
    minVisibleMenuItems = visibleMenuItems.filter(function(menuItem) {
      return menuItem.ellisis || !menuItem.stackable;
    }, this);
  this._setFirstLastMenuMarker(minVisibleMenuItems, true);
  prefSize = this._prefSize(minVisibleMenuItems, true);
  return prefSize;
}

_prefSize(menuItems, considerEllipsis) {
  var prefSize = new Dimension(0, 0),
    itemSize = new Dimension(0, 0);
  considerEllipsis = scout.nvl(considerEllipsis, this._overflowMenuItems.length > 0);
  menuItems.forEach(function(menuItem) {
    itemSize = new Dimension(0, 0);
    if (menuItem.ellipsis) {
      if (considerEllipsis) {
        itemSize = this._menuItemSize(menuItem);
      }
    } else {
      itemSize = this._menuItemSize(menuItem);
    }
    prefSize.height = Math.max(prefSize.height, itemSize.height);
    prefSize.width += itemSize.width;
  }, this);
  return prefSize;
}

_menuItemSize(menuItem) {
  var prefSize,
    classList = menuItem.$container.attr('class');

  menuItem.$container.removeClass('overflown');
  menuItem.$container.removeClass('hidden');

  menuItem.htmlComp.invalidateLayout();
  prefSize = menuItem.htmlComp.prefSize({
    useCssSize: true,
    exact: true
  }).add(graphics.margins(menuItem.$container));

  menuItem.$container.attrOrRemove('class', classList);
  return prefSize;
}

_setFirstLastMenuMarker(visibleMenuItems, considerEllipsis) {
  var menuItems = visibleMenuItems;
  considerEllipsis = scout.nvl(considerEllipsis, this._overflowMenuItems.length > 0);

  // reset
  this._menuBar.orderedMenuItems.all.forEach(function(menuItem) {
    menuItem.$container.removeClass('first last');
  });

  // set first and last
  if (!considerEllipsis) {
    // remove ellipsis
    menuItems = menuItems.filter(function(menuItem) {
      return !menuItem.ellipsis;
    });
  }
  if (menuItems.length > 0) {
    menuItems[0].$container.addClass('first');
    menuItems[menuItems.length - 1].$container.addClass('last');
  }
}

/**
 * Makes the text invisible of all shrinkable menus with an icon
 */
shrink(menus) {
  menus.forEach(function(menu) {
    if (menu.textVisibleOrig !== undefined) {
      // already done
      return;
    }
    if (menu.shrinkable && menu.icon) {
      menu.textVisibleOrig = menu.textVisible;
      menu.htmlComp.suppressInvalidate = true;
      menu.setTextVisible(false);
      menu.htmlComp.suppressInvalidate = false;
    }
  }, this);
}

undoShrink(menus) {
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
}

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf MenuBarLayout
 */
static size(htmlMenuBar, containerSize) {
  var menuBarSize = htmlMenuBar.prefSize();
  menuBarSize.width = containerSize.width;
  menuBarSize = menuBarSize.subtract(htmlMenuBar.margins());
  return menuBarSize;
}
}

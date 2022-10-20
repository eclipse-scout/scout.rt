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
import {AbstractLayout, arrays, Dimension, graphics, HtmlComponent, MenuBar, scout} from '../../index';

export default class MenuBarLayout extends AbstractLayout {

  constructor(menuBar) {
    super();
    this._menuBar = menuBar;

    this._overflowMenuItems = [];
    this._ellipsis = null;
    this.collapsed = false;
  }

  layout($container) {
    let menuItems = this._menuBar.orderedMenuItems.left.concat(this._menuBar.orderedMenuItems.right);
    let visibleMenuItems = this.visibleMenuItems();
    let htmlContainer = HtmlComponent.get($container);
    let ellipsis = arrays.find(menuItems, menuItem => menuItem.ellipsis);

    this._setFirstLastMenuMarker(visibleMenuItems); // is required to determine available size correctly
    this.preferredLayoutSize($container, {
      widthHint: htmlContainer.availableSize().width,
      undo: false
    });

    // first set visible to ensure the correct menu gets the tabindex. Therefore the ellipsis visibility is split.
    if (ellipsis && this._overflowMenuItems.length > 0) {
      ellipsis.setHidden(false);
    }
    visibleMenuItems.forEach(menuItem => menuItem._setOverflown(false));
    this._overflowMenuItems.forEach(menuItem => menuItem._setOverflown(true));
    if (ellipsis && this._overflowMenuItems.length === 0) {
      ellipsis.setHidden(true);
    }
    // remove all separators
    this._overflowMenuItems = this._overflowMenuItems.filter(menuItem => !menuItem.separator);

    if (ellipsis) {
      ellipsis._closePopup();
      ellipsis.setChildActions(this._overflowMenuItems);
    }

    // trigger menu items layout
    visibleMenuItems.forEach(menuItem => menuItem.validateLayout());

    visibleMenuItems.forEach(menuItem => {
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
    let visibleMenuItems = this.visibleMenuItems();
    let overflowMenuItems = visibleMenuItems.filter(menuItem => {
      let overflown = menuItem.overflown;
      menuItem._setOverflown(false);
      return overflown;
    });
    let shrinkedMenuItems = visibleMenuItems.filter(menuItem => {
      let shrinked = !menuItem.textVisible;
      this.undoShrink([menuItem]);
      return shrinked;
    });
    let notShrinkedMenuItems = [...visibleMenuItems];
    let htmlComp = HtmlComponent.get($container);
    let prefSize = new Dimension(0, 0);
    let prefWidth = Number.MAX_VALUE;

    // consider avoid falsy 0 in tabboxes a 0 withHint will be used to calculate the minimum width
    if (options.widthHint === 0 || options.widthHint) {
      prefWidth = options.widthHint - htmlComp.insets().horizontal();
    }
    if (prefWidth <= 0) {
      // shortcut for minimum size.
      prefSize = this._minSize(visibleMenuItems);
    } else {
      prefSize = this._prefSize(visibleMenuItems);
      if (prefSize.width > prefWidth) {
        this.shrink(visibleMenuItems);
      }
      prefSize = this._prefSizeWithOverflow(visibleMenuItems, prefWidth);
    }

    if (scout.nvl(options.undo, true)) {
      // Reset state
      this.undoOverflow(overflowMenuItems);
      this.undoShrink(notShrinkedMenuItems);
      this.shrink(shrinkedMenuItems);
    }

    return prefSize.add(htmlComp.insets());
  }

  /**
   * Moves menu items into _overflowMenuItems until prefSize.width is smaller than prefWidth.
   * The moved menu items will be removed from the given visibleMenuItems parameter.
   * @returns {Dimension} the calculated preferred size
   */
  _prefSizeWithOverflow(visibleMenuItems, prefWidth) {
    let overflowableIndexes = [];
    visibleMenuItems.forEach((menuItem, index) => {
      if (menuItem.stackable) {
        overflowableIndexes.push(index);
      }
    });

    let overflowIndex = -1;
    this._setFirstLastMenuMarker(visibleMenuItems);
    let prefSize = this._prefSize(visibleMenuItems);
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
    return prefSize;
  }

  _minSize(visibleMenuItems) {
    let minVisibleMenuItems = visibleMenuItems.filter(menuItem => !menuItem.stackable);
    this.shrink(visibleMenuItems);
    this._setFirstLastMenuMarker(minVisibleMenuItems, true);
    return this._prefSize(minVisibleMenuItems, true);
  }

  _prefSize(menuItems, considerEllipsis) {
    let prefSize = new Dimension(0, 0);
    considerEllipsis = scout.nvl(considerEllipsis, this._overflowMenuItems.length > 0);
    this._setFirstLastMenuMarker(menuItems, considerEllipsis);
    menuItems.forEach(menuItem => {
      let itemSize = new Dimension(0, 0);
      if (menuItem.ellipsis) {
        if (considerEllipsis) {
          itemSize = this._menuItemSize(menuItem);
        }
      } else {
        itemSize = this._menuItemSize(menuItem);
      }
      prefSize.height = Math.max(prefSize.height, itemSize.height);
      prefSize.width += itemSize.width;
    });
    return prefSize;
  }

  _menuItemSize(menuItem) {
    let classList = menuItem.$container.attr('class');

    menuItem.$container.removeClass('overflown');
    menuItem.$container.removeClass('hidden');

    menuItem.htmlComp.invalidateLayout();
    let prefSize = menuItem.htmlComp.prefSize({
      useCssSize: true,
      exact: true
    }).add(graphics.margins(menuItem.$container));

    menuItem.$container.attrOrRemove('class', classList);
    return prefSize;
  }

  _setFirstLastMenuMarker(visibleMenuItems, considerEllipsis) {
    let menuItems = visibleMenuItems;
    considerEllipsis = scout.nvl(considerEllipsis, this._overflowMenuItems.length > 0);

    // reset
    this._menuBar.orderedMenuItems.all.forEach(menuItem => {
      menuItem.$container.removeClass('first last');
    });

    // set first and last
    if (!considerEllipsis) {
      // remove ellipsis
      menuItems = menuItems.filter(menuItem => {
        return !menuItem.ellipsis;
      });
    }
    if (menuItems.length > 0) {
      menuItems[0].$container.addClass('first');
      menuItems[menuItems.length - 1].$container.addClass('last');
    }
  }

  undoOverflow(overflowMenuItems) {
    overflowMenuItems.forEach(menuItem => {
      menuItem._setOverflown(true);
    });
  }

  /**
   * Makes the text invisible of all shrinkable menus with an icon
   */
  shrink(menus) {
    menus.forEach(menu => {
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
    menus.forEach(menu => {
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

  visibleMenuItems() {
    return this._menuBar.orderedMenuItems.all.filter(menuItem => {
      return menuItem.visible;
    }, this);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * @memberOf MenuBarLayout
   */
  static size(htmlMenuBar, containerSize) {
    let menuBarSize = htmlMenuBar.prefSize({widthHint: containerSize.width});
    menuBarSize.width = containerSize.width;
    menuBarSize = menuBarSize.subtract(htmlMenuBar.margins());
    return menuBarSize;
  }
}

/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, arrays, Dimension, EllipsisMenu, graphics, HtmlComponent, HtmlCompPrefSizeOptions, Menu, MenuBar, scout} from '../../index';

export class MenuBarLayout extends AbstractLayout {
  collapsed: boolean;
  protected _menuBar: MenuBar;
  protected _overflowMenuItems: Menu[];

  constructor(menuBar: MenuBar) {
    super();
    this._menuBar = menuBar;
    this._overflowMenuItems = [];
    this.collapsed = false;
  }

  override layout($container: JQuery) {
    let menuItems = this._menuBar.orderedMenuItems.left.concat(this._menuBar.orderedMenuItems.right);
    let visibleMenuItems = this.visibleMenuItems();
    let htmlContainer = HtmlComponent.get($container);
    let ellipsis = arrays.find(menuItems, menuItem => menuItem.ellipsis) as EllipsisMenu;

    this._setFirstLastMenuMarker(visibleMenuItems); // is required to determine available size correctly
    this.preferredLayoutSize($container, {
      widthHint: htmlContainer.availableSize().width,
      undo: false
    });

    // first set visible to ensure the correct menu gets the tabindex. Therefore, the ellipsis visibility is split.
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

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions & { undo?: boolean }): Dimension {
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
    let shrunkMenuItems = visibleMenuItems.filter(menuItem => {
      let shrunk = !menuItem.textVisible;
      this.undoShrink([menuItem]);
      return shrunk;
    });
    let notShrunkMenuItems = [...visibleMenuItems];
    let htmlComp = HtmlComponent.get($container);
    let prefSize = new Dimension(0, 0);
    let prefWidth = Number.MAX_VALUE;

    // consider avoid falsy 0 in tab-boxes a 0 withHint will be used to calculate the minimum width
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
      this.undoShrink(notShrunkMenuItems);
      this.shrink(shrunkMenuItems);
    }

    return prefSize.add(htmlComp.insets());
  }

  /**
   * Moves menu items into _overflowMenuItems until {@link prefSize.width} is smaller than prefWidth.
   * The moved menu items will be removed from the given visibleMenuItems parameter.
   * @returns the calculated preferred size
   */
  protected _prefSizeWithOverflow(visibleMenuItems: Menu[], prefWidth: number): Dimension {
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

  protected _minSize(visibleMenuItems: Menu[]): Dimension {
    let minVisibleMenuItems = visibleMenuItems.filter(menuItem => !menuItem.stackable);
    this.shrink(visibleMenuItems);
    this._setFirstLastMenuMarker(minVisibleMenuItems, true);
    return this._prefSize(minVisibleMenuItems, true);
  }

  protected _prefSize(menuItems: Menu[], considerEllipsis?: boolean): Dimension {
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

  protected _menuItemSize(menuItem: Menu): Dimension {
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

  protected _setFirstLastMenuMarker(visibleMenuItems: Menu[], considerEllipsis?: boolean) {
    let menuItems = visibleMenuItems;
    considerEllipsis = scout.nvl(considerEllipsis, this._overflowMenuItems.length > 0);

    // reset
    this._menuBar.orderedMenuItems.all.forEach(menuItem => menuItem.$container.removeClass('first last'));

    // set first and last
    if (!considerEllipsis) {
      // remove ellipsis
      menuItems = menuItems.filter(menuItem => !menuItem.ellipsis);
    }
    if (menuItems.length > 0) {
      menuItems[0].$container.addClass('first');
      menuItems[menuItems.length - 1].$container.addClass('last');
    }
  }

  undoOverflow(overflowMenuItems: Menu[]) {
    overflowMenuItems.forEach(menuItem => menuItem._setOverflown(true));
  }

  /**
   * Makes the text invisible of all shrinkable menus with an icon
   */
  shrink(menus: Menu[]) {
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
    });
  }

  undoShrink(menus: Menu[]) {
    menus.forEach(menu => {
      if (menu.textVisibleOrig === undefined) {
        return;
      }
      // Restore old text visible state
      menu.htmlComp.suppressInvalidate = true;
      menu.setTextVisible(menu.textVisibleOrig);
      menu.htmlComp.suppressInvalidate = false;
      menu.textVisibleOrig = undefined;
    });
  }

  visibleMenuItems(): Menu[] {
    return this._menuBar.orderedMenuItems.all.filter(menuItem => menuItem.visible);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static size(htmlMenuBar: HtmlComponent, containerSize: Dimension): Dimension {
    let menuBarSize = htmlMenuBar.prefSize({widthHint: containerSize.width});
    menuBarSize.width = containerSize.width;
    menuBarSize = menuBarSize.subtract(htmlMenuBar.margins());
    return menuBarSize;
  }
}

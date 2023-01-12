/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, EllipsisMenu, graphics, HtmlCompPrefSizeOptions, Menu, MenuBox, menus as menuUtil} from '../../index';

export class MenuBoxLayout extends AbstractLayout {
  menuBox: MenuBox;
  /** References to prevent too many DOM updates */
  firstMenu: Menu;
  lastMenu: Menu;
  protected _ellipsis: EllipsisMenu;

  constructor(menuBox: MenuBox) {
    super();
    this.menuBox = menuBox;
    this.firstMenu = null;
    this.lastMenu = null;
  }

  override layout($container: JQuery) {
    let htmlContainer = this.menuBox.htmlComp,
      containerSize = htmlContainer.size(),
      menus = this.visibleMenus(),
      menusWidth = 0;

    // Make sure open popups are at the correct position after layouting
    this.menuBox.session.layoutValidator.schedulePostValidateFunction(() => {
      menus.forEach(menu => {
        if (menu.popup) {
          menu.popup.position();
        }
      });
    });

    this.updateFirstAndLastMenuMarker(menus);
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
    this.collapse(containerSize, menusWidth);
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let menus = this.visibleMenus();

    this.updateFirstAndLastMenuMarker(menus);
    this.undoCollapse(menus);
    this.undoCompact(menus);
    this.undoShrink(menus);

    return this.actualPrefSize();
  }

  compact(menus?: Menu[]) {
    if (this.menuBox.compactOrig === undefined) {
      this.menuBox.compactOrig = this.menuBox.compact;
      this.menuBox.htmlComp.suppressInvalidate = true;
      this.menuBox.setCompact(true);
      this.menuBox.htmlComp.suppressInvalidate = false;
    }

    this.compactMenus(menus);
  }

  undoCompact(menus?: Menu[]) {
    if (this.menuBox.compactOrig !== undefined) {
      this.menuBox.htmlComp.suppressInvalidate = true;
      this.menuBox.setCompact(this.menuBox.compactOrig);
      this.menuBox.htmlComp.suppressInvalidate = false;
      this.menuBox.compactOrig = undefined;
    }

    this.undoCompactMenus(menus);
  }

  /**
   * Sets all menus into compact mode.
   */
  compactMenus(menus?: Menu[]) {
    menus = menus || this.visibleMenus();
    menus.forEach(menu => {
      if (menu.compactOrig !== undefined) {
        // already done
        return;
      }
      menu.compactOrig = menu.compact;
      menu.htmlComp.suppressInvalidate = true;
      menu.setCompact(true);
      menu.htmlComp.suppressInvalidate = false;
    });

    if (this._ellipsis) {
      this._ellipsis.setCompact(true);
    }
  }

  /**
   * Restores to the previous state of the compact property.
   */
  undoCompactMenus(menus?: Menu[]) {
    menus = menus || this.visibleMenus();
    menus.forEach(menu => {
      if (menu.compactOrig === undefined) {
        return;
      }
      // Restore old compact state
      menu.htmlComp.suppressInvalidate = true;
      menu.setCompact(menu.compactOrig);
      menu.htmlComp.suppressInvalidate = false;
      menu.compactOrig = undefined;
    });

    if (this._ellipsis) {
      this._ellipsis.setCompact(false);
    }
  }

  shrink(menus?: Menu[]) {
    this.shrinkMenus(menus);
  }

  /**
   * Makes the text invisible of all menus with an icon.
   */
  shrinkMenus(menus?: Menu[]) {
    menus = menus || this.visibleMenus();
    menus.forEach(menu => {
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
    });
  }

  undoShrink(menus?: Menu[]) {
    this.undoShrinkMenus(menus);
  }

  undoShrinkMenus(menus?: Menu[]) {
    menus = menus || this.visibleMenus();
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

  collapse(containerSize: Dimension, menusWidth: number) {
    this._createAndRenderEllipsis(this.menuBox.$container);
    let collapsedMenus = this._moveOverflowMenusIntoEllipsis(containerSize, menusWidth);
    this.updateFirstAndLastMenuMarker(collapsedMenus);
  }

  /**
   * Undoes the collapsing by removing ellipsis and rendering non rendered menus.
   */
  undoCollapse(menus?: Menu[]) {
    menus = menus || this.visibleMenus();
    this._destroyEllipsis();
    this._removeMenusFromEllipsis(menus);
  }

  protected _createAndRenderEllipsis($container: JQuery) {
    let ellipsis = menuUtil.createEllipsisMenu({
      parent: this.menuBox,
      hidden: false,
      compact: this.menuBox.compact
    });
    ellipsis.uiCssClass = this.menuBox.uiMenuCssClass;
    ellipsis.render($container);
    this._ellipsis = ellipsis;
  }

  protected _destroyEllipsis() {
    if (this._ellipsis) {
      this._ellipsis.destroy();
      this._ellipsis = null;
    }
  }

  /**
   * Moves every menu which doesn't fit into the container into the ellipsis menu.
   * Returns the list of "surviving" menus (with the ellipsis menu being the last element).
   */
  protected _moveOverflowMenusIntoEllipsis(containerSize: Dimension, menusWidth: number): Menu[] {
    let collapsedMenus: Menu[] = [this._ellipsis];
    let ellipsisSize = graphics.size(this._ellipsis.$container, true);
    menusWidth += ellipsisSize.width;
    this.visibleMenus().slice().reverse().forEach(menu => {
      if (menusWidth > containerSize.width) {
        // Menu does not fit -> move to ellipsis menu
        let menuSize = graphics.size(menu.$container, true);
        menusWidth -= menuSize.width;
        menuUtil.moveMenuIntoEllipsis(menu, this._ellipsis);
      } else {
        collapsedMenus.unshift(menu); // add as first element
      }
    });
    return collapsedMenus;
  }

  protected _removeMenusFromEllipsis(menus?: Menu[]) {
    menus = menus || this.visibleMenus();
    menus.forEach(menu => menuUtil.removeMenuFromEllipsis(menu, this.menuBox.$container));
  }

  actualPrefSize(menus?: Menu[]): Dimension {
    menus = menus || this.visibleMenus();
    let menusWidth = this._menusWidth(menus);
    let prefSize = graphics.prefSize(this.menuBox.$container);
    prefSize.width = menusWidth + this.menuBox.htmlComp.insets().horizontal();
    return prefSize;
  }

  /**
   * @returns the current width of all menus incl. the ellipsis
   */
  protected _menusWidth(menus?: Menu[]): number {
    let menusWidth = 0;
    let size = menu => graphics.size(menu.htmlComp.$comp, {includeMargin: true, exact: true});

    menus = menus || this.visibleMenus();
    menus.forEach(menu => {
      if (menu.rendered) {
        menusWidth += size(menu).width;
      }
    });
    if (this._ellipsis) {
      menusWidth += size(this._ellipsis).width;
    }

    return menusWidth;
  }

  compactPrefSize(menus?: Menu[]): Dimension {
    menus = menus || this.visibleMenus();

    this.updateFirstAndLastMenuMarker(menus);
    this.undoCollapse(menus);
    this.undoShrink(menus);
    this.compact(menus);

    return this.actualPrefSize();
  }

  shrinkPrefSize(menus?: Menu[]): Dimension {
    menus = menus || this.visibleMenus();

    this.updateFirstAndLastMenuMarker(menus);
    this.undoCollapse(menus);
    this.compact(menus);
    this.shrink(menus);

    return this.actualPrefSize();
  }

  visibleMenus(): Menu[] {
    return this.menuBox.menus.filter(menu => menu.visible);
  }

  updateFirstAndLastMenuMarker(menus?: Menu[]) {
    // Find first and last rendered menu
    let firstMenu = null;
    let lastMenu = null;
    (menus || []).forEach(menu => {
      if (menu.rendered) {
        if (!firstMenu) {
          firstMenu = menu;
        }
        lastMenu = menu;
      }
    });

    // Check if first or last menu has changed (prevents unnecessary DOM updates)
    if (firstMenu !== this.firstMenu || lastMenu !== this.lastMenu) {
      // Remove existing markers
      if (this.firstMenu && this.firstMenu.rendered) {
        this.firstMenu.$container.removeClass('first');
      }
      if (this.lastMenu && this.lastMenu.rendered) {
        this.lastMenu.$container.removeClass('last');
      }
      // Remember found menus
      this.firstMenu = firstMenu;
      this.lastMenu = lastMenu;
      // Add markers to found menus
      if (this.firstMenu) {
        this.firstMenu.$container.addClass('first');
      }
      if (this.lastMenu) {
        this.lastMenu.$container.addClass('last');
      }
    }
  }
}

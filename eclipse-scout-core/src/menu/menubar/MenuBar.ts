/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, ComboMenu, EllipsisMenu, EnumObject, Event, EventHandler, GroupBoxMenuItemsOrder, HtmlComponent, InitModelOf, keys, KeyStroke, KeyStrokeContext, Menu, MenuBarBox, MenuBarEventMap, MenuBarLayout, MenuBarLeftKeyStroke, MenuBarModel,
  MenuBarRightKeyStroke, MenuDestinations, MenuFilter, MenuOrder, menus, ObjectOrChildModel, OrderedMenuItems, PropertyChangeEvent, scout, TooltipPosition, Widget, widgets
} from '../../index';

export type MenuBarEllipsisPosition = EnumObject<typeof MenuBar.EllipsisPosition>;
export type MenuBarPosition = EnumObject<typeof MenuBar.Position>;

export class MenuBar extends Widget implements MenuBarModel {
  declare model: MenuBarModel;
  declare eventMap: MenuBarEventMap;
  declare self: MenuBar;

  menuSorter: MenuOrder & { menuBar?: MenuBar };
  menuFilter: MenuFilter;
  position: MenuBarPosition;
  tabbable: boolean;
  menuboxLeft: MenuBarBox;
  menuboxRight: MenuBarBox;
  menuItems: Menu[]; // original list of menuItems that was passed to setMenuItems(), only used to check if menubar has changed
  orderedMenuItems: OrderedMenuItems;
  defaultMenu: Menu;
  ellipsisPosition: MenuBarEllipsisPosition;
  hiddenByUi: boolean;
  tabbableMenu: Menu;

  protected _menuItemPropertyChangeHandler: EventHandler<PropertyChangeEvent>;
  protected _focusHandler: EventHandler<Event<Menu>>;
  protected _ellipsis: EllipsisMenu;

  constructor() {
    super();

    this.menuSorter = null;
    this.menuFilter = null;
    this.position = MenuBar.Position.TOP;
    this.tabbable = true;
    this.menuboxLeft = null;
    this.menuboxRight = null;
    this.menuItems = [];
    this.orderedMenuItems = {
      left: [],
      right: [],
      all: []
    };
    this.defaultMenu = null;
    this.visible = false;
    this.ellipsisPosition = MenuBar.EllipsisPosition.RIGHT;
    this._menuItemPropertyChangeHandler = this._onMenuItemPropertyChange.bind(this);
    this._focusHandler = this._onMenuItemFocus.bind(this);
    this.hiddenByUi = false;
    this._addWidgetProperties('menuItems');
  }

  static EllipsisPosition = {
    LEFT: 'left',
    RIGHT: 'right'
  } as const;

  static Position = {
    TOP: 'top',
    BOTTOM: 'bottom'
  } as const;

  protected override _init(options: InitModelOf<this>) {
    super._init(options);

    this.menuSorter = options.menuOrder || new GroupBoxMenuItemsOrder();
    this.menuSorter.menuBar = this;
    if (options.menuFilter) {
      this.menuFilter = (menus, destination) => options.menuFilter(menus, MenuDestinations.MENU_BAR);
    }

    this.menuboxLeft = scout.create(MenuBarBox, {
      parent: this,
      cssClass: 'left',
      tooltipPosition: this._oppositePosition()
    });
    this.menuboxRight = scout.create(MenuBarBox, {
      parent: this,
      cssClass: 'right',
      tooltipPosition: this._oppositePosition()
    });

    this._setMenuItems(arrays.ensure(this.menuItems));
    this.updateVisibility();
  }

  protected override _destroy() {
    super._destroy();
    this._detachMenuHandlers();
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStrokes([
      new MenuBarLeftKeyStroke(this),
      new MenuBarRightKeyStroke(this)
    ]);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('menubar');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new MenuBarLayout(this));

    this.menuboxRight.render(this.$container);
    this.menuboxLeft.render(this.$container);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderMenuItems();
    this._renderPosition();
  }

  setPosition(position: MenuBarPosition) {
    this.setProperty('position', position);
  }

  protected _setPosition(position: MenuBarPosition) {
    this._setProperty('position', position);
    this.menuboxLeft.setTooltipPosition(this._oppositePosition());
    this.menuboxRight.setTooltipPosition(this._oppositePosition());
  }

  protected _renderPosition() {
    this.$container.toggleClass('bottom', this.position === MenuBar.Position.BOTTOM);
  }

  protected _oppositePosition(): TooltipPosition {
    return this.position === MenuBar.Position.TOP ?
      MenuBar.Position.BOTTOM : MenuBar.Position.TOP;
  }

  setEllipsisPosition(ellipsisPosition: MenuBarEllipsisPosition) {
    this.setProperty('ellipsisPosition', ellipsisPosition);
  }

  /**
   * Set the filter of the menu bar to all the menu items.
   */
  protected _setChildMenuFilters() {
    this.orderedMenuItems.all.forEach(item => item.setMenuFilter(this.menuFilter));
  }

  /**
   * This function can be called multiple times. The function attaches the menu handlers only if they are not yet added.
   */
  protected _attachMenuHandlers() {
    this.orderedMenuItems.all.forEach(item => {
      if (item.events.count('propertyChange', this._menuItemPropertyChangeHandler) === 0) {
        item.on('propertyChange', this._menuItemPropertyChangeHandler);
      }
      if (item.events.count('focus', this._focusHandler) === 0) {
        item.on('focus', this._focusHandler);
      }
    });
  }

  protected _detachMenuHandlers() {
    this.orderedMenuItems.all.forEach(item => {
      item.off('propertyChange', this._menuItemPropertyChangeHandler);
      item.off('focus', this._focusHandler);
    });
  }

  setMenuItems(menuOrModels: ObjectOrChildModel<Menu> | ObjectOrChildModel<Menu>[]) {
    let menuItems = arrays.ensure(menuOrModels);
    if (arrays.equals(this.menuItems, menuItems)) {
      // Ensure existing menus are correctly linked even if the given menuItems are the same (see TableSpec for reasons)
      this.menuboxRight.link(this.menuboxRight.menuItems);
      this.menuboxLeft.link(this.menuboxLeft.menuItems);
      return;
    }
    this.setProperty('menuItems', menuItems);
  }

  protected _setMenuItems(menuItems: Menu[], rightFirst?: boolean) {
    // remove property listeners of old menu items.
    this._detachMenuHandlers();
    this.orderedMenuItems = this._createOrderedMenus(menuItems);

    if (rightFirst) {
      this.menuboxRight.setMenuItems(this.orderedMenuItems.right);
      this.menuboxLeft.setMenuItems(this.orderedMenuItems.left);

    } else {
      this.menuboxLeft.setMenuItems(this.orderedMenuItems.left);
      this.menuboxRight.setMenuItems(this.orderedMenuItems.right);
    }

    this._setChildMenuFilters();
    this._attachMenuHandlers();

    this.updateVisibility();
    this.updateDefaultMenu();

    this._setProperty('menuItems', menuItems);
  }

  protected _renderMenuItems() {
    widgets.updateFirstLastMarker(this.menuItems);
    this.updateLeftOfButtonMarker();
    this.invalidateLayoutTree();
  }

  protected _removeMenuItems() {
    // NOP: by implementing this function we avoid the call to Widget.js#_internalRemoveWidgets
    // which would remove our menuItems, because they are defined as widget-property (see constructor).
  }

  protected _createOrderedMenus(menuItems: Menu[]): OrderedMenuItems {
    let orderedMenuItems = this.menuSorter.order(menuItems),
      ellipsisIndex = -1,
      ellipsis;
    orderedMenuItems.right.forEach(item => {
      item.rightAligned = true;
    });

    if (orderedMenuItems.all.length > 0) {
      if (this._ellipsis) {
        // Disconnect existing child actions from ellipsis menu
        this._ellipsis.setChildActions([]);
        this._ellipsis.destroy();
      }
      ellipsis = scout.create(EllipsisMenu, {
        parent: this,
        cssClass: 'overflow-menu-item'
      });
      this._ellipsis = ellipsis;

      // add ellipsis to the correct position
      if (this.ellipsisPosition === MenuBar.EllipsisPosition.RIGHT) {
        // try right
        // noinspection JSVoidFunctionReturnValueUsed
        let reverseIndexPosition = this._getFirstStackableIndexPosition(orderedMenuItems.right.slice().reverse());
        if (reverseIndexPosition > -1) {
          ellipsisIndex = orderedMenuItems.right.length - reverseIndexPosition;
          ellipsis.rightAligned = true;
          orderedMenuItems.right.splice(ellipsisIndex, 0, ellipsis);
        } else {
          // try left
          // noinspection JSVoidFunctionReturnValueUsed
          reverseIndexPosition = this._getFirstStackableIndexPosition(orderedMenuItems.left.slice().reverse());
          if (reverseIndexPosition > -1) {
            ellipsisIndex = orderedMenuItems.left.length - reverseIndexPosition;
            orderedMenuItems.left.splice(ellipsisIndex, 0, ellipsis);
          }
        }
      } else {
        // try left
        ellipsisIndex = this._getFirstStackableIndexPosition(orderedMenuItems.left);
        if (ellipsisIndex > -1) {
          orderedMenuItems.left.splice(ellipsisIndex, 0, ellipsis);
        } else {
          // try right
          ellipsisIndex = this._getFirstStackableIndexPosition(orderedMenuItems.right);
          if (ellipsisIndex > -1) {
            ellipsis.rightAligned = true;
            orderedMenuItems.right.splice(ellipsisIndex, 0, ellipsis);
          }
        }
      }
      orderedMenuItems.all = orderedMenuItems.left.concat(orderedMenuItems.right);
    }
    return orderedMenuItems;
  }

  protected _getFirstStackableIndexPosition(menuList: Menu[]): number {
    let foundIndex = -1;
    menuList.some((menu: Menu, index: number) => {
      if (menu.stackable && menu.visible) {
        foundIndex = index;
        return true;
      }
      return false;
    });

    return foundIndex;
  }

  protected _updateTabbableMenu() {
    // Make first valid MenuItem tabbable so that it can be focused. All other items
    // are not tabbable. But they can be selected with the arrow keys.
    if (this.tabbable) {
      if (this.defaultMenu && this.defaultMenu.isTabTarget()) {
        this.setTabbableMenu(this.defaultMenu);
      } else {
        this.setTabbableMenu(arrays.find(this.orderedMenuItems.all, item => item.isTabTarget()));
      }
    }
  }

  setTabbableMenu(menu: Menu) {
    if (!this.tabbable || menu === this.tabbableMenu) {
      return;
    }
    if (this.tabbableMenu) {
      this.tabbableMenu.setTabbable(false);
    }
    this.tabbableMenu = menu;
    if (menu) {
      menu.setTabbable(true);
    }
  }

  /**
   * Sets the property hiddenByUi. This does not automatically update the visibility of the menus.
   * We assume that {@link updateVisibility} is called later anyway.
   * @internal
   */
  setHiddenByUi(hiddenByUi: boolean) {
    this.setProperty('hiddenByUi', hiddenByUi);
  }

  updateVisibility() {
    menus.updateSeparatorVisibility(this.orderedMenuItems.left);
    menus.updateSeparatorVisibility(this.orderedMenuItems.right);
    this.setVisible(!this.hiddenByUi && this.orderedMenuItems.all.some(m => m.visible && !m.ellipsis));
  }

  /**
   * First rendered item that is enabled and reacts to ENTER keystroke shall be marked as 'defaultMenu'
   *
   * @param updateTabbableMenu if true (default), the "tabbable menu" is updated at the end of this method.
   */
  updateDefaultMenu(updateTabbableMenu?: boolean) {
    let defaultMenu: Menu = null;
    for (let i = 0; i < this.orderedMenuItems.all.length; i++) {
      let item = this.orderedMenuItems.all[i];

      if (!item.visible || !item.enabled || item.defaultMenu === false) {
        // Invisible or disabled menus and menus that explicitly have the "defaultMenu"
        // property set to false cannot be the default menu.
        continue;
      }
      if (item.defaultMenu) {
        defaultMenu = item;
        break;
      }
      if (!defaultMenu && this._isDefaultKeyStroke(item.actionKeyStroke)) {
        defaultMenu = item;
      }
    }

    this.setDefaultMenu(defaultMenu);
    if (scout.nvl(updateTabbableMenu, true)) {
      this._updateTabbableMenu();
    }
  }

  protected _isDefaultKeyStroke(keyStroke: KeyStroke): boolean {
    return scout.isOneOf(keys.ENTER, keyStroke.which) &&
      !keyStroke.ctrl &&
      !keyStroke.alt &&
      !keyStroke.shift;
  }

  setDefaultMenu(defaultMenu: Menu) {
    this.setProperty('defaultMenu', defaultMenu);
  }

  protected _setDefaultMenu(defaultMenu: Menu) {
    if (this.defaultMenu) {
      this.defaultMenu.setMenuStyle(Menu.MenuStyle.NONE);
    }
    if (defaultMenu) {
      defaultMenu.setMenuStyle(Menu.MenuStyle.DEFAULT);
    }
    this._setProperty('defaultMenu', defaultMenu);
  }

  /**
   * Add class 'left-of-button' to every menu item which is on the left of a button
   */
  updateLeftOfButtonMarker() {
    this._updateLeftOfButtonMarker(this.orderedMenuItems.left);
    this._updateLeftOfButtonMarker(this.orderedMenuItems.right);
  }

  protected _updateLeftOfButtonMarker(items: Menu[]) {
    let item, previousItem;

    items = items.filter(item => item.visible && item.rendered);

    for (let i = 0; i < items.length; i++) {
      item = items[i];
      item.$container.removeClass('left-of-button');
      if (i > 0 && item.isButton()) {
        previousItem = items[i - 1];
        previousItem.$container.addClass('left-of-button');
      }
    }
  }

  protected _onMenuItemPropertyChange(event: PropertyChangeEvent) {
    // We do not update the items directly, because this listener may be fired many times in one
    // user request (because many menus change one or more properties). Therefore, we just invalidate
    // the MenuBarLayout. It will be updated automatically after the user request has finished,
    // because the layout calls rebuildItemsInternal().
    let source = event.source as Menu;
    if (event.propertyName === 'overflown' || event.propertyName === 'enabledComputed' || event.propertyName === 'visible' || event.propertyName === 'hidden') {
      if (!this.tabbableMenu || source === this.tabbableMenu) {
        this._updateTabbableMenu();
      }
    }
    if (event.propertyName === 'overflown' || event.propertyName === 'hidden') {
      if (!this.defaultMenu || source === this.defaultMenu) {
        this.updateDefaultMenu();
      }
    }
    if (event.propertyName === 'horizontalAlignment') {
      // reorder
      this.reorderMenus(event.newValue <= 0);
    }
    if (event.propertyName === 'visible') {
      let oldVisible = this.visible;
      this.updateVisibility();
      if (!oldVisible && this.visible) {
        // If the menubar was previously invisible (because all menus were invisible) but
        // is now visible, the menu-boxes and the menus have to be rendered now. Otherwise,
        // calculating the preferred size of the menubar, e.g. in the TableLayout, would
        // return the wrong value (even if the menubar itself is visible).
        this.revalidateLayout();
      }
      // recalculate position of ellipsis if any menu item changed visibility.
      // separators may change visibility during reordering menu items. Since separators do not have any
      // impact of right/left order of menu items they have not to be considered to enforce a reorder.
      if (!source.separator) {
        this.reorderMenus();
      }
    }
    if (event.propertyName === 'keyStroke' || event.propertyName === 'enabledComputed' || event.propertyName === 'defaultMenu' || event.propertyName === 'visible') {
      this.updateDefaultMenu();
    }
  }

  protected _onMenuItemFocus(event: Event<Menu>) {
    this.setTabbableMenu(event.source);
  }

  reorderMenus(rightFirst?: boolean) {
    let menuItems = this.menuItems;
    this._setMenuItems(menuItems, rightFirst);
    if (this.rendered) {
      this.updateLeftOfButtonMarker();
    }
  }

  allMenusAsFlatList(): Menu[] {
    return arrays.flatMap(this.orderedMenuItems.all, item => {
      if (item instanceof ComboMenu) {
        return item.childActions;
      }
      return [item];
    });
  }
}

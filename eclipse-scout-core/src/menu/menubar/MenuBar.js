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
import {arrays, GroupBoxMenuItemsOrder, HtmlComponent, keys, KeyStrokeContext, Menu, MenuBarLayout, MenuBarLeftKeyStroke, MenuBarRightKeyStroke, MenuDestinations, menus, scout, Widget, widgets} from '../../index';
import ComboMenu from '../ComboMenu';

export default class MenuBar extends Widget {

  constructor() {
    super();

    this.menuSorter = null;
    this.menuFilter = null;
    this.position = MenuBar.Position.TOP;
    this.tabbable = true;
    this.menuboxLeft = null;
    this.menuboxRight = null;
    this.menuItems = []; // original list of menuItems that was passed to setMenuItems(), only used to check if menubar has changed
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
  };

  static Position = {
    TOP: 'top',
    BOTTOM: 'bottom'
  };

  _init(options) {
    super._init(options);

    this.menuSorter = options.menuOrder || new GroupBoxMenuItemsOrder();
    this.menuSorter.menuBar = this;
    if (options.menuFilter) {
      this.menuFilter = (menus, destination, onlyVisible, enableDisableKeyStroke) => options.menuFilter(menus, MenuDestinations.MENU_BAR, onlyVisible, enableDisableKeyStroke);
    }

    this.menuboxLeft = scout.create('MenuBarBox', {
      parent: this,
      cssClass: 'left',
      tooltipPosition: this._oppositePosition()
    });
    this.menuboxRight = scout.create('MenuBarBox', {
      parent: this,
      cssClass: 'right',
      tooltipPosition: this._oppositePosition()
    });

    this._setMenuItems(arrays.ensure(this.menuItems));
    this.updateVisibility();
  }

  _destroy() {
    super._destroy();
    this._detachMenuHandlers();
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke([
      new MenuBarLeftKeyStroke(this),
      new MenuBarRightKeyStroke(this)
    ]);
  }

  /**
   * @override Widget.js
   */
  _render() {
    this.$container = this.$parent.appendDiv('menubar');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new MenuBarLayout(this));

    this.menuboxRight.render(this.$container);
    this.menuboxLeft.render(this.$container);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderMenuItems();
    this._renderPosition();
  }

  setPosition(position) {
    this.setProperty('position', position);
  }

  _setPosition(position) {
    this._setProperty('position', position);
    this.menuboxLeft.setTooltipPosition(this._oppositePosition());
    this.menuboxRight.setTooltipPosition(this._oppositePosition());
  }

  _renderPosition() {
    this.$container.toggleClass('bottom', this.position === MenuBar.Position.BOTTOM);
  }

  _oppositePosition() {
    return this.position === MenuBar.Position.TOP ?
      MenuBar.Position.BOTTOM : MenuBar.Position.TOP;
  }

  setEllipsisPosition(ellipsisPosition) {
    this.setProperty('ellipsisPosition', ellipsisPosition);
  }

  /**
   * Set the filter of the menu bar to all the menu items.
   */
  _setChildMenuFilters() {
    this.orderedMenuItems.all.forEach(function(item) {
      item.setMenuFilter(this.menuFilter);
    }, this);
  }

  /**
   * This function can be called multiple times. The function attaches the menu handlers only if they are not yet added.
   */
  _attachMenuHandlers() {
    this.orderedMenuItems.all.forEach(function(item) {
      if (item.events.count('propertyChange', this._menuItemPropertyChangeHandler) === 0) {
        item.on('propertyChange', this._menuItemPropertyChangeHandler);
      }
      if (item.events.count('focus', this._focusHandler) === 0) {
        item.on('focus', this._focusHandler);
      }
    }, this);
  }

  _detachMenuHandlers() {
    this.orderedMenuItems.all.forEach(item => {
      item.off('propertyChange', this._menuItemPropertyChangeHandler);
      item.off('focus', this._focusHandler);
    });
  }

  setMenuItems(menuItems) {
    menuItems = arrays.ensure(menuItems);
    if (arrays.equals(this.menuItems, menuItems)) {
      // Ensure existing menus are correctly linked even if the given menuItems are the same (see TableSpec for reasons)
      this.menuboxRight.link(this.menuboxRight.menuItems);
      this.menuboxLeft.link(this.menuboxLeft.menuItems);
      return;
    }
    this.setProperty('menuItems', menuItems);
  }

  _setMenuItems(menuItems, rightFirst) {
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

  _renderMenuItems() {
    widgets.updateFirstLastMarker(this.menuItems);
    this.updateLeftOfButtonMarker();
    this.invalidateLayoutTree();
  }

  _removeMenuItems() {
    // NOP: by implementing this function we avoid the call to Widget.js#_internalRemoveWidgets
    // which would remove our menuItems, because they are defined as widget-property (see constructor).
  }

  _createOrderedMenus(menuItems) {
    let orderedMenuItems = this.menuSorter.order(menuItems, this),
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
      ellipsis = scout.create('EllipsisMenu', {
        parent: this,
        cssClass: 'overflow-menu-item'
      });
      this._ellipsis = ellipsis;

      // add ellipsis to the correct position
      if (this.ellipsisPosition === MenuBar.EllipsisPosition.RIGHT) {
        // try right
        let reverseIndexPosition = this._getFirstStackableIndexPosition(orderedMenuItems.right.slice().reverse());
        if (reverseIndexPosition > -1) {
          ellipsisIndex = orderedMenuItems.right.length - reverseIndexPosition;
          ellipsis.rightAligned = true;
          orderedMenuItems.right.splice(ellipsisIndex, 0, ellipsis);
        } else {
          // try left
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

  _getFirstStackableIndexPosition(menuList) {
    let foundIndex = -1;
    menuList.some((menu, index) => {
      if (menu.stackable && menu.visible) {
        foundIndex = index;
        return true;
      }
      return false;
    }, this);

    return foundIndex;
  }

  _updateTabbableMenu() {
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

  setTabbableMenu(menu) {
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
   * We assume that #updateVisibility() is called later anyway.
   *
   * @param {boolean} hiddenByUi
   */
  setHiddenByUi(hiddenByUi) {
    this.setProperty('hiddenByUi', hiddenByUi);
  }

  updateVisibility() {
    menus.updateSeparatorVisibility(this.orderedMenuItems.left);
    menus.updateSeparatorVisibility(this.orderedMenuItems.right);
    this.setVisible(!this.hiddenByUi && this.orderedMenuItems.all.some(m => {
      return m.visible && !m.ellipsis;
    }));
  }

  /**
   * First rendered item that is enabled and reacts to ENTER keystroke shall be marked as 'defaultMenu'
   *
   * @param {boolean} [updateTabbableMenu] if true (default), the "tabbable menu" is updated at the end of this method.
   */
  updateDefaultMenu(updateTabbableMenu) {
    let defaultMenu = null;
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

  _isDefaultKeyStroke(keyStroke) {
    return scout.isOneOf(keys.ENTER, keyStroke.which) &&
      !keyStroke.ctrl &&
      !keyStroke.alt &&
      !keyStroke.shift;
  }

  setDefaultMenu(defaultMenu) {
    this.setProperty('defaultMenu', defaultMenu);
  }

  _setDefaultMenu(defaultMenu) {
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

  _updateLeftOfButtonMarker(items) {
    let item, previousItem;

    items = items.filter(item => {
      return item.visible && item.rendered;
    });

    for (let i = 0; i < items.length; i++) {
      item = items[i];
      item.$container.removeClass('left-of-button');
      if (i > 0 && item.isButton()) {
        previousItem = items[i - 1];
        previousItem.$container.addClass('left-of-button');
      }
    }
  }

  _onMenuItemPropertyChange(event) {
    // We do not update the items directly, because this listener may be fired many times in one
    // user request (because many menus change one or more properties). Therefore, we just invalidate
    // the MenuBarLayout. It will be updated automatically after the user request has finished,
    // because the layout calls rebuildItemsInternal().
    if (event.propertyName === 'overflown' || event.propertyName === 'enabledComputed' || event.propertyName === 'visible' || event.propertyName === 'hidden') {
      if (!this.tabbableMenu || event.source === this.tabbableMenu) {
        this._updateTabbableMenu();
      }
    }
    if (event.propertyName === 'overflown' || event.propertyName === 'hidden') {
      if (!this.defaultMenu || event.source === this.defaultMenu) {
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
        // is now visible, the menuboxes and the menus have to be rendered now. Otherwise,
        // calculating the preferred size of the menubar, e.g. in the TableLayout, would
        // return the wrong value (even if the menubar itself is visible).
        this.revalidateLayout();
      }
      // recalculate position of ellipsis if any menu item changed visibility.
      // separators may change visibility during reordering menu items. Since separators do not have any
      // impact of right/left order of menu items they have not to be considered to enforce a reorder.
      if (!event.source.separator) {
        this.reorderMenus();
      }
    }
    if (event.propertyName === 'keyStroke' || event.propertyName === 'enabledComputed' || event.propertyName === 'defaultMenu' || event.propertyName === 'visible') {
      this.updateDefaultMenu();
    }
  }

  _onMenuItemFocus(event) {
    this.setTabbableMenu(event.source);
  }

  reorderMenus(rightFirst) {
    let menuItems = this.menuItems;
    this._setMenuItems(menuItems, rightFirst);
    if (this.rendered) {
      this.updateLeftOfButtonMarker();
    }
  }

  _allMenusAsFlatList() {
    return arrays.flatMap(this.orderedMenuItems.all, item => {
      if (item instanceof ComboMenu) {
        return item.childActions;
      }
      return [item];
    });
  }

}

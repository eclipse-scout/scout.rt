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
scout.MenuBar = function() {
  scout.MenuBar.parent.call(this);

  this._addEventSupport();

  this.menuSorter;
  this.position = 'top'; // or 'bottom'
  this.size = 'small'; // or 'large'
  this.tabbable = true;
  this._internalMenuItems = []; // original list of menuItems that was passed to updateItems(), only used to check if menubar has changed
  this.menuItems = []; // list of menuItems (ordered, may contain additional UI separators, some menus may not be rendered)
  this._orderedMenuItems = {
    left: [],
    right: []
  }; // Object containing "left" and "right" menus
  this.defaultMenu = null;
  this.visible = false;

  this._menuItemPropertyChangeListener = function(event) {
    if (event.changedProperties.indexOf('enabled') > -1) {
      this.updateDefaultMenu();
      //this.updateTabbableMenu();
    }
    if (event.changedProperties.indexOf('visible') > -1) {
      this.updateVisibility();
      this.updateTabbableMenu();
      // Mainly necessary for menus currently not rendered (e.g. in ellipsis menu).
      // If the menu is rendered, the menu itself triggers invalidateLayoutTree (see Menu.js#_renderVisible)
      this.invalidateLayoutTree();
    }
  }.bind(this);

  this._menuBoxUpdateVisibleMenusListener = function(event) {
    this.updateTabbableMenu();
  }.bind(this);
};
scout.inherits(scout.MenuBar, scout.Widget);

scout.MenuBar.prototype._init = function(options) {
  scout.MenuBar.parent.prototype._init.call(this, options);

  this.menuSorter = options.menuOrder;
  this.menuSorter.menuBar = this;
  this.menuFilter = options.menuFilter;

  this.menuBoxLeft = scout.create('MenuBox', {
    parent: this,
    shrinkable: false
  });
  this.menuBoxRight = scout.create('MenuBox', {
    parent: this
  });
  // Link (used by key strokes)
  this.menuBoxLeft.nextMenuBox = this.menuBoxRight;
  this.menuBoxRight.prevMenuBox = this.menuBoxLeft;

  this.menuBoxLeft.on('updateVisibleMenus', this._menuBoxUpdateVisibleMenusListener);
  this.menuBoxRight.on('updateVisibleMenus', this._menuBoxUpdateVisibleMenusListener);
};

scout.MenuBar.prototype.destroy = function() {
  this._uninstallMenuItemPropertyChangeListener();
  scout.MenuBar.parent.prototype.destroy.call(this);
};

scout.MenuBar.prototype._render = function($parent) {
  this.$container = $parent.makeDiv('menubar')
    .attr('id', 'MenuBar-' + scout.objectFactory.createUniqueId())
    .toggleClass('main-menubar', this.size === 'large');

  if (this.position === 'top') {
    $parent.prepend(this.$container);
  } else {
    this.$container.addClass('bottom');
    $parent.append(this.$container);
  }

  this.menuBoxLeft.render(this.$container);
  this.menuBoxLeft.$container.addClass('left');

  this.menuBoxRight.render(this.$container);
  this.menuBoxRight.$container.addClass('right');

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.MenuBarLayout(this));

  this.updateVisibility();
  this.updateTabbableMenu();
};

scout.MenuBar.prototype._renderProperties = function() {
  scout.MenuBar.parent.prototype._renderProperties.call(this);
  this._renderVisible();
};

scout.MenuBar.prototype._renderVisible = function() {
  this.$container.setVisible(this.visible);
  this.invalidateLayoutTree();
};

scout.MenuBar.prototype.bottom = function() {
  this.position = 'bottom';
  this.updateTooltipPosition();
};

scout.MenuBar.prototype.top = function() {
  this.position = 'top';
  this.updateTooltipPosition();
};

scout.MenuBar.prototype.large = function() {
  this.size = 'large';
};

scout.MenuBar.prototype._destroyMenuSorterSeparators = function() {
  this.menuItems.forEach(function(item) {
    if (item.createdBy === this.menuSorter) {
      item.destroy();
    }
  }, this);
};

scout.MenuBar.prototype.setMenuItems = function(menuItems) {
  menuItems = scout.arrays.ensure(menuItems);
  // Only update if list of menus changed. Don't compare this.menuItems, because that list
  // may contain additional UI separators, and may not be in the same order
  var sameMenuItems = scout.arrays.equals(this._internalMenuItems, menuItems);

  if (!sameMenuItems) {
    this._uninstallMenuItemPropertyChangeListener();

    // The menuSorter may add separators to the list of items -> destroy the old ones first
    this._destroyMenuSorterSeparators();

    this._internalMenuItems = menuItems;
    this._orderedMenuItems = this.menuSorter.order(menuItems, this);
    this.menuItems = this._orderedMenuItems.left.concat(this._orderedMenuItems.right);

    this.menuBoxLeft.setMenus(this._orderedMenuItems.left);
    this.menuBoxRight.setMenus(this._orderedMenuItems.right);

    this._installMenuItemPropertyChangeListener();
  }

  this.updateDefaultMenu();
  this.updateTooltipPosition();

  if (this.rendered) {
    this.updateVisibility();
    this.updateTabbableMenu();
  }
};

scout.MenuBar.prototype.updateVisibility = function() {
  this.setVisible(!this.hiddenByUi && this.menuItems.some(function(m) {
    return m.visible;
  }));
};

scout.MenuBar.prototype.setVisible = function(visible) {
  if (this.visible === visible) {
    return;
  }
  this._setProperty('visible', visible);
  if (this.rendered) {
    this._renderVisible();
  }
};

/**
 * First rendered item that is enabled and reacts to ENTER keystroke shall be marked as 'defaultMenu'
 */
scout.MenuBar.prototype.updateDefaultMenu = function() {
  this.setDefaultMenu(null);
  var found = this._updateDefaultMenuInItems(this._orderedMenuItems.right);
  if (!found) {
    this._updateDefaultMenuInItems(this._orderedMenuItems.left);
  }
};

scout.MenuBar.prototype._updateDefaultMenuInItems = function(items) {
  var found = false;
  items.some(function(item) {
    if (item.visible && item.enabled && this._isDefaultKeyStroke(item.actionKeyStroke)) {
      this.setDefaultMenu(item);
      found = true;
      return true;
    }
  }.bind(this));
  return found;
};

scout.MenuBar.prototype.setDefaultMenu = function(defaultMenu) {
  if (this.defaultMenu === defaultMenu) {
    return;
  }
  if (this.defaultMenu) {
    this.defaultMenu.setDefaultMenu(false);
  }
  this.defaultMenu = defaultMenu;
  if (this.defaultMenu) {
    this.defaultMenu.setDefaultMenu(true);
  }
};

scout.MenuBar.prototype._isDefaultKeyStroke = function(keyStroke) {
  return scout.isOneOf(scout.keys.ENTER, keyStroke.which) &&
    !keyStroke.ctrl &&
    !keyStroke.alt &&
    !keyStroke.shift;
};

scout.MenuBar.prototype.updateTooltipPosition = function() {
  var tooltipPosition = (this.position === 'top' ? 'bottom' : 'top');
  this.menuItems.forEach(function(menu) {
    menu.tooltipPosition = tooltipPosition;
  }, this);
};

scout.MenuBar.prototype.updateTabbableMenu = function() {
  if (this.tabbable) {
    scout.menus.updateTabbableMenu(this.menuItems, this.defaultMenu);
  }
};

scout.MenuBar.prototype._uninstallMenuItemPropertyChangeListener = function() {
  this._internalMenuItems.forEach(function(menu) {
    menu.off('propertyChange', this._menuItemPropertyChangeListener);
  }, this);
};

scout.MenuBar.prototype._installMenuItemPropertyChangeListener = function() {
  this._internalMenuItems.forEach(function(menu) {
    menu.on('propertyChange', this._menuItemPropertyChangeListener);
  }, this);
};

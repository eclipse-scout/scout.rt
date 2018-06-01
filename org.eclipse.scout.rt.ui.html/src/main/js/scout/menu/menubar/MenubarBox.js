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
scout.MenubarBox = function() {
  scout.MenubarBox.parent.call(this);
  this.menuItems = [];
  this._addWidgetProperties('menuItems');
  this._menuItemPropertyChangeHandler = this._onMenuItemPropertyChange.bind(this);
};
scout.inherits(scout.MenubarBox, scout.Widget);

scout.MenubarBox.prototype._init = function(options) {
  scout.MenubarBox.parent.prototype._init.call(this, options);
};

scout.MenubarBox.prototype._destroy = function() {
  scout.MenubarBox.parent.prototype._destroy.call(this);
  this._removeMenuHandlers();
};

scout.MenubarBox.prototype._render = function() {
  this.$container = this.$parent.appendDiv('menubox');

  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.MenubarBoxLayout(this));
};

scout.MenubarBox.prototype._renderProperties = function() {
  scout.MenubarBox.parent.prototype._renderProperties.call(this);
  this._renderMenuItems();
};

scout.MenubarBox.prototype.setMenuItems = function(menuItems) {
  menuItems = scout.arrays.ensure(menuItems);
  if (!scout.arrays.equals(this.menuItems, menuItems)) {
    this.setProperty('menuItems', menuItems);
  }
};

scout.MenubarBox.prototype._setMenuItems = function(menuItems) {
  // remove property listeners of old menu items.
  this._removeMenuHandlers();

  this._setProperty('menuItems', menuItems);
  // add property listener of new menus
  this._addMenuHandlers();
};

/**
 * @override Widget.js
 */
scout.MenubarBox.prototype._remove = function() {
  this._removeMenuItems();
  scout.MenubarBox.parent.prototype._remove.call(this);
};

scout.MenubarBox.prototype._removeMenuItems = function() {
  this._removeMenuHandlers();
  this.menuItems.forEach(function(item) {
    item.overflow = false;
    item.remove();
  }.bind(this));
};

scout.MenubarBox.prototype._renderMenuItems = function() {
  var tooltipPosition = (this.position === 'top' ? 'bottom' : 'top');

  this.menuItems.forEach(function(item) {
    item.tooltipPosition = tooltipPosition;
    item.render(this.$container);
    item.$container.addClass('menubar-item');
  }.bind(this));

  if (!this.rendering) {
    this.invalidateLayoutTree();
  }
};

scout.MenubarBox.prototype._addMenuHandlers = function() {
  this.menuItems.forEach(function(item) {
    item.off('propertyChange', this._menuItemPropertyChangeHandler);
  }, this);
};

scout.MenubarBox.prototype._removeMenuHandlers = function() {
  this.menuItems.forEach(function(item) {
    item.off('propertyChange', this._menuItemPropertyChangeHandler);
  }, this);
};

scout.MenubarBox.prototype._renderVisible = function() {
  scout.MenubarBox.parent.prototype._renderVisible.call(this);
  this.revalidateLayout();
};

scout.MenubarBox.prototype._onMenuItemPropertyChange = function(event) {
  if (event.propertyName === 'visible') {
    this.setVisible(this.menuItems.some(function(m) {
      return m.visible && !m.ellipsis;
    }));
  }
};

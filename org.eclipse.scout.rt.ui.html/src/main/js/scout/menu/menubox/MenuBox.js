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
scout.MenuBox = function(menuBar) {
  scout.MenuBox.parent.call(this);
  this.compact = false;
};
scout.inherits(scout.MenuBox, scout.Widget);

scout.MenuBox.prototype._init = function(options) {
  scout.MenuBox.parent.prototype._init.call(this, options);
  this.menus = options.menus || [];
  this.uiMenuCssClass = options.uiMenuCssClass || '';
  this.uiMenuCssClass += ' ' + 'menu-box-item';
  this._initMenus(this.menus);
};

scout.MenuBox.prototype._initMenus = function(menus) {
  menus.forEach(this._initMenu.bind(this));
};

scout.MenuBox.prototype._initMenu = function(menu) {
  menu.setParent(this);
  menu.uiCssClass = this.uiMenuCssClass;
};

/**
 * @override Widget.js
 */
scout.MenuBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('menu-box');

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.MenuBoxLayout(this));
};

scout.MenuBox.prototype._renderProperties = function() {
  this._renderMenus();
  this._renderCompact();
};

scout.MenuBox.prototype._renderMenus = function() {
  this.menus.forEach(function(menu) {
    menu.render(this.$container);
  }, this);
  this.invalidateLayoutTree();
};

scout.MenuBox.prototype._renderCompact = function() {
  this.$container.toggleClass('compact', this.compact);
  this.invalidateLayoutTree();
};

scout.MenuBox.prototype.setCompact = function(compact) {
  if (this.compact === compact) {
    return;
  }
  this.compact = compact;
  if (this.rendered) {
    this._renderCompact();
  }
};

scout.MenuBox.prototype.setMenus = function(menus) {
  if (this.menus) {
    this.menus.forEach(function(menu) {
      menu.remove();
    });
  }
  this.menus = menus;
  if (this.rendered) {
    this._renderMenus();
  }
};

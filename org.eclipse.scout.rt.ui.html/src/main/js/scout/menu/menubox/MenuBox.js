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

  this._addKeyStrokeContextSupport();
  this._addEventSupport();

  this.menus = [];
  this.compact = false;
  this.tabbable = false;
  this.shrinkable = true;

  // Set by MenuBar, used by MenuBox[Left|Right]KeyStroke
  this.prevMenuBox;
  this.nextMenuBox;
};
scout.inherits(scout.MenuBox, scout.Widget);

scout.MenuBox.prototype._init = function(options) {
  scout.MenuBox.parent.prototype._init.call(this, options);
  this.uiMenuCssClass = options.uiMenuCssClass || '';
  this.uiMenuCssClass += ' ' + 'menu-box-item';
  this.shrinkable = scout.nvl(options.shrinkable, this.shrinkable);
  this._setMenus(options.menus || []);
};

scout.MenuBox.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  keyStrokeContext.registerKeyStroke([
    new scout.MenuBoxLeftKeyStroke(this),
    new scout.MenuBoxRightKeyStroke(this)
  ]);
};

scout.MenuBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('menu-box');

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.MenuBoxLayout(this));
};

scout.MenuBox.prototype._renderProperties = function() {
  this._renderMenus();
  this._renderCompact();
};

scout.MenuBox.prototype.setMenus = function(menus) {
  if (scout.arrays.equals(this.menus, menus)) {
    return;
  }
  if (this.rendered) {
    this._removeMenus();
  }
  this._setMenus(menus);
  if (this.rendered) {
    this._renderMenus();
  }
};

scout.MenuBox.prototype._setMenus = function(menus) {
  // Unlink old menus (but only if they are not already linked to a different widget)
  this.menus.forEach(function(menu) {
    if (menu.parent === this) {
      menu.setParent(null);
    }
  }, this);
  this.menus = scout.arrays.ensure(menus);
  this._initMenus(this.menus);
};

scout.MenuBox.prototype._initMenus = function(menus) {
  menus.forEach(this._initMenu.bind(this));
};

scout.MenuBox.prototype._initMenu = function(menu) {
  menu.setParent(this);
  menu.uiCssClass = this.uiMenuCssClass;
};

scout.MenuBox.prototype._renderMenus = function() {
  this.menus.forEach(function(menu) {
    menu.render(this.$container);
  }, this);
  this.updateVisibility();
  this.updateTabbableMenu();
  this.invalidateLayoutTree();
};

scout.MenuBox.prototype._removeMenus = function() {
  this.menus.forEach(function(menu) {
    menu.remove();
  });
  this.trigger('removeAllMenus');
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

scout.MenuBox.prototype._renderCompact = function() {
  this.$container.toggleClass('compact', this.compact);
  this.invalidateLayoutTree();
};

scout.MenuBox.prototype.visibleMenus = function(includeUnrenderedMenus) {
  includeUnrenderedMenus = scout.nvl(includeUnrenderedMenus, true);
  return this.menus.filter(function(menu) {
    return menu.visible && (includeUnrenderedMenus || menu.rendered);
  });
};

scout.MenuBox.prototype.updateTabbableMenu = function() {
  if (this.tabbable) {
    scout.menus.updateTabbableMenu(this.menus);
  }
};

scout.MenuBox.prototype.updateVisibility = function() {
  // Hide menu box with no menu items because on iOS the width of an empty menu box is 1px which breaks the menubar layout (rightWidth === 0 check)
  this.$container.toggleClass('hidden', this.menus.length === 0);
};

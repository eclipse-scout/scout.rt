/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ContextMenuPopup = function() {
  scout.ContextMenuPopup.parent.call(this);

  // Make sure head won't be rendered, there is a css selector which is applied only if there is a head
  this._headVisible = false;
};
scout.inherits(scout.ContextMenuPopup, scout.PopupWithHead);

scout.ContextMenuPopup.prototype._init = function(options) {
  options.focusableContainer = true; // In order to allow keyboard navigation, the popup must gain focus. Because menu-items are not focusable, make the container focusable instead.
  scout.ContextMenuPopup.parent.prototype._init.call(this, options);

  this.menuItems = options.menuItems;
  this.filterFunc = options.filterFunc;
  this.options = $.extend({
    cloneMenuItems: true
  }, options);
};

/**
 * @override Popup.js
 */
scout.ContextMenuPopup.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.ContextMenuPopup.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  scout.menuNavigationKeyStrokes.registerKeyStrokes(keyStrokeContext, this, 'menu-item');
};

scout.ContextMenuPopup.prototype._render = function($parent) {
  scout.ContextMenuPopup.parent.prototype._render.call(this, $parent);
  scout.scrollbars.install(this.$body, {
    parent: this
  });
  this._renderMenuItems();
};

scout.ContextMenuPopup.prototype._renderMenuItems = function() {
  var menuClone, menus = this._getMenuItems();
  if(this.menu && this.menu.filterFunc){
    // TODO nbu figure out if we are in menu bar or contextmenu on table (following instanceof check does not work)
    menus = this.menu.filterFunc(menus, this instanceof scout.MenuBarPopup ?   'menuBar': 'contextMenu');
  }
  if (!menus || menus.length === 0) {
    return;
  }
  menus.forEach(function(menu) {
    // Invisible menus are rendered as well because their visibility might change dynamically
    if (menu.separator) {
      return;
    }
    if (this.options.cloneMenuItems) {
      menu = menu.cloneAdapter({
        parent: this,
        filterFunc: menu.filterFunc
      });
    } else {
      menu.setParent(this);
    }
    menu.render(this.$body);
    menu.afterSendDoAction = this.close.bind(this);
    menu.on('propertyChange', this._onMenuItemPropertyChange.bind(this));
  }, this);
  this._updateFirstLastClass();
};

/**
 * When cloneMenuItems is true, it means the menu instance is also used elsewhere (for instance in a menu-bar).
 * When cloneMenuItems is false, it means the menu instance is only used in this popup.
 * In the first case we must _not_ call the remove() method, since the menu is still in use outside of the
 * popup. In the second case we must call remove(), because the menu is only used in the popup and no one
 * else would remove the widget otherwise.
 *
 * @override Widget.js
 */
scout.ContextMenuPopup.prototype._remove = function() {
  this._getMenuItems().forEach(function(menu) {
    if (this.options.cloneMenuItems) {
      if (this.session.hasClones(menu)) {
        this.session.unregisterAllAdapterClones(menu);
      }
    } else {
      menu.remove();
    }
  }, this);
  scout.scrollbars.uninstall(this.$body, this.session);
  scout.ContextMenuPopup.parent.prototype._remove.call(this);
};

/**
 * @override PopupWithHead.js
 */
scout.ContextMenuPopup.prototype._modifyBody = function() {
  this.$body.addClass('context-menu');
};

/**
 * Override this method to return menu items or actions used to render menu items.
 */
scout.ContextMenuPopup.prototype._getMenuItems = function() {
  return this.menuItems;
};

/**
 * Updates the first and last visible menu items with the according css classes.
 * Necessary because invisible menu-items are rendered.
 */
scout.ContextMenuPopup.prototype._updateFirstLastClass = function(event) {
  var $firstMenuItem, $lastMenuItem;

  // TODO CGU after refactoring of menu-item to context-menu-item we can use last/first instead of a fully qualified name. We also could move this function to jquery-scout to make it reusable.
  this.$body.children('.menu-item').each(function() {
    var $menuItem = $(this);
    $menuItem.removeClass('context-menu-item-first context-menu-item-last');

    if ($menuItem.isVisible()) {
      if (!$firstMenuItem) {
        $firstMenuItem = $menuItem;
      }
      $lastMenuItem = $menuItem;
    }
  });
  if ($firstMenuItem) {
    $firstMenuItem.addClass('context-menu-item-first');
  }
  if ($lastMenuItem) {
    $lastMenuItem.addClass('context-menu-item-last');
  }
};

scout.ContextMenuPopup.prototype._onMenuItemPropertyChange = function(event) {
  if (!this.rendered) {
    return;
  }
  if (event.changedProperties.indexOf('visible') !== -1) {
    this._updateFirstLastClass();
  }
  // Make sure menu is positioned correctly afterwards (if it is opened upwards hiding/showing a menu item makes it necessary to reposition)
  this.position();
};

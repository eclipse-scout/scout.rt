scout.ContextMenuPopup = function(session, options) {
  options = options || {};
  options.focusableContainer = true; // In order to allow keyboard navigation, the popup must gain focus. Because menu-items are not focusable, make the container focusable instead.
  scout.ContextMenuPopup.parent.call(this, session, options);

  this.menuItems = options.menuItems;
  this.options = $.extend({
    cloneMenuItems: true
  }, options);
  // Make sure head won't be rendered, there is a css selector which is applied only if there is a head
  this._headVisible = false;
};
scout.inherits(scout.ContextMenuPopup, scout.PopupWithHead);

scout.ContextMenuPopup.prototype._render = function($parent) {
  scout.ContextMenuPopup.parent.prototype._render.call(this, $parent);
  this._beforeRenderMenuItems();
  this._renderMenuItems();
  this._afterRenderMenuItems();
};

/**
 * Override this method to do something before menu items are rendered.
 * The default impl. does nothing.
 */
scout.ContextMenuPopup.prototype._beforeRenderMenuItems = function() {
};

/**
 * Override this method to do something before menu items are rendered.
 * The default impl. does nothing.
 */
scout.ContextMenuPopup.prototype._afterRenderMenuItems = function() {
};

/**
 * Override this method to return menu items or actions used to render menu items.
 */
scout.ContextMenuPopup.prototype._getMenuItems = function() {
  return this.menuItems;
};

scout.ContextMenuPopup.prototype._renderMenuItems = function() {
  var menuClone, menus = this._getMenuItems();
  if (!menus || menus.length === 0) {
    return;
  }
  menus.forEach(function(menu) {
    // Invisible menus are rendered as well because their visibility might change dynamically
    if (menu.separator) {
      return;
    }
    if (this.options.cloneMenuItems) {
      menuClone = this._cloneMenuItem(menu);
      this.session.registerAdapterClone(menu, menuClone);
      menu.hasClone = true;
      menu = menuClone;
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
  scout.ContextMenuPopup.parent.prototype._remove.call(this);
  this._getMenuItems().forEach(function(menu) {
    if (this.options.cloneMenuItems) {
      if (menu.hasClone) {
        this.session.unregisterAllAdapterClones(menu);
        menu.hasClone = false;
      }
    } else {
      menu.remove();
    }
  }, this);
};

/**
 * Creates a shallow copy of the given menu instance, all references to DOM elements are removed
 * and the rendered property is set to false. Thus the method can be used to render an already rendered
 * menu again, as required when a pop-up menu is opened in a table or in a tree (where the same item
 * is already rendered in the menu-bar).
 *
 * @param menuItem can be a Button or a Menu instance.
 */
scout.ContextMenuPopup.prototype._cloneMenuItem = function(menuItem) {
  var clone = $.extend({}, menuItem);
  clone.rendered = false;
  clone.$container = null;
  return clone;
};

scout.ContextMenuPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.ContextMenuKeyStrokeAdapter(this);
};

/**
 * Updates the first and last visible menu items with the according css classes.
 * Necessary because invisible menu-items are rendered.
 */
scout.ContextMenuPopup.prototype._updateFirstLastClass = function(event) {
  var $firstMenuItem, $lastMenuItem;

  //TODO CGU after refactoring of menu-item to context-menu-item we can use last/first instead of a fully qualified name. We also could move this function to jquery-scout to make it reusable.
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

scout.ContextMenuPopup = function(session, options) {
  scout.ContextMenuPopup.parent.call(this, session, options);
  this.menuItems = options.menuItems;
  this.options = $.extend({
    cloneMenuItems: true
  }, options);
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
    if (!menu.visible || menu.separator) {
      return;
    }
    if (this.options.cloneMenuItems) {
      menuClone = this._cloneMenuItem(menu);
      this.session.registerAdapterClone(menu, menuClone);
      menu = menuClone;
    }
    menu.render(this.$body);
    menu.afterSendDoAction = this.close.bind(this);
  }, this);
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
      if (menu.visible && !menu.separator) {
        this.session.unregisterAllAdapterClones(menu);
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


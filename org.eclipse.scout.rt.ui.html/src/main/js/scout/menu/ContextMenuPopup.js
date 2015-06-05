scout.ContextMenuPopup = function(session, menuItems, options) {
  scout.ContextMenuPopup.parent.call(this, session);
  this.menuItems = menuItems;
  this.options = $.extend({
    cloneMenuItems: true
  }, options);
};
scout.inherits(scout.ContextMenuPopup, scout.Popup);

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
  var session = this.session,
    menus = this._getMenuItems();
  if (!menus || menus.length === 0) {
    return;
  }
  var menuClone;
  menus.forEach(function(menu) {
    if (!menu.visible || menu.separator) {
      return;
    }
    if (this.options.cloneMenuItems) {
      menuClone = this._cloneMenuItem(menu);
      session.registerAdapterClone(menu, menuClone);
      menu = menuClone;
    }
    menu.render(this.$body);

    var oldSendDoAction = menu.sendDoAction;
    var that = this;
    menu.sendDoAction = function() {
      oldSendDoAction.call(this);
      that.closePopup();
    };
  }.bind(this));
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
  return new scout.PopupMenuItemKeyStrokeAdapter(this);
};

/**
 * @override Popup.js
 */
scout.ContextMenuPopup.prototype.closePopup = function() {
  scout.ContextMenuPopup.parent.prototype.closePopup.call(this);
  var session = this.session;
  if (this.options.cloneMenuItems) {
    this._getMenuItems().forEach(function(menu) {
      if (menu.visible && !menu.separator) {
        session.unregisterAllAdapterClones(menu);
      }
    }, this);
  }
};

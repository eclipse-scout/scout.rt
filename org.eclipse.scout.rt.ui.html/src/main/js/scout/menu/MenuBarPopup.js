/**
 * The MenuBarPopup is a special Popup that is used in the menu-bar. It is tightly coupled with a menu-item and shows a header
 * which has a different size than the popup-body.
 */
scout.MenuBarPopup = function(menu, session, options) {
  options = options || {};
  options.triggerPopupOpenEvent = scout.helpers.nvl(options.triggerPopupOpenEvent, true);
  options.$anchor = menu.$container;
  scout.MenuBarPopup.parent.call(this, session, options);
  this.menu = menu;
  this.$headBlueprint = this.menu.$container;
  this.ignoreEvent = options.ignoreEvent;
  this._headVisible = true;
};
scout.inherits(scout.MenuBarPopup, scout.ContextMenuPopup);

/**
 * @override ContextMenuPopup.js
 */
scout.MenuBarPopup.prototype._getMenuItems = function() {
  return this.menu.childActions || this.menu.menus;
};

/**
 * @override Popup.js
 */
scout.MenuBarPopup.prototype.close = function(event) {
  if (!event || !this.ignoreEvent || event.originalEvent !== this.ignoreEvent.originalEvent) {
    scout.MenuBarPopup.parent.prototype.close.call(this);
  }
};

/**
 * @override PopupWithHead.js
 */
scout.MenuBarPopup.prototype._renderHead = function() {
  scout.MenuBarPopup.parent.prototype._renderHead.call(this);

  // FIXME AWE throws exception if this.menu is a button because button is not rendered (MenuButtonAdapter is)
  if (this.menu.$container.parent().hasClass('main-menubar')) {
    this.$head.addClass('in-main-menubar');
  }

  if (this.menu._customCssClasses) {
    this._copyCssClassToHead(this.menu._customCssClasses);
  }
  this._copyCssClassToHead('unfocusable');
  this._copyCssClassToHead('button');
  this._copyCssClassToHead('has-submenu');
};

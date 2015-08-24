scout.MenuBarRightKeyStroke = function(menuBar) {
  scout.MenuBarRightKeyStroke.parent.call(this);
  this.field = menuBar;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.keyStrokeMode.DOWN;
};
scout.inherits(scout.MenuBarRightKeyStroke, scout.KeyStroke);

scout.MenuBarRightKeyStroke.prototype.handle = function(event) {
  var menuItems = this.field.visibleMenuItems,
    $menuItemFocused = this.field.$container.find(':focus'),
    i, menuItem, elementToFocus, focusNext = false;

  for (i = 0; i < menuItems.length; i++) {
    menuItem = menuItems[i];
    if (focusNext && menuItem.isTabTarget()) {
      this.field.setTabbableMenu(menuItem);
      this.field.session.focusManager.requestFocus(menuItem.$container);
      break;
    }
    if ($menuItemFocused[0] === menuItem.$container[0]) {
      focusNext = true;
    }
  }
};

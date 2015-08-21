scout.MenuBarLeftKeyStroke = function(menuBar) {
  scout.MenuBarLeftKeyStroke.parent.call(this);
  this.field = menuBar;
  this.which = [scout.keys.LEFT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
};
scout.inherits(scout.MenuBarLeftKeyStroke, scout.KeyStroke);

scout.MenuBarLeftKeyStroke.prototype.handle = function(event) {
  var menuItems = this.field.visibleMenuItems,
    $menuItemFocused = this.field.$container.find(':focus'),
    i, menuItem, lastValidItem, elementToFocus;

  for (i = 0; i < menuItems.length; i++) {
    menuItem = menuItems[i];
    if ($menuItemFocused[0] === menuItem.$container[0]) {
      if (lastValidItem) {
        this.field.setTabbableMenu(lastValidItem);
        this.field.session.focusManager.requestFocus(lastValidItem.$container);
      }
      break;
    }
    if (menuItem.isTabTarget()) {
      lastValidItem = menuItem;
    }
  }
};

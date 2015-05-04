scout.TableContextMenuPopup = function(table, session, menuItems) {
  scout.TableContextMenuPopup.parent.call(this, session);
  this.table = table;
  this.$head;
  this.$deco;
  this.menuItems = menuItems;
};
scout.inherits(scout.TableContextMenuPopup, scout.Popup);

scout.TableContextMenuPopup.prototype._render = function($parent) {
  scout.TableContextMenuPopup.parent.prototype._render.call(this, $parent);
  var menus = this.menuItems;
  if (!menus || menus.length === 0) {
    return;
  }
  var i;

  for (i = 0; i < menus.length; i++) {
    var menu = menus[i];
    if (!menu.visible) {
      continue;
    }
    if (menu.separator) {
      continue;
    }
    menu.sendAboutToShow();
    this.$body.appendDiv('menu-item')
      .text(menu.text)
      .on('click', '', this.onMenuItemClicked.bind(this, menu))
      .one(scout.menus.CLOSING_EVENTS, $.suppressEvent);
  }
};

scout.TableContextMenuPopup.prototype.onMenuItemClicked = function(menu) {
  this.closePopup();
  menu.sendDoAction();
};

scout.TableContextMenuPopup.prototype._createKeyStrokeAdapter = function() {
  return new scout.PopupMenuItemKeyStrokeAdapter(this);
};

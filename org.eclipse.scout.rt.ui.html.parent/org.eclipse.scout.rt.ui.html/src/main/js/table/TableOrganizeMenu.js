scout.TableOrganizeMenu = function() {
  scout.TableOrganizeMenu.parent.call(this);
};

scout.inherits(scout.TableOrganizeMenu, scout.Menu);

scout.TableOrganizeMenu.prototype._onMenuClicked = function(event) {
  var popup = new scout.PopupMenuItem($(event.target));
  popup.render(this.parent.$container);
  popup.addClassToBody('table-menu-organize');
  popup.appendToBody(this._createBody(popup));
  popup.alignTo();
};

scout.TableOrganizeMenu.prototype._createBody = function(popup) {
  return $('<button>').
    text(scout.texts.get('resetColumns')).
    click(function() {
      var table = this.parent;
      popup.remove();
      this.session.send('resetColumns', table.id);
    }.bind(this)).
    one(scout.menus.CLOSING_EVENTS, $.suppressEvent);
};


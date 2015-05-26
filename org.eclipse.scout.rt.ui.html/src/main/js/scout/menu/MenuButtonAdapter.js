scout.MenuButtonAdapter = function(button) {
  scout.MenuButtonAdapter.parent.call(this);

  this.session = button.session;
  this._button = button;
};
scout.inherits(scout.MenuButtonAdapter, scout.Menu);

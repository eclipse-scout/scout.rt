scout.MenuButtonAdapter = function(button) {
  scout.MenuButtonAdapter.parent.call(this);

  this.session = button.session;
  this._button = button;

  this.enabled = this._button.enabled;
  this.visible = this._button.visible;
  this.selected = this._button.selected;
  this.text = this._button.label;
  this.id = this._button.id;
  this.keyStrokes = this._button.keyStrokes;

  this.objectType = 'Menu';

  // FIXME AWE: (menu) do this in #init | create through objectFactory
  if (this._button.systemType === scout.Button.SYSTEM_TYPE.OK ||
      this._button.systemType === scout.Button.SYSTEM_TYPE.SAVE_WITHOUT_MARKER_CHANGE) {
    this.defaultMenu = true;
  }

  if (this._button.displayStyle !== scout.Button.DISPLAY_STYLE.LINK) {
    this.menuStyle = 'button';
  }

};
scout.inherits(scout.MenuButtonAdapter, scout.Menu);

scout.MenuButtonAdapter.prototype._onMenuClicked = function(event) {
  if (this.$container.isEnabled()) {
    this._button.doAction($(event.target));
  }
};

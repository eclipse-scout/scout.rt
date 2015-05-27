/**
 * This Menu-class is an Adapter for a Button. It's used to display a button
 * in a menu-bar. It has the same ID as the original Button and the objectType
 * is 'Menu'.
 */
scout.MenuButtonAdapter = function() {
  scout.MenuButtonAdapter.parent.call(this);

  this.defaultMenu = false;
  this.menuStyle = 'default';
};
scout.inherits(scout.MenuButtonAdapter, scout.Menu);

// FIXME AWE: (menu) prüfen, ob wir das hier über objectFactories erzeugen können.

/**
 * @override Menu.js
 */
scout.MenuButtonAdapter.prototype.init = function(button) {
  if (!button) {
    throw new Error('Property "button" is not set');
  }

  var model = {
      id: button.id,
      objectType: 'Menu'
  };
  scout.MenuButtonAdapter.parent.prototype.init.call(this, model, button.session);

  this._button = button;
  this.enabled = button.enabled;
  this.visible = button.visible;
  this.selected = button.selected;
  this.text = button.label;
  this.keyStrokes = button.keyStrokes;

  if (button.systemType === scout.Button.SYSTEM_TYPE.OK ||
      button.systemType === scout.Button.SYSTEM_TYPE.SAVE_WITHOUT_MARKER_CHANGE) {
    this.defaultMenu = true;
  }
  if (button.displayStyle !== scout.Button.DISPLAY_STYLE.LINK) {
    this.menuStyle = 'button';
  }
};

/**
 * @override Menu.js
 */
scout.MenuButtonAdapter.prototype._onMenuClicked = function(event) {
  if (this.$container.isEnabled()) {
    this._button.doAction($(event.target));
  }
};

/**
 * @override Menu.js
 */
scout.Menu.prototype._renderText = function(text) {
   text = text ? scout.strings.removeAmpersand(text) : '';
   scout.Menu.parent.prototype._renderText.call(this, text);
};

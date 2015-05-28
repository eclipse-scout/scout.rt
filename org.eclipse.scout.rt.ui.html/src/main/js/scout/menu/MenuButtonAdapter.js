/**
 * This Menu-class is an Adapter for a Button. It's used to display a button
 * in a menu-bar. It has the same ID as the original Button and the objectType
 * is 'Menu'.
 */
scout.MenuButtonAdapter = function() {
  scout.MenuButtonAdapter.parent.call(this);

  this.defaultMenu = false;
  this.actionStyle = 'default';
};
scout.inherits(scout.MenuButtonAdapter, scout.Menu);

/**
 * @override Menu.js
 */
scout.MenuButtonAdapter.prototype.init = function(button) {
  if (!button) {
    throw new Error('Property "button" is not set');
  }

  var model = {
      id: button.id,
      objectType: 'Menu',
      keyStroke: button.keyStroke
  };
  scout.MenuButtonAdapter.parent.prototype.init.call(this, model, button.session);

  // FIXME AWE: (menu) MenuButtonAdapter verbessern:
  // - aktuell verdrängt der MenuButtonAdapter den Button (gleiche ID, siehe init())
  //   das funktioniert ganz gut, aber korrekter wäre es, wenn wir zwei
  //   IDs und Instanzen hätten und die Synchronisation via Listener
  //   funktionieren würde. Aktuell funktioniert nämlich das Setzen vom
  //   Label nicht, da die Property beim Button "label" und beim Menu "text"
  //   heisst. Den MenuButtonAdapter würden wir dann eher mit session.createUiObject
  //   erzeugen. Es braucht einen Sync für adapter.destroy()
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
    this.actionStyle = 'button';
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
scout.MenuButtonAdapter.prototype._render = function($parent) {
  scout.MenuButtonAdapter.parent.prototype._render.call(this, $parent);
  this._registerButtonKeyStroke();
};

/**
 * @override Menu.js
 */
scout.MenuButtonAdapter.prototype._renderText = function(text) {
   text = text ? scout.strings.removeAmpersand(text) : '';
   scout.Menu.parent.prototype._renderText.call(this, text);
};

scout.MenuButtonAdapter.prototype._syncLabel = function(label) {
  this.text = label;
};

scout.MenuButtonAdapter.prototype._renderLabel = function() {
  this._renderText(this.text);
};

scout.MenuButtonAdapter.prototype._renderGridData = function() {
  // NOP - since in a menu-bar we don't work with grid-data
};

scout.MenuButtonAdapter.prototype._registerButtonKeyStroke = function() {
  //register buttons key stroke on root Groupbox
  this._unregisterButtonKeyStroke();
  if (this.keyStroke) {
    this._button.getForm().rootGroupBox.keyStrokeAdapter.registerKeyStroke(this);
  }
};

scout.MenuButtonAdapter.prototype._unregisterButtonKeyStroke = function() {
  //unregister buttons key stroke on root Groupbox
    this._button.getForm().rootGroupBox.keyStrokeAdapter.unregisterKeyStroke(this);
};
/**
 * @override Action.js
 */
scout.MenuButtonAdapter.prototype._remove = function() {
  scout.MenuButtonAdapter.parent.prototype._remove.call(this);
  this._unregisterButtonKeyStroke();
};


/**
 * @override Action.js
 */
scout.MenuButtonAdapter.prototype._syncKeyStroke = function(keyStroke) {
  scout.MenuButtonAdapter.parent.prototype._syncKeyStroke.call(this, keyStroke);
  this._registerButtonKeyStroke();
};

/**
 * @Override scout.KeyStroke
 */
scout.MenuButtonAdapter.prototype.handle = function(event) {
  if (this._button.enabled && this._button.visible) {
    this._button.doAction($(event.target));
    if (this.preventDefaultOnEvent) {
      event.preventDefault();
    }
  }
};



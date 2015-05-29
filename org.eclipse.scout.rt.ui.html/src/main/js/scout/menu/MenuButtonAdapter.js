/**
 * This Menu-class is an Adapter for a Button. It's used to display a button
 * in a menu-bar. It has the same ID as the original Button and the objectType
 * is 'Menu'.
 */
scout.MenuButtonAdapter = function() {
  scout.MenuButtonAdapter.parent.call(this);

  this.defaultMenu = false;
  this.actionStyle = scout.Action.ActionStyle.DEFAULT;
};
scout.inherits(scout.MenuButtonAdapter, scout.Menu);

// FIXME AWE: (menu) MenuButtonAdapter verbessern:
// - aktuell verdrängt der MenuButtonAdapter den Button (gleiche ID, siehe init())
//   das funktioniert ganz gut, aber korrekter wäre es, wenn wir zwei
//   IDs und Instanzen hätten und die Synchronisation via Listener
//   funktionieren würde. Aktuell funktioniert nämlich das Setzen vom
//   Label nicht, da die Property beim Button "label" und beim Menu "text"
//   heisst. Den MenuButtonAdapter würden wir dann eher mit session.createUiObject
//   erzeugen. Es braucht einen Sync für adapter.destroy()

/**
 * @override Menu.js
 */
scout.MenuButtonAdapter.prototype.init = function(button) {
  if (!button) {
    throw new Error('missing parameter "button"');
  }
  var model = {
      objectType: 'Menu',
      id: button.id,
      enabled: button.enabled,
      visible: button.visible,
      selected: button.selected,
      text: button.label,
      keyStroke: button.keyStroke,
      keyStrokes: button.keyStrokes,
      modelClass: button.modelClass
  };
  scout.MenuButtonAdapter.parent.prototype.init.call(this, model, button.session);
  this._button = button;

  if (button.systemType === scout.Button.SYSTEM_TYPE.OK ||
      button.systemType === scout.Button.SYSTEM_TYPE.SAVE_WITHOUT_MARKER_CHANGE) {
    this.defaultMenu = true;
  }

  if (button.displayStyle === scout.Button.DISPLAY_STYLE.TOGGLE) {
    this.actionStyle = scout.Action.ActionStyle.TOGGLE;
  }
  else if (button.displayStyle === scout.Button.DISPLAY_STYLE.DEFAULT) {
    this.actionStyle = scout.Action.ActionStyle.BUTTON;
  }
  else {
    this.actionStyle = scout.Action.ActionStyle.DEFAULT;
  }
};

/**
 * @override Menu.js
 */
scout.MenuButtonAdapter.prototype._onClick = function() {
  if (this.enabled) {
    this._button.doAction();
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
  this._unregisterButtonKeyStroke();
  if (this.keyStroke) {
    this._button.registerRootKeyStroke(this);
  }
};

scout.MenuButtonAdapter.prototype._unregisterButtonKeyStroke = function() {
  this._button.unregisterRootKeyStroke(this);
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
 * @override scout.KeyStroke
 */
scout.MenuButtonAdapter.prototype.handle = function(event) {
  if (this.enabled && this.visible) {
    this._button.doAction();
    if (this.preventDefaultOnEvent) {
      event.preventDefault();
    }
  }
};

/* FIXME AWE Unterschied:
 * - Action sends doAction when clicked
 * - Button sends clicked  when clicked
 */

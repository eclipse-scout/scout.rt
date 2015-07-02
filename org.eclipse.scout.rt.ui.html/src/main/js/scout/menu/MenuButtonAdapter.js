/**
 * This Menu-class is an Adapter for a Button. It's used to display a button
 * in a menu-bar. It has the same ID as the original Button and the objectType
 * is 'Menu'.
 */
scout.MenuButtonAdapter = function() {
  scout.MenuButtonAdapter.parent.call(this);

  this.actionStyle = scout.Action.ActionStyle.DEFAULT;

  // FIXME AWE: Da der Button von AbstractFormField erbt, MenuButtonAdapter.js aber nicht,
  // kann es sein, dass Property Change Events fuer Properties ankommen, welche es auf
  // MenuButtonAdapter nicht gibt (z.B. mandatory). Es gibt dann einen Fehler in
  // ModelAdapter._renderPropertiesOnPropertyChange(). Darum kopieren wir hier als
  // _Workaround_ (!) einfach alle _render* Methoden von FormField.js und stellen
  // eine leere dummy Methode bereit, damit diese Fehler verhindert werden.
  var  emptyDummyFunction = function() {};
  for (var prop in scout.FormField.prototype) {
    if (/^_render.+/.test(prop) && this[prop] === undefined) {
      this[prop] = emptyDummyFunction.bind(this);
    }
  }
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
      modelClass: button.modelClass,
      horizontalAlignment: (button.gridData ? button.gridData.horizontalAlignment : null)
  };
  scout.MenuButtonAdapter.parent.prototype.init.call(this, model, button.session);
  button._renderSelected = this._renderSelected.bind(this);
  this._button = button;

  if (button.displayStyle === scout.Button.DisplayStyle.TOGGLE) {
    this.actionStyle = scout.Action.ActionStyle.TOGGLE;
  }
  else if (button.displayStyle === scout.Button.DisplayStyle.DEFAULT) {
    this.actionStyle = scout.Action.ActionStyle.BUTTON;
  }
  else {
    this.actionStyle = scout.Action.ActionStyle.DEFAULT;
  }
};

/**
 * @override Menu.js
 */
scout.MenuButtonAdapter.prototype._onMouseEvent = function(event) {
  if( event.type === 'click'){
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


/**
 * @override Menu.js
 */
scout.MenuButtonAdapter.prototype.doAction = function(text) {
   this._button.doAction();
};

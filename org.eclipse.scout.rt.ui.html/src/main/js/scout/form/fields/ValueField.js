/**
 * ValueField assumes $field has a .val() method which returns the value of that field.
 * @abstract
 */
scout.ValueField = function() {
  scout.ValueField.parent.call(this);
  this._keyUpListener;
  this._addAdapterProperties('menus');
};
scout.inherits(scout.ValueField, scout.FormField);

scout.ValueField.prototype._renderProperties = function() {
  scout.ValueField.parent.prototype._renderProperties.call(this);
  this._renderDisplayText(this.displayText);
  this._renderMenus(this.menus);
};

scout.ValueField.prototype.init = function(model, session) {
  scout.ValueField.parent.prototype.init.call(this, model, session);
  this._syncMenus(this.menus);
};

scout.ValueField.prototype._syncMenus = function(menus) {
  if (this._hasMenus()) {
    this.menus.forEach(function(menu) {
      this.keyStrokeAdapter.unregisterKeyStroke(menu);
    }, this);
  }
  this.menus = menus;
  if (this._hasMenus()) {
    this.menus.forEach(function(menu) {
      if (menu.enabled) {
        this.keyStrokeAdapter.registerKeyStroke(menu);
      }
    }, this);
  }
};

/**
 * @override FormField.js
 */
scout.ValueField.prototype._onStatusClick = function(event) {
  if (this._hasMenus()) {
    // showing menus is more important than showing tooltips
    var popup = new scout.ContextMenuPopup(this.session, {
      menuItems: this.menus,
      cloneMenuItems: false,
      $anchor: this.$status
    });
    popup.render();
  } else {
    // super call shows tooltip
    scout.ValueField.parent.prototype._onStatusClick.call(this);
  }
};

scout.ValueField.prototype._hasMenus = function() {
  return !!(this.menus && this.menus.length > 0);
};

scout.ValueField.prototype._renderMenus = function() {
  this._updateMenus();
};

scout.ValueField.prototype._renderMenusVisible = function() {
  this._updateMenus();
};

scout.ValueField.prototype._updateMenus = function() {
  this.$container.toggleClass('has-menus', this._hasMenus() && this.menusVisible);
};

scout.ValueField.prototype._renderDisplayText = function(displayText) {
  this.$field.val(displayText);
};

scout.ValueField.prototype._readDisplayText = function() {
  return this.$field.val();
};

/**
 * "Update display-text on modify" does not really belong to ValueField, but is available here
 * as a convenience for all subclasses that want to support it.
 */
scout.ValueField.prototype._renderUpdateDisplayTextOnModify = function() {
  if (this.updateDisplayTextOnModify) {
    this._keyUpListener = this._onFieldKeyUp.bind(this);
    this.$field.on('keyup', this._keyUpListener);
  } else {
    this.$field.off('keyup', this._keyUpListener);
  }
};

scout.ValueField.prototype._onFieldBlur = function() {
  this.displayTextChanged(false);
};

// FIXME AWE: (naming) in JavaStyleGuide ergänzen:
// - wenn als event handler registriert $field.on('click', this._onClick.bind(this));
// - Wenn event vom server kommt, z.B. selection _onSelection(event)
// - Wenn Wert an Server gesendet werden soll displayTextChanged();
//   wird typischerweise auch im _onChange oder _onKeyUp aufgerufen.
//   ruft typischerweise auch sendDisplayText(displayText) auf

scout.ValueField.prototype.displayTextChanged = function(whileTyping) {
  whileTyping = !!whileTyping; // cast to boolean
  var displayText = scout.helpers.nvl(this._readDisplayText(), ''),
    oldDisplayText = scout.helpers.nvl(this.displayText, '');

  // send only if displayText has really changed OR if updateDisplayTextOnModify is true
  // 2. check is necessary to make sure the value and not only the display text gets written to the model (IBasicFieldUIFacade.parseAndSetValueFromUI vs setDisplayTextFromUI)
  if (displayText !== oldDisplayText || (this.updateDisplayTextOnModify && !whileTyping)) {
    this.displayText = displayText;
    this._sendDisplayTextChanged(displayText, whileTyping);
  }
};

scout.ValueField.prototype._onFieldKeyUp = function() {
  this.displayTextChanged(true);
};

scout.ValueField.prototype._sendDisplayTextChanged = function(displayText, whileTyping) {
  // In 'updateDisplayTextOnModify' mode, each change of text is sent to the server with whileTyping=true.
  // On field blur, the text is sent again with whileTyping=false. The following logic prevents sending
  // to many events to the server. When whileTyping is false, the text has only to be send to the server
  // when there have been any whileTyping=true events. When the field looses the focus without any
  // changes, no request should be sent.
  if (this.updateDisplayTextOnModify) {
    if (whileTyping) {
      // Remember that we sent some events to the server with "whileTyping=true".
      this._displayTextChangedWhileTyping = true;
    }
    else {
      if (!this._displayTextChangedWhileTyping) {
        // If there were no "whileTyping=true" events, don't send anything to the server.
        return;
      }
      this._displayTextChangedWhileTyping = false; // Reset
    }
  }

  this.session.send(this.id, 'displayTextChanged', {
    displayText: displayText,
    whileTyping: whileTyping
  });
};

scout.ValueField.prototype.addField = function($field) {
  scout.ValueField.parent.prototype.addField.call(this, $field);
  this.$field.data('valuefield', this);
};

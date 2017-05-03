scout.SmartField2 = function() {
  scout.SmartField2.parent.call(this);

  this.popup;
  this.lookupCall;
  this.codeType;
};
scout.inherits(scout.SmartField2, scout.ValueField);

/**
 * @override
 */
scout.SmartField2.prototype._init = function(model) {
  scout.SmartField2.parent.prototype._init.call(this, model);
  this._setLookupCall(this.lookupCall);
  this._setCodeType(this.codeType);
};

scout.SmartField2.prototype._initKeyStrokeContext = function() {
  scout.SmartField2.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2CancelKeyStroke(this));
  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2ToggleKeyStroke(this));
};

scout.SmartField2.prototype._render = function($parent) {
  this.addContainer($parent, 'dropdown-field');
  this.addLabel();

  var $field = scout.fields.makeInputDiv(this.$container)
    .on('mousedown', this._onFieldMousedown.bind(this))
    .on('blur', this._onFieldBlur.bind(this))
    .on('keydown', this._onFieldKeydown.bind(this));

  this.addField($field);
  this.addMandatoryIndicator();
  this.addIcon();
  this.addStatus();
};

/**
 * @override
 */
scout.SmartField2.prototype._renderDisplayText = function() {
  this.$field.text(this.displayText);
};

/**
 * Accepts the selected lookup row and sets its id as value.
 * This function is called on blur, by a keystroke or programmatically at any time.
 *
 * @override
 */
scout.SmartField2.prototype.acceptInput = function(whileTyping) {
  if (!this.popup) {
    return;
  }

  var lookupRow = this.popup.getSelectedLookupRow();
  if (lookupRow) {
    this.setValue(lookupRow.key);
  }
  this.closePopup();
};

/**
 * @override
 */
scout.SmartField2.prototype._renderEnabled = function() {
  scout.SmartField2.parent.prototype._renderEnabled.call(this);

  this.$field.setTabbable(this.enabledComputed);
};

scout.SmartField2.prototype._setLookupCall = function(lookupCall) {
  if (typeof lookupCall === 'string') {
    lookupCall = scout.create(lookupCall, {
      session: this.session
    });
  }
  this._setProperty('lookupCall', lookupCall);
};

scout.SmartField2.prototype._setCodeType = function(codeType) {
  this._setProperty('codeType', codeType);
  if (!codeType) {
    return;
  }
  var lookupCall = scout.create('CodeLookupCall', {
    session: this.session,
    codeType: codeType
  });
  this.setProperty('lookupCall', lookupCall);
};

/**
 * @override
 */
scout.SmartField2.prototype._formatValue = function(value) {
  if (!this.lookupCall) {
    return scout.strings.nvl(value) + '';
  }
  return this.lookupCall.textById(value);
};

scout.SmartField2.prototype.openPopup = function() {
  if (this.popup) {
    // already open
    return;
  }

  this.lookupCall.getAll().done(function(result) {
    this.$container.addClass('popup-open');
    // On touch devices the field does not get the focus.
    // But it should look focused when the popup is open.
    this.$field.addClass('focused');
    this.popup = this._createPopup();
    this.popup.setLookupRows(result.lookupRows);
    this.popup.open();
    this.popup.on('select', this._onItemSelect.bind(this));
    this.popup.on('remove', function() {
      this.popup = null;
      if (this.rendered) {
        this.$container.removeClass('popup-open');
        this.$field.removeClass('focused');
      }
    }.bind(this));
  }.bind(this));
};

scout.SmartField2.prototype.closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

scout.SmartField2.prototype.togglePopup = function() {
  if (this.popup) {
    this.closePopup();
  } else {
    this.openPopup();
  }
};

scout.SmartField2.prototype._createPopup = function() {
  return scout.create('SmartField2Popup', {
    parent: this,
    $anchor: this.$field,
    boundToAnchor: true,
    closeOnAnchorMousedown: false,
    field: this
  });
};

/**
 * Calls acceptInput if mouse down happens outside of the field or popup
 * @override
 */
scout.SmartField2.prototype.aboutToBlurByMouseDown = function(target) {
  var eventOnField = this.$field.isOrHas(target);
  var eventOnPopup = this.popup && this.popup.$container.isOrHas(target);
  if (!eventOnField && !eventOnPopup) {
    this.acceptInput(); // event outside this value field
  }
};

scout.SmartField2.prototype._onFieldMousedown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.togglePopup();
};

scout.SmartField2.prototype._onIconMousedown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.$field.focus();
  this.togglePopup();
};


scout.SmartField2.prototype._onFieldKeydown = function(event) {
 if (this._isNavigationKey(event)) {
   if (this.popup) {
     this.popup.delegateKeyEvent(event);
   } else {
     this.openPopup();
   }
 }
};

scout.SmartField2.prototype._isNavigationKey = function(e) {
  return e.which === scout.keys.PAGE_UP ||
    e.which === scout.keys.PAGE_DOWN ||
    e.which === scout.keys.UP ||
    e.which === scout.keys.DOWN ||
    e.which === scout.keys.HOME ||
    e.which === scout.keys.END;
};

scout.SmartField2.prototype._onItemSelect = function(event) {
  this.setValue(event.lookupRow.key);
  this.closePopup();
};

/**
 * When you already have a complete lookup-row you can use this method
 * instead of #setValue(value). This avoids that the smart-field must perform
 * a lookup to resolve the display text for the value, because the text is
 * already available on the lookup-row.
 */
scout.SmartField2.prototype.setLookupRow = function(lookupRow) {
  this.value = lookupRow.key;
  this.setDisplayText(lookupRow.text);
  // FIXME [awe] 7.0 - SF2: set other properties, see applyLazyStyles in Java
};

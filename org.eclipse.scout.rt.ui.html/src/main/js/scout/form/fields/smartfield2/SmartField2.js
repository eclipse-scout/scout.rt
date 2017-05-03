scout.SmartField2 = function() {
  scout.SmartField2.parent.call(this);

  this.popup = null;
  this.lookupCall = null;
  this.codeType = null;
  this._pendingLookup = null;

  this.variant = scout.SmartField2.Variant.DROPDOWN;
};
scout.inherits(scout.SmartField2, scout.ValueField);

// FIXME [awe] 7.0 - SF2: überlegen ob wir das mit flags, mit subklassen oder mit strategies lösen wollen
// zuerst mal flag ansatz ausprobieren und je nach code die eine oder andere methode anwenden.
scout.SmartField2.Variant = {
  DEFAULT: 'smart',
  PROPOSAL: 'proposal',
  DROPDOWN: 'dropdown'
};

scout.SmartField2.DEBOUNCE_DELAY = 200;

/**
 * @override
 */
scout.SmartField2.prototype._init = function(model) {
  scout.SmartField2.parent.prototype._init.call(this, model);
  this._setLookupCall(this.lookupCall);
  this._setCodeType(this.codeType);
};

/**
 * @override Widget.js
 */
scout.SmartField2.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.SmartField2.prototype._initKeyStrokeContext = function() {
  scout.SmartField2.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2CancelKeyStroke(this));
  this.keyStrokeContext.registerKeyStroke(new scout.SmartField2ToggleKeyStroke(this));
};

scout.SmartField2.prototype._render = function($parent) {
  var cssClass = this.variant + '-field';
  this.addContainer($parent, cssClass, new scout.SmartFieldLayout(this));
  this.addLabel();

  var $field = scout.fields.makeInputOrDiv(this)
    .on('mousedown', this._onFieldMousedown.bind(this));
  if (!this.touch) {
    $field
      .blur(this._onFieldBlur.bind(this))
      .focus(this._onFieldFocus.bind(this))
      .keyup(this._onFieldKeyup.bind(this))
      .keydown(this._onFieldKeydown.bind(this));
  }
  this.addField($field);

  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon();
  this.addStatus();
};

/**
 * @override
 */
scout.SmartField2.prototype._renderDisplayText = function() {
  scout.fields.valOrText(this, this.$field, this.displayText);
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
    this.popup.on('select', this._onLookupRowSelect.bind(this));
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

scout.SmartField2.prototype._onFieldFocus = function(event) {
  // FIXME [awe] 7.0 - SF2: im original wird hier mit dem displayText was komisches gemacht. Ich hoffe das ist nicht mehr nötig
};

scout.SmartField2.prototype._onFieldKeyup = function(event) {
  // Escape
  if (event.which === scout.keys.ESCAPE) {
    event.stopPropagation();
    return;
  }

  // Enter
  if (event.which === scout.keys.ENTER) {
    event.stopPropagation();
    return;
  }

  // Pop-ups shouldn't open when one of the following keys is pressed
  var w = event.which;
  if (
    event.ctrlKey || event.altKey ||
    w === scout.keys.TAB ||
    w === scout.keys.SHIFT ||
    w === scout.keys.CTRL ||
    w === scout.keys.HOME ||
    w === scout.keys.END ||
    w === scout.keys.LEFT ||
    w === scout.keys.RIGHT ||
    this._isNavigationKey(event) ||
    this._isFunctionKey(event)) {
    return;
  }

  // The typed character is not available until the keyUp event happens
  // That's why we must deal with that event here (and not in keyDown)
  // We don't use _displayText() here because we always want the text the
  // user has typed.
  if (this.popup) {
    this._proposalTyped();
  } else {
    this.openPopup();
  }
};

/**
 * @override Widget.js
 */
scout.SmartField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
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

scout.SmartField2.prototype._isNavigationKey = function(event) {
  var w = event.which;
  return w === scout.keys.PAGE_UP ||
    w === scout.keys.PAGE_DOWN ||
    w === scout.keys.UP ||
    w === scout.keys.DOWN ||
    w === scout.keys.HOME ||
    w === scout.keys.END;
};

scout.SmartField2.prototype._isFunctionKey = function(e) {
  return e.which >= scout.keys.F1 && e.which < scout.keys.F12;
};


scout.SmartField2.prototype._proposalTyped = function() {
  var displayText = this._readDisplayText();
  $.log.trace('(SmartField2#_proposalTyped) displayText=' + displayText);

  // debounce lookup
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
  }

  this._pendingLookup = setTimeout(function() {
    $.log.debug('(SmartField2#_proposalTyped) send displayText=' + displayText);
    this.lookupCall.getByText(displayText).done(function(result) {
      if (this.popup) {
        this.popup.setLookupRows(result.lookupRows);
      }
    }.bind(this));
  }.bind(this), scout.SmartField2.DEBOUNCE_DELAY);
};

scout.SmartField2.prototype._onLookupRowSelect = function(event) {
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

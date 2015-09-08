scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.FORMAT = {
  LOWER: 'a' /* IStringField.FORMAT_LOWER */ ,
  UPPER: 'A' /* IStringField.FORMAT_UPPER */
};

/**
 * @override ModelAdapter.js
 */
scout.StringField.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.StringField.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke([
    new scout.StringFieldEnterKeyStroke(this),
    new scout.StringFieldCtrlEnterKeyStroke(this)
  ]);
};

/**
 * @override Widget.js
 */
scout.StringField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.StringField.prototype._render = function($parent) {
  this.addContainer($parent, 'string-field');
  this.addLabel();
  this.addMandatoryIndicator();

  var $field;
  if (this.multilineText) {
    $field = $('<textarea>').
    on('DOMMouseScroll mousewheel', function(event) {
      // otherwise scout.Scrollbar.prototype would handle this event for scrollable group boxes and prevent scrolling on textarea
      event.stopPropagation();
    });
  } else {
    $field = scout.fields.new$TextField();
  }
  $field.on('blur', this._onFieldBlur.bind(this))
    .on('select', this._onSelect.bind(this));


  // add drag and drop support
  this.dragAndDropHandler = scout.dragAndDrop.handler(this,
    scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    function() {
      return this.dropType;
    }.bind(this),
    function() {
      return this.dropMaximumSize;
    }.bind(this));
  this.dragAndDropHandler.install($field);

  this.addField($field);
  this.addStatus();
};

/**
 * @override FormField.js
 */
scout.StringField.prototype._renderEnabled = function() {
  scout.StringField.parent.prototype._renderEnabled.call(this);
  this._renderDisabledOverlay();
};

/**
 * Add or remove an overlay DIV for browsers that don't support copy from disabled text-fields.
 * The overlay provides a custom 'copy' menu which opens the ClipboardForm.
 */
scout.StringField.prototype._renderDisabledOverlay = function() {
  if (scout.device.supportsCopyFromDisabledInputFields()) {
    return;
  }

  if (this.enabled) {
    if (this._$disabledOverlay) {
      this._$disabledOverlay.remove();
      this._$disabledOverlay = null;
    }
  } else if (!this._$disabledOverlay) {
    this._$disabledOverlay = $.makeDiv('disabled-overlay')
      .on('contextmenu', this._createCopyContextMenu.bind(this))
      .appendTo(this.$container);
  }
};

scout.StringField.prototype._createCopyContextMenu = function(event) {
  if (!this.visible) {
    return;
  }

  var fieldId = this.id;
  var menu = scout.localObjects.createObject(this.session, {
    objectType: 'Menu',
    text: this.session.text('ui.copy')
  });
  menu.remoteHandler = function(target, type) {
    if ('doAction' === type) {
      this.session.send(fieldId, 'exportToClipboard');
    }
  };

  var popup = new scout.ContextMenuPopup(this.session, {
    menuItems: [menu],
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    },
    $anchor: this._$disabledOverlay
  });
  popup.render();
};

scout.StringField.prototype._renderProperties = function() {
  scout.StringField.parent.prototype._renderProperties.call(this);

  this._renderUpdateDisplayTextOnModify();
  this._renderInputMasked(this.inputMasked);
  this._renderWrapText(this.wrapText);
  this._renderFormat(this.format);
  this._renderSpellCheckEnabled(this.spellCheckEnabled);
  this._renderHasAction(this.hasAction);
  this._renderSelectionStart(this.selectionStart);
  this._renderSelectionEnd(this.selectionEnd);
  this._renderMaxLength();
  // no render operation necessary: this._renderSelectionTrackingEnabled(...);
};

scout.StringField.prototype._renderMaxLength = function(maxLength0) {
  var maxLength = maxLength0 || this.maxLength;
  if (this.$field[0].maxLength) {
    this.$field[0].maxLength = maxLength;
  } else {
    this.$field.on("keyup paste", function(e) {
      setTimeout(function() {
        var currLength = this.$field.val().length;

        if (currLength > this.maxLength) {
          this.$field.val(this.$field.val().slice(0, this.maxLength));
        }
      }.bind(this), 0);
    }.bind(this));
  }
};

scout.StringField.prototype._renderSelectionStart = function(selectionStart) {
  this.$field[0].selectionStart = selectionStart;
};

scout.StringField.prototype._renderSelectionEnd = function(selectionEnd) {
  this.$field[0].selectionEnd = selectionEnd;
};

scout.StringField.prototype._renderInputMasked = function(inputMasked) {
  if (this.multilineText) {
    return;
  }
  this.$field.attr('type', (inputMasked ? 'password' : 'text'));
};

scout.StringField.prototype._renderHasAction = function(decorationLink) {
  if (decorationLink) {
    this.$container.addClass("has-action");
    this.addIcon();
    this.revalidateLayout();
  } else {
    if (this.$icon) {
      this.$icon.remove();
      this.$container.removeClass("has-action");
    }
  }
};

scout.StringField.prototype._renderFormat = function(fmt) {
  if (fmt === scout.StringField.FORMAT.LOWER) {
    this.$field.css('text-transform', 'lowercase');
  } else if (fmt === scout.StringField.FORMAT.UPPER) {
    this.$field.css('text-transform', 'uppercase');
  }
};

scout.StringField.prototype._renderSpellCheckEnabled = function(spellCheckEnabled) {
  if (spellCheckEnabled) {
    this.$field.attr('spellcheck', 'true');
  } else {
    this.$field.attr('spellcheck', 'false');
  }
};

//Not called in _renderProperties() because this is not really a property (more like an event)
scout.StringField.prototype._renderInsertText = function() {
  var s = this.insertText;
  if (s && this.$field.length > 0) {
    var elem = this.$field[0];
    var a = 0;
    var b = 0;
    if (elem.selectionStart !== undefined && elem.selectionEnd !== undefined) {
      a = elem.selectionStart;
      b = elem.selectionEnd;
    }
    var text = elem.value;
    text = text.slice(0, a) + s + text.slice(b);
    elem.value = text;
  }
};

scout.StringField.prototype._renderWrapText = function() {
  this.$field.attr('wrap', this.wrapText ? 'soft' : 'off');
};

scout.StringField.prototype._renderGridData = function() {
  this.updateInnerAlignment({
    useHorizontalAlignment: (this.multilineText ? false : true)
  });
};

scout.StringField.prototype._onIconClick = function(event) {
  this.acceptInput();
  scout.StringField.parent.prototype._onIconClick.call(this, event);
  this.remoteHandler(this.id, 'callAction');
};

scout.StringField.prototype._onSelect = function(event) {
  if (this.selectionTrackingEnabled) {
    this._sendSelectionChanged();
  }
};

scout.StringField.prototype._sendSelectionChanged = function() {
  var event = new scout.Event(this.id, 'selectionChanged', {
    selectionStart: this.$field[0].selectionStart,
    selectionEnd: this.$field[0].selectionEnd
  });

  // Only send the latest selection changed event for a field
  event.coalesce = function(previous) {
    return this.id === previous.id && this.type === previous.type;
  };

  // send delayed to avoid a lot of requests while selecting
  this.session.sendEvent(event, 500);
};

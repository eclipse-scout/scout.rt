/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.BasicField);

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

scout.StringField.prototype._renderProperties = function() {
  scout.StringField.parent.prototype._renderProperties.call(this);

  this._renderInputMasked(this.inputMasked);
  this._renderWrapText(this.wrapText);
  this._renderFormat(this.format);
  this._renderSpellCheckEnabled(this.spellCheckEnabled);
  this._renderHasAction(this.hasAction);
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

// Not called in _renderProperties() because this is not really a property (more like an event)
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

    // Make sure display text gets sent (necessary if field does not have the focus)
    if (this.updateDisplayTextOnModify) {
      // If flag is true, we need to send two events (First while typing=true, second = false)
      this.acceptInput(true);
    }
    this.acceptInput();
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
  this._send('callAction');
};

scout.StringField.prototype._onSelect = function(event) {
  if (this.selectionTrackingEnabled) {
    this._sendSelectionChanged();
  }
};

scout.StringField.prototype._sendSelectionChanged = function() {
  var eventData = {
    selectionStart: this.$field[0].selectionStart,
    selectionEnd: this.$field[0].selectionEnd
  };

  // send delayed to avoid a lot of requests while selecting
  // coalesce: only send the latest selection changed event for a field
  this._send('selectionChanged', eventData, 500, function(previous) {
    return this.id === previous.id && this.type === previous.type;
  });
};

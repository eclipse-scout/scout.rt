/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.StringField = function() {
  scout.StringField.parent.call(this);

  this.format;
  this.hasAction = false;
  this.inputMasked = false;
  this.inputObfuscated = false;
  this.maxLength = 4000;
  this.multilineText = false;
  this.selectionStart = 0;
  this.selectionEnd = 0;
  this.selectionTrackingEnabled = false;
  this.spellCheckEnabled = false;
  this.trimText = true;
  this.wrapText = false;

  this._onSelectionChangingActionHandler = this._onSelectionChangingAction.bind(this);
};
scout.inherits(scout.StringField, scout.BasicField);

scout.StringField.Format = {
  LOWER: 'a' /* IStringField.FORMAT_LOWER */ ,
  UPPER: 'A' /* IStringField.FORMAT_UPPER */
};

scout.StringField.TRIM_REGEXP = new RegExp('^(\\s*)(.*?)(\\s*)$');

/**
 * Resolves the text key if value contains one.
 * This cannot be done in _init because the value field would call _setValue first
 */
scout.StringField.prototype._initValue = function(value) {
  value = scout.texts.resolveText(value, this.session.locale.languageTag);
  scout.StringField.parent.prototype._initValue.call(this, value);
};

/**
 * @override ModelAdapter.js
 */
scout.StringField.prototype._initKeyStrokeContext = function() {
  scout.StringField.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([
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

scout.StringField.prototype._render = function() {
  this.addContainer(this.$parent, 'string-field', new scout.StringFieldLayout(this));
  this.addLabel();
  this.addMandatoryIndicator();

  var $field;
  if (this.multilineText) {
    $field = this._makeMultilineField();
    this.$container.addClass('multiline');
  } else {
    $field = scout.fields.makeTextField(this.$parent);
  }
  $field.on('paste', this._onFieldPaste.bind(this));

  this.addField($field);
  this.addStatus();
};

scout.StringField.prototype._makeMultilineField = function() {
  var mouseDownHandler = function() {
    this.mouseClicked = true;
  }.bind(this);

  return this.$parent.makeElement('<textarea>')
    .on('DOMMouseScroll mousewheel', this._onMouseWheel.bind(this))
    .on('mousedown', mouseDownHandler)
    .on('focus', function(event) {
      this.$field.off('mousedown', mouseDownHandler);
      if (!this.mouseClicked) { // only trigger on tab focus in
        setTimeout(function() {
          if (!this.rendered || this.session.focusManager.isElementCovertByGlassPane(this.$field)) {
            return;
          }
          this._renderSelectionStart();
          this._renderSelectionEnd();
        }.bind(this));
      }
      this.mouseClicked = false;
    }.bind(this))
    .on('focusout', function() {
      this.$field.on('mousedown', mouseDownHandler);
    }.bind(this))
    .addDeviceClass();
};

scout.StringField.prototype._onFieldBlur = function() {
  scout.StringField.parent.prototype._onFieldBlur.call(this);
  if (this.multilineText) {
    this._updateSelection();
  }
  if (this.inputObfuscated) {
    // Restore obfuscated display text.
    this.$field.val(this.displayText);
  }
};

scout.StringField.prototype._onMouseWheel = function(event) {
  event = event.originalEvent || this.$container.window(true).event.originalEvent;
  var delta = event.wheelDelta ? -event.wheelDelta : event.detail;
  var scrollTop = this.$field[0].scrollTop;
  if (delta < 0 && scrollTop === 0) {
    // StringField is scrolled to the very top -> parent may scroll
    return;
  }
  var maxScrollTop = this.$field[0].scrollHeight - this.$field[0].clientHeight;
  if (delta > 0 && scrollTop >= maxScrollTop - 1) { // -1 because it can sometimes happen that scrollTop is maxScrollTop -1 or +1, just because clientHeight and scrollHeight are rounded values
    // StringField is scrolled to the very bottom -> parent may scroll
    this.$field[0].scrollTop = maxScrollTop; // Ensure it is really at the bottom (not -1px above)
    return;
  }
  // Don't allow others to scroll (e.g. scout.Scrollbar) while scrolling in the text area
  event.stopPropagation();
};

scout.StringField.prototype._renderProperties = function() {
  scout.StringField.parent.prototype._renderProperties.call(this);

  this._renderInputMasked();
  this._renderWrapText();
  this._renderFormat();
  this._renderSpellCheckEnabled();
  this._renderHasAction();
  this._renderMaxLength();
  this._renderSelectionTrackingEnabled();
  // Do not render selectionStart and selectionEnd here, because that would cause the focus to
  // be set to <textarea>s in IE. Instead, the selection is rendered when the focus has entered
  // the field, see _render(). #168648
  this._renderDropType();
};

/**
 * Adds a click handler instead of a mouse down handler because it executes an action.
 * @override
 */
scout.StringField.prototype.addIcon = function() {
  this.$icon = scout.fields.appendIcon(this.$container)
    .on('click', this._onIconClick.bind(this));
};

/**
 * override to ensure dropdown fields and touch mode smart fields does not have a clear icon.
 */
scout.StringField.prototype.isClearable = function() {
  return scout.StringField.parent.prototype.isClearable.call(this) && !this.multilineText;
};

scout.StringField.prototype.setMaxLength = function(maxLength) {
  this.setProperty('maxLength', maxLength);
};

scout.StringField.prototype._renderMaxLength = function() {
  // Check if "maxLength" attribute is supported by browser
  if (this.$field[0].maxLength) {
    this.$field.attr('maxlength', this.maxLength);
  } else {
    // Fallback for IE9
    this.$field.on('keyup paste', function(e) {
      setTimeout(truncate.bind(this), 0);
    }.bind(this));
  }

  // Make sure current text does not exceed max length
  truncate.call(this);
  if (!this.rendering) {
    this.parseAndSetValue(this._readDisplayText());
  }

  function truncate() {
    var text = this.$field.val();
    if (text.length > this.maxLength) {
      this.$field.val(text.slice(0, this.maxLength));
    }
  }
};

scout.StringField.prototype.setSelectionStart = function(selectionStart) {
  this.setProperty('selectionStart', selectionStart);
};

scout.StringField.prototype._renderSelectionStart = function() {
  if (scout.nvl(this.selectionStart, null) !== null) {
    this.$field[0].selectionStart = this.selectionStart;
  }
};

scout.StringField.prototype.setSelectionEnd = function(selectionEnd) {
  this.setProperty('selectionEnd', selectionEnd);
};

scout.StringField.prototype._renderSelectionEnd = function() {
  if (scout.nvl(this.selectionEnd, null) !== null) {
    this.$field[0].selectionEnd = this.selectionEnd;
  }
};

scout.StringField.prototype.setSelectionTrackingEnabled = function(selectionTrackingEnabled) {
  this.setProperty('selectionTrackingEnabled', selectionTrackingEnabled);
};

scout.StringField.prototype._renderSelectionTrackingEnabled = function() {
  this.$field
    .off('select', this._onSelectionChangingActionHandler)
    .off('mousedown', this._onSelectionChangingActionHandler)
    .off('keydown', this._onSelectionChangingActionHandler)
    .off('input', this._onSelectionChangingActionHandler);
  if (this.selectionTrackingEnabled) {
    this.$field.on('select', this._onSelectionChangingActionHandler)
      .on('mousedown', this._onSelectionChangingActionHandler)
      .on('keydown', this._onSelectionChangingActionHandler)
      .on('input', this._onSelectionChangingActionHandler);
  }
};

scout.StringField.prototype.setInputMasked = function(inputMasked) {
  this.setProperty('inputMasked', inputMasked);
};

scout.StringField.prototype._renderInputMasked = function() {
  if (this.multilineText) {
    return;
  }
  this.$field.attr('type', (this.inputMasked ? 'password' : 'text'));
};

scout.StringField.prototype._renderInputObfuscated = function() {
  if (this.inputObfuscated && this.focused) {
    // If a new display text is set (e.g. because value in model changed) and field is focused,
    // do not display new display text but clear content (as in _onFieldFocus).
    // Depending on order of property render, either this or _renderDisplayText is called first
    // (inputObfuscated flag might be still in the old state in _renderDisplayText).
    this.$field.val('');
  }
};

scout.StringField.prototype.setHasAction = function(hasAction) {
  this.setProperty('hasAction', hasAction);
};

scout.StringField.prototype._renderHasAction = function() {
  if (this.hasAction) {
    if (!this.$icon) {
      this.addIcon();
    }
    this.$container.addClass('has-icon');
  } else {
    this._removeIcon();
    this.$container.removeClass('has-icon');
  }
  this.revalidateLayout();
};

scout.StringField.prototype.setFormatUpper = function(formatUpper) {
  if (formatUpper) {
    this.setFormat(scout.StringField.Format.UPPER);
  } else {
    this.setFormat(null);
  }
};

scout.StringField.prototype.setFormatLower = function(formatLower) {
  if (formatLower) {
    this.setFormat(scout.StringField.Format.LOWER);
  } else {
    this.setFormat(null);
  }
};

scout.StringField.prototype.setFormat = function(format) {
  this.setProperty('format', format);
};

scout.StringField.prototype._renderFormat = function() {
  if (this.format === scout.StringField.Format.LOWER) {
    this.$field.css('text-transform', 'lowercase');
  } else if (this.format === scout.StringField.Format.UPPER) {
    this.$field.css('text-transform', 'uppercase');
  } else {
    this.$field.css('text-transform', '');
  }
};

scout.StringField.prototype.setSpellCheckEnabled = function(spellCheckEnabled) {
  this.setProperty('spellCheckEnabled', spellCheckEnabled);
};

scout.StringField.prototype._renderSpellCheckEnabled = function() {
  if (this.spellCheckEnabled) {
    this.$field.attr('spellcheck', 'true');
  } else {
    this.$field.attr('spellcheck', 'false');
  }
};

/**
 * @override
 */
scout.StringField.prototype._renderDisplayText = function() {
  if (this.inputObfuscated && this.focused) {
    // If a new display text is set (e.g. because value in model changed) and field is focused,
    // do not display new display text but clear content (as in _onFieldFocus).
    // Depending on order of property render, either this or _renderInputObfuscated is called first
    // (inputObfuscated flag might be still in the old state in this method).
    this.$field.val('');
    return;
  }

  var displayText = scout.strings.nvl(this.displayText);
  var oldDisplayText = scout.strings.nvl(this.$field.val());
  var oldSelection = this._getSelection();
  scout.StringField.parent.prototype._renderDisplayText.call(this);
  // Try to keep the current selection for cases where the old and new display
  // text only differ because of the automatic trimming.
  if (this.trimText && oldDisplayText !== displayText) {
    var matches = oldDisplayText.match(scout.StringField.TRIM_REGEXP);
    if (matches && matches[2] === displayText) {
      this._setSelection({
        start: Math.max(oldSelection.start - matches[1].length, 0),
        end: Math.min(oldSelection.end - matches[1].length, displayText.length)
      });
    }
  }
};

scout.StringField.prototype.insertText = function(text) {
  if (!this.rendered) {
    this._postRenderActions.push(this.insertText.bind(this, text));
    return;
  }
  this._insertText(text);
};

scout.StringField.prototype._insertText = function(textToInsert) {
  if (!textToInsert) {
    return;
  }

  // Prevent insert if new length would exceed maxLength to prevent unintended deletion of characters at the end of the string
  var selection = this._getSelection();
  var text = this._applyTextToSelection(this.$field.val(), textToInsert, selection);
  if (text.length > this.maxLength) {
    this._showNotification('ui.CannotInsertTextTooLong');
    return;
  }

  this.$field.val(text);
  this._setSelection(selection.start + textToInsert.length);

  // Make sure display text gets sent (necessary if field does not have the focus)
  if (this.updateDisplayTextOnModify) {
    // If flag is true, we need to send two events (First while typing=true, second = false)
    this.acceptInput(true);
  }
  this.acceptInput();
};

scout.StringField.prototype._applyTextToSelection = function(text, textToInsert, selection) {
  if (this.inputObfuscated) {
    // Use empty text when input is obfuscated, otherwise text will be added to obfuscated text
    text = '';
  }
  return text.slice(0, selection.start) + textToInsert + text.slice(selection.end);
};

scout.StringField.prototype.setWrapText = function(wrapText) {
  this.setProperty('wrapText', wrapText);
};

scout.StringField.prototype._renderWrapText = function() {
  this.$field.attr('wrap', this.wrapText ? 'soft' : 'off');
};

scout.StringField.prototype.setTrimText = function(trimText) {
  this.setProperty('trimText', trimText);
};

scout.StringField.prototype._renderTrimText = function() {
  // nop, property used in _validateDisplayText()
};

scout.StringField.prototype._renderGridData = function() {
  scout.StringField.parent.prototype._renderGridData.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: (this.multilineText ? false : true)
  });
};

scout.StringField.prototype._renderGridDataHints = function() {
  scout.StringField.parent.prototype._renderGridDataHints.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};

scout.StringField.prototype._onIconClick = function(event) {
  this.acceptInput();
  this.$field.focus();
  this.trigger('action');
};

scout.StringField.prototype._onSelectionChangingAction = function(event) {
  if (event.type === 'mousedown') {
    this.$field.window().one('mouseup.stringfield', function() {
      // For some reason, when clicking side an existing selection (which clears the selection), the old
      // selection is still visible. To get around this case, we use setTimeout to handle the new selection
      // after it really has been changed.
      setTimeout(this._updateSelection.bind(this));
    }.bind(this));
  } else if (event.type === 'keydown') {
    // Use set timeout to let the cursor move to the target position
    setTimeout(this._updateSelection.bind(this));
  } else {
    this._updateSelection();
  }
};

scout.StringField.prototype._getSelection = function() {
  var start = scout.nvl(this.$field[0].selectionStart, null);
  var end = scout.nvl(this.$field[0].selectionEnd, null);
  if (start === null || end === null) {
    start = 0;
    end = 0;
  }
  return {
    start: start,
    end: end
  };
};

scout.StringField.prototype._setSelection = function(selectionStart, selectionEnd) {
  if (typeof selectionStart === 'number') {
    selectionEnd = scout.nvl(selectionEnd, selectionStart);
  } else if (typeof selectionStart === 'object') {
    selectionEnd = selectionStart.end;
    selectionStart = selectionStart.start;
  }
  this.$field[0].selectionStart = selectionStart;
  this.$field[0].selectionEnd = selectionEnd;
  this._updateSelection();
};

scout.StringField.prototype._updateSelection = function() {
  var oldSelectionStart = this.selectionStart;
  var oldSelectionEnd = this.selectionEnd;
  this.selectionStart = this.$field[0].selectionStart;
  this.selectionEnd = this.$field[0].selectionEnd;
  if (this.selectionTrackingEnabled) {
    var selectionChanged = (this.selectionStart !== oldSelectionStart || this.selectionEnd !== oldSelectionEnd);
    if (selectionChanged) {
      this.triggerSelectionChange();
    }
  }
};

scout.StringField.prototype.triggerSelectionChange = function() {
  this.trigger('selectionChange', {
    selectionStart: this.selectionStart,
    selectionEnd: this.selectionEnd
  });
};

scout.StringField.prototype._validateValue = function(value) {
  if (scout.objects.isNullOrUndefined(value)) {
    return value;
  }
  value = scout.strings.asString(value);
  if (this.trimText) {
    value = value.trim();
  }
  return scout.StringField.parent.prototype._validateValue(value);
};

/**
 * @override ValueField.js
 */
scout.StringField.prototype._clear = function() {
  scout.StringField.parent.prototype._clear.call(this);

  // Disable obfuscation when user clicks on clear icon.
  this.inputObfuscated = false;
};

/**
 * @override ValueField.js
 */
scout.StringField.prototype._updateEmpty = function() {
  this.empty = scout.strings.empty(this.value);
};

/**
 * @override ValueField.js
 */
scout.StringField.prototype.acceptInput = function(whileTyping) {
  var displayText = scout.nvl(this._readDisplayText(), '');
  if (this.inputObfuscated && displayText !== '') {
    // Disable obfuscation if user has typed text (on focus, field will be cleared if obfuscated, so any typed text is new text).
    this.inputObfuscated = false;
  }

  scout.StringField.parent.prototype.acceptInput.call(this, whileTyping);
};

/**
 * @override BasicField.js
 */
scout.StringField.prototype._onFieldFocus = function(event) {
  scout.StringField.parent.prototype._onFieldFocus.call(this, event);

  if (this.inputObfuscated) {
    this.$field.val('');

    // Without properly setting selection start and end, cursor is not visible in IE and Firefox.
    setTimeout(function() {
      if (!this.rendered) {
        return;
      }
      var $field = this.$field[0];
      $field.selectionStart = 0;
      $field.selectionEnd = 0;
    }.bind(this));
  }
};

/**
 * Get clipboard data, different strategies for browsers.
 * Must use a callback because this is required by Chrome's clipboard API.
 */
scout.StringField.prototype._getClipboardData = function(event, doneHandler) {
  var data = event.originalEvent.clipboardData || this.$container.window(true).clipboardData;
  if (data) {
    // Chrome, Firefox
    if (data.items && data.items.length) {
      var item = scout.arrays.find(data.items, function(item) {
        return item.type === 'text/plain';
      });
      if (item) {
        item.getAsString(doneHandler);
      }
      return;
    }

    // IE, Safari
    if (data.getData) {
      doneHandler(data.getData('Text'));
    }
  }

  // Can't access clipboard -> don't call done handler
};

scout.StringField.prototype._onFieldPaste = function(event) {
  // must store text and selection because when the callback is executed, the clipboard content has already been applied to the input field
  var text = this.$field.val();
  var selection = this._getSelection();

  this._getClipboardData(event, function(pastedText) {
    if (!pastedText) {
      return;
    }

    // Make sure the user is notified about pasted text which is cut off because of maxlength constraints
    text = this._applyTextToSelection(text, pastedText, selection);
    if (text.length > this.maxLength) {
      this._showNotification('ui.PastedTextTooLong');
    }

  }.bind(this));
};

scout.StringField.prototype._showNotification = function(textKey) {
  scout.create('DesktopNotification', {
    parent: this,
    severity: scout.Status.Severity.WARNING,
    message: this.session.text(textKey)
  }).show();
};

/**
 * @override BasicField.js
 */
scout.StringField.prototype._checkDisplayTextChanged = function(displayText, whileTyping) {
  var displayTextChanged = scout.StringField.parent.prototype._checkDisplayTextChanged.call(this, displayText, whileTyping);

  // Display text hasn't changed if input is obfuscated and current display text is empty (because field will be cleared if user focuses obfuscated text field).
  if (displayTextChanged && this.inputObfuscated && displayText === '') {
    return false;
  }

  return displayTextChanged;
};

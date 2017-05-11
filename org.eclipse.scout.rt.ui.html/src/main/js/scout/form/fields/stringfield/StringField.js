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

  this.format;
  this.hasAction = false;
  this.inputMasked = false;
  this.maxLength = 4000;
  this.multilineText = false;
  this.selectionStart = 0;
  this.selectionEnd = 0;
  this.selectionTrackingEnabled = false;
  this.trimText = true;
  this.updateDisplayTextOnModify = false;
  this.wrapText = false;

  this._onSelectionChangingActionHandler = this._onSelectionChangingAction.bind(this);
};
scout.inherits(scout.StringField, scout.BasicField);

scout.StringField.FORMAT = {
  LOWER: 'a' /* IStringField.FORMAT_LOWER */ ,
  UPPER: 'A' /* IStringField.FORMAT_UPPER */
};

scout.StringField.TRIM_REGEXP = new RegExp('^(\\s*)(.*?)(\\s*)$');

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
  this.addContainer(this.$parent, 'string-field');
  this.addLabel();
  this.addMandatoryIndicator();

  var $field;
  if (this.multilineText) {
    var mousedownHandler = function() {
      this.mouseClicked = true;
    }.bind(this);
    $field = this.$parent.makeElement('<textarea>')
      .on('DOMMouseScroll mousewheel', function(event) {
        // otherwise scout.Scrollbar.prototype would handle this event for scrollable group boxes and prevent scrolling on textarea
        event.stopPropagation();
      })
      .on('mousedown', mousedownHandler)
      .on('focus', function(event) {
        this.$field.off('mousedown', mousedownHandler);
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
        this.$field.on('mousedown', mousedownHandler);
      }.bind(this));
  } else {
    $field = scout.fields.makeTextField(this.$parent);
  }
  $field.on('blur', this._onFieldBlur.bind(this));

  this.addField($field);
  this.addStatus();
};

scout.StringField.prototype._onFieldBlur = function() {
  scout.StringField.parent.prototype._onFieldBlur.call(this);
  if (this.multilineText) {
    this._updateSelection();
  }
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

scout.StringField.prototype._renderMaxLength = function() {
  // Check if "maxLength" attribute is supported by browser
  if (this.$field[0].maxLength) {
    this.$field.attr('maxlength', this.maxLength);
  } else {
    // Fallback for IE9
    this.$field.on('keyup paste', function(e) {
      setTimeout(function() {
        var text = this.$field.val();
        if (text.length > this.maxLength) {
          this.$field.val(text.slice(0, this.maxLength));
        }
      }.bind(this), 0);
    }.bind(this));
  }
};

scout.StringField.prototype._renderSelectionStart = function() {
  if (scout.nvl(this.selectionStart, null) !== null) {
    this.$field[0].selectionStart = this.selectionStart;
  }
};

scout.StringField.prototype._renderSelectionEnd = function() {
  if (scout.nvl(this.selectionEnd, null) !== null) {
    this.$field[0].selectionEnd = this.selectionEnd;
  }
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

scout.StringField.prototype._renderInputMasked = function() {
  if (this.multilineText) {
    return;
  }
  this.$field.attr('type', (this.inputMasked ? 'password' : 'text'));
};

scout.StringField.prototype._renderHasAction = function() {
  if (this.hasAction) {
    this.$container.addClass("has-action");
    this.addIcon();
    this.invalidateLayoutTree(false);
  } else {
    if (this.$icon) {
      this.$icon.remove();
      this.$container.removeClass("has-action");
    }
  }
};

scout.StringField.prototype._renderFormat = function() {
  if (this.format === scout.StringField.FORMAT.LOWER) {
    this.$field.css('text-transform', 'lowercase');
  } else if (this.format === scout.StringField.FORMAT.UPPER) {
    this.$field.css('text-transform', 'uppercase');
  }
};

scout.StringField.prototype._renderSpellCheckEnabled = function() {
  if (this.spellCheckEnabled) {
    this.$field.attr('spellcheck', 'true');
  } else {
    this.$field.attr('spellcheck', 'false');
  }
};

scout.StringField.prototype._renderDisplayText = function() {
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
  this.trigger('insertText', {
    text: text
  });
  this._insertText(text);
};

scout.StringField.prototype._insertText = function(textToInsert) {
  if (!textToInsert) {
    return;
  }
  var text = this.$field.val();

  // Prevent insert if new length would exceed maxLength to prevent unintended deletion of characters at the end of the string
  if (textToInsert.length + text.length > this.maxLength) {
    scout.create('DesktopNotification', {
      parent: this,
      severity: scout.Status.Severity.WARNING,
      message: this.session.text('ui.CannotInsertTextTooLong')
    }).show();
    return;
  }

  var selection = this._getSelection();
  text = text.slice(0, selection.start) + textToInsert + text.slice(selection.end);
  this.$field.val(text);

  this._setSelection(selection.start + textToInsert.length);

  // Make sure display text gets sent (necessary if field does not have the focus)
  if (this.updateDisplayTextOnModify) {
    // If flag is true, we need to send two events (First while typing=true, second = false)
    this.acceptInput(true);
  }
  this.acceptInput();
};

scout.StringField.prototype._renderWrapText = function() {
  this.$field.attr('wrap', this.wrapText ? 'soft' : 'off');
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
  if (this.trimText && !scout.objects.isNullOrUndefined(value)) {
    value = value.trim();
  }
  return scout.StringField.parent.prototype._validateValue(value);
};

/**
 * @override ValueField.js
 */
scout.StringField.prototype._updateEmpty = function() {
  this.empty = scout.strings.empty(this.value);
};

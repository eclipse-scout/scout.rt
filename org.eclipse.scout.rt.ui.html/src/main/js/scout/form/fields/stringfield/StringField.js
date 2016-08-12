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
    var mousedownHandler = function() {
      this.mouseClicked = true;
    }.bind(this);
    $field = $parent.makeElement('<textarea>')
      .on('DOMMouseScroll mousewheel', function(event) {
        // otherwise scout.Scrollbar.prototype would handle this event for scrollable group boxes and prevent scrolling on textarea
        event.stopPropagation();
      })
      .on('mousedown', mousedownHandler)
      .on('focus', function(event) {
        this.$field.off('mousedown', mousedownHandler);
        if (!this.mouseClicked) { // only trigger on tab focus in
          setTimeout(function() {
            if (this.rendered) {
              this._renderSelectionStart();
              this._renderSelectionEnd();
            }
          }.bind(this));
        }
        this.mouseClicked = false;
      }.bind(this))
      .on('focusout', function() {
        this.$field.on('mousedown', mousedownHandler);
      }.bind(this));
  } else {
    $field = scout.fields.makeTextField($parent);
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

  this._renderInputMasked(this.inputMasked);
  this._renderWrapText(this.wrapText);
  this._renderFormat(this.format);
  this._renderSpellCheckEnabled(this.spellCheckEnabled);
  this._renderHasAction(this.hasAction);
  this._renderMaxLength();
  this._renderSelectionTrackingEnabled();
  // Do not render selectionStart and selectionEnd here, because that would cause the focus to
  // be set to <textarea>s in IE. Instead, the selection is rendered when the focus has entered
  // the field, see _render(). #168648
  this._renderDropType();
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

scout.StringField.prototype._renderDisplayText = function(displayText) {
  displayText = scout.strings.nvl(displayText);
  var oldDisplayText = scout.strings.nvl(this.$field.val());
  var oldSelection = this._getSelection();
  scout.StringField.parent.prototype._renderDisplayText.call(this, displayText);
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

// Not called in _renderProperties() because this is not really a property (more like an event)
scout.StringField.prototype._renderInsertText = function() {
  if (!this.insertText) {
    return;
  }
  var text = this.$field.val();

  // Prevent insert if new length would exceed maxLength to prevent unintended deletion of characters at the end of the string
  if (this.insertText.length + text.length > this.maxLength) {
    scout.create('DesktopNotification', {
      parent: this,
      id: scout.numbers.randomId(),
      desktop: this.session.desktop,
      duration: 3000,
      status: {
        message: this.session.text('ui.CannotInsertTextTooLong'),
        severity: scout.Status.Severity.WARNING
      }
    }).show();
    return;
  }

  var selection = this._getSelection();
  text = text.slice(0, selection.start) + this.insertText + text.slice(selection.end);
  this.$field.val(text);

  this._setSelection(selection.start + this.insertText.length);

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
  this.updateInnerAlignment({
    useHorizontalAlignment: (this.multilineText ? false : true)
  });
};

scout.StringField.prototype._onIconClick = function(event) {
  this.acceptInput();
  scout.StringField.parent.prototype._onIconClick.call(this, event);
  this._send('callAction');
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
  }
  else if (typeof selectionStart === 'object') {
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
      this._sendSelectionChanged();
    }
  }
};

scout.StringField.prototype._sendSelectionChanged = function() {
  var eventData = {
    selectionStart: this.selectionStart,
    selectionEnd: this.selectionEnd
  };

  // send delayed to avoid a lot of requests while selecting
  // coalesce: only send the latest selection changed event for a field
  this._send('selectionChanged', eventData, {
    delay: 500,
    coalesce: function(previous) {
      return this.id === previous.id && this.type === previous.type;
    }
  });
};

scout.StringField.prototype._validateDisplayText = function(displayText, whileTyping) {
  if (!whileTyping && this.trimText) {
    displayText = displayText.trim();
  }
  return scout.StringField.parent.prototype._validateDisplayText(displayText, whileTyping);
};

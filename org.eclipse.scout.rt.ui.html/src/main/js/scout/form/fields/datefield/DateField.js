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
scout.DateField = function() {
  scout.DateField.parent.call(this);
  this.enabledWhenOffline = true;
  this.disabledCopyOverlay = true;

  this.$dateField;
  this.$timeField;
  this.$dateFieldIcon;
  this.$timeFieldIcon;
  this._$predictDateField;
  this._$predictTimeField;

  // This is the storage for the time (as date) while the focus in the field (e.g. when
  // pressing up/down). In date fields, the date picker is used for that purposes.
  this._tempTimeDate;

  // The UI does some parsing validations that may set error status to "this.errorStatus"
  // (see _setDateValid() and _setTimeValid()). When the UI finds no errors, the UI error
  // status is removed. In this case, the original error status of the server model
  // has to be set again. Therefore, we have to store it in a private variable.
  this._modelErrorStatus = null;

  this._popup;
  this._datePickerDateSelectedHandler;
  this._popupCloseHandler;
};
scout.inherits(scout.DateField, scout.ValueField);

/**
 * @override Widget.js
 */
scout.DateField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.DateField.prototype._init = function(model) {
  scout.DateField.parent.prototype._init.call(this, model);
  scout.fields.initTouch(this, model);
  this._popup = model.popup;
};

scout.DateField.prototype.addPopup = function() {
  if (!this._popup) {
    var popupType = this.touch ? 'DatePickerTouchPopup' : 'DatePickerPopup';
    this._popup = scout.create(popupType, {
      parent: this,
      $anchor: this.$field,
      boundToAnchor: !this.touch,
      field: this
    });
  }
};

scout.DateField.prototype._syncErrorStatus = function(errorStatus) {
  scout.DateField.parent.prototype._syncErrorStatus.call(this, errorStatus);
  this._modelErrorStatus = this.errorStatus;
};

scout.DateField.prototype._render = function($parent) {
  this.addContainer($parent, 'date-field');
  this.addLabel();
  this.addField($parent.makeDiv('date-time-composite'));
  this.addStatus(this.$field);
  if (!this.embedded) {
    this.addMandatoryIndicator();
  }

  this.htmlDateTimeComposite = new scout.HtmlComponent(this.$field, this.session);
  this.htmlDateTimeComposite.setLayout(new scout.DateTimeCompositeLayout(this));
  this.addPopup();
};

scout.DateField.prototype._renderProperties = function() {
  this._renderHasDate();
  this._renderHasTime();
  this._renderDateFormatPattern();
  this._renderTimeFormatPattern();
  this._renderTimestamp();
  this._renderAutoTimestamp();

  // Has to be the last call, otherwise _renderErrorStatus() would operate on the wrong state.
  scout.DateField.parent.prototype._renderProperties.call(this);
};

scout.DateField.prototype._remove = function() {
  scout.DateField.parent.prototype._remove.call(this);
  this.$dateField = null;
  this.$timeField = null;
  this.$dateFieldIcon = null;
  this.$timeFieldIcon = null;
  this._$predictDateField = null;
  this._$predictTimeField = null;
  // popup may not be reused because $anchor would point to a removed field
  // Don't set to null in case of embedded mode to make sure not another popup gets created by the field itself when touch popup get rendered!
  if (this._popup && !this.embedded) {
    this._popup = null;
  }
};

scout.DateField.prototype._renderHasDate = function() {
  if (this.hasDate && !this.$dateField) {
    // Add $dateField
    this.$dateField = scout.fields.makeInputOrDiv(this, 'date')
      .on('mousedown', this._onDateFieldClick.bind(this))
      .appendTo(this.$field);
    if (!this.touch) {
      this.$dateField
        .on('keydown', this._onDateFieldKeydown.bind(this))
        .on('input', this._onDateFieldInput.bind(this))
        .on('blur', this._onDateFieldBlur.bind(this));
    }

    new scout.HtmlComponent(this.$dateField, this.session);

    this.$dateFieldIcon = scout.fields.appendIcon(this.$field, 'date')
      .on('mousedown', this._onDateIconClick.bind(this));

    this.invalidateLayout();

  } else if (!this.hasDate && this.$dateField) {
    // Remove $dateField
    this.$dateField.remove();
    this.$dateField = null;
    this.$dateFieldIcon.remove();
    this.$dateFieldIcon = null;

    this.invalidateLayout();
  }
};

scout.DateField.prototype._renderHasTime = function() {
  if (this.hasTime && !this.$timeField) {
    // Add $timeField
    this.$timeField = scout.fields.makeInputOrDiv(this, 'time')
      .on('mousedown', this._onTimeFieldClick.bind(this))
      .appendTo(this.$field);
    if (!this.touch) {
      this.$timeField
        .on('keydown', this._onTimeFieldKeydown.bind(this))
        .on('input', this._onTimeFieldInput.bind(this))
        .on('blur', this._onTimeFieldBlur.bind(this));
    }
    this.$timeFieldIcon = scout.fields.appendIcon(this.$field, 'time')
      .on('mousedown', this._onTimeIconClick.bind(this));
    new scout.HtmlComponent(this.$timeField, this.session);

    this.invalidateLayout();

  } else if (!this.hasTime && this.$timeField) {
    // Remove $timeField
    this.$timeField.remove();
    this.$timeField = null;
    this.$timeFieldIcon.remove();
    this.$timeFieldIcon = null;

    this.invalidateLayout();
  }
};

/**
 * @override FormField.js
 */
scout.DateField.prototype._renderPlaceholder = function() {
  scout.DateField.parent.prototype._renderPlaceholder.call(this,
      this._fieldForPlaceholder());
};

/**
 * @override FormField.js
 */
scout.DateField.prototype._removePlaceholder = function() {
  scout.DateField.parent.prototype._removePlaceholder.call(this,
      this._fieldForPlaceholder());
};

scout.DateField.prototype._fieldForPlaceholder = function() {
  if (this.hasDate) {
    return this.$dateField;
  } else if (this.hasTime) {
    return this.$timeField;
  }
  return null;
};

scout.DateField.prototype._renderDateFormatPattern = function() {
  this.isolatedDateFormat = new scout.DateFormat(this.session.locale, this.dateFormatPattern);
  this.getDatePicker().dateFormat = this.isolatedDateFormat;
  if (this.rendered) {
    this._renderTimestamp();
  }
};

scout.DateField.prototype._renderTimeFormatPattern = function() {
  this.isolatedTimeFormat = new scout.DateFormat(this.session.locale, this.timeFormatPattern);
  if (this.rendered) {
    this._renderTimestamp();
  }
};

/**
 * @override BasicField.js
 */
scout.DateField.prototype._renderEnabled = function() {
  scout.DateField.parent.prototype._renderEnabled.call(this);
  this.$container.setEnabled(this.enabled);
  if (this.$dateField) {
    this.$dateField.setEnabled(this.enabled);
  }
  if (this.$timeField) {
    this.$timeField.setEnabled(this.enabled);
  }
};

/**
 * @override
 */
scout.DateField.prototype._renderDisplayText = function() {
  //nop -> handled in _renderTimestamp
};

scout.DateField.prototype._renderTimestamp = function() {
  this.timestampAsDate = scout.dates.parseJsonDate(this.timestamp);
  this.renderDate(this.timestampAsDate);
};

scout.DateField.prototype._renderAutoTimestamp = function() {
  this.autoTimestampAsDate = scout.dates.parseJsonDate(this.autoTimestamp);
};

/**
 * @override FormField.js
 */
scout.DateField.prototype._renderErrorStatus = function() {
  scout.DateField.parent.prototype._renderErrorStatus.call(this);
  var hasStatus = !!this.errorStatus,
    statusClass = hasStatus ? this.errorStatus.cssClass() : '';

  if (this.$dateField) {
    this.$dateField.removeClass(scout.Status.cssClasses);
    this.$dateField.toggleClass(statusClass, hasStatus);

    // Because the error color of field icons depends on the error status of sibling <input> elements.
    // The prediction fields are clones of the input fields, so the 'has-error' class has to be
    // removed from them as well to make the icon "valid".
    if (this._$predictDateField) {
      this._$predictDateField.removeClass(scout.Status.cssClasses);
      this._$predictDateField.toggleClass(statusClass, hasStatus);
    }
    // Put invalid input in the field, if the current input differs
    // (don't do it always, this would alter the cursor position)
    if (!this._isDateValid() && this.$dateField.val() !== this.errorStatus.invalidDateText) {
      this._setDateDisplayText(this.errorStatus.invalidDateText);
    }
  }

  // Do the same for the time field
  if (this.$timeField) {
    this.$timeField.removeClass(scout.Status.cssClasses);
    this.$timeField.toggleClass(statusClass, hasStatus);
    if (this._$predictTimeField) {
      this._$predictTimeField.removeClass(scout.Status.cssClasses);
      this._$predictTimeField.toggleClass(statusClass, hasStatus);
    }
    if (!this._isTimeValid() && this.$timeField.val() !== this.errorStatus.invalidTimeText) {
      this._setTimeDisplayText(this.errorStatus.invalidTimeText);
    }
  }

  if (this.hasDate && this._popup.isOpen()) {
    this._popup.$container.removeClass(scout.Status.cssClasses);
    this._popup.$container.toggleClass(statusClass, hasStatus);
  }
};

/**
 * @Override FormField.js
 */
scout.DateField.prototype._renderFont = function() {
  this.$dateField && scout.styles.legacyStyle(this, this.$dateField);
  this.$timeField && scout.styles.legacyStyle(this, this.$timeField);
};

/**
 * @Override FormField.js
 */
scout.DateField.prototype._renderForegroundColor = function() {
  this.$dateField && scout.styles.legacyStyle(this, this.$dateField);
  this.$timeField && scout.styles.legacyStyle(this, this.$timeField);
};

/**
 * @Override FormField.js
 */
scout.DateField.prototype._renderBackgroundColor = function() {
  this.$dateField && scout.styles.legacyStyle(this, this.$dateField);
  this.$timeField && scout.styles.legacyStyle(this, this.$timeField);
};

scout.DateField.prototype._onDateFieldClick = function() {
  if (scout.fields.handleOnClick(this)) {
    this._openDatePicker(this.timestampAsDate);
  }
};

scout.DateField.prototype._onDateIconClick = function(event) {
  if (scout.fields.handleOnClick(this)) {
    this.$dateField.focus();
    this._openDatePicker(this.timestampAsDate);
  }
};

scout.DateField.prototype._onTimeFieldClick = function(event) {
  if (scout.fields.handleOnClick(this) && this.touch) {
    this._openDatePicker(this.timestampAsDate);
  }
};

scout.DateField.prototype._onTimeIconClick = function(event) {
  if (scout.fields.handleOnClick(this)) {
    this.$timeField.focus();
    if (this.touch) {
      this._openDatePicker(this.timestampAsDate);
    }
  }
};

scout.DateField.prototype._onDateFieldBlur = function() {
  // Close picker and update model
  if (!this.embedded) {
    // in embedded mode we must update the date prediction but not close the popup
    this._closeDatePicker();
  }
  this._acceptDateTimePrediction(true, false);
};

scout.DateField.prototype._onTimeFieldBlur = function() {
  this._tempTimeDate = null;
  this._acceptDateTimePrediction(false, true);
};

/**
 * Handle "navigation" keys, i.e. keys that don't emit visible characters. Character input is handled
 * in _onDateFieldInput(), which is fired after 'keydown'.
 */
scout.DateField.prototype._onDateFieldKeydown = function(event) {
  var delta = 0,
    diffYears = 0,
    diffMonths = 0,
    diffDays = 0,
    cursorPos = this.$dateField[0].selectionStart,
    displayText = this.$dateField.val(),
    prediction = this._$predictDateField && this._$predictDateField.val(),
    modifierCount = (event.ctrlKey ? 1 : 0) + (event.shiftKey ? 1 : 0) + (event.altKey ? 1 : 0) + (event.metaKey ? 1 : 0),
    pickerStartDate = this.timestampAsDate || this._referenceDate(),
    shiftDate = true;

  // Don't propagate tab to cell editor -> tab should focus time field
  if (this.hasTime &&
    this.mode === scout.FormField.MODE_CELLEDITOR &&
    event.which === scout.keys.TAB &&
    modifierCount === 0) {
    event.stopPropagation();
    return;
  }

  if (event.which === scout.keys.TAB ||
    event.which === scout.keys.SHIFT ||
    event.which === scout.keys.HOME ||
    event.which === scout.keys.END ||
    event.which === scout.keys.CTRL ||
    event.which === scout.keys.ALT) {
    // Default handling
    return;
  }

  if (event.which === scout.keys.ENTER) {
    if (this._popup.isOpen() || this._$predictDateField) {
      // Close the picker and accept the current prediction (if available)
      this._acceptDateTimePrediction(true, false); // accept date part
      this._closeDatePicker();
      $.suppressEvent(event);
    }
    return;
  }

  if (event.which === scout.keys.ESC) {
    if (this._popup.isOpen()) {
      // Close the picker, but don't do anything else
      this._closeDatePicker();
      $.suppressEvent(event);
    }
    return;
  }

  if (event.which === scout.keys.RIGHT && cursorPos === displayText.length) {
    // Move cursor one right and apply next char of the prediction
    if (prediction) {
      this._setDateDisplayText(prediction.substring(0, displayText.length + 1));
    }
    return;
  }

  if (event.which === scout.keys.PAGE_UP || event.which === scout.keys.PAGE_DOWN) {
    if (!displayText || !this._isDateValid()) {
      // If input is empty or invalid, set picker to reference date
      pickerStartDate = this._referenceDate();
      if (this.hasTime) { // keep time part
        pickerStartDate = scout.dates.combineDateTime(pickerStartDate, this.timestampAsDate);
      }
      this._openDatePicker(pickerStartDate);
      this.renderDate(pickerStartDate);
      shiftDate = false; // don't shift if field has no value yet and popup was not open
    } else if (!this._popup.isOpen()) {
      // Otherwise, ensure picker is open
      this._openDatePicker(pickerStartDate);
    }
    if (shiftDate) {
      diffMonths = (event.which === scout.keys.PAGE_UP ? -1 : 1);
      this.shiftSelectedDate(0, diffMonths, 0);
    }
    $.suppressEvent(event);
    return;
  }

  if (event.which === scout.keys.UP || event.which === scout.keys.DOWN) {
    delta = (event.which === scout.keys.UP ? -1 : 1);
    //event.ctrlKey||event.metaKey  --> some keystrokes with ctrl modifier are captured and suppressed by osx use in this cases command key
    if ((event.ctrlKey || event.metaKey) && modifierCount === 1) { // only ctrl
      diffYears = delta;
    } else if (event.shiftKey && modifierCount === 1) { // only shift
      diffMonths = delta;
    } else if (modifierCount === 0) { // no modifier
      diffDays = delta;
    } else {
      // Unsupported modifier or too many modifiers
      $.suppressEvent(event);
      return;
    }

    if (!displayText || !this._isDateValid()) {
      // If input is empty or invalid, set picker to reference date
      pickerStartDate = this._referenceDate();
      if (this.hasTime) { // keep time part
        pickerStartDate = scout.dates.combineDateTime(pickerStartDate, this.timestampAsDate);
      }
      this._openDatePicker(pickerStartDate);
      this.renderDate(pickerStartDate);
      shiftDate = false; // don't shift if field has no value yet and popup was not open
    } else if (!this._popup.isOpen()) {
      // Otherwise, ensure picker is open
      this._openDatePicker(pickerStartDate);
    }
    if (shiftDate) {
      this.shiftSelectedDate(diffYears, diffMonths, diffDays);
    }
    $.suppressEvent(event);
    return;
  }
};

/**
 * Handle changed input. This method is fired when the field's content has been altered by a user
 * action (not by JS) such as pressing a character key, deleting a character using DELETE or
 * BACKSPACE, cutting or pasting text with ctrl-x / ctrl-v or mouse drag'n'drop.
 * Keys that don't alter the content (e.g. modifier keys, arrow keys, home, end etc.) are handled
 * in _onDateFieldKeydown().
 */
scout.DateField.prototype._onDateFieldInput = function(event) {
  var displayText = this.$dateField.val();

  // If the focus has changed to another field in the meantime, don't predict anything and
  // don't show the picker. Just validate the input.
  if (this.$dateField[0] !== this.$dateField.activeElement(true)) {
    return;
  }

  // Create $predictDateField if necessary
  if (!this._$predictDateField) {
    this._$predictDateField = this._createPredictionField(this.$dateField);
  }

  // Predict date
  var datePrediction = this._predictDate(displayText); // this also updates the errorStatus
  if (datePrediction) {
    this._$predictDateField.val(datePrediction.text);
    this._openDatePicker(datePrediction.date);
  } else {
    // No valid prediction!
    this._removePredictionFields();
  }
};

/**
 * Handle "navigation" keys, i.e. keys that don't emit visible characters. Character input is handled
 * in _onTimeFieldInput(), which is fired after 'keydown'.
 */
scout.DateField.prototype._onTimeFieldKeydown = function(event) {
  var delta = 0,
    diffHours = 0,
    diffMinutes = 0,
    diffSeconds = 0,
    cursorPos = this.$timeField[0].selectionStart,
    displayText = this.$timeField.val(),
    prediction = this._$predictTimeField && this._$predictTimeField.val(),
    modifierCount = (event.ctrlKey ? 1 : 0) + (event.shiftKey ? 1 : 0) + (event.altKey ? 1 : 0) + (event.metaKey ? 1 : 0),
    shiftDate = true,
    date = null;

  // Don't propagate shift-tab to cell editor -> shift tab should focus date field
  if (this.hasDate &&
    this.mode === scout.FormField.MODE_CELLEDITOR &&
    event.which === scout.keys.TAB &&
    event.shiftKey &&
    modifierCount === 1) {
    event.stopPropagation();
    return;
  }

  if (event.which === scout.keys.TAB ||
    event.which === scout.keys.SHIFT ||
    event.which === scout.keys.HOME ||
    event.which === scout.keys.END ||
    event.which === scout.keys.CTRL ||
    event.which === scout.keys.ALT ||
    event.which === scout.keys.ESC) {
    // Default handling
    return;
  }

  if (event.which === scout.keys.ENTER) {
    if (this._$predictTimeField) {
      // Accept the current prediction (if available)
      this._tempTimeDate = null;
      this._acceptDateTimePrediction(false, true); // accept time part
      // in embedded mode, we want to close the whole popup when user presses enter and time field is focused
      if (this.embedded) {
        this._closeDatePicker();
      }
      $.suppressEvent(event);
    }
    return;
  }

  if (event.which === scout.keys.RIGHT && cursorPos === displayText.length) {
    // Move cursor one right and apply next char of the prediction
    if (prediction) {
      this._setTimeDisplayText(prediction.substring(0, displayText.length + 1));
    }
    return;
  }

  if (event.which === scout.keys.UP || event.which === scout.keys.DOWN) {
    delta = (event.which === scout.keys.UP ? -1 : 1);
    if (event.ctrlKey && modifierCount === 1) { // only ctrl
      diffSeconds = delta;
    } else if (event.shiftKey && modifierCount === 1) { // only shift
      diffHours = delta;
    } else if (modifierCount === 0) { // no modifier
      diffMinutes = delta;
    } else {
      // Unsupported modifier or too many modifiers
      $.suppressEvent(event);
      return;
    }

    if (!this._tempTimeDate) {
      var timePrediction = this._predictTime(displayText); // this also updates the errorStatus
      if (timePrediction && timePrediction.date) {
        this._tempTimeDate = timePrediction.date;
      } else {
        this._tempTimeDate = this._referenceDate();
        shiftDate = false;
      }
    }
    if (shiftDate) {
      this._tempTimeDate = scout.dates.shiftTime(this._tempTimeDate, diffHours, diffMinutes, diffSeconds);
    }
    if (this.hasDate) {
      // Combine _tempTimeDate with existing date part
      this._tempTimeDate = scout.dates.combineDateTime(this.timestampAsDate || this._referenceDate(), this._tempTimeDate);
    }
    this.renderDate(this._tempTimeDate);
    $.suppressEvent(event);
    return;
  }
};

/**
 * Handle changed input. This method is fired when the field's content has been altered by a user
 * action (not by JS) such as pressing a character key, deleting a character using DELETE or
 * BACKSPACE, cutting or pasting text with ctrl-x / ctrl-v or mouse drag'n'drop.
 * Keys that don't alter the content (e.g. modifier keys, arrow keys, home, end etc.) are handled
 * in _onTimeFieldKeydown().
 */
scout.DateField.prototype._onTimeFieldInput = function(event) {
  var displayText = this.$timeField.val();

  // If the focus has changed to another field in the meantime, don't predict anything and
  // don't show the picker. Just validate the input.
  if (this.$timeField[0] !== this.$timeField.activeElement(true)) {
    return;
  }

  // Create $predictTimeField if necessary
  if (!this._$predictTimeField) {
    this._$predictTimeField = this._createPredictionField(this.$timeField);
  }

  // Predict time
  var timePrediction = this._predictTime(displayText); // this also updates the errorStatus
  if (timePrediction) {
    this._$predictTimeField.val(timePrediction.text);
  } else {
    // No valid prediction!
    this._tempTimeDate = null;
    this._removePredictionFields();
  }
};

scout.DateField.prototype._onDatePickerDateSelected = function(event) {
  this._setDateValid(true);
  this._setTimeValid(true);
  this.renderDate(event.date);
  if (!event.shifting) {
    // close popup and write to model
    this.updateTimestamp(this._newTimestampAsDate(event.date, this.timestampAsDate));
    // closeDatePicker is set to false when date-field is in touch-mode
    // since popup will be closed by the embedded date-field
    if (scout.nvl(event.closeDatePicker, true)) {
      this._closeDatePicker();
    }
  }
};

scout.DateField.prototype._createPredictionField = function($inputField) {
  var $predictionField = $inputField.clone()
    .addClass('predict')
    .attr('tabIndex', '-1')
    .insertBefore($inputField);
  if ($inputField.hasClass('has-error')) {
    $predictionField.addClass('has-error');
  }
  return $predictionField;
};

scout.DateField.prototype._removePredictionFields = function() {
  if (this._$predictDateField) {
    this._$predictDateField.remove();
    this._$predictDateField = null;
  }
  if (this._$predictTimeField) {
    this._$predictTimeField.remove();
    this._$predictTimeField = null;
  }
};

/**
 * Formats the given date (or this.timestampAsDate) using the specified formats and writes
 * the resulting string to the $dateField and $timeField, respectively. Prediction field
 * are removed.
 */
scout.DateField.prototype.renderDate = function(date) {
  date = date || this.timestampAsDate;

  // Only update displayTexts when value is invalid. Otherwise, we would
  // override the invalid display text from the model. Also note that we
  // have to backup and restore the selection, otherwise, the cursor would
  // move to the end after every key press.
  var selection;
  if (this.hasDate && this._isDateValid()) {
    selection = this.$dateField.backupSelection();
    this._setDateDisplayText(this.isolatedDateFormat.format(date));
    this.$dateField.restoreSelection(selection);
  }
  if (this.hasTime && this._isTimeValid()) {
    selection = this.$timeField.backupSelection();
    this._setTimeDisplayText(this.isolatedTimeFormat.format(date));
    this.$timeField.restoreSelection(selection);
  }
  // Make sure there is no invisible and wrong prediction
  this._removePredictionFields();
};

scout.DateField.prototype._setDateDisplayText = function(displayText) {
  scout.fields.valOrText(this, this.$dateField, displayText);
  this._updateDisplayTextProperty();
};

scout.DateField.prototype._setTimeDisplayText = function(displayText) {
  scout.fields.valOrText(this, this.$timeField, displayText);
  this._updateDisplayTextProperty();
};

/**
 * Note: this.displayText is only used in the UI (BasicField.js) and is not synchronized with the server.
 */
scout.DateField.prototype._updateDisplayTextProperty = function() {
  var dateText = this.$dateField ? this.$dateField.val() : '',
    timeText = this.$timeField ? this.$timeField.val() : '';
  this.displayText = scout.strings.join(' ', dateText, timeText);
};

/**
 * @override ValueField.js
 */
scout.DateField.prototype.acceptInput = function(whileTyping) {
  this._acceptDateTimePrediction(this.hasDate, this.hasTime);
};

/**
 * @override ValueField.js
 */
scout.DateField.prototype.aboutToBlurByMouseDown = function(target) {
  var dateFieldActive, timeFieldActive, eventOnDatePicker,
    eventOnDateField = this.$dateField ? this.$dateField.isOrHas(target) : false,
    eventOnTimeField = this.$timeField ? this.$timeField.isOrHas(target) : false,
    eventOnPopup = this._popup.isOpen() && this._popup.$container.isOrHas(target);

  if (!eventOnDateField && !eventOnTimeField && !eventOnPopup) {
    // event outside this field.
    dateFieldActive = scout.focusUtils.isActiveElement(this.$dateField);
    timeFieldActive = scout.focusUtils.isActiveElement(this.$timeField);
    // Accept only the currently focused part (the other one cannot have a pending change)
    this._acceptDateTimePrediction(dateFieldActive, timeFieldActive);
    return;
  }

  // when date-field is embedded, time-prediction must be accepted before
  // the date-picker triggers the 'dateSelect' event.
  if (this.embedded) {
    eventOnDatePicker = this.getDatePicker().$container.isOrHas(target);
    timeFieldActive = scout.focusUtils.isActiveElement(this.$timeField);
    if (eventOnDatePicker && timeFieldActive) {
      this._acceptDateTimePrediction(false, true);
    }
  }
};

/**
 * @Override BasicField.js
 */
scout.DateField.prototype._onDisplayTextModified = function() {
  // TODO [5.2] bsh: What to do? Maybe change on server that this method is never called.
};

/**
 * Returns null if both arguments are not set. Otherwise, this.timestampAsDate or the current date
 * is used as basis and the given arguments are applied to that date. The result is returned.
 */
scout.DateField.prototype._newTimestampAsDate = function(date, time) {
  var result = null;
  if (date || time) {
    result = this._referenceDate();
    if (date) {
      result = scout.dates.combineDateTime(date, result);
    }
    if (time) {
      result = scout.dates.combineDateTime(result, time);
    }
  }
  return result;
};

/**
 * Returns the reference date for this date field, which is used in various places (i.e. opening the date picker, analyzing user inputs).
 *
 * The reference date is either (in that order):
 * - the model's "auto timestamp" (as date), or
 * - the current date/time
 */
scout.DateField.prototype._referenceDate = function() {
  return this.autoTimestampAsDate || scout.dates.trunc(new Date());
};

scout.DateField.prototype.updateTimestamp = function(timestampAsDate, syncToServer) {
  var timestamp = scout.dates.toJsonDate(timestampAsDate, false, this.hasDate, this.hasTime);
  if (timestamp !== this.timestamp || this.errorStatus) {
    this.timestamp = timestamp;
    this.timestampAsDate = timestampAsDate;
    if (scout.nvl(syncToServer, true)) {
      this._syncToServer();
    }
  }
};

scout.DateField.prototype._syncToServer = function() {
  if (this._hasUiErrorStatus()) {
    this._send('parsingError', {
      invalidDisplayText: this.errorStatus.invalidDisplayText,
      invalidDateText: this.errorStatus.invalidDateText,
      invalidTimeText: this.errorStatus.invalidTimeText
    });
  } else {
    var event = {
      timestamp: this.timestamp
    };
    this._send('timestampChanged', event);
    // FIXME AWE: (filter) improv. trigger/send in date-field , sendAndTrigger method
    this.trigger('timestampChanged', event);
  }
};

/**
 * Opens picker and selects date
 *
 * @param date
 *          optional, Date to pass to the date picker. If no date is specified, the reference date
 *          is preselected (not selected!).
 */
scout.DateField.prototype._openDatePicker = function(date) {
  if (!date) {
    this.preselectDate(this._referenceDate());
  } else {
    this.selectDate(date);
  }

  // Add popup close handler
  if (!this._popupCloseHandler) {
    this._popupCloseHandler = function() {
      if (this.touch) {
        this._onDatePickerDateSelected({
          date: this._popup._field.timestampAsDate,
          closeDatePicker: false
        });
      } else {
        this.getDatePicker().off('dateSelect', this._datePickerDateSelectedHandler);
        this._datePickerDateSelectedHandler = null;
      }
      this._popup.off('close', this._popupCloseHandler);
      this._popupCloseHandler = null;
    }.bind(this);
    this._popup.on('close', this._popupCloseHandler);
  }

  // Add date-picker dateSelect handler
  // In touch mode only the date-field in the popup (=embedded) should register a listener
  // but not the 'touch' date-field. The touch-field is updated when the popup is closed.
  // In desktop-mode the date-field must register the listener.
  if (!this.touch) {
    this._attachDatePickerDateSelectedHandler();
  }
};

scout.DateField.prototype._attachDatePickerDateSelectedHandler = function() {
  if (!this._datePickerDateSelectedHandler) {
    this._datePickerDateSelectedHandler = this._onDatePickerDateSelected.bind(this);
    this.getDatePicker().on('dateSelect', this._datePickerDateSelectedHandler);
  }
};

scout.DateField.prototype._closeDatePicker = function() {
  if (this._popup.isOpen()) {
    this._popup.close();
  }
};

scout.DateField.prototype._acceptDateTimePrediction = function(acceptDate, acceptTime) {
  var dateText, timeText, datePrediction, timePrediction;

  var success = true;
  if (acceptDate) {
    dateText = (this._$predictDateField ? this._$predictDateField.val() : this.$dateField.val());
    datePrediction = this._predictDate(dateText); // this also updates the errorStatus
    if (!datePrediction) {
      success = false;
    }
  }
  if (acceptTime) {
    timeText = (this._$predictTimeField ? this._$predictTimeField.val() : this.$timeField.val());
    timePrediction = this._predictTime(timeText); // this also updates the errorStatus
    if (!timePrediction) {
      success = false;
    }
  }
  this._removePredictionFields();

  if (success) {
    // parse success -> send new timestamp to server
    var newTimestamp = null;
    if ((acceptDate && datePrediction.date) || (acceptTime && timePrediction.date)) {
      newTimestamp = this._newTimestampAsDate(
        (acceptDate ? datePrediction.date : this.timestampAsDate), (acceptTime ? timePrediction.date : this.timestampAsDate));
    }
    this.updateTimestamp(newTimestamp);
    this.renderDate(this.timestampAsDate);
  } else {
    // parse error -> send error to server
    this._syncToServer();
  }
};

/**
 * @returns null if input is invalid, otherwise an object with properties 'date' and 'text'
 */
scout.DateField.prototype._predictDate = function(inputText) {
  inputText = inputText || '';

  // "Date calculations"
  var m = inputText.match(/^([+-])(\d*)$/);
  if (m) {
    var now = new Date();
    var daysToAdd = Number(m[1] + (m[2] || '0'));
    now.setDate(now.getDate() + daysToAdd);
    this._setDateValid(true, inputText);
    return {
      date: now,
      text: inputText
    };
  }

  var analyzeInfo = this.isolatedDateFormat.analyze(inputText, this._referenceDate());
  if (analyzeInfo.error) {
    this._setDateValid(false, inputText);
    return null;
  }

  // No predicted date? -> return empty string (may happen if inputText is empty)
  if (!analyzeInfo.predictedDate) {
    this._setDateValid(true, '');
    return {
      date: null,
      text: ''
    };
  }

  var predictedDate = analyzeInfo.predictedDate;
  var predictionFormat = new scout.DateFormat(this.isolatedDateFormat.locale, analyzeInfo.parsedPattern);
  var predictedDateFormatted = predictionFormat.format(predictedDate, true);

  // If predicted date format starts with validatedText, ensure that the capitalization matches.
  // Example: input = 'frid', predicted = 'Friday, 1.10.2014' --> return 'friday, 1.10.2014')
  m = predictedDateFormatted.match(new RegExp('^' + scout.strings.quote(inputText) + '(.*)$', 'i'));
  if (m) {
    predictedDateFormatted = inputText + m[1];
  }

  this._setDateValid(true, predictedDateFormatted);
  return {
    date: predictedDate,
    text: predictedDateFormatted
  };
};

/**
 * @returns null if input is invalid, otherwise an object with properties 'date' and 'text'
 */
scout.DateField.prototype._predictTime = function(inputText) {
  inputText = inputText || '';

  var analyzeInfo = this.isolatedTimeFormat.analyze(inputText, this._referenceDate());
  if (analyzeInfo.error) {
    this._setTimeValid(false, inputText);
    return null;
  }

  // No predicted date? -> return empty string (may happen if inputText is empty)
  if (!analyzeInfo.predictedDate) {
    this._setTimeValid(true, '');
    return {
      date: null,
      text: ''
    };
  }

  var predictedDate = analyzeInfo.predictedDate;
  var predictionFormat = new scout.DateFormat(this.isolatedTimeFormat.locale, analyzeInfo.parsedPattern);
  var predictedTimeFormatted = predictionFormat.format(predictedDate, true);

  // If predicted date format starts with validatedText, ensure that the capitalization matches.
  // Example: input = 'frid', predicted = 'Friday, 1.10.2014' --> return 'friday, 1.10.2014')
  var m = predictedTimeFormatted.match(new RegExp('^' + scout.strings.quote(inputText) + '(.*)$', 'i'));
  if (m) {
    predictedTimeFormatted = inputText + m[1];
  }

  this._setTimeValid(true, predictedTimeFormatted);
  return {
    date: predictedDate,
    text: predictedTimeFormatted
  };
};

scout.DateField.prototype._setDateValid = function(valid, dateText) {
  var errorStatus = this.errorStatus;
  if (valid) {
    // Set to valid
    if (errorStatus) {
      errorStatus.invalidDateText = null;
      // If no other UI error remains, revert to errorStatus from model
      if (!this._hasUiErrorStatus()) {
        errorStatus = this._modelErrorStatus;
      }
      this.setErrorStatus(errorStatus);
    }
  } else {
    // Set to invalid (this is always a UI error)
    if (!this._hasUiErrorStatus()) {
      errorStatus = new scout.Status({
        message: this.session.text('ui.InvalidDateFormat'),
        severity: scout.Status.Severity.ERROR
      });
    }
    errorStatus.invalidDateText = dateText;
    errorStatus.invalidDisplayText = scout.strings.join(' ',
      dateText,
      this.$timeField && this.$timeField.val());
    this.setErrorStatus(errorStatus);
  }
  this.setErrorStatus(errorStatus);
  // The layout might have been invalidated by setErrorStatus() when showing/hiding the status icon
  // automatically. Because this is a UI only operation, we have to trigger validation manually.
  this.validateLayout();
};

scout.DateField.prototype._setTimeValid = function(valid, timeText) {
  var errorStatus = this.errorStatus;
  if (valid) {
    // Set to valid
    if (errorStatus) {
      errorStatus.invalidTimeText = null;
      // If no other UI error remains, revert to errorStatus from model
      if (!this._hasUiErrorStatus()) {
        errorStatus = this._modelErrorStatus;
      }
      this.setErrorStatus(errorStatus);
    }
  } else {
    // Set to invalid (this is always a UI error)
    if (!this._hasUiErrorStatus()) {
      errorStatus = new scout.Status({
        message: this.session.text('ui.InvalidDateFormat'),
        severity: scout.Status.Severity.ERROR
      });
    }
    errorStatus.invalidTimeText = timeText;
    errorStatus.invalidDisplayText = scout.strings.join(' ',
      this.$dateField && this.$dateField.val(),
      timeText);
    this.setErrorStatus(errorStatus);
  }
  // The layout might have been invalidated by setErrorStatus() when showing/hiding the status icon
  // automatically. Because this is a UI only operation, we have to trigger validation manually.
  this.validateLayout();
};

scout.DateField.prototype._isDateValid = function() {
  if (this.errorStatus && this.errorStatus.invalidDateText) {
    return false;
  }
  return true;
};

scout.DateField.prototype._isTimeValid = function() {
  if (this.errorStatus && this.errorStatus.invalidTimeText) {
    return false;
  }
  return true;
};

scout.DateField.prototype._hasUiErrorStatus = function() {
  return !!(this.errorStatus && (this.errorStatus.invalidDateText || this.errorStatus.invalidTimeText));
};

/**
 * Method invoked if being rendered within a cell-editor (mode='scout.FormField.MODE_CELLEDITOR'), and once the editor finished its rendering.
 */
scout.DateField.prototype.onCellEditorRendered = function(options) {
  if (options.openFieldPopup && this.hasDate) {
    this._openDatePicker(this.timestampAsDate);
  }
};

/**
 * @override FormField.js
 */
scout.DateField.prototype.prepareForCellEdit = function(opts) {
  opts = opts || {};
  scout.DateField.parent.prototype.prepareForCellEdit.call(this, opts);

  this.$field.removeClass('cell-editor-field first');
  if (this.$dateField) {
    this.$dateField.addClass('cell-editor-field');
    if (opts.firstCell) {
      this.$dateField.addClass('first');
    }
  }
  if (this.$timeField) {
    this.$timeField.addClass('cell-editor-field');
    if (opts.firstCell && !this.$dateField) {
      this.$timeField.addClass('first');
    }
  }
};

/**
 * @returns DatePicker instance from popup, because the property name is different
 *    for DatePickerPopup and DatePickerTouchPopup.
 */
scout.DateField.prototype.getDatePicker = function() {
  return this._popup.getDatePicker();
};

scout.DateField.prototype.preselectDate = function(date, animated) {
  this._popup.ensureOpen();
  this.getDatePicker().preselectDate(date, animated);
};

scout.DateField.prototype.selectDate = function(date, animated) {
  this._popup.ensureOpen();
  this.getDatePicker().selectDate(date, animated);
};

scout.DateField.prototype.shiftSelectedDate = function(years, months, days) {
  this._popup.ensureOpen();
  this.getDatePicker().shiftSelectedDate(years, months, days);
};

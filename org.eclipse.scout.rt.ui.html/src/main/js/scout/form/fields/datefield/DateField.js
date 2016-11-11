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

  this.hasDate = true;
  this.hasTime = false;
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

  this.popup;
  this.oldDisplayText = '';
  this.timestamp = null;
  this._addCloneProperties(['timestamp', 'timestampAsDate']);
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
  this.popup = model.popup;
  this._syncDateFormatPattern(this.dateFormatPattern);
  this._syncTimeFormatPattern(this.timeFormatPattern);
  this._syncTimestamp(this.timestamp);
  this._syncAutoTimestamp(this.autoTimestamp);
  this._syncDisplayText(this.displayText);
  this._syncAllowedDates(this.allowedDates);
};

scout.DateField.prototype.createPopup = function() {
  var popupType = this.touch ? 'DatePickerTouchPopup' : 'DatePickerPopup';
  return scout.create(popupType, {
    parent: this,
    $anchor: this.$field,
    boundToAnchor: !this.touch,
    closeOnAnchorMousedown: false,
    field: this,
    allowedDates: this.allowedDates,
    dateFormat: this.isolatedDateFormat
  });
};

scout.DateField.prototype._render = function($parent) {
  this.addContainer($parent, 'date-field');
  this.addLabel();
  this.addField($parent.makeDiv('date-time-composite'));
  this.addStatus(this.$field);
  if (!this.embedded) {
    this.addMandatoryIndicator();
  }

  this.htmlDateTimeComposite = scout.HtmlComponent.install(this.$field, this.session);
  this.htmlDateTimeComposite.setLayout(new scout.DateTimeCompositeLayout(this));
};

scout.DateField.prototype._renderProperties = function() {
  this._renderHasDate();
  this._renderHasTime();
  this._renderDateFormatPattern();

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
  this.popup = null;
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

    scout.HtmlComponent.install(this.$dateField, this.session);

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
    scout.HtmlComponent.install(this.$timeField, this.session);

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

scout.DateField.prototype._syncDateFormatPattern = function(dateFormatPattern) {
  this._setProperty('dateFormatPattern', dateFormatPattern);
  this.isolatedDateFormat = new scout.DateFormat(this.session.locale, this.dateFormatPattern);
};

scout.DateField.prototype._renderDateFormatPattern = function() {
  if (!this.popup) {
    return;
  }
  this.getDatePicker().dateFormat = this.isolatedDateFormat;
};

scout.DateField.prototype._syncTimeFormatPattern = function(timeFormatPattern) {
  this._setProperty('timeFormatPattern', timeFormatPattern);
  this.isolatedTimeFormat = new scout.DateFormat(this.session.locale, this.timeFormatPattern);
};

/**
 * @override FormField.js
 */
scout.DateField.prototype._renderEnabled = function() {
  scout.DateField.parent.prototype._renderEnabled.call(this);
  this.$container.setEnabled(this.enabledComputed);
  if (this.$dateField) {
    this.$dateField.setEnabled(this.enabledComputed);
  }
  if (this.$timeField) {
    this.$timeField.setEnabled(this.enabledComputed);
  }
};

/**
 * @override ValueField.js
 */
scout.DateField.prototype._renderDisplayText = function() {
  if (this.hasDate) {
    this._renderDateDisplayText();
  }
  if (this.hasTime) {
    this._renderTimeDisplayText();
  }
  this._removePredictionFields();
};

scout.DateField.prototype._renderDateDisplayText = function() {
  scout.fields.valOrText(this, this.$dateField, this.dateDisplayText);
};

scout.DateField.prototype._renderTimeDisplayText = function() {
  scout.fields.valOrText(this, this.$timeField, this.timeDisplayText);
};

scout.DateField.prototype._syncDisplayText = function(displayText) {
  var dateText = '',
    timeText = '',
    parts;

  this.oldDisplayText = this.displayText;
  this._setProperty('displayText', displayText);

  if (scout.strings.hasText(this.displayText)) {
    parts = this.displayText.split('\n');
    dateText = parts[0];
    timeText = parts[1];
  }
  this.dateDisplayText = dateText;
  this.timeDisplayText = timeText;
};

scout.DateField.prototype._syncTimestamp = function(timestamp) {
  // TODO CGU [6.1] no need to keep two timestamp properties, only hold date based type. Convert to back to string when sending. Also rename to value instead of timestamp
  timestamp = scout.dates.ensure(timestamp);
  this.timestampAsDate = timestamp;
};

scout.DateField.prototype._syncAutoTimestamp = function(autoTimestamp) {
  autoTimestamp = scout.dates.ensure(autoTimestamp);
  this.autoTimestampAsDate = autoTimestamp;
};

scout.DateField.prototype._syncAllowedDates = function(allowedDates) {
  if (Array.isArray(allowedDates)) {
    var convAllowedDates = [];
    allowedDates.forEach(function(dateString) {
      convAllowedDates.push(scout.dates.parseJsonDate(dateString));
    });
    this._setProperty('allowedDates', convAllowedDates);
  } else {
    this._setProperty('allowedDates', null);
  }
};

scout.DateField.prototype._renderAllowedDates = function() {
  if (!this.popup) {
    return;
  }
  this.getDatePicker().allowedDates = this.allowedDates;
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
  }

  // Do the same for the time field
  if (this.$timeField) {
    this.$timeField.removeClass(scout.Status.cssClasses);
    this.$timeField.toggleClass(statusClass, hasStatus);
    if (this._$predictTimeField) {
      this._$predictTimeField.removeClass(scout.Status.cssClasses);
      this._$predictTimeField.toggleClass(statusClass, hasStatus);
    }
  }

  if (this.hasDate && this.popup) {
    this.popup.$container.removeClass(scout.Status.cssClasses);
    this.popup.$container.toggleClass(statusClass, hasStatus);
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
    this.openPopupAndSelect(this.timestampAsDate);
  }
};

scout.DateField.prototype._onDateIconClick = function(event) {
  if (scout.fields.handleOnClick(this)) {
    this.$dateField.focus();
    this.openPopupAndSelect(this.timestampAsDate);
  }
};

scout.DateField.prototype._onTimeFieldClick = function(event) {
  if (scout.fields.handleOnClick(this) && this.touch) {
    this.openPopupAndSelect(this.timestampAsDate);
  }
};

scout.DateField.prototype._onTimeIconClick = function(event) {
  if (scout.fields.handleOnClick(this)) {
    this.$timeField.focus();
    if (this.touch) {
      this.openPopupAndSelect(this.timestampAsDate);
    }
  }
};

scout.DateField.prototype._onDateFieldBlur = function() {
  // Close picker and update model
  if (!this.embedded) {
    // in embedded mode we must update the date prediction but not close the popup
    this.closePopup();
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
    if (this.popup || this._$predictDateField) {
      // Close the picker and accept the current prediction (if available)
      this._acceptDateTimePrediction(true, false); // accept date part
      this.closePopup();
      $.suppressEvent(event);
    }
    return;
  }

  if (event.which === scout.keys.ESC) {
    if (this.popup) {
      // Close the picker, but don't do anything else
      this.closePopup();
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
      this.openPopupAndSelect(pickerStartDate);
      this.renderDate(pickerStartDate);
      shiftDate = false; // don't shift if field has no value yet and popup was not open
    } else if (!this.popup) {
      // Otherwise, ensure picker is open
      this.openPopupAndSelect(pickerStartDate);
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
      this.openPopupAndSelect(pickerStartDate);
      this.renderDate(pickerStartDate);
      shiftDate = false; // don't shift if field has no value yet and popup was not open
    } else if (!this.popup) {
      // Otherwise, ensure picker is open
      this.openPopupAndSelect(pickerStartDate);
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
    this.openPopupAndSelect(datePrediction.date);
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
    shiftDate = true;

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
    // Timefield is shown in touch popup, so we need to make sure time gets accepted and popup closed, even if the regular time field itself has no popup
    if (this.popup || this._$predictDateField) {
      // Accept the current prediction (if available)
      this._tempTimeDate = null;
      this._acceptDateTimePrediction(false, true); // accept time part
      this.closePopup();
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

scout.DateField.prototype._onDatePickerDateSelect = function(event) {
  this._setDateValid(true);
  this._setTimeValid(true);
  this.renderDate(event.date);
  if (!event.shifting) {
    this._triggerDisplayTextIfChanged();
    this.updateTimestamp(this._newTimestampAsDate(event.date, this.timestampAsDate));
    // closeDatePicker is set to false when date-field is in touch-mode
    // since popup will be closed by the embedded date-field
    if (scout.nvl(event.closeDatePicker, true)) {
      this.closePopup();
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
 * the resulting string to the $dateField and $timeField, respectively. Prediction fields
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
  this.dateDisplayText = displayText;
  this._updateDisplayTextProperty();
  if (this.rendered) {
    this._renderDateDisplayText();
  }
};

scout.DateField.prototype._setTimeDisplayText = function(displayText) {
  this.timeDisplayText = displayText;
  this._updateDisplayTextProperty();
  if (this.rendered) {
    this._renderTimeDisplayText();
  }
};

scout.DateField.prototype._updateDisplayTextProperty = function() {
  var dateText = this.dateDisplayText || '',
    timeText = this.timeDisplayText || '';

  this.displayText = scout.strings.join('\n', dateText, timeText);
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
    eventOnPopup = this.popup && this.popup.$container.isOrHas(target);

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
  var referenceDate = this.autoTimestampAsDate || scout.dates.trunc(new Date());
  if (this.allowedDates) {
    referenceDate = this._findAllowedReferenceDate(referenceDate);
  }
  return referenceDate;
};

/**
 * Find nearest allowed date which is equals or greater than the current referenceDate.
 */
scout.DateField.prototype._findAllowedReferenceDate = function(referenceDate) {
  var i, allowedDate;
  // 1st: try to find a date which is equals or greater than the referenceDate (today)
  for (i = 0; i < this.allowedDates.length; i++) {
    allowedDate = this.allowedDates[i];
    if (scout.dates.compare(allowedDate, referenceDate) >= 0) {
      return allowedDate;
    }
  }
  // 2nd: try to find an allowed date in the past
  for (i = this.allowedDates.length - 1; i >= 0; i--) {
    allowedDate = this.allowedDates[i];
    if (scout.dates.compare(allowedDate, referenceDate) <= 0) {
      return allowedDate;
    }
  }
  return referenceDate;
};

scout.DateField.prototype.updateTimestamp = function(timestampAsDate, syncToServer) {
  var timestamp = scout.dates.toJsonDate(timestampAsDate, false, this.hasDate, this.hasTime);
  if (timestamp !== this.timestamp || this.errorStatus) {
    this.timestamp = timestamp;
    this.timestampAsDate = timestampAsDate;

    if (!this._hasUiErrorStatus()) {
      this.trigger('timestampChanged', {
        timestamp: this.timestamp
      });
      if (scout.nvl(syncToServer, true)) {
        this._send('timestampChanged', {
          timestamp: this.timestamp
        });
      }
    }
  }
};

scout.DateField.prototype._sendParsingError = function() {
  this._send('parsingError');
};

scout.DateField.prototype._triggerDisplayTextIfChanged = function() {
  if (this.oldDisplayText !== this.displayText) {
    this._triggerDisplayTextChanged(this.displayText);
    this.oldDisplayText = this.displayText;
  }
};

scout.DateField.prototype.openPopup = function(date) {
  if (this.popup) {
    // already open
    return;
  }

  this.popup = this.createPopup();
  this.popup.open();
  this.popup.on('remove', function() {
    this.popup = null;
  }.bind(this));
  this.getDatePicker().on('dateSelect', this._onDatePickerDateSelect.bind(this));
};

scout.DateField.prototype.closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

scout.DateField.prototype._acceptDateTimePrediction = function(acceptDate, acceptTime) {
  var datePrediction, timePrediction,
    dateText = '',
    timeText = '';

  var success = true;
  if (acceptDate) {
    dateText = (this._$predictDateField ? this._$predictDateField.val() : this.$dateField.val());
    datePrediction = this._predictDate(dateText); // this also updates the errorStatus
    if (!datePrediction) {
      success = false;
    }
    this._setDateDisplayText(dateText);
  }
  if (acceptTime) {
    timeText = (this._$predictTimeField ? this._$predictTimeField.val() : this.$timeField.val());
    timePrediction = this._predictTime(timeText); // this also updates the errorStatus
    if (!timePrediction) {
      success = false;
    }
    this._setTimeDisplayText(timeText);
  }
  this._removePredictionFields();

  // Send display text before timestamp, otherwise server would create a display text by its own and send it back
  this._triggerDisplayTextIfChanged();

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
    this._sendParsingError();
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
    this._setDateValid(true);
    return {
      date: now,
      text: inputText
    };
  }

  var analyzeInfo = this.isolatedDateFormat.analyze(inputText, this._referenceDate());
  if (analyzeInfo.error) {
    this._setDateValid(false);
    return null;
  }

  // No predicted date? -> return empty string (may happen if inputText is empty)
  if (!analyzeInfo.predictedDate) {
    this._setDateValid(true);
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

  this._setDateValid(true);
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
    this._setTimeValid(false);
    return null;
  }

  // No predicted date? -> return empty string (may happen if inputText is empty)
  if (!analyzeInfo.predictedDate) {
    this._setTimeValid(true);
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

  this._setTimeValid(true);
  return {
    date: predictedDate,
    text: predictedTimeFormatted
  };
};

/**
 * @returns {scout.Status} error status from UI or error status set by model.
 */
scout.DateField.prototype._errorStatus = function(valid) {
  var errorStatus = this.errorStatus;
  if (valid) {
    // Set to valid
    if (errorStatus) {
      // If no other UI error remains, revert to errorStatus from model
      if (!this._hasUiErrorStatus()) {
        errorStatus = this._modelErrorStatus;
      }
    }
  } else {
    // Set to invalid (this is always a UI error)
    if (!this._hasUiErrorStatus()) {
      errorStatus = new scout.Status({
        message: this.session.text('ui.InvalidDateFormat'),
        severity: scout.Status.Severity.ERROR
      });
    }
  }
  return errorStatus;
};

/**
 * This method updates the UI parts (date, time) of the error status. When both UI parts are valid,
 * we use the error status from the model, which can be null (= no errors).
 */
scout.DateField.prototype._setErrorStatusPart = function(property, valid) {
  var errorStatus = this._errorStatus(valid);
  if (errorStatus) {
    errorStatus[property] = !valid;
    if (!errorStatus.invalidDate && !errorStatus.invalidTime) {
      errorStatus = this._modelErrorStatus;
    }
  }
  // clone error status, to prevent errorStatus and _modelErrorStatus pointing to the same instance
  this.setErrorStatus(scout.Status.clone(errorStatus));
};

scout.DateField.prototype._setDateValid = function(valid) {
  this._setErrorStatusPart('invalidDate', valid);
};

scout.DateField.prototype._setTimeValid = function(valid) {
  this._setErrorStatusPart('invalidTime', valid);
};

scout.DateField.prototype._isErrorStatusPartValid = function(property) {
  if (this.errorStatus && this.errorStatus[property]) {
    return false;
  }
  return true;
};

scout.DateField.prototype._isDateValid = function() {
  return this._isErrorStatusPartValid('invalidDate');
};

scout.DateField.prototype._isTimeValid = function() {
  return this._isErrorStatusPartValid('invalidTime');
};

scout.DateField.prototype._hasUiErrorStatus = function() {
  return !!(this.errorStatus && (this.errorStatus.invalidDate || this.errorStatus.invalidTime));
};

/**
 * Method invoked if being rendered within a cell-editor (mode='scout.FormField.MODE_CELLEDITOR'), and once the editor finished its rendering.
 */
scout.DateField.prototype.onCellEditorRendered = function(options) {
  if (options.openFieldPopup && this.hasDate) {
    this.openPopupAndSelect(this.timestampAsDate);
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
  return this.popup.getDatePicker();
};

/**
 * Opens picker and selects date
 *
 * @param date
 *          optional, Date to pass to the date picker. If no date is specified, the reference date
 *          is preselected (not selected!).
 */
scout.DateField.prototype.openPopupAndSelect = function(date) {
  if (!date) {
    this.preselectDate(this._referenceDate());
  } else {
    this.selectDate(date);
  }
};

scout.DateField.prototype.preselectDate = function(date, animated) {
  this.openPopup();
  this.getDatePicker().preselectDate(date, animated);
};

scout.DateField.prototype.selectDate = function(date, animated) {
  this.openPopup();
  this.getDatePicker().selectDate(date, animated);
};

scout.DateField.prototype.shiftSelectedDate = function(years, months, days) {
  this.openPopup();
  this.getDatePicker().shiftSelectedDate(years, months, days);
};

// TODO awe, cgu: (value-field): we should refactor this setTimestamp / formatTimestamp
// as soon as we have implemented parse/format/validation logic on ValueField.js. This
// should work like the Java client (AbstractValueField) does. Also rename timestamp to value
scout.DateField.prototype.setTimestamp = function(timestamp) {
  if (this.timestamp === timestamp) {
    return;
  }
  this._setProperty('timestamp', timestamp);
  this.timestampAsDate = scout.dates.parseJsonDate(timestamp);
  this.formatTimestamp(this.timestampAsDate);
  if (this.rendered) {
    this._renderDisplayText();
  }
};

scout.DateField.prototype.formatTimestamp = function(timestamp) {
  var
    dateText = '',
    timeText = '';

  if (timestamp) {
    if (this.hasDate) {
      dateText = this.isolatedDateFormat.format(timestamp);
    }
    if (this.hasTime) {
      timeText = this.isolatedTimeFormat.format(timestamp);
    }
  }

  this.dateDisplayText = dateText;
  this.timeDisplayText = timeText;
  this._updateDisplayTextProperty();
};

scout.DateField.prototype._renderDisabledStyle = function() {
  this._renderDisabledStyleInternal(this.$dateField);
  this._renderDisabledStyleInternal(this.$timeField);
  this._renderDisabledStyleInternal(this.$mandatory);
};

/**
 * @override
 */
scout.DateField.prototype._updateEmpty = function() {
  // TODO [6.1] CGU remove this method as soon as timestamp is renamed to value. Then the default implementation of value field may be used
  this.empty = this.timestamp === null || this.timestamp === undefined;
};

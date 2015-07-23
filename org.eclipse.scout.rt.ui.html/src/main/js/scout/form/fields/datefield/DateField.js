scout.DateField = function() {
  scout.DateField.parent.call(this);
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
};
scout.inherits(scout.DateField, scout.ValueField);

scout.DateField.prototype._init = function(model, session) {
  scout.DateField.parent.prototype._init.call(this, model, session);
  this._syncErrorStatus(this.errorStatus);
};

scout.DateField.prototype._createKeyStrokeAdapter = function() {
  return new scout.DateFieldKeyStrokeAdapter(this);
};

scout.DateField.prototype._syncErrorStatus = function(errorStatus) {
  this.errorStatus = errorStatus;
  this._modelErrorStatus = errorStatus;
};

scout.DateField.prototype._render = function($parent) {
  this.addContainer($parent, 'date-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField($.makeDiv('date-time-composite'));
  this.addStatus(this.$field);

  this.htmlDateTimeComposite = new scout.HtmlComponent(this.$field, this.session);
  this.htmlDateTimeComposite.setLayout(new scout.DateFieldLayout(this));

  // Create date picker popup
  this._datePickerPopup = new scout.DatePickerPopup(this.session, {
    $anchor: this.$field
  });
  this.addChild(this._datePickerPopup);
  this._datePickerPopup.picker
    .on('dateSelect', this._onDatePickerDateSelected.bind(this));
  if (this.hasDate && this.cellEditor && this.cellEditor.openFieldPopupOnCellEdit) {
    this._openDatePicker();
  }
};

scout.DateField.prototype._renderProperties = function() {
  this._renderHasDate();
  this._renderHasTime();
  this._renderDateFormatPattern();
  this._renderTimeFormatPattern();
  this._renderTimestamp();

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
};

scout.DateField.prototype._renderHasDate = function() {
  // Add $dateField
  if (this.hasDate && !this.$dateField) {
    this.$dateField = scout.fields.new$TextField()
      .addClass('date')
      .on('keydown', this._onDateFieldKeydown.bind(this))
      .on('mousedown', this._onDateFieldClick.bind(this))
      .on('blur', this._onDateFieldBlur.bind(this))
      .appendTo(this.$field);
    new scout.HtmlComponent(this.$dateField, this.session);

    this.$dateFieldIcon = scout.fields.new$Icon()
      .addClass('date')
      .on('mousedown', this._onDateIconClick.bind(this))
      .appendTo(this.$field);

    this.invalidateLayout();

    // Remove $dateField
  } else if (!this.hasDate && this.$dateField) {
    this.$dateField.remove();
    this.$dateField = null;
    this.$dateFieldIcon.remove();
    this.$dateFieldIcon = null;

    this.invalidateLayout();
  }
};

scout.DateField.prototype._renderHasTime = function() {
  // Add $timeField
  if (this.hasTime && !this.$timeField) {
    this.$timeField = scout.fields.new$TextField()
      .addClass('time')
      .on('keydown', this._onTimeFieldKeydown.bind(this))
      .on('blur', this._onTimeFieldBlur.bind(this))
      .appendTo(this.$field);
    this.$timeFieldIcon = scout.fields.new$Icon()
      .addClass('time')
      .on('mousedown', this._onTimeIconClick.bind(this))
      .appendTo(this.$field);
    new scout.HtmlComponent(this.$timeField, this.session);

    this.invalidateLayout();

    // Remove $timeField
  } else if (!this.hasTime && this.$timeField) {
    this.$timeField.remove();
    this.$timeField = null;
    this.$timeFieldIcon.remove();
    this.$timeFieldIcon = null;

    this.invalidateLayout();
  }
};

scout.DateField.prototype._renderDateFormatPattern = function() {
  this.isolatedDateFormat = new scout.DateFormat(this.session.locale, this.dateFormatPattern);
  this._datePickerPopup.picker.dateFormat = this.isolatedDateFormat;
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
 * @Override FormField.js
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
 * @Override
 */
scout.DateField.prototype._renderDisplayText = function(text) {
  //nop -> handled in _renderTimestamp
};

scout.DateField.prototype._renderTimestamp = function() {
  this.timestampAsDate = scout.dates.parseJsonDate(this.timestamp);
  this.updateDisplayText(this.timestampAsDate);
};

/**
 * @override FormField.js
 */
scout.DateField.prototype._renderErrorStatus = function() {
  scout.DateField.parent.prototype._renderErrorStatus.call(this);
  var hasError = !!(this.errorStatus) ;

  if (this.$dateField) {
    this.$dateField.toggleClass('has-error', hasError);
    // Because the error color of field icons depends on the error status of sibling <input> elements.
    // The prediction fields are clones of the input fields, so the 'has-error' class has to be
    // removed from them as well to make the icon "valid".
    if (this._$predictDateField) {
      this._$predictDateField.toggleClass('has-error', hasError);
    }
    // Put invalid input in the field, if the current input differs
    // (don't do it always, this would alter the cursor position)
    if (!this._isDateValid() && this.$dateField.val() !== this.errorStatus.invalidDateText) {
      this.$dateField.val(this.errorStatus.invalidDateText);
    }
  }

  // Do the same for the time field
  if (this.$timeField) {
    this.$timeField.toggleClass('has-error', hasError);
    if (this._$predictTimeField) {
      this._$predictTimeField.toggleClass('has-error', hasError);
    }
    if (!this._isTimeValid() && this.$timeField.val() !== this.errorStatus.invalidTimeText) {
      this.$timeField.val(this.errorStatus.invalidTimeText);
    }
  }

  if (this.hasDate && this._datePickerPopup.$container) {
    this._datePickerPopup.$container.toggleClass('has-error', hasError);
  }
};

scout.DateField.prototype._onDateFieldClick = function() {
  this._openDatePicker();
};

scout.DateField.prototype._onDateIconClick = function(event) {
  this.$dateField.focus();
  this._openDatePicker();
};

scout.DateField.prototype._onTimeIconClick = function(event) {
  this.$timeField.focus();
};

scout.DateField.prototype._onDateFieldBlur = function() {
  // Close picker and update model
  this._closeDatePicker();
  this._acceptDatePrediction();
};

scout.DateField.prototype._onTimeFieldBlur = function() {
  this._tempTimeDate = null;
  this._acceptTimePrediction();
};

scout.DateField.prototype._onDateFieldKeydown = function(event) {
  var delta = 0,
    diffYears = 0,
    diffMonths = 0,
    diffDays = 0,
    cursorPos = this.$dateField[0].selectionStart,
    displayText = this.$dateField.val(),
    prediction = this._$predictDateField && this._$predictDateField.val(),
    modifierCount = (event.ctrlKey ? 1 : 0) + (event.shiftKey ? 1 : 0) + (event.altKey ? 1 : 0) + (event.metaKey ? 1 : 0),
    pickerStartDate = null,
    shiftDate = true;

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
    if (this._datePickerPopup.isOpen() || this._$predictDateField) {
      // Close the picker and accept the current prediction (if available)
      this._closeDatePicker();
      this._acceptDatePrediction();
      $.suppressEvent(event);
    }
    return;
  }

  if (event.which === scout.keys.ESC) {
    if (this._datePickerPopup.isOpen()) {
      // Close the picker, but don't do anything else
      this._closeDatePicker();
      $.suppressEvent(event);
    }
    return;
  }

  if (event.which === scout.keys.RIGHT && cursorPos === displayText.length) {
    // Move cursor one right and apply next char of the prediction
    if (prediction) {
      this.$dateField.val(prediction.substring(0, displayText.length + 1));
    }
    return;
  }

  if (event.which === scout.keys.PAGE_UP || event.which === scout.keys.PAGE_DOWN) {
    if (!this._datePickerPopup.isOpen()) {
      if (!displayText || !this.timestamp) {
        pickerStartDate = new Date();
        this.updateDisplayText(pickerStartDate);
        shiftDate = false; // don't shift if field has no value yet and popup was not open
      }
      this._openDatePicker(pickerStartDate);
    }
    if (shiftDate) {
      diffMonths = (event.which === scout.keys.PAGE_UP ? -1 : 1);
      this._datePickerPopup.shiftSelectedDate(0, diffMonths, 0);
    }
    $.suppressEvent(event);
    return;
  }

  if (event.which === scout.keys.UP || event.which === scout.keys.DOWN) {
    delta = (event.which === scout.keys.UP ? -1 : 1);
    if (event.ctrlKey && modifierCount === 1) { // only ctrl
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

    if (!this._datePickerPopup.isOpen()) {
      if (!displayText || !this.timestamp) {
        pickerStartDate = new Date();
        this.updateDisplayText(pickerStartDate);
        shiftDate = false; // don't shift if field has no value yet and popup was not open
      }
      this._openDatePicker(pickerStartDate);
    }
    if (shiftDate) {
      this._datePickerPopup.shiftSelectedDate(diffYears, diffMonths, diffDays);
    }
    $.suppressEvent(event);
    return;
  }

  // All other keys are treated as text input, but only if shift is pressed or no modifier at all.
  if (modifierCount > 1 || (modifierCount === 1 && !event.shiftKey)) {
    return;
  }

  // Remember the old displayText (which is not changed yet in the 'keydown' event) and then use setTimeout()
  // to check and handle the new (altered) displayText. We don't use the keyUp event because it feels slow.
  var oldDisplayText = this.$dateField.val();
  setTimeout(function(event) {
    var displayText = this.$dateField.val();

    // If key did not alter the displayText (e.g. when an F key was pressed) or the focus has changed to another
    // field in the meantime, don't predict anything and don't show the picker. Just validate the input.
    if (oldDisplayText === displayText || this.$dateField[0] !== document.activeElement) {
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
  }.bind(this));
};

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
      this._acceptTimePrediction();
      $.suppressEvent(event);
    }
    return;
  }

  if (event.which === scout.keys.RIGHT && cursorPos === displayText.length) {
    // Move cursor one right and apply next char of the prediction
    if (prediction) {
      this.$timeField.val(prediction.substring(0, displayText.length + 1));
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
        this._tempTimeDate = this.timestampAsDate || new Date();
        shiftDate = false;
      }
    }
    if (shiftDate) {
      this._tempTimeDate = scout.dates.combineDateTime(
        this._tempTimeDate,
        scout.dates.shiftTime(this._tempTimeDate, diffHours, diffMinutes, diffSeconds));
    }
    this.updateDisplayText(this._tempTimeDate);
    $.suppressEvent(event);
    return;
  }

  // All other keys are treated as text input, but only if shift is pressed or no modifier at all.
  if (modifierCount > 1 || (modifierCount === 1 && !event.shiftKey)) {
    return;
  }

  // Remember the old displayText (which is not changed yet in the 'keydown' event) and then use setTimeout()
  // to check and handle the new (altered) displayText. We don't use the keyUp event because it feels slow.
  var oldDisplayText = this.$timeField.val();
  setTimeout(function(event) {
    var displayText = this.$timeField.val();

    // If key did not alter the displayText (e.g. when an F key was pressed) or the focus has changed to another
    // field in the meantime, don't predict anything and don't show the picker. Just validate the input.
    if (oldDisplayText === displayText || this.$timeField[0] !== document.activeElement) {
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
  }.bind(this));
};

scout.DateField.prototype._onDatePickerDateSelected = function(event) {
  this._setDateValid(true);
  this._setTimeValid(true);
  this.updateDisplayText(event.date);
  if (!event.shifting) {
    // close popup and write to model
    this._closeDatePicker();
    this.updateTimestamp(this._newTimestampAsDate(event.date, this.timestampAsDate));
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

scout.DateField.prototype.updateDisplayText = function(date) {
  date = date || this.timestampAsDate;

  // Only update displayTexts when value is invalid. Otherwise, we would
  // override the invalid display text from the model. Also note that we
  // have to backup and restore the selection, otherwise, the cursor would
  // move to the end after every key press.
  var selection;
  if (this.hasDate && this._isDateValid()) {
    selection = this.$dateField.backupSelection();
    this.$dateField.val(this.isolatedDateFormat.format(date));
    this.$dateField.restoreSelection(selection);
  }
  if (this.hasTime && this._isTimeValid()) {
    selection = this.$timeField.backupSelection();
    this.$timeField.val(this.isolatedTimeFormat.format(date));
    this.$timeField.restoreSelection(selection);
  }
  // Make sure there is no invisible and wrong prediction
  this._removePredictionFields();
};

/**
 * @override ValueField.js
 */
scout.DateField.prototype.displayTextChanged = function(whileTyping, forceSend) {
  var newTimestampAsDate = null;
  if (this.hasTime && this.hasDate) {
    newTimestampAsDate = scout.dates.combineDateTime(
      this.isolatedDateFormat.parse(this.$dateField.val()),
      this.isolatedTimeFormat.parse(this.$timeField.val())
    );
  } else if (this.hasDate) {
    newTimestampAsDate = this.isolatedDateFormat.parse(this.$dateField.val());
  } else if (this.hasTime) {
    newTimestampAsDate = this.isolatedTimeFormat.parse(this.$timeField.val());
  }
  this.updateTimestamp(newTimestampAsDate);
};

/**
 * Returns null if both arguments are not set. Otherwise, this.timestampAsDate or the current date
 * is used as basis and the given arguments are applied to that date. The result is returned.
 */
scout.DateField.prototype._newTimestampAsDate = function(date, time) {
  var result = null;
  if (date || time) {
    result = this.timestampAsDate || new Date();
    if (date) {
      result = scout.dates.combineDateTime(date, result);
    }
    if (time) {
      result = scout.dates.combineDateTime(result, time);
    }
  }
  return result;
};

scout.DateField.prototype.updateTimestamp = function(timestampAsDate) {
  var timestamp = scout.dates.toJsonDate(timestampAsDate, false, this.hasDate, this.hasTime);
  if (timestamp !== this.timestamp || this.errorStatus) {
    this.timestamp = timestamp;
    this.timestampAsDate = timestampAsDate;
    this._syncToServer();
  }
};

scout.DateField.prototype._syncToServer = function() {
  if (this._hasUiErrorStatus()) {
    this.session.send(this.id, 'parsingError', {
      invalidDisplayText: this.errorStatus.invalidDisplayText,
      invalidDateText: this.errorStatus.invalidDateText,
      invalidTimeText: this.errorStatus.invalidTimeText
    });
  } else {
    this.session.send(this.id, 'timestampChanged', {
      timestamp: this.timestamp
    });
  }
};

/**
 * Opens picker and selects date
 *
 * @param date
 *          optional, Date to pass to the date picker
 */
scout.DateField.prototype._openDatePicker = function(date) {
  date = date || this.timestampAsDate;
  this._datePickerPopup.selectDate(date);
};

scout.DateField.prototype._closeDatePicker = function() {
  if (this._datePickerPopup.isOpen()) {
    this._datePickerPopup.close();
  }
};

scout.DateField.prototype._acceptDatePrediction = function() {
  var text = (this._$predictDateField ? this._$predictDateField.val() : this.$dateField.val());
  this._removePredictionFields();

  var datePrediction = this._predictDate(text);
  if (datePrediction) {
    // parse success -> send new timestamp to server
    this.updateTimestamp(this._newTimestampAsDate(datePrediction.date, null));
    this.updateDisplayText();
  } else {
    // parse error -> send error to server
    this._syncToServer();
  }
};

scout.DateField.prototype._acceptTimePrediction = function() {
  var text = (this._$predictTimeField ? this._$predictTimeField.val() : this.$timeField.val());
  this._removePredictionFields();

  var timePrediction = this._predictTime(text);
  if (timePrediction) {
    // parse success -> send new timestamp to server
    this.updateTimestamp(this._newTimestampAsDate(null, timePrediction.date));
    this.updateDisplayText();
  } else {
    // parse error -> send error to server
    this._syncToServer();
  }
};

scout.DateField.prototype._predictDate = function(inputText) {
  inputText = inputText || '';

  // "Date calculations"
  var m = inputText.match(/^([+-])(\d*)$/);
  if (m) {
    var now = new Date();
    var daysToAdd = Number(m[1] + (m[2] || '0'));
    now.setDate(now.getDate() + daysToAdd);
    return {
      date: now,
      text: inputText
    };
  }

  var analyzeInfo = this.isolatedDateFormat.analyze(inputText, this.timestampAsDate);
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

scout.DateField.prototype._predictTime = function(inputText) {
  inputText = inputText || '';

  var analyzeInfo = this.isolatedTimeFormat.analyze(inputText, this.timestampAsDate);
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
      errorStatus = {
          message: this.session.text('ui.InvalidDateFormat')
      };
    }
    errorStatus.invalidDateText = dateText;
    errorStatus.invalidDisplayText = scout.strings.join(" ",
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
      errorStatus = {
          message: this.session.text('ui.InvalidDateFormat')
      };
    }
    errorStatus.invalidTimeText = timeText;
    errorStatus.invalidDisplayText = scout.strings.join(" ",
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

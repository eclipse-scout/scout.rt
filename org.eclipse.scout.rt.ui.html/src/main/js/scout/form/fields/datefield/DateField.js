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

  this.popup;
  this.autoDate;
  this.dateDisplayText = null;
  this.dateHasText = false;
  this.dateFocused = false;
  this.dateFormatPattern;
  this.disabledCopyOverlay = true;
  this.hasDate = true;
  this.oldDisplayText = null;

  this.hasTime = false;
  this.hasTimePopup = true;
  this.timeDisplayText = null;
  this.timeHasText = false;
  this.timePickerResolution;
  this.timeFormatPattern;
  this.timeFocused = false;

  this.$dateField;
  this.$timeField;
  this.$dateFieldIcon;
  this.$timeFieldIcon;
  this.$dateClearIcon;
  this.$timeClearIcon;
  this._$predictDateField;
  this._$predictTimeField;

  // This is the storage for the time (as date) while the focus in the field (e.g. when
  // pressing up/down). In date fields, the date picker is used for that purposes.
  this._tempTimeDate;
  this.invalidValueMessageKey = 'ui.InvalidDate';
  this._addCloneProperties(['hasDate', 'hasTime', 'dateFormatPattern', 'timeFormatPattern', 'allowedDates', 'autoDate']);
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
  this._setAutoDate(this.autoDate);
  this._setDisplayText(this.displayText);
  this._setAllowedDates(this.allowedDates);
  this._setTimePickerResolution(this.timePickerResolution || 30);
};

/**
 * Initializes the date format before calling set value.
 * This cannot be done in _init because the value field would call _setValue first
 */
scout.DateField.prototype._initValue = function(value) {
  this._setDateFormatPattern(this.dateFormatPattern);
  this._setTimeFormatPattern(this.timeFormatPattern);
  scout.DateField.parent.prototype._initValue.call(this, value);
};

scout.DateField.prototype.createDatePopup = function() {
  var popupType = this.touch ? 'DatePickerTouchPopup' : 'DatePickerPopup';
  return scout.create(popupType, {
    parent: this,
    $anchor: this.$field,
    boundToAnchor: !this.touch,
    cssClass: this._errorStatusClass(),
    closeOnAnchorMouseDown: false,
    field: this,
    allowedDates: this.allowedDates,
    dateFormat: this.isolatedDateFormat,
    displayText: this.dateDisplayText
  });
};

scout.DateField.prototype.createTimePopup = function() {
  var popupType = this.touch ? 'TimePickerTouchPopup' : 'TimePickerPopup';
  return scout.create(popupType, {
    parent: this,
    $anchor: this.$timeField,
    boundToAnchor: !this.touch,
    cssClass: this._errorStatusClass(),
    closeOnAnchorMouseDown: false,
    field: this,
    timeResolution: this.timePickerResolution
  });
};

scout.DateField.prototype._render = function() {
  this.addContainer(this.$parent, 'date-field');
  this.addLabel();
  this.addField(this.$parent.makeDiv('date-time-composite'));
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

  // Has to be the last call, otherwise _renderErrorStatus() would operate on the wrong state.
  scout.DateField.parent.prototype._renderProperties.call(this);

  this._renderDateHasText();
  this._renderTimeHasText();
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

scout.DateField.prototype.setHasDate = function(hasDate) {
  this.setProperty('hasDate', hasDate);
};

scout.DateField.prototype._setHasDate = function(hasDate) {
  this._setProperty('hasDate', hasDate);
  if (this.initialized) {
    // if property changes on the fly, update the display text
    this._updateDisplayTextProperty();
  }
};

scout.DateField.prototype._renderHasDate = function() {
  if (this.hasDate && !this.$dateField) {
    // Add $dateField
    this.$dateField = scout.fields.makeInputOrDiv(this, 'date')
      .on('mousedown', this._onDateFieldMouseDown.bind(this))
      .appendTo(this.$field);
    if (this.$timeField) {
      // make sure date field comes before time field, otherwise tab won't work as expected
      this.$dateField.insertBefore(this.$timeField);
    }
    if (!this.touch) {
      this.$dateField
        .on('keydown', this._onDateFieldKeyDown.bind(this))
        .on('input', this._onDateFieldInput.bind(this))
        .on('blur', this._onDateFieldBlur.bind(this))
        .on('focus', this._onDateFieldFocus.bind(this));
    }

    scout.HtmlComponent.install(this.$dateField, this.session);

    this.$dateFieldIcon = scout.fields.appendIcon(this.$field, 'date')
      .on('mousedown', this._onDateIconMouseDown.bind(this));
    // avoid fastclick on icon. Otherwise the blur event overtakes the mousedown event.
    this.$dateFieldIcon.addClass('needsclick');

  } else if (!this.hasDate && this.$dateField) {
    // Remove $dateField
    this.$dateField.remove();
    this.$dateField = null;
    this.$dateFieldIcon.remove();
    this.$dateFieldIcon = null;
  }

  if (!this.rendering) {
    this._renderDisplayText();
    this.htmlDateTimeComposite.invalidateLayoutTree();
  }
  this._renderDateClearable();
};

scout.DateField.prototype.setHasTime = function(hasTime) {
  this.setProperty('hasTime', hasTime);
};

scout.DateField.prototype._setHasTime = function(hasTime) {
  this._setProperty('hasTime', hasTime);
  if (this.initialized) {
    // if property changes on the fly, update the display text
    this._updateDisplayTextProperty();
  }
};

scout.DateField.prototype._renderHasTime = function() {
  if (this.hasTime && !this.$timeField) {
    // Add $timeField
    this.$timeField = scout.fields.makeInputOrDiv(this, 'time')
      .on('mousedown', this._onTimeFieldMouseDown.bind(this))
      .appendTo(this.$field);
    if (this.$dateField) {
      // make sure time field comes after date field, otherwise tab won't work as expected
      this.$timeField.insertAfter(this.$dateField);
    }
    if (!this.touch || !this.hasTimePopup) {
      this.$timeField
        .on('keydown', this._onTimeFieldKeyDown.bind(this))
        .on('input', this._onTimeFieldInput.bind(this))
        .on('blur', this._onTimeFieldBlur.bind(this))
        .on('focus', this._onTimeFieldFocus.bind(this));
    }

    scout.HtmlComponent.install(this.$timeField, this.session);

    this.$timeFieldIcon = scout.fields.appendIcon(this.$field, 'time')
      .on('mousedown', this._onTimeIconMouseDown.bind(this));
    // avoid fastclick on icon. Otherwise the blur event overtakes the mousedown event.
    this.$timeFieldIcon.addClass('needsclick');
    
  } else if (!this.hasTime && this.$timeField) {
    // Remove $timeField
    this.$timeField.remove();
    this.$timeField = null;
    this.$timeFieldIcon.remove();
    this.$timeFieldIcon = null;
  }

  if (!this.rendering) {
    this._renderDisplayText();
    this.htmlDateTimeComposite.invalidateLayoutTree();
  }
  this._renderTimeClearable();
};

scout.DateField.prototype.setTimePickerResolution = function(timePickerResolution) {
  this.setProperty('timePickerResolution', timePickerResolution);
};

scout.DateField.prototype._setTimePickerResolution = function(timePickerResolution) {
  if (timePickerResolution < 1) {
    // default
    timePickerResolution = 10;
    this.hasTimePopup = false;
  } else {
    this.hasTimePopup = true;
  }
  this._setProperty('timePickerResolution', timePickerResolution);
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

scout.DateField.prototype.setDateFormatPattern = function(dateFormatPattern) {
  this.setProperty('dateFormatPattern', dateFormatPattern);
};

scout.DateField.prototype._setDateFormatPattern = function(dateFormatPattern) {
  if (!dateFormatPattern) {
    dateFormatPattern = this.session.locale.dateFormatPatternDefault;
  }
  this._setProperty('dateFormatPattern', dateFormatPattern);
  this.isolatedDateFormat = new scout.DateFormat(this.session.locale, this.dateFormatPattern);

  if (this.initialized) {
    // if format changes on the fly, just update the display text
    this._updateDisplayText();
  }
};

scout.DateField.prototype.setTimeFormatPattern = function(timeFormatPattern) {
  this.setProperty('timeFormatPattern', timeFormatPattern);
};

scout.DateField.prototype._setTimeFormatPattern = function(timeFormatPattern) {
  if (!timeFormatPattern) {
    timeFormatPattern = this.session.locale.timeFormatPatternDefault;
  }
  this._setProperty('timeFormatPattern', timeFormatPattern);
  this.isolatedTimeFormat = new scout.DateFormat(this.session.locale, this.timeFormatPattern);

  if (this.initialized) {
    // if format changes on the fly, just update the display text
    this._updateDisplayText();
  }
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

scout.DateField.prototype._readDisplayText = function() {
  var dateDisplayText, timeDisplayText;
  if (this.hasDate) {
    dateDisplayText = this._readDateDisplayText();
  }
  if (this.hasTime) {
    timeDisplayText = this._readTimeDisplayText();
  }
  return this._computeDisplayText(dateDisplayText, timeDisplayText);
};

scout.DateField.prototype._renderDateDisplayText = function() {
  scout.fields.valOrText(this.$dateField, this.dateDisplayText);
  this._updateDateHasText();
};

scout.DateField.prototype._readDateDisplayText = function() {
  return (this._$predictDateField ? scout.fields.valOrText(this._$predictDateField) : scout.fields.valOrText(this.$dateField));
};

scout.DateField.prototype._renderTimeDisplayText = function() {
  scout.fields.valOrText(this.$timeField, this.timeDisplayText);
  this._updateTimeHasText();
};

scout.DateField.prototype._readTimeDisplayText = function() {
  return (this._$predictTimeField ? scout.fields.valOrText(this._$predictTimeField) : scout.fields.valOrText(this.$timeField));
};

/**
 * @override
 */
scout.DateField.prototype.setDisplayText = function(displayText) {
  // Overridden to avoid the equals check -> make sure renderDisplayText is executed whenever setDisplayText is called
  // Reason: key up/down and picker day click modify the display text, but input doesn't
  // -> reverting to a date using day click or up down after the input changed would not work anymore
  // changing 'onXyInput' to always update the display text would fix that, but would break acceptInput
  this._setDisplayText(displayText);
  if (this.rendered) {
    this._renderDisplayText();
  }
};

scout.DateField.prototype._setDisplayText = function(displayText) {
  this.oldDisplayText = this.displayText;
  this._setProperty('displayText', displayText);

  var parts = this._splitDisplayText(displayText);
  if (this.hasDate) {
    // preserve dateDisplayText if hasDate is set to false (only override if it is true)
    this.dateDisplayText = parts.dateText;
  }
  if (this.hasTime) {
    // preserve timeDisplayText if hasTime is set to false (only override if it is true)
    this.timeDisplayText = parts.timeText;
  }
};

/**
 * @override
 */
scout.DateField.prototype._ensureValue = function(value) {
  return scout.dates.ensure(value);
};

/**
 * @param {Date} the date to validate
 * @return {Date} the validated date
 * @override
 */
scout.DateField.prototype._validateValue = function(value) {
  if (scout.objects.isNullOrUndefined(value)) {
    return value;
  }
  if (!(value instanceof Date)) {
    throw this.session.text(this.invalidValueMessageKey);
  }
  if (!this.hasDate && !this.value) {
    // truncate to 01.01.1970 if no date was entered before. Otherwise preserve date part (important for toggling hasDate on the fly)
    value = scout.dates.combineDateTime(null, value);
  }
  return value;
};

scout.DateField.prototype.setAutoDate = function(autoDate) {
  this.setProperty('autoDate', autoDate);
};

scout.DateField.prototype._setAutoDate = function(autoDate) {
  autoDate = scout.dates.ensure(autoDate);
  this._setProperty('autoDate', autoDate);
};

scout.DateField.prototype._setAllowedDates = function(allowedDates) {
  if (Array.isArray(allowedDates)) {
    allowedDates = allowedDates.map(function(date) {
      return scout.dates.ensure(date);
    });
    this._setProperty('allowedDates', allowedDates);
  } else {
    this._setProperty('allowedDates', null);
  }
};

/**
 * @override FormField.js
 */
scout.DateField.prototype._renderErrorStatus = function() {
  scout.DateField.parent.prototype._renderErrorStatus.call(this);
  var hasStatus = !!this.errorStatus,
    statusClass = this._errorStatusClass();

  if (this.$dateField) {
    this.$dateField.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
    this.$dateField.toggleClass(statusClass, hasStatus);

    // Because the error color of field icons depends on the error status of sibling <input> elements.
    // The prediction fields are clones of the input fields, so the 'has-error' class has to be
    // removed from them as well to make the icon "valid".
    if (this._$predictDateField) {
      this._$predictDateField.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
      this._$predictDateField.toggleClass(statusClass, hasStatus);
    }
  }

  // Do the same for the time field
  if (this.$timeField) {
    this.$timeField.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
    this.$timeField.toggleClass(statusClass, hasStatus);
    if (this._$predictTimeField) {
      this._$predictTimeField.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
      this._$predictTimeField.toggleClass(statusClass, hasStatus);
    }
  }

  if (this.popup) {
    this.popup.$container.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
    this.popup.$container.toggleClass(statusClass, hasStatus);
  }
};

scout.DateField.prototype._errorStatusClass = function() {
  return !!this.errorStatus ? 'has-' + this.errorStatus.cssClass() : '';
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

scout.DateField.prototype._onDateFieldMouseDown = function() {
  if (scout.fields.handleOnClick(this)) {
    this.openDatePopupAndSelect(this.value);
  }
};

scout.DateField.prototype._onTimeFieldMouseDown = function() {
  if (scout.fields.handleOnClick(this)) {
    this.openTimePopupAndSelect(this.value);
  }
};

scout.DateField.prototype.setDateFocused = function(dateFocused) {
  this.setProperty('dateFocused', dateFocused);
};

scout.DateField.prototype._renderDateFocused = function() {
  this.$container.toggleClass('date-focused', this.dateFocused);
};

scout.DateField.prototype._updateTimeHasText = function() {
  this.setTimeHasText(scout.strings.hasText(this._readTimeDisplayText()));
};

scout.DateField.prototype.setTimeHasText = function(timeHasText) {
  this.setProperty('timeHasText', timeHasText);
};

scout.DateField.prototype._renderTimeHasText = function() {
  if (this.$timeField) {
    this.$timeField.toggleClass('has-text', this.timeHasText);
  }
  this.$container.toggleClass('time-has-text', this.timeHasText);
};

scout.DateField.prototype._updateDateHasText = function() {
  this.setDateHasText(scout.strings.hasText(this._readDateDisplayText()));
};

scout.DateField.prototype.setDateHasText = function(dateHasText) {
  this.setProperty('dateHasText', dateHasText);
};

scout.DateField.prototype._renderDateHasText = function() {
  if (this.$dateField) {
    this.$dateField.toggleClass('has-text', this.dateHasText);
  }
  this.$container.toggleClass('date-has-text', this.dateHasText);
};

/**
 * @override
 */
scout.DateField.prototype.clear = function() {
  if (!(this.hasDate && this.hasTime)) {
    scout.DateField.parent.prototype.clear.call(this);
    return;
  }
  this._clear();
  // If field shows date and time, don't accept input while one field has the focus
  // Reason: x icon is shown in one field, pressing that icon should clear the content of that field.
  // Accept input would set the value to '', thus clearing both fields which may be unexpected.
  if (!this.dateFocused && !this.timeFocused) {
    this.acceptInput();
  }
  this._triggerClear();
};

scout.DateField.prototype._clear = function() {
  this._removePredictionFields();
  if (this.hasDate && !this.timeFocused) {
    this.$dateField.val('');
    this._setDateValid(true);
    this._updateDateHasText();
  }
  if (this.hasTime && !this.dateFocused) {
    this.$timeField.val('');
    this._setTimeValid(true);
    this._updateTimeHasText();
  }
};

scout.DateField.prototype._onDateClearIconMouseDown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.$dateField.focus();
  this.clear();
  if (this.value) {
    this.selectDate(this.value, false);
  } else {
    this.preselectDate(this._referenceDate(), false);
  }
  event.preventDefault();
};

scout.DateField.prototype._onDateIconMouseDown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.$dateField.focus();
  if (!this.embedded) {
    this.openDatePopupAndSelect(this.value);
  }
};

scout.DateField.prototype.setTimeFocused = function(timeFocused) {
  this.setProperty('timeFocused', timeFocused);
};

scout.DateField.prototype._renderTimeFocused = function() {
  this.$container.toggleClass('time-focused', this.timeFocused);
};

scout.DateField.prototype._renderClearable = function() {
  this._renderDateClearable();
  this._renderTimeClearable();
  this._updateClearableStyles();
};

scout.DateField.prototype._renderDateClearable = function() {
  if (this.hasDate || this.isClearable()) {
    if (!this.$dateClearIcon) {
      // date clear icon
      this.$dateClearIcon = this.$field.appendSpan('icon date-clear unfocusable')
        .on('mousedown', this._onDateClearIconMouseDown.bind(this));
      // avoid fastclick on icon. Otherwise the blur event overtakes the mousedown event.
      this.$dateClearIcon.addClass('needsclick');
    }
  } else {
    if (this.$dateClearIcon) {
      // Remove clear icon
      this.$dateClearIcon.remove();
      this.$dateClearIcon = null;
    }
  }
};

scout.DateField.prototype._renderTimeClearable = function() {
  if (this.hasTime && this.isClearable() && !this.$timeClearIcon) {
    // date clear icon
    this.$timeClearIcon = this.$field.appendSpan('icon time-clear unfocusable')
      .on('mousedown', this._onTimeClearIconMouseDown.bind(this));
    // avoid fastclick on icon. Otherwise the blur event overtakes the mousedown event.
    this.$timeClearIcon.addClass('needsclick');
  } else if ((!this.hasTime || !this.isClearable()) && this.$timeClearIcon) {
    // Remove $dateField
    this.$timeClearIcon.remove();
    this.$timeClearIcon = null;
  }
};

scout.DateField.prototype._onTimeClearIconMouseDown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.$timeField.focus();
  this.clear();
  if (this.value) {
    this.selectTime(this.value, false);
  } else {
    this.preselectTime(this._referenceDate(), false);
  }
  event.preventDefault();
  return;
};

scout.DateField.prototype._onTimeIconMouseDown = function(event) {
  if (!this.enabledComputed) {
    return;
  }
  this.$timeField.focus();
  if (!this.embedded) {
    this.openTimePopupAndSelect(this.value);
  }
};

scout.DateField.prototype._onDateFieldBlur = function(event) {
  this.setDateFocused(false);
  if (this.embedded) {
    // Don't execute, otherwise date would be accepted even though touch popup is still open.
    // This prevents following behavior: user clears date by pressing x and then selects another date. Now a blur event is triggered which would call acceptDate and eventually remove the time
    // -> Don't accept as long as touch dialog is open
    return;
  }

  // Close picker and update model
  if (this.popup instanceof scout.DatePickerPopup) {
    // in embedded mode we must update the date prediction but not close the popup (don't accidentially close time picker poupp)
    this.closePopup();
  }
  this.setDateFocused(false);
  this.acceptDate();
  this._removePredictionFields();
};

scout.DateField.prototype._onDateFieldFocus = function(event) {
  this.setDateFocused(true);
};

scout.DateField.prototype._onTimeFieldBlur = function(event) {
  this._tempTimeDate = null;
  this.setTimeFocused(false);
  if (this.embedded) {
    // Don't execute, otherwise time would be accepted even though touch popup is still open.
    // This prevents following behavior: user clears time by pressing x and then selects another time. Now a blur event is triggered which would call acceptTime and eventually remove the date
    // -> Don't accept as long as touch dialog is open
    return;
  }

  // Close picker and update model
  if (this.popup instanceof scout.TimePickerPopup) {
    // in embedded mode we must update the date prediction but not close the popup
    this.closePopup();
  }
  this._tempTimeDate = null;
  this.setTimeFocused(false);
  this.acceptTime();
  this._removePredictionFields();
};

scout.DateField.prototype._onTimeFieldFocus = function() {
  this.setTimeFocused(true);
};

/**
 * Handle "navigation" keys, i.e. keys that don't emit visible characters. Character input is handled
 * in _onDateFieldInput(), which is fired after 'keydown'.
 */
scout.DateField.prototype._onDateFieldKeyDown = function(event) {
  var delta = 0,
    diffYears = 0,
    diffMonths = 0,
    diffDays = 0,
    cursorPos = this.$dateField[0].selectionStart,
    displayText = scout.fields.valOrText(this.$dateField),
    prediction = this._$predictDateField && scout.fields.valOrText(this._$predictDateField),
    modifierCount = (event.ctrlKey ? 1 : 0) + (event.shiftKey ? 1 : 0) + (event.altKey ? 1 : 0) + (event.metaKey ? 1 : 0),
    pickerStartDate = this.value || this._referenceDate(),
    shiftDate = true;

  // Don't propagate tab to cell editor -> tab should focus time field
  if (this.hasTime &&
    this.mode === scout.FormField.Mode.CELLEDITOR &&
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
      this.acceptDate();
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

  if (event.which === scout.keys.UP || event.which === scout.keys.DOWN ||
    event.which === scout.keys.PAGE_UP || event.which === scout.keys.PAGE_DOWN) {
    if (displayText && !this._isDateValid()) {
      // If there is an error, try to parse the date. If it may be parsed, the error was likely a validation error.
      // In that case use the parsed date as starting point and not the for the user invisible value
      var parsedValue = this.isolatedDateFormat.parse(displayText, pickerStartDate);
      if (parsedValue) {
        pickerStartDate = parsedValue;
        this._setDateValid(true);
      }
    }
  }
  if (event.which === scout.keys.PAGE_UP || event.which === scout.keys.PAGE_DOWN) {
    if (!displayText || !this._isDateValid()) {
      // If input is empty or invalid, set picker to reference date
      pickerStartDate = this._referenceDate();
      if (this.hasTime) { // keep time part
        pickerStartDate = scout.dates.combineDateTime(pickerStartDate, this.value || this._referenceDate());
      }
      this.openDatePopupAndSelect(pickerStartDate);
      this._updateDisplayText(pickerStartDate);
      this._setDateValid(true);
      shiftDate = false; // don't shift if field has no value yet and popup was not open
    } else if (!this.popup) {
      // Otherwise, ensure picker is open
      this.openDatePopupAndSelect(pickerStartDate);
    }
    if (shiftDate) {
      diffMonths = (event.which === scout.keys.PAGE_UP ? -1 : 1);
      this.shiftSelectedDate(0, diffMonths, 0);
      this._updateDisplayText(this.getDatePicker().selectedDate);
    }
    $.suppressEvent(event);
    return;
  }

  if (event.which === scout.keys.UP || event.which === scout.keys.DOWN) {
    delta = (event.which === scout.keys.UP ? -1 : 1);
    // event.ctrlKey || event.metaKey  --> some keystrokes with ctrl modifier are captured and suppressed by osx, use command key instead
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
        pickerStartDate = scout.dates.combineDateTime(pickerStartDate, this.value || this._referenceDate());
      }
      this.openDatePopupAndSelect(pickerStartDate);
      this._updateDisplayText(pickerStartDate);
      this._setDateValid(true);
      shiftDate = false; // don't shift if field has no value yet and popup was not open
    } else if (!this.popup) {
      // Otherwise, ensure picker is open
      this.openDatePopupAndSelect(pickerStartDate);
    }
    if (shiftDate) {
      this.shiftSelectedDate(diffYears, diffMonths, diffDays);
      this._updateDisplayText(this.getDatePicker().selectedDate);
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
 * in _onDateFieldKeyDown().
 */
scout.DateField.prototype._onDateFieldInput = function(event) {
  var displayText = scout.fields.valOrText(this.$dateField);

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
    scout.fields.valOrText(this._$predictDateField, datePrediction.text);
    this.openDatePopupAndSelect(datePrediction.date);
  } else {
    // No valid prediction!
    this._removePredictionFields();
  }
  this._updateDateHasText();
};

scout.DateField.prototype.acceptInput = function() {
  var displayText = scout.nvl(this._readDisplayText(), '');

  var inputChanged = this._checkDisplayTextChanged(displayText);
  if (inputChanged) {
    this.parseAndSetValue(displayText);
  } else {
    var oldValue = this.value;
    this.parseAndSetValue(displayText);
    if (!scout.dates.equals(this.value, oldValue)) {
      inputChanged = true;
    }
  }
  if (inputChanged) {
    this._triggerAcceptInput();
  }
};

/**
 * Clears the time field if date field is empty before accepting the input
 */
scout.DateField.prototype.acceptDate = function() {
  if (this.hasTime && !this.errorStatus && scout.strings.empty(this.$dateField.val())) {
    this.$timeField.val('');
  }
  this.acceptInput();
};

/**
 * Clears the date field if time field is empty before accepting the input
 */
scout.DateField.prototype.acceptTime = function() {
  if (this.hasDate && !this.errorStatus && scout.strings.empty(this.$timeField.val())) {
    this.$dateField.val('');
  }
  this.acceptInput();
};

scout.DateField.prototype.acceptDateTime = function(acceptDate, acceptTime) {
  if (acceptDate) {
    this.acceptDate();
  } else if (acceptTime) {
    this.acceptTime();
  }
};

/**
 * Handle "navigation" keys, i.e. keys that don't emit visible characters. Character input is handled
 * in _onTimeFieldInput(), which is fired after 'keydown'.
 */
scout.DateField.prototype._onTimeFieldKeyDown = function(event) {
  var delta = 0,
    diffHours = 0,
    diffMinutes = 0,
    diffSeconds = 0,
    cursorPos = this.$timeField[0].selectionStart,
    displayText = this.$timeField.val(),
    prediction = this._$predictTimeField && this._$predictTimeField.val(),
    modifierCount = (event.ctrlKey ? 1 : 0) + (event.shiftKey ? 1 : 0) + (event.altKey ? 1 : 0) + (event.metaKey ? 1 : 0),
    pickerStartTime = this.value || this._referenceDate(),
    shiftTime = true;

  // Don't propagate shift-tab to cell editor -> shift tab should focus date field
  if (this.hasDate &&
    this.mode === scout.FormField.Mode.CELLEDITOR &&
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
    event.which === scout.keys.ALT) {
    // Default handling
    return;
  }

  if (event.which === scout.keys.ENTER) {
    // Timefield is shown in touch popup, so we need to make sure time gets accepted and popup closed, even if the regular time field itself has no popup
    if (this.popup || this._$predictDateField) {
      // Accept the current prediction (if available)
      this._tempTimeDate = null;
      this.acceptTime();
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

    if (this.hasTimePopup) {
      if (!displayText || !this._isTimeValid()) {
        // If input is empty or invalid, set picker to reference date
        pickerStartTime = this._referenceDate();
        this.openTimePopupAndSelect(pickerStartTime);
        this._updateDisplayText(pickerStartTime);
        this._setTimeValid(true);
        shiftTime = false; // don't shift if field has no value yet and popup was not open
      } else if (!this.popup) {
        // Otherwise, ensure picker is open
        this.openTimePopupAndSelect(pickerStartTime);
      }
      if (shiftTime) {
        this.shiftSelectedTime(diffHours, diffMinutes, diffSeconds);
        this._updateDisplayText(this.getTimePicker().selectedTime);
      }
      $.suppressEvent(event);
      return;
    } else {
      // without picker
      if (!this._tempTimeDate) {
        var timePrediction = this._predictTime(displayText); // this also updates the errorStatus
        if (timePrediction && timePrediction.date) {
          this._tempTimeDate = timePrediction.date;
        } else {
          this._tempTimeDate = this._referenceDate();
          shiftTime = false;
        }
      }
      if (shiftTime) {
        this._tempTimeDate = scout.dates.shiftTime(this._tempTimeDate, diffHours, diffMinutes, diffSeconds);
      }
      if (this.hasDate) {
        // Combine _tempTimeDate with existing date part
        this._tempTimeDate = scout.dates.combineDateTime(this.value || this._referenceDate(), this._tempTimeDate);
      }
      this._updateDisplayText(this._tempTimeDate);
      this._setTimeValid(true);
      $.suppressEvent(event);
      return;
    }
  }
};

/**
 * Handle changed input. This method is fired when the field's content has been altered by a user
 * action (not by JS) such as pressing a character key, deleting a character using DELETE or
 * BACKSPACE, cutting or pasting text with ctrl-x / ctrl-v or mouse drag'n'drop.
 * Keys that don't alter the content (e.g. modifier keys, arrow keys, home, end etc.) are handled
 * in _onTimeFieldKeyDown().
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
    this.openTimePopupAndSelect(timePrediction.date);
  } else {
    // No valid prediction!
    this._tempTimeDate = null;
    this._removePredictionFields();
  }
  this._updateTimeHasText();
};

scout.DateField.prototype._onDatePickerDateSelect = function(event) {
  this._setDateValid(true);
  this._setTimeValid(true);
  var newValue = this._newTimestampAsDate(event.date, this.value);
  this.setValue(newValue);
  this.closePopup();
  this._triggerAcceptInput();
};

scout.DateField.prototype._onTimePickerTimeSelect = function(event) {
  this._setDateValid(true);
  this._setTimeValid(true);
  var newValue = this._newTimestampAsDate(this.value, event.time);
  this.setValue(newValue);
  this.closePopup();
  this._triggerAcceptInput();
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

scout.DateField.prototype._computeDisplayText = function(dateDisplayText, timeDisplayText) {
  var dateText = dateDisplayText || '',
    timeText = timeDisplayText || '';

  // do not use scout.strings.join which ignores empty components
  var displayText = (this.hasDate ? dateText : '') + (this.hasDate && this.hasTime ? '\n' : '') + (this.hasTime ? timeText : '');

  // empty display text should always be just an empty string
  if (displayText === '\n') {
    displayText = '';
  }
  return displayText;
};

scout.DateField.prototype._splitDisplayText = function(displayText) {
  var dateText = '',
    timeText = '';

  if (scout.strings.hasText(displayText)) {
    var parts = displayText.split('\n');
    dateText = this.hasDate ? parts[0] : '';
    timeText = this.hasTime ? (this.hasDate ? parts[1] : parts[0]) : '';
  }
  return {
    dateText: dateText,
    timeText: timeText
  };
};

scout.DateField.prototype._updateDisplayTextProperty = function() {
  this._setProperty('displayText', this._computeDisplayText(this.dateDisplayText, this.timeDisplayText));
};

/**
 * @override ValueField.js
 */
scout.DateField.prototype.aboutToBlurByMouseDown = function(target) {
  var dateFieldActive, timeFieldActive, eventOnDatePicker, eventOnTimePicker,
    eventOnDateField = this.$dateField ? (this.$dateField.isOrHas(target) || this.$dateFieldIcon.isOrHas(target) || (this.$dateClearIcon && this.$dateClearIcon.isOrHas(target))) : false,
    eventOnTimeField = this.$timeField ? (this.$timeField.isOrHas(target) || this.$timeFieldIcon.isOrHas(target) || (this.$timeClearIcon && this.$timeClearIcon.isOrHas(target))) : false,
    eventOnPopup = this.popup && this.popup.$container.isOrHas(target),
    datePicker = this.getDatePicker(),
    timePicker = this.getTimePicker();

  if (!eventOnDateField && !eventOnTimeField && !eventOnPopup) {
    // event outside this field.
    dateFieldActive = scout.focusUtils.isActiveElement(this.$dateField);
    timeFieldActive = scout.focusUtils.isActiveElement(this.$timeField);
    // Accept only the currently focused part (the other one cannot have a pending change)
    this.acceptDateTime(dateFieldActive, timeFieldActive);
    return;
  }

  // when date-field is embedded, time-prediction must be accepted before
  // the date-picker triggers the 'dateSelect' event.
  if (this.embedded) {
    eventOnDatePicker = datePicker && datePicker.$container.isOrHas(target);
    eventOnTimePicker = timePicker && timePicker.$container.isOrHas(target);
    if (eventOnDatePicker && eventOnTimePicker) {
      this.acceptTime();
    }
  }
};

/**
 * Returns null if both arguments are not set. Otherwise, this.value or the current date
 * is used as basis and the given arguments are applied to that date. The result is returned.
 */
scout.DateField.prototype._newTimestampAsDate = function(date, time) {
  var result = null;
  if (date || time) {
    result = this.value || this._referenceDate();
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
  var referenceDate = this.autoDate || scout.dates.ceil(new Date(), this.timePickerResolution);
  if (this.autoDate) {
    referenceDate = this.autoDate;
  } else if (this.hasTime) {
    referenceDate = scout.dates.ceil(new Date(), this.timePickerResolution);
  } else {
    referenceDate = scout.dates.trunc(new Date());
  }
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

scout.DateField.prototype.openDatePopup = function(date) {
  if (this.popup) {
    // already open
    return;
  }

  this.popup = this.createDatePopup();
  this.popup.open();
  this.$dateField.addClass('focused');
  this.popup.on('remove', function(event) {
    this._onPopupRemove(event);
    this.popup = null;
    this.$dateField.removeClass('focused');
  }.bind(this));
  this.getDatePicker().on('dateSelect', this._onDatePickerDateSelect.bind(this));
};

scout.DateField.prototype.closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

scout.DateField.prototype.toggleDatePopup = function() {
  $.log.info('(DateField#toggleDatePopup) popupOpen=', !!this.popup);
  if (this.popup) {
    this.closePopup();
  } else {
    this.openDatePopupAndSelect(this.value);
  }
};

scout.DateField.prototype.openTimePopup = function(date) {
  if (!this.hasTimePopup || this.popup) {
    // already open
    return;
  }
  this.popup = this.createTimePopup();
  this.popup.open();
  this.$timeField.addClass('focused');
  this.popup.on('remove', function(event) {
    this._onPopupRemove(event);
    this.popup = null;
    this.$timeField.removeClass('focused');
  }.bind(this));
  this.getTimePicker().on('timeSelect', this._onTimePickerTimeSelect.bind(this));
};

scout.DateField.prototype.toggleTimePopup = function() {
  $.log.info('(DateField#toggleTimePopup) popupOpen=', !!this.popup);
  if (this.popup) {
    this.closePopup();
  } else {
    this.openTimePopupAndSelect(this.value);
  }
};

scout.DateField.prototype._parseValue = function(displayText) {
  var parts = this._splitDisplayText(displayText);
  var dateText = parts.dateText;
  var datePrediction = {};
  var timeText = parts.timeText;
  var timePrediction = {};
  var success = true;

  if (this.hasDate) {
    datePrediction = this._predictDate(dateText); // this also updates the errorStatus
    if (!datePrediction) {
      success = false;
    }
    this._setDateDisplayText(dateText);
  }

  if (this.hasTime) {
    timePrediction = this._predictTime(timeText); // this also updates the errorStatus
    if (!timePrediction) {
      success = false;
    }
    this._setTimeDisplayText(timeText);
  }

  // Error status was already set by _predict functions, just throw it so that setValue is not called
  if (!success) {
    throw this.errorStatus;
  }

  // parse success -> return new value
  if (datePrediction.date || timePrediction.date) {
    return this._newTimestampAsDate(datePrediction.date, timePrediction.date);
  }
  return null;
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

  var analyzeInfo = this.isolatedDateFormat.analyze(inputText, this.value || this._referenceDate());
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
  var analyzeInfo = this.isolatedTimeFormat.analyze(inputText, this.value || this._referenceDate());
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
 * This method updates the parts (date, time) of the error status.
 */
scout.DateField.prototype._setErrorStatusPart = function(property, valid) {
  if (valid) {
    this.setErrorStatus(null);
    return;
  }
  var errorStatus = this.errorStatus;
  if (!errorStatus) {
    errorStatus = this._createErrorStatus();
  }
  errorStatus[property] = true;
  this.setErrorStatus(errorStatus);
};

scout.DateField.prototype._createErrorStatus = function() {
  return new scout.Status({
    message: this.session.text('ui.InvalidDate'),
    severity: scout.Status.Severity.ERROR
  });
};

/**
 * @override
 */
scout.DateField.prototype._createInvalidValueStatus = function(value, error) {
  var errorStatus = scout.DateField.parent.prototype._createInvalidValueStatus.call(this, value, error);
  // Set date and time to invalid, otherwise isDateValid and isTimeValid return false even though there is a validation error
  errorStatus.invalidDate = true;
  errorStatus.invalidTime = true;
  return errorStatus;
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

/**
 * Method invoked if being rendered within a cell-editor (mode='scout.FormField.Mode.CELLEDITOR'), and once the editor finished its rendering.
 */
scout.DateField.prototype.onCellEditorRendered = function(options) {
  if (options.openFieldPopup) {
    if (this.hasDate && !this.hasTime) {
      this.openDatePopupAndSelect(this.value);
    } else if (!this.hasDate && this.hasTime) {
      this.openTimePopupAndSelect(this.value);
    } else if (!scout.device.supportsTouch()) {
      // If date AND time are active, don't open popup on touch devices because the user has to choose first what he wants to edit
      this.openDatePopupAndSelect(this.value);
    }
  }
  if (this.touch) {
    this._cellEditorPopup = options.cellEditorPopup;
  }
};

scout.DateField.prototype._onPopupRemove = function(event) {
  if (!this.touch || !this._cellEditorPopup) {
    return;
  }
  if (this.hasDate && this.hasTime) {
    // If date and time is shown, user might want to change both, let him close the cell editor when he is finished
    return;
  }
  // Close cell editor when touch popup closes
  this._cellEditorPopup.completeEdit();
  this._cellEditorPopup = null;
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
  if (this.popup && this.popup.getDatePicker) {
    return this.popup.getDatePicker();
  }
};

/**
 * Opens picker and selects date
 *
 * @param date
 *          optional, Date to pass to the date picker. If no date is specified, the reference date
 *          is preselected (not selected!).
 */
scout.DateField.prototype.openDatePopupAndSelect = function(date) {
  this.openDatePopup();
  if (!date) {
    this.preselectDate(this._referenceDate(), false);
  } else {
    this.selectDate(date, false);
  }
};

scout.DateField.prototype.preselectDate = function(date, animated) {
  var datePicker = this.getDatePicker();
  if (datePicker) {
    datePicker.preselectDate(date, animated);
  }
};

scout.DateField.prototype.selectDate = function(date, animated) {
  var datePicker = this.getDatePicker();
  if (datePicker) {
    datePicker.selectDate(date, animated);
  }
};

/**
 * @returns DatePicker instance from popup, because the property name is different
 *    for DatePickerPopup and DatePickerTouchPopup.
 */
scout.DateField.prototype.getTimePicker = function() {
  if (this.popup && this.popup.getTimePicker) {
    return this.popup.getTimePicker();
  }
};
/**
 * Opens picker and selects date
 *
 * @param date
 *          optional, Date to pass to the date picker. If no date is specified, the reference date
 *          is preselected (not selected!).
 */
scout.DateField.prototype.openTimePopupAndSelect = function(time) {
  // resolution < 1 means no picker required
  if (!this.hasTimePopup) {
    return;
  }
  this.openTimePopup();
  if (!time) {
    this.preselectTime(this._referenceDate());
  } else {
    this.selectTime(time);
  }
};

scout.DateField.prototype.preselectTime = function(time) {
  var timePicker = this.getTimePicker();
  if (timePicker) {
    timePicker.preselectTime(time);
  }
};

scout.DateField.prototype.selectTime = function(time) {
  var timePicker = this.getTimePicker();
  if (timePicker) {
    timePicker.selectTime(time);
  }
};

scout.DateField.prototype.shiftSelectedDate = function(years, months, days) {
  this.openDatePopup();
  this.getDatePicker().shiftSelectedDate(years, months, days);
};
scout.DateField.prototype.shiftSelectedTime = function(hourUnits, minuteUnits, secondUnits) {
  this.openTimePopup();
  this.getTimePicker().shiftSelectedTime(hourUnits, minuteUnits, secondUnits);
};

scout.DateField.prototype._formatValue = function(value) {
  var
    dateText = '',
    timeText = '';

  if (this.hasDate) {
    if (value) {
      dateText = this.isolatedDateFormat.format(value);
    }
    this.dateDisplayText = dateText;
  }
  if (this.hasTime) {
    if (value) {
      timeText = this.isolatedTimeFormat.format(value);
    }
    this.timeDisplayText = timeText;
  }

  return this._computeDisplayText(this.dateDisplayText, this.timeDisplayText);
};

scout.DateField.prototype._renderDisabledStyle = function() {
  this._renderDisabledStyleInternal(this.$dateField);
  this._renderDisabledStyleInternal(this.$timeField);
  this._renderDisabledStyleInternal(this.$mandatory);
};

/**
 * @override
 */
scout.DateField.prototype._triggerAcceptInput = function() {
  var event = {
    displayText: this.displayText,
    errorStatus: this.errorStatus,
    value: this.value
  };
  this.trigger('acceptInput', event);
};

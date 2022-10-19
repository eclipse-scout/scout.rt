/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, DateFormat, DatePickerPopup, DatePredictionFailedStatus, dates, DateTimeCompositeLayout, Device, fields, focusUtils, FormField, HtmlComponent, InputFieldKeyStrokeContext, keys, objects, ParsingFailedStatus, scout, Status, strings, styles, TimePickerPopup, ValueField} from '../../../index';
import $ from 'jquery';

export default class DateField extends ValueField {

  constructor() {
    super();

    this.allowedDates = [];
    this.popup = null;
    this.autoDate = null;
    this.dateDisplayText = null;
    this.dateHasText = false;
    this.dateFocused = false;
    this.dateFormatPattern = null;
    this.disabledCopyOverlay = true;
    this.hasDate = true;
    this.touchMode = false;
    this.embedded = false;

    this.hasTime = false;
    this.hasTimePopup = true;
    this.timeDisplayText = null;
    this.timeHasText = false;
    this.timeDisplayText = null;
    this.timePickerResolution = 30;
    this.timeFormatPattern = null;
    this.timeFocused = false;

    this.$dateField = null;
    this.$timeField = null;
    this.$dateFieldIcon = null;
    this.$timeFieldIcon = null;
    this.$dateClearIcon = null;
    this.$timeClearIcon = null;
    this._$predictDateField = null;
    this._$predictTimeField = null;

    // This is the storage for the time (as date) while the focus in the field (e.g. when
    // pressing up/down). In date fields, the date picker is used for that purposes.
    this._tempTimeDate = null;
    this.invalidValueMessageKey = 'ui.InvalidDate';
    this._addCloneProperties(['hasDate', 'hasTime', 'dateFormatPattern', 'timeFormatPattern', 'allowedDates', 'autoDate']);
  }

  static ErrorCode = {
    PARSE_ERROR: -1
  };

  /**
   * Predicate function to find a PARSE_ERROR.
   */
  static PARSE_ERROR_PREDICATE = function(status) {
    return status.code === DateField.ErrorCode.PARSE_ERROR;
  };

  /**
   * @override Widget.js
   */
  _createKeyStrokeContext() {
    return new InputFieldKeyStrokeContext();
  }

  _init(model) {
    super._init(model);
    fields.initTouch(this, model);
    this.popup = model.popup;
    this._setAutoDate(this.autoDate);
    this._setDisplayText(this.displayText);
    this._setAllowedDates(this.allowedDates);
    this._setTimePickerResolution(this.timePickerResolution);
  }

  /**
   * Initializes the date format before calling set value.
   * This cannot be done in _init because the value field would call _setValue first
   */
  _initValue(value) {
    this._setDateFormatPattern(this.dateFormatPattern);
    this._setTimeFormatPattern(this.timeFormatPattern);
    super._initValue(value);
  }

  createDatePopup() {
    let popupType = this.touchMode ? 'DatePickerTouchPopup' : 'DatePickerPopup';
    return scout.create(popupType, {
      parent: this,
      $anchor: this.$field,
      boundToAnchor: !this.touchMode,
      cssClass: this._errorStatusClass(),
      closeOnAnchorMouseDown: false,
      field: this,
      allowedDates: this.allowedDates,
      dateFormat: this.isolatedDateFormat,
      displayText: this.dateDisplayText
    });
  }

  createTimePopup() {
    let popupType = this.touchMode ? 'TimePickerTouchPopup' : 'TimePickerPopup';
    return scout.create(popupType, {
      parent: this,
      $anchor: this.$timeField,
      boundToAnchor: !this.touchMode,
      cssClass: this._errorStatusClass(),
      closeOnAnchorMouseDown: false,
      field: this,
      timeResolution: this.timePickerResolution
    });
  }

  _render() {
    this.addContainer(this.$parent, 'date-field');
    this.addLabel();
    this.addField(this.$parent.makeDiv('date-time-composite'));
    this.addStatus(this.$field);
    if (!this.embedded) {
      this.addMandatoryIndicator();
    }

    this.htmlDateTimeComposite = HtmlComponent.install(this.$field, this.session);
    this.htmlDateTimeComposite.setLayout(new DateTimeCompositeLayout(this));
  }

  _renderProperties() {
    this._renderHasDate();
    this._renderHasTime();

    // Has to be the last call, otherwise _renderErrorStatus() would operate on the wrong state.
    super._renderProperties();

    this._renderDateHasText();
    this._renderTimeHasText();
  }

  _remove() {
    super._remove();
    this.$dateField = null;
    this.$timeField = null;
    this.$dateFieldIcon = null;
    this.$timeFieldIcon = null;
    this.$dateClearIcon = null;
    this.$timeClearIcon = null;
    this._$predictDateField = null;
    this._$predictTimeField = null;
    this.popup = null;
  }

  setHasDate(hasDate) {
    this.setProperty('hasDate', hasDate);
  }

  _setHasDate(hasDate) {
    this._setProperty('hasDate', hasDate);
    if (this.initialized) {
      // if property changes on the fly, update the display text
      this._updateDisplayTextProperty();
    }
  }

  _renderHasDate() {
    if (this.hasDate && !this.$dateField) {
      // Add $dateField
      this.$dateField = fields.makeInputOrDiv(this, 'date')
        .on('mousedown', this._onDateFieldMouseDown.bind(this))
        .appendTo(this.$field);
      if (this.$timeField) {
        // make sure date field comes before time field, otherwise tab won't work as expected
        this.$dateField.insertBefore(this.$timeField);
      }
      if (!this.touchMode) {
        this.$dateField
          .on('keydown', this._onDateFieldKeyDown.bind(this))
          .on('input', this._onDateFieldInput.bind(this))
          .on('blur', this._onDateFieldBlur.bind(this))
          .on('focus', this._onDateFieldFocus.bind(this));
      }
      this._linkWithLabel(this.$dateField);
      HtmlComponent.install(this.$dateField, this.session);

      this.$dateFieldIcon = fields.appendIcon(this.$field, 'date')
        .on('mousedown', this._onDateIconMouseDown.bind(this));

    } else if (!this.hasDate && this.$dateField) {
      // Remove $dateField
      this.$dateField.remove();
      this.$dateField = null;
      this.$dateFieldIcon.remove();
      this.$dateFieldIcon = null;
    }

    if (!this.rendering) {
      this._renderDisplayText();
      this._renderFieldStyle();
      this._renderEnabled();
      this.htmlDateTimeComposite.invalidateLayoutTree();
    }
    this._renderDateClearable();
    this.$container.toggleClass('has-date', this.hasDate);
  }

  setHasTime(hasTime) {
    this.setProperty('hasTime', hasTime);
  }

  _setHasTime(hasTime) {
    this._setProperty('hasTime', hasTime);
    if (this.initialized) {
      // if property changes on the fly, update the display text
      this._updateDisplayTextProperty();
    }
  }

  _renderHasTime() {
    if (this.hasTime && !this.$timeField) {
      // Add $timeField
      this.$timeField = fields.makeInputOrDiv(this, 'time')
        .on('mousedown', this._onTimeFieldMouseDown.bind(this))
        .appendTo(this.$field);
      if (this.$dateField) {
        // make sure time field comes after date field, otherwise tab won't work as expected
        this.$timeField.insertAfter(this.$dateField);
      }
      if (!this.touchMode || !this.hasTimePopup) {
        this.$timeField
          .on('keydown', this._onTimeFieldKeyDown.bind(this))
          .on('input', this._onTimeFieldInput.bind(this))
          .on('blur', this._onTimeFieldBlur.bind(this))
          .on('focus', this._onTimeFieldFocus.bind(this));
      }
      this._linkWithLabel(this.$timeField);
      HtmlComponent.install(this.$timeField, this.session);

      this.$timeFieldIcon = fields.appendIcon(this.$field, 'time')
        .on('mousedown', this._onTimeIconMouseDown.bind(this));

    } else if (!this.hasTime && this.$timeField) {
      // Remove $timeField
      this.$timeField.remove();
      this.$timeField = null;
      this.$timeFieldIcon.remove();
      this.$timeFieldIcon = null;
    }

    if (!this.rendering) {
      this._renderDisplayText();
      this._renderFieldStyle();
      this._renderEnabled();
      this.htmlDateTimeComposite.invalidateLayoutTree();
    }
    this._renderTimeClearable();
    this.$container.toggleClass('has-time', this.hasTime);
  }

  setTimePickerResolution(timePickerResolution) {
    this.setProperty('timePickerResolution', timePickerResolution);
  }

  _setTimePickerResolution(timePickerResolution) {
    if (timePickerResolution < 1) {
      // default
      timePickerResolution = 10;
      this.hasTimePopup = false;
    } else {
      this.hasTimePopup = true;
    }
    this._setProperty('timePickerResolution', timePickerResolution);
  }

  /**
   * @override FormField.js
   */
  _renderPlaceholder($field) {
    super._renderPlaceholder(
      this._fieldForPlaceholder());
  }

  /**
   * @override FormField.js
   */
  _removePlaceholder($field) {
    super._removePlaceholder(
      this._fieldForPlaceholder());
  }

  _fieldForPlaceholder() {
    if (this.hasDate) {
      return this.$dateField;
    } else if (this.hasTime) {
      return this.$timeField;
    }
    return null;
  }

  setDateFormatPattern(dateFormatPattern) {
    this.setProperty('dateFormatPattern', dateFormatPattern);
  }

  _setDateFormatPattern(dateFormatPattern) {
    if (!dateFormatPattern) {
      dateFormatPattern = this.session.locale.dateFormatPatternDefault;
    }
    this._setProperty('dateFormatPattern', dateFormatPattern);
    this.isolatedDateFormat = new DateFormat(this.session.locale, this.dateFormatPattern);

    if (this.initialized) {
      // if format changes on the fly, just update the display text
      this._updateDisplayText();
    }
  }

  setTimeFormatPattern(timeFormatPattern) {
    this.setProperty('timeFormatPattern', timeFormatPattern);
  }

  _setTimeFormatPattern(timeFormatPattern) {
    if (!timeFormatPattern) {
      timeFormatPattern = this.session.locale.timeFormatPatternDefault;
    }
    this._setProperty('timeFormatPattern', timeFormatPattern);
    this.isolatedTimeFormat = new DateFormat(this.session.locale, this.timeFormatPattern);

    if (this.initialized) {
      // if format changes on the fly, just update the display text
      this._updateDisplayText();
    }
  }

  /**
   * @override FormField.js
   */
  _renderEnabled() {
    super._renderEnabled();
    this.$container.setEnabled(this.enabledComputed);
    if (this.$dateField) {
      this.$dateField.setEnabled(this.enabledComputed);
    }
    if (this.$timeField) {
      this.$timeField.setEnabled(this.enabledComputed);
    }
  }

  /**
   * @override ValueField.js
   */
  _renderDisplayText() {
    if (this.hasDate) {
      this._renderDateDisplayText();
    }
    if (this.hasTime) {
      this._renderTimeDisplayText();
    }
    this._removePredictionFields();
  }

  _readDisplayText() {
    let dateDisplayText, timeDisplayText;
    if (this.hasDate) {
      dateDisplayText = this._readDateDisplayText();
    }
    if (this.hasTime) {
      timeDisplayText = this._readTimeDisplayText();
    }
    return this._computeDisplayText(dateDisplayText, timeDisplayText);
  }

  _renderDateDisplayText() {
    fields.valOrText(this.$dateField, this.dateDisplayText);
    this._updateDateHasText();
  }

  _readDateDisplayText() {
    return (this._$predictDateField ? fields.valOrText(this._$predictDateField) : fields.valOrText(this.$dateField));
  }

  _renderTimeDisplayText() {
    fields.valOrText(this.$timeField, this.timeDisplayText);
    this._updateTimeHasText();
  }

  _readTimeDisplayText() {
    return (this._$predictTimeField ? fields.valOrText(this._$predictTimeField) : fields.valOrText(this.$timeField));
  }

  /**
   * @override
   */
  setDisplayText(displayText) {
    // Overridden to avoid the equals check -> make sure renderDisplayText is executed whenever setDisplayText is called
    // Reason: key up/down and picker day click modify the display text, but input doesn't
    // -> reverting to a date using day click or up down after the input changed would not work anymore
    // changing 'onXyInput' to always update the display text would fix that, but would break acceptInput
    this._setDisplayText(displayText);
    if (this.rendered) {
      this._renderDisplayText();
    }
  }

  _setDisplayText(displayText) {
    this._setProperty('displayText', displayText);

    let parts = this._splitDisplayText(displayText);
    if (this.hasDate) {
      // preserve dateDisplayText if hasDate is set to false (only override if it is true)
      this.dateDisplayText = parts.dateText;
    }
    if (this.hasTime) {
      // preserve timeDisplayText if hasTime is set to false (only override if it is true)
      this.timeDisplayText = parts.timeText;
    }
  }

  /**
   * @override
   */
  _ensureValue(value) {
    return dates.ensure(value);
  }

  /**
   * @param {Date} value the date to validate
   * @return {Date} the validated date
   * @override
   */
  _validateValue(value) {
    if (objects.isNullOrUndefined(value)) {
      return value;
    }
    if (!(value instanceof Date)) {
      throw this.session.text(this.invalidValueMessageKey);
    }

    if (!this.isDateAllowed(value)) {
      throw this.session.text('DateIsNotAllowed');
    }

    if (!this.hasDate && !this.value) {
      // truncate to 01.01.1970 if no date was entered before. Otherwise preserve date part (important for toggling hasDate on the fly)
      value = dates.combineDateTime(null, value);
    }
    return value;
  }

  isDateAllowed(date) {
    if (!date || this.allowedDates.length === 0 || this.embedded) { // in embedded mode, main date field must take care of validation, otherwise error status won't be shown
      return true;
    }
    let dateAsTimestamp = dates.trunc(date).getTime();
    return this.allowedDates.some(allowedDate => allowedDate.getTime() === dateAsTimestamp);
  }

  _valueEquals(valueA, valueB) {
    return dates.equals(valueA, valueB);
  }

  setAutoDate(autoDate) {
    this.setProperty('autoDate', autoDate);
  }

  _setAutoDate(autoDate) {
    autoDate = dates.ensure(autoDate);
    this._setProperty('autoDate', autoDate);
  }

  setAllowedDates(allowedDates) {
    this.setProperty('allowedDates', allowedDates);
  }

  _setAllowedDates(allowedDates) {
    let truncDates = [];
    arrays.ensure(allowedDates).forEach(date => {
      if (date) {
        truncDates.push(dates.trunc(dates.ensure(date)));
      }
    });
    truncDates = truncDates.sort(dates.compare);
    this._setProperty('allowedDates', truncDates);
  }

  /**
   * @override FormField.js
   */
  _renderErrorStatus() {
    super._renderErrorStatus();
    let hasStatus = !!this.errorStatus,
      statusClass = this._errorStatusClass();

    if (this.$dateField) {
      this._updateErrorStatusClassesOnElement(this.$dateField, statusClass, hasStatus);

      // Because the error color of field icons depends on the error status of sibling <input> elements.
      // The prediction fields are clones of the input fields, so the 'has-error' class has to be
      // removed from them as well to make the icon "valid".
      this._updateErrorStatusClassesOnElement(this._$predictDateField, statusClass, hasStatus);
    }

    // Do the same for the time field
    if (this.$timeField) {
      this._updateErrorStatusClassesOnElement(this.$timeField, statusClass, hasStatus);
      this._updateErrorStatusClassesOnElement(this._$predictTimeField, statusClass, hasStatus);
    }

    if (this.popup) {
      this._updateErrorStatusClassesOnElement(this.popup.$container, statusClass, hasStatus);
    }
  }

  _errorStatusClass() {
    return (this.errorStatus && !this._isSuppressStatusField()) ? 'has-' + this.errorStatus.cssClass() : '';
  }

  /**
   * @Override FormField.js
   */
  _renderFont() {
    this.$dateField && styles.legacyFont(this, this.$dateField);
    this.$timeField && styles.legacyFont(this, this.$timeField);
  }

  /**
   * @Override FormField.js
   */
  _renderForegroundColor() {
    this.$dateField && styles.legacyForegroundColor(this, this.$dateField);
    this.$timeField && styles.legacyForegroundColor(this, this.$timeField);
  }

  /**
   * @override FormField.js
   */
  _renderBackgroundColor() {
    this.$dateField && styles.legacyBackgroundColor(this, this.$dateField);
    this.$timeField && styles.legacyBackgroundColor(this, this.$timeField);
  }

  /**
   * @override
   */
  activate() {
    if (!this.enabledComputed || !this.rendered) {
      return;
    }
    if (this.$dateField) {
      this.$dateField.focus();
      this._onDateFieldMouseDown();
    } else if (this.$timeField) {
      this.$timeField.focus();
      this._onTimeFieldMouseDown();
    }
  }

  /**
   * @override
   */
  getFocusableElement() {
    if (this.$dateField) {
      return this.$dateField;
    }
    if (this.$timeField) {
      return this.$timeField;
    }
    return null;
  }

  _onDateFieldMouseDown() {
    if (fields.handleOnClick(this)) {
      this.openDatePopupAndSelect(this.value);
    }
  }

  _onTimeFieldMouseDown() {
    if (fields.handleOnClick(this)) {
      this.openTimePopupAndSelect(this.value);
    }
  }

  setDateFocused(dateFocused) {
    this.setProperty('dateFocused', dateFocused);
  }

  _renderDateFocused() {
    this.$container.toggleClass('date-focused', this.dateFocused);
  }

  _updateTimeHasText() {
    this.setTimeHasText(strings.hasText(this._readTimeDisplayText()));
  }

  setTimeHasText(timeHasText) {
    this.setProperty('timeHasText', timeHasText);
  }

  _renderTimeHasText() {
    if (this.$timeField) {
      this.$timeField.toggleClass('has-text', this.timeHasText);
    }
    this.$container.toggleClass('time-has-text', this.timeHasText);
  }

  _updateDateHasText() {
    this.setDateHasText(strings.hasText(this._readDateDisplayText()));
  }

  setDateHasText(dateHasText) {
    this.setProperty('dateHasText', dateHasText);
  }

  _renderDateHasText() {
    if (this.$dateField) {
      this.$dateField.toggleClass('has-text', this.dateHasText);
    }
    this.$container.toggleClass('date-has-text', this.dateHasText);
  }

  /**
   * @override
   */
  clear() {
    if (!(this.hasDate && this.hasTime)) {
      super.clear();
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
  }

  _clear() {
    this._removePredictionFields();
    if (this.hasDate && !this.timeFocused) {
      fields.valOrText(this.$dateField, '');
      this._setDateValid(true);
      this._updateDateHasText();
    }
    if (this.hasTime && !this.dateFocused) {
      fields.valOrText(this.$timeField, '');
      this._setTimeValid(true);
      this._updateTimeHasText();
    }
  }

  _onDateClearIconMouseDown(event) {
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
  }

  _onDateIconMouseDown(event) {
    if (!this.enabledComputed) {
      return;
    }
    this.$dateField.focus();
    if (!this.embedded) {
      this.openDatePopupAndSelect(this.value);
    }
  }

  setTimeFocused(timeFocused) {
    this.setProperty('timeFocused', timeFocused);
  }

  _renderTimeFocused() {
    this.$container.toggleClass('time-focused', this.timeFocused);
  }

  _renderClearable() {
    this._renderDateClearable();
    this._renderTimeClearable();
    this._updateClearableStyles();
  }

  _renderDateClearable() {
    if (this.hasDate && this.isClearable()) {
      if (!this.$dateClearIcon) {
        // date clear icon
        this.$dateClearIcon = this.$field.appendSpan('icon date-clear unfocusable text-field-icon action')
          .on('mousedown', this._onDateClearIconMouseDown.bind(this));
      }
    } else {
      if (this.$dateClearIcon) {
        // Remove clear icon
        this.$dateClearIcon.remove();
        this.$dateClearIcon = null;
      }
    }
  }

  _renderTimeClearable() {
    if (this.hasTime && this.isClearable()) {
      if (!this.$timeClearIcon) {
        // time clear icon
        this.$timeClearIcon = this.$field.appendSpan('icon time-clear unfocusable text-field-icon action')
          .on('mousedown', this._onTimeClearIconMouseDown.bind(this));
      }
    } else {
      if (this.$timeClearIcon) {
        // Remove clear icon
        this.$timeClearIcon.remove();
        this.$timeClearIcon = null;
      }
    }
  }

  _onTimeClearIconMouseDown(event) {
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
  }

  _onTimeIconMouseDown(event) {
    if (!this.enabledComputed) {
      return;
    }
    this.$timeField.focus();
    if (!this.embedded) {
      this.openTimePopupAndSelect(this.value);
    }
  }

  _onDateFieldBlur(event) {
    this.setFocused(false);
    this.setDateFocused(false);
    if (this.embedded) {
      // Don't execute, otherwise date would be accepted even though touch popup is still open.
      // This prevents following behavior: user clears date by pressing x and then selects another date. Now a blur event is triggered which would call acceptDate and eventually remove the time
      // -> Don't accept as long as touch dialog is open
      return;
    }

    // Close picker and update model
    if (this.popup instanceof DatePickerPopup) {
      // in embedded mode we must update the date prediction but not close the popup (don't accidentally close time picker popup)
      this.closePopup();
    }
    this.setDateFocused(false);
    this.acceptDate();
    this._removePredictionFields();
  }

  _onDateFieldFocus(event) {
    this.setFocused(true);
    this.setDateFocused(true);
  }

  _onTimeFieldBlur(event) {
    this._tempTimeDate = null;
    this.setFocused(false);
    this.setTimeFocused(false);
    if (this.embedded) {
      // Don't execute, otherwise time would be accepted even though touch popup is still open.
      // This prevents following behavior: user clears time by pressing x and then selects another time. Now a blur event is triggered which would call acceptTime and eventually remove the date
      // -> Don't accept as long as touch dialog is open
      return;
    }

    // Close picker and update model
    if (this.popup instanceof TimePickerPopup) {
      // in embedded mode we must update the date prediction but not close the popup
      this.closePopup();
    }
    this._tempTimeDate = null;
    this.setTimeFocused(false);
    this.acceptTime();
    this._removePredictionFields();
  }

  _onTimeFieldFocus() {
    this.setFocused(true);
    this.setTimeFocused(true);
  }

  /**
   * Handle "navigation" keys, i.e. keys that don't emit visible characters. Character input is handled
   * in _onDateFieldInput(), which is fired after 'keydown'.
   */
  _onDateFieldKeyDown(event) {
    let delta = 0,
      diffYears = 0,
      diffMonths = 0,
      diffDays = 0,
      cursorPos = this.$dateField[0].selectionStart,
      displayText = fields.valOrText(this.$dateField),
      prediction = this._$predictDateField && fields.valOrText(this._$predictDateField),
      modifierCount = (event.ctrlKey ? 1 : 0) + (event.shiftKey ? 1 : 0) + (event.altKey ? 1 : 0) + (event.metaKey ? 1 : 0),
      pickerStartDate = this.value || this._referenceDate(),
      shiftDate = true;

    // Don't propagate tab to cell editor -> tab should focus time field
    if (this.hasTime &&
      this.mode === FormField.Mode.CELLEDITOR &&
      event.which === keys.TAB &&
      modifierCount === 0) {
      event.stopPropagation();
      return;
    }

    if (event.which === keys.TAB ||
      event.which === keys.SHIFT ||
      event.which === keys.HOME ||
      event.which === keys.END ||
      event.which === keys.CTRL ||
      event.which === keys.ALT) {
      // Default handling
      return;
    }

    if (event.which === keys.ENTER) {
      if (this.popup || this._$predictDateField) {
        // Close the picker and accept the current prediction (if available)
        this.acceptDate();
        this.closePopup();
        $.suppressEvent(event);
      }
      return;
    }

    if (event.which === keys.ESC) {
      if (this.popup) {
        // Close the picker, but don't do anything else
        this.closePopup();
        $.suppressEvent(event);
      }
      return;
    }

    if (event.which === keys.RIGHT && cursorPos === displayText.length) {
      // Move cursor one right and apply next char of the prediction
      if (prediction) {
        this._setDateDisplayText(prediction.substring(0, displayText.length + 1));
      }
      return;
    }

    if (event.which === keys.UP || event.which === keys.DOWN ||
      event.which === keys.PAGE_UP || event.which === keys.PAGE_DOWN) {
      if (displayText && !this._isDateValid()) {
        // If there is an error, try to parse the date. If it may be parsed, the error was likely a validation error.
        // In that case use the parsed date as starting point and not the for the user invisible value
        let parsedValue = this.isolatedDateFormat.parse(displayText, pickerStartDate);
        if (parsedValue) {
          pickerStartDate = parsedValue;
          this._setDateValid(true);
        }
      }
    }
    if (event.which === keys.PAGE_UP || event.which === keys.PAGE_DOWN) {
      if (!displayText || !this._isDateValid()) {
        // If input is empty or invalid, set picker to reference date
        pickerStartDate = this._referenceDate();
        if (this.hasTime) { // keep time part
          pickerStartDate = dates.combineDateTime(pickerStartDate, this.value || this._referenceDate());
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
        diffMonths = (event.which === keys.PAGE_UP ? -1 : 1);
        this.shiftSelectedDate(0, diffMonths, 0);
        this._updateDisplayText(this.getDatePicker().selectedDate);
      }
      $.suppressEvent(event);
      return;
    }

    if (event.which === keys.UP || event.which === keys.DOWN) {
      delta = (event.which === keys.UP ? -1 : 1);
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
          pickerStartDate = dates.combineDateTime(pickerStartDate, this.value || this._referenceDate());
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
    }
  }

  /**
   * Handle changed input. This method is fired when the field's content has been altered by a user
   * action (not by JS) such as pressing a character key, deleting a character using DELETE or
   * BACKSPACE, cutting or pasting text with ctrl-x / ctrl-v or mouse drag'n'drop.
   * Keys that don't alter the content (e.g. modifier keys, arrow keys, home, end etc.) are handled
   * in _onDateFieldKeyDown().
   */
  _onDateFieldInput(event) {
    let displayText = fields.valOrText(this.$dateField);

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
    this._removePredictErrorStatus();
    let datePrediction = this._predictDate(displayText); // this also updates the errorStatus
    if (datePrediction) {
      fields.valOrText(this._$predictDateField, datePrediction.text);
      this.openDatePopupAndSelect(datePrediction.date);
    } else {
      // No valid prediction!
      this._removePredictionFields();
    }
    this._updateDateHasText();

    // Hide the prediction field if input field is scrolled to the left. Otherwise, the
    // two fields would not be aligned correctly, which looks bad. This can only happen
    // when the fields are rather small, so the prediction would be of limited use anyway.
    // Unfortunately, most browsers don't fire 'scroll' events for input fields. Also,
    // when the 'input' even is fired, the scrollLeft() position sometimes has not been
    // updated yet, that's why we must use setTimeout() with a short delay.
    setTimeout(() => {
      if (this._$predictDateField) {
        this._$predictDateField.setVisible(this.$dateField.scrollLeft() === 0);
      }
    }, 50);
  }

  acceptInput() {
    let displayText = scout.nvl(this._readDisplayText(), '');

    let inputChanged = this._checkDisplayTextChanged(displayText);
    if (inputChanged) {
      this.parseAndSetValue(displayText);
    } else {
      let oldValue = this.value;
      this.parseAndSetValue(displayText);
      if (!dates.equals(this.value, oldValue)) {
        inputChanged = true;
      }
    }
    if (inputChanged) {
      this._triggerAcceptInput();
    }
  }

  /**
   * Clears the time field if date field is empty before accepting the input.<br/>
   * Don't delete invalid input from the time field.
   */
  acceptDate() {
    let invalid = this.containsStatus(ParsingFailedStatus);
    if (this.hasTime && !invalid && strings.empty(this.$dateField.val())) {
      this.$timeField.val('');
    }
    this.acceptInput();
  }

  /**
   * Clears the date field if time field is empty before accepting the input.<br/>
   * Don't delete invalid input from the time field.
   */
  acceptTime() {
    let invalid = this.containsStatus(ParsingFailedStatus);
    if (this.hasDate && !invalid && strings.empty(this.$timeField.val())) {
      this.$dateField.val('');
    }
    this.acceptInput();
  }

  acceptDateTime(acceptDate, acceptTime) {
    if (acceptDate) {
      this.acceptDate();
    } else if (acceptTime) {
      this.acceptTime();
    }
  }

  /**
   * Handle "navigation" keys, i.e. keys that don't emit visible characters. Character input is handled
   * in _onTimeFieldInput(), which is fired after 'keydown'.
   */
  _onTimeFieldKeyDown(event) {
    let delta = 0,
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
      this.mode === FormField.Mode.CELLEDITOR &&
      event.which === keys.TAB &&
      event.shiftKey &&
      modifierCount === 1) {
      event.stopPropagation();
      return;
    }

    if (event.which === keys.TAB ||
      event.which === keys.SHIFT ||
      event.which === keys.HOME ||
      event.which === keys.END ||
      event.which === keys.CTRL ||
      event.which === keys.ALT) {
      // Default handling
      return;
    }

    if (event.which === keys.ENTER) {
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

    if (event.which === keys.ESC) {
      if (this.popup) {
        // Close the picker, but don't do anything else
        this.closePopup();
        $.suppressEvent(event);
      }
      return;
    }

    if (event.which === keys.RIGHT && cursorPos === displayText.length) {
      // Move cursor one right and apply next char of the prediction
      if (prediction) {
        this._setTimeDisplayText(prediction.substring(0, displayText.length + 1));
      }
      return;
    }

    if (event.which === keys.UP || event.which === keys.DOWN) {
      delta = (event.which === keys.UP ? -1 : 1);
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
      } else {
        // without picker
        if (!this._tempTimeDate) {
          let timePrediction = this._predictTime(displayText); // this also updates the errorStatus
          if (timePrediction && timePrediction.date) {
            this._tempTimeDate = timePrediction.date;
          } else {
            this._tempTimeDate = this._referenceDate();
            shiftTime = false;
          }
        }
        if (shiftTime) {
          this._tempTimeDate = dates.shiftTime(this._tempTimeDate, diffHours, diffMinutes, diffSeconds);
        }
        if (this.hasDate) {
          // Combine _tempTimeDate with existing date part
          this._tempTimeDate = dates.combineDateTime(this.value || this._referenceDate(), this._tempTimeDate);
        }
        this._updateDisplayText(this._tempTimeDate);
        this._setTimeValid(true);
        $.suppressEvent(event);
      }
    }
  }

  /**
   * Handle changed input. This method is fired when the field's content has been altered by a user
   * action (not by JS) such as pressing a character key, deleting a character using DELETE or
   * BACKSPACE, cutting or pasting text with ctrl-x / ctrl-v or mouse drag'n'drop.
   * Keys that don't alter the content (e.g. modifier keys, arrow keys, home, end etc.) are handled
   * in _onTimeFieldKeyDown().
   */
  _onTimeFieldInput(event) {
    let displayText = this.$timeField.val();

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
    let timePrediction = this._predictTime(displayText); // this also updates the errorStatus
    if (timePrediction) {
      this._$predictTimeField.val(timePrediction.text);
      this.openTimePopupAndSelect(timePrediction.date);
    } else {
      // No valid prediction!
      this._tempTimeDate = null;
      this._removePredictionFields();
    }
    this._updateTimeHasText();

    // See comment for similar code in _onDateFieldInput()
    setTimeout(() => {
      if (this._$predictTimeField) {
        this._$predictTimeField.setVisible(this.$timeField.scrollLeft() === 0);
      }
    }, 50);
  }

  _onDatePickerDateSelect(event) {
    this._setNewDateTimeValue(this._newTimestampAsDate(event.date, this.value));
  }

  _onTimePickerTimeSelect(event) {
    this._setNewDateTimeValue(this._newTimestampAsDate(this.value, event.time));
  }

  _setNewDateTimeValue(newValue) {
    this._setDateValid(true);
    this._setTimeValid(true);
    this.setValue(newValue);
    this._triggerAcceptInput();
    this.closePopup();
  }

  _createPredictionField($inputField) {
    this.setSuppressStatus(FormField.SuppressStatus.ALL);
    let $predictionField = $inputField.clone()
      .addClass('predict')
      .attr('tabIndex', '-1')
      .insertBefore($inputField);
    if ($inputField.hasClass('has-error')) {
      $predictionField.addClass('has-error');
    }
    return $predictionField;
  }

  _removePredictionFields() {
    this.setSuppressStatus(null);
    if (this._$predictDateField) {
      this._$predictDateField.remove();
      this._$predictDateField = null;
    }
    if (this._$predictTimeField) {
      this._$predictTimeField.remove();
      this._$predictTimeField = null;
    }
  }

  _setDateDisplayText(displayText) {
    this.dateDisplayText = displayText;
    this._updateDisplayTextProperty();
    if (this.rendered) {
      this._renderDateDisplayText();
    }
  }

  _setTimeDisplayText(displayText) {
    this.timeDisplayText = displayText;
    this._updateDisplayTextProperty();
    if (this.rendered) {
      this._renderTimeDisplayText();
    }
  }

  _computeDisplayText(dateDisplayText, timeDisplayText) {
    let dateText = dateDisplayText || '',
      timeText = timeDisplayText || '';

    // do not use strings.join which ignores empty components
    let displayText = (this.hasDate ? dateText : '') + (this.hasDate && this.hasTime ? '\n' : '') + (this.hasTime ? timeText : '');

    // empty display text should always be just an empty string
    if (displayText === '\n') {
      displayText = '';
    }
    return displayText;
  }

  _splitDisplayText(displayText) {
    let dateText = '',
      timeText = '';

    if (strings.hasText(displayText)) {
      let parts = displayText.split('\n');
      dateText = this.hasDate ? parts[0] : '';
      timeText = this.hasTime ? (this.hasDate ? parts[1] : parts[0]) : '';
    }
    return {
      dateText: dateText,
      timeText: timeText
    };
  }

  _updateDisplayTextProperty() {
    this._setProperty('displayText', this._computeDisplayText(this.dateDisplayText, this.timeDisplayText));
  }

  /**
   * @override ValueField.js
   */
  aboutToBlurByMouseDown(target) {
    let dateFieldActive, timeFieldActive, eventOnDatePicker, eventOnTimePicker,
      eventOnDateField = this.$dateField ? (this.$dateField.isOrHas(target) || this.$dateFieldIcon.isOrHas(target) || (this.$dateClearIcon && this.$dateClearIcon.isOrHas(target))) : false,
      eventOnTimeField = this.$timeField ? (this.$timeField.isOrHas(target) || this.$timeFieldIcon.isOrHas(target) || (this.$timeClearIcon && this.$timeClearIcon.isOrHas(target))) : false,
      eventOnPopup = this.popup && this.popup.$container.isOrHas(target),
      eventOnStatus = this.fieldStatus && this.fieldStatus.$container.isOrHas(target),
      datePicker = this.getDatePicker(),
      timePicker = this.getTimePicker();

    if (!eventOnDateField && !eventOnTimeField && !eventOnPopup && !eventOnStatus) {
      // event outside this field.
      dateFieldActive = focusUtils.isActiveElement(this.$dateField);
      timeFieldActive = focusUtils.isActiveElement(this.$timeField);
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
  }

  /**
   * Returns null if both arguments are not set. Otherwise, this.value or the current date
   * is used as basis and the given arguments are applied to that date. The result is returned.
   */
  _newTimestampAsDate(date, time) {
    let result = null;
    if (date || time) {
      result = this.value || this._referenceDate();
      if (date) {
        result = dates.combineDateTime(date, result);
      }
      if (time) {
        result = dates.combineDateTime(result, time);
      }
    }
    return result;
  }

  /**
   * Returns the reference date for this date field, which is used in various places (i.e. opening the date picker, analyzing user inputs).
   *
   * The reference date is either (in that order):
   * - the model's "auto timestamp" (as date), or
   * - the current date/time
   */
  _referenceDate() {
    let referenceDate = this.autoDate || dates.ceil(dates.newDate(), this.timePickerResolution);
    if (this.autoDate) {
      referenceDate = this.autoDate;
    } else if (this.hasTime) {
      referenceDate = dates.ceil(dates.newDate(), this.timePickerResolution);
    } else {
      referenceDate = dates.trunc(dates.newDate());
    }
    if (this.allowedDates) {
      referenceDate = this._findAllowedReferenceDate(referenceDate);
    }
    return referenceDate;
  }

  /**
   * Find nearest allowed date which is equals or greater than the current referenceDate.
   */
  _findAllowedReferenceDate(referenceDate) {
    let i, allowedDate;
    // 1st: try to find a date which is equals or greater than the referenceDate (today)
    for (i = 0; i < this.allowedDates.length; i++) {
      allowedDate = this.allowedDates[i];
      if (dates.compare(allowedDate, referenceDate) >= 0) {
        return allowedDate;
      }
    }
    // 2nd: try to find an allowed date in the past
    for (i = this.allowedDates.length - 1; i >= 0; i--) {
      allowedDate = this.allowedDates[i];
      if (dates.compare(allowedDate, referenceDate) <= 0) {
        return allowedDate;
      }
    }
    return referenceDate;
  }

  openDatePopup(date) {
    if (this.popup) {
      // already open
      return;
    }

    this.popup = this.createDatePopup();
    this.popup.open();
    this.$dateField.addClass('focused');
    this.popup.one('destroy', event => {
      // Removing the class must happen before _onPopupDestroy() is called, otherwise the date field no longer exists,
      // because in touch mode _onPopupDestroy() destroys the date field.
      this.$dateField.removeClass('focused');
      this._onPopupDestroy(event);
      this.popup = null;
    });
    this.getDatePicker().on('dateSelect', this._onDatePickerDateSelect.bind(this));
  }

  closePopup() {
    if (this.popup) {
      this.popup.close();
    }
  }

  toggleDatePopup() {
    $.log.isInfoEnabled() && $.log.info('(DateField#toggleDatePopup) popupOpen=', !!this.popup);
    if (this.popup) {
      this.closePopup();
    } else {
      this.openDatePopupAndSelect(this.value);
    }
  }

  openTimePopup(date) {
    if (!this.hasTimePopup || this.popup) {
      // already open
      return;
    }
    this.popup = this.createTimePopup();
    this.popup.open();
    this.$timeField.addClass('focused');
    this.popup.one('destroy', event => {
      // Removing the class must happen before _onPopupDestroy() is called, otherwise the date field no longer exists,
      // because in touch mode _onPopupDestroy() destroys the date field.
      this.$timeField.removeClass('focused');
      this._onPopupDestroy(event);
      this.popup = null;
    });
    this.getTimePicker().on('timeSelect', this._onTimePickerTimeSelect.bind(this));
  }

  toggleTimePopup() {
    $.log.isInfoEnabled() && $.log.info('(DateField#toggleTimePopup) popupOpen=', !!this.popup);
    if (this.popup) {
      this.closePopup();
    } else {
      this.openTimePopupAndSelect(this.value);
    }
  }

  _parseValue(displayText) {
    let parts = this._splitDisplayText(displayText);
    let dateText = parts.dateText;
    let datePrediction = {};
    let timeText = parts.timeText;
    let timePrediction = {};
    let success = true;

    this._removePredictErrorStatus();
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

    // Error status was already set by _predict functions, throw this typed Status so DateField knows it must
    // not set the error status again when the parse error is catched
    if (!success) {
      throw new DatePredictionFailedStatus();
    }

    // parse success -> return new value
    if (datePrediction.date || timePrediction.date) {
      return this._newTimestampAsDate(datePrediction.date, timePrediction.date);
    }
    return null;
  }

  /**
   * Don't add error if it is a DatePredictionFailedStatus because the predict function
   * has already added a parsing error.
   */
  _addParsingFailedErrorStatus(displayText, error) {
    if (error instanceof DatePredictionFailedStatus) {
      return;
    }
    super._addParsingFailedErrorStatus(displayText, error);
  }

  /**
   * @returns null if input is invalid, otherwise an object with properties 'date' and 'text'
   */
  _predictDate(inputText) {
    inputText = inputText || '';

    // "Date calculations"
    let m = inputText.match(/^([+-])(\d*)$/);
    if (m) {
      let now = dates.newDate();
      let daysToAdd = Number(m[1] + (m[2] || '0'));
      now.setDate(now.getDate() + daysToAdd);
      if (isNaN(now.valueOf()) || now.getDate() < 0) { // Some older browsers don't set NaN but return invalid values
        this._setDateValid(false);
        return null;
      }
      this._setDateValid(true);
      return {
        date: now,
        text: inputText
      };
    }

    let analyzeInfo = this._analyzeInputAsDate(inputText, this.value || this._referenceDate());
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

    let predictedDate = analyzeInfo.predictedDate;
    let predictionFormat = new DateFormat(this.isolatedDateFormat.locale, analyzeInfo.parsedPattern);
    let predictedDateFormatted = predictionFormat.format(predictedDate, true);

    // If predicted date format starts with validatedText, ensure that the capitalization matches.
    // Example: input = 'frid', predicted = 'Friday, 1.10.2014' --> return 'friday, 1.10.2014')
    m = predictedDateFormatted.match(new RegExp('^' + strings.quote(inputText) + '(.*)$', 'i'));
    if (m) {
      predictedDateFormatted = inputText + m[1];
    }

    this._setDateValid(true);
    return {
      date: predictedDate,
      text: predictedDateFormatted
    };
  }

  /**
   * @returns null if input is invalid, otherwise an object with properties 'date' and 'text'
   */
  _predictTime(inputText) {
    inputText = inputText || '';

    let analyzeInfo = this._analyzeInputAsTime(inputText, this.value || this._referenceDate());
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

    let predictedDate = analyzeInfo.predictedDate;
    let predictionFormat = new DateFormat(this.isolatedTimeFormat.locale, analyzeInfo.parsedPattern);
    let predictedTimeFormatted = predictionFormat.format(predictedDate, true);

    // If predicted date format starts with validatedText, ensure that the capitalization matches.
    // Example: input = 'frid', predicted = 'Friday, 1.10.2014' --> return 'friday, 1.10.2014')
    let m = predictedTimeFormatted.match(new RegExp('^' + strings.quote(inputText) + '(.*)$', 'i'));
    if (m) {
      predictedTimeFormatted = inputText + m[1];
    }

    this._setTimeValid(true);
    return {
      date: predictedDate,
      text: predictedTimeFormatted
    };
  }

  _analyzeInputAsDate(inputText, startDate) {
    return this.isolatedDateFormat.analyze(inputText, startDate);
  }

  _analyzeInputAsTime(inputText, startDate) {
    return this.isolatedTimeFormat.analyze(inputText, startDate);
  }

  /**
   * This method updates the parts (date, time) of the error status.
   */
  _setErrorStatusPart(property, valid) {

    // check if date/time error exists
    let status = null;
    let storedStatus = null;
    if (this.errorStatus) {
      storedStatus = arrays.find(this.errorStatus.asFlatList(), DateField.PARSE_ERROR_PREDICATE);
    }

    if (storedStatus) {
      status = storedStatus;
    } else {
      status = new ParsingFailedStatus({
        message: this.session.text('ui.InvalidDate'),
        severity: Status.Severity.ERROR,
        code: DateField.ErrorCode.PARSE_ERROR
      });
    }

    if (valid) {
      delete status[property];
    } else {
      status[property] = true;
    }

    if (!status.hasOwnProperty('invalidDate') && !status.hasOwnProperty('invalidTime')) {
      status = null;
    }

    if (status && !storedStatus) {
      this.addErrorStatus(status);
    } else if (!status && storedStatus) {
      this._removePredictErrorStatus();
    } // else: just update existing error
  }

  _removePredictErrorStatus() {
    this.removeErrorStatusByPredicate(DateField.PARSE_ERROR_PREDICATE);
  }

  /**
   * @override
   */
  _createInvalidValueStatus(statusType, value, error) {
    let errorStatus = super._createInvalidValueStatus(statusType, value, error);
    // Set date and time to invalid, otherwise isDateValid and isTimeValid return false even though there is a validation error
    errorStatus.invalidDate = true;
    errorStatus.invalidTime = true;
    errorStatus.code = DateField.ErrorCode.PARSE_ERROR;
    return errorStatus;
  }

  _setDateValid(valid) {
    this._setErrorStatusPart('invalidDate', valid);
  }

  _setTimeValid(valid) {
    this._setErrorStatusPart('invalidTime', valid);
  }

  _isErrorStatusPartValid(property) {
    if (!this.errorStatus) {
      return true;
    }

    // return false if one of the status has the invalid* property
    return !this.errorStatus.asFlatList().some(status => {
      return !!status[property];
    });
  }

  _isDateValid() {
    return this._isErrorStatusPartValid('invalidDate');
  }

  _isTimeValid() {
    return this._isErrorStatusPartValid('invalidTime');
  }

  /**
   * Method invoked if being rendered within a cell-editor (mode='scout.FormField.Mode.CELLEDITOR'), and once the editor finished its rendering.
   */
  onCellEditorRendered(options) {
    if (options.openFieldPopup) {
      if (this.hasDate && !this.hasTime) {
        this.openDatePopupAndSelect(this.value);
      } else if (!this.hasDate && this.hasTime) {
        this.openTimePopupAndSelect(this.value);
      } else if (!Device.get().supportsOnlyTouch()) {
        // If date AND time are active, don't open popup on touch devices because the user has to choose first what he wants to edit
        this.openDatePopupAndSelect(this.value);
      }
    }
    if (this.touchMode) {
      this._cellEditorPopup = options.cellEditorPopup;
    }
  }

  _onPopupDestroy(event) {
    if (!this.touchMode || !this._cellEditorPopup) {
      return;
    }
    if (this.hasDate && this.hasTime) {
      // If date and time is shown, user might want to change both, let him close the cell editor when he is finished
      return;
    }
    // Close cell editor when touch popup closes
    this._cellEditorPopup.completeEdit();
    this._cellEditorPopup = null;
  }

  /**
   * @override FormField.js
   */
  prepareForCellEdit(opts) {
    opts = opts || {};
    super.prepareForCellEdit(opts);

    this.$field.removeClass('cell-editor-field first last');
    if (this.$dateField) {
      this.addCellEditorFieldCssClasses(this.$dateField, opts);
    }
    if (this.$timeField) {
      if (!this.$dateField) {
        opts.cssClass = '';
      }
      this.addCellEditorFieldCssClasses(this.$timeField, opts);
    }
  }

  /**
   * @returns DatePicker instance from popup, because the property name is different
   *    for DatePickerPopup and DatePickerTouchPopup.
   */
  getDatePicker() {
    if (this.popup && this.popup.getDatePicker) {
      return this.popup.getDatePicker();
    }
  }

  /**
   * Opens picker and selects date
   *
   * @param date
   *          optional, Date to pass to the date picker. If no date is specified, the reference date
   *          is preselected (not selected!).
   */
  openDatePopupAndSelect(date) {
    this.openDatePopup();
    if (!date) {
      this.preselectDate(this._referenceDate(), false);
    } else {
      this.selectDate(date, false);
    }
  }

  preselectDate(date, animated) {
    let datePicker = this.getDatePicker();
    if (datePicker) {
      datePicker.preselectDate(date, animated);
    }
  }

  selectDate(date, animated) {
    let datePicker = this.getDatePicker();
    if (datePicker) {
      datePicker.selectDate(date, animated);
    }
  }

  /**
   * @returns DatePicker instance from popup, because the property name is different
   *    for DatePickerPopup and DatePickerTouchPopup.
   */
  getTimePicker() {
    if (this.popup && this.popup.getTimePicker) {
      return this.popup.getTimePicker();
    }
  }

  /**
   * Opens picker and selects date
   *
   * @param date
   *          optional, Date to pass to the date picker. If no date is specified, the reference date
   *          is preselected (not selected!).
   */
  openTimePopupAndSelect(time) {
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
  }

  preselectTime(time) {
    let timePicker = this.getTimePicker();
    if (timePicker) {
      timePicker.preselectTime(time);
    }
  }

  selectTime(time) {
    let timePicker = this.getTimePicker();
    if (timePicker) {
      timePicker.selectTime(time);
    }
  }

  shiftSelectedDate(years, months, days) {
    this.openDatePopup();
    this.getDatePicker().shiftSelectedDate(years, months, days);
  }

  shiftSelectedTime(hourUnits, minuteUnits, secondUnits) {
    this.openTimePopup();
    this.getTimePicker().shiftSelectedTime(hourUnits, minuteUnits, secondUnits);
  }

  _formatValue(value) {
    let
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
  }

  /**
   * @override
   */
  _renderFieldStyle() {
    super._renderFieldStyle();
    this._renderFieldStyleInternal(this.$dateField);
    this._renderFieldStyleInternal(this.$timeField);
  }

  /**
   * @override
   */
  _renderDisabledStyle() {
    super._renderDisabledStyle();
    this._renderDisabledStyleInternal(this.$dateField);
    this._renderDisabledStyleInternal(this.$timeField);
  }

  /**
   * @override
   */
  _triggerAcceptInput() {
    let event = {
      displayText: this.displayText,
      errorStatus: this.errorStatus,
      value: this.value
    };
    this.trigger('acceptInput', event);
  }
}

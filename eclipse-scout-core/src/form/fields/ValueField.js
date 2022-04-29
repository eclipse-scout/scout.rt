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
import {arrays, Event, focusUtils, FormField, objects, ParsingFailedStatus, scout, Status, strings, ValidationFailedStatus} from '../../index';
import $ from 'jquery';

/**
 * @abstract
 */
export default class ValueField extends FormField {

  constructor() {
    super();
    this.clearable = ValueField.Clearable.FOCUSED;
    this.displayText = null;
    this.formatter = this._formatValue.bind(this);
    this.hasText = false;
    this.initialValue = null;
    this.invalidValueMessageKey = 'InvalidValueMessageX';
    this.parser = this._parseValue.bind(this);
    this.value = null;
    this.validators = [];
    this.validators.push(this._validateValue.bind(this));
    this._updateDisplayTextPending = false;

    this.$clearIcon = null;

    this._addCloneProperties(['value', 'displayText', 'clearable']);
  }

  static Clearable = {
    /**
     * The clear icon is showed when the field has text.
     */
    ALWAYS: 'always',
    /**
     * The clear icon will be showed when the field is focused and has text.
     */
    FOCUSED: 'focused',
    /**
     * Never show the clear icon.
     */
    NEVER: 'never'
  };

  _init(model) {
    super._init(model);
    if (this.validator) {
      // Validators are kept in a list, allow a single validator to be set in the model, similar to parser and formatter.
      // setValidator will add the new validator to this.validators and remove the other ones.
      this.setValidator(this.validator);
      delete this.validator;
    }
    this._initValue(this.value);
  }

  /**
   * Override this method if you need to influence the value initialization (e.g. do something before the value is initially set)
   */
  _initValue(value) {
    // Delete value first, value may be invalid and must not be set
    this.value = null;
    this._setValue(value);
    this._updateEmpty();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderDisplayText();
    this._renderClearable();
    this._renderHasText();
  }

  _remove() {
    super._remove();
    this.$clearIcon = null;
  }

  /**
   * The default impl. is a NOP, because not every ValueField has a sensible display text.
   */
  _renderDisplayText() {
    this._updateHasText();
  }

  /**
   * The default impl. returns an empty string, because not every ValueField has a sensible display text.
   */
  _readDisplayText() {
    return '';
  }

  _onClearIconMouseDown(event) {
    this.clear();
    event.preventDefault();
  }

  _onFieldBlur() {
    super._onFieldBlur();
    this.acceptInput(false);
  }

  /**
   * Accepts the current input and writes it to the model.
   * <p>
   * This method is typically called by the _onBlur() function of the field, but may actually be called from anywhere (e.g. button, actions, cell editor, etc).
   * It is also called by the _aboutToBlurByMouseDown() function, which is required because our Ok- and Cancel-buttons are not focusable (thus _onBlur() is
   * never called) but changes in the value-field must be sent to the server anyway when a button is clicked.
   * <p>
   * The default reads the display text using this._readDisplayText() and writes it to the model by calling _triggerAcceptInput().
   * If subclasses don't have a display-text or want to write another state to the server, they may override this method.
   */
  acceptInput(whileTyping) {
    whileTyping = !!whileTyping; // cast to boolean
    let displayText = scout.nvl(this._readDisplayText(), '');

    // trigger only if displayText has really changed
    if (this._checkDisplayTextChanged(displayText, whileTyping)) {
      // Don't call setDisplayText() to prevent re-rendering of display text (which is unnecessary and
      // might change the cursor position). Don't call _callSetProperty() as well, as this eventually
      // executes this._setDisplayText(), which updates the value.
      this._setProperty('displayText', displayText);
      if (!whileTyping) {
        this.parseAndSetValue(displayText);
      }
      // Display text may be formatted -> Use this.displayText
      this._triggerAcceptInput(whileTyping);
    }
  }

  parseAndSetValue(displayText) {
    this.removeErrorStatus(ParsingFailedStatus);
    try {
      let event = new Event({
        displayText: displayText
      });
      this.trigger('parse', event);
      if (!event.defaultPrevented) {
        let parsedValue = this.parseValue(displayText);
        this.setValue(parsedValue);
      }
    } catch (error) {
      this._parsingFailed(displayText, error);
    }
  }

  _parsingFailed(displayText, error) {
    $.log.isDebugEnabled() && $.log.debug('Parsing failed for field with id ' + this.id, error);
    let event = new Event({
      displayText: displayText,
      error: error
    });
    this.trigger('parseError', event);
    if (!event.defaultPrevented) {
      this._addParsingFailedErrorStatus(displayText, error);
    }
  }

  _addParsingFailedErrorStatus(displayText, error) {
    let status = this._createParsingFailedStatus(displayText, error);
    this.addErrorStatus(status);
  }

  _createParsingFailedStatus(displayText, error) {
    return this._createInvalidValueStatus('ParsingFailedStatus', displayText, error);
  }

  /**
   * Replaces the existing parser. The parser is called during {@link #parseValue(displayText)}.
   * <p>
   * Remember calling the default parser passed as parameter to the parse function, if needed.
   * @param {function} parser the new parser. If null, the default parser is used.
   */
  setParser(parser) {
    this.setProperty('parser', parser);
    if (this.initialized) {
      this.parseAndSetValue(this.displayText);
    }
  }

  _setParser(parser) {
    if (!parser) {
      parser = this._parseValue.bind(this);
    }
    this._setProperty('parser', parser);
  }

  /**
   * @returns {*} the parsed value
   * @throws a message, a Status or an error if the parsing fails
   */
  parseValue(displayText) {
    let defaultParser = this._parseValue.bind(this);
    return this.parser(displayText, defaultParser);
  }

  /**
   * @throws a message, a Status or an error if the parsing fails
   */
  _parseValue(displayText) {
    return displayText;
  }

  _checkDisplayTextChanged(displayText, whileTyping) {
    let oldDisplayText = scout.nvl(this.displayText, '');
    return displayText !== oldDisplayText;
  }

  /**
   * Method invoked upon a mousedown click with this field as the currently focused control, and is invoked just before the mousedown click will be interpreted.
   * However, the mousedown target must not be this control, but any other control instead.
   *
   * The default implementation checks, whether the click occurred outside this control, and if so invokes 'ValueField.acceptInput'.
   *
   * @param target
   *        the DOM target where the mouse down event occurred.
   */
  aboutToBlurByMouseDown(target) {
    let eventOnField = this.isFocusOnField(target);
    if (!eventOnField) {
      this.acceptInput(); // event outside this value field.
    }
  }

  /**
   * @override
   */
  isFocused() {
    return this.rendered && focusUtils.isActiveElement(this.$field);
  }

  isFocusOnField(target) {
    return this.$field.isOrHas(target) || (this.$clearIcon && this.$clearIcon.isOrHas(target));
  }

  _triggerAcceptInput(whileTyping) {
    let event = {
      displayText: this.displayText,
      whileTyping: !!whileTyping
    };
    this.trigger('acceptInput', event);
  }

  setDisplayText(displayText) {
    this.setProperty('displayText', displayText);
  }

  _updateHasText() {
    this.setHasText(strings.hasText(this._readDisplayText()));
  }

  setHasText(hasText) {
    this.setProperty('hasText', hasText);
  }

  _renderHasText() {
    if (this.$field) {
      this.$field.toggleClass('has-text', this.hasText);
    }
    this.$container.toggleClass('has-text', this.hasText);
  }

  setClearable(clearableStyle) {
    this.setProperty('clearable', clearableStyle);
  }

  _renderClearable() {
    if (this.isClearable()) {
      if (!this.$clearIcon) {
        this.addClearIcon();
      }
    } else {
      if (this.$clearIcon) {
        this.$clearIcon.remove();
        this.$clearIcon = null;
      }
    }
    this.invalidateLayoutTree(false);
    this._updateClearableStyles();
  }

  _updateClearableStyles() {
    this.$container.removeClass('clearable-always clearable-focused');
    if (this.isClearable()) {
      if (this.clearable === ValueField.Clearable.ALWAYS) {
        this.$container.addClass('clearable-always');
      } else if (this.clearable === ValueField.Clearable.FOCUSED) {
        this.$container.addClass('clearable-focused');
      }
    }
  }

  isClearable() {
    return this.clearable === ValueField.Clearable.ALWAYS || this.clearable === ValueField.Clearable.FOCUSED;
  }

  /**
   * Clears the display text and the value to null.
   */
  clear() {
    this._clear();
    this._updateHasText();
    this.acceptInput();
    this._triggerClear();
  }

  _clear() {
    // to be implemented by sub-classes
  }

  _triggerClear() {
    this.trigger('clear');
  }

  setValue(value) {
    // Same code as in Widget#setProperty expect for the equals check
    // -> _setValue has to be called even if the value is equal so that update display text will be executed
    value = this._prepareProperty('value', value);
    if (this.rendered) {
      this._callRemoveProperty('value');
    }
    this._callSetProperty('value', value);
    if (this.rendered) {
      this._callRenderProperty('value');
    }
  }

  /**
   * Resets the value to its initial value.
   */
  resetValue() {
    this.setValue(this.initialValue);
  }

  /**
   * Default does nothing because the value field does not know which type the concrete field uses.
   * May be overridden to cast the value to the required type.
   * @returns {*} the value with the correct type.
   */
  _ensureValue(value) {
    return value;
  }

  _setValue(value) {
    // When widget is initialized with a given errorStatus and a value -> don't remove the error
    // status. This is a typical case for Scout Classic: field has a ParsingFailedError and user
    // hits reload.
    if (this.initialized) {
      this.removeErrorStatus(ParsingFailedStatus);
      this.removeErrorStatus(ValidationFailedStatus);
    }
    let oldValue = this.value;
    let typedValue = null;
    try {
      typedValue = this._ensureValue(value);
      this.value = this.validateValue(typedValue);
    } catch (error) {
      typedValue = typedValue || value;
      this._validationFailed(typedValue, error);
      return;
    }

    this._updateDisplayText();
    if (this._valueEquals(oldValue, this.value)) {
      return;
    }

    this._valueChanged();
    this._updateTouched();
    this._updateEmpty();
    this.triggerPropertyChange('value', oldValue, this.value);
  }

  _valueEquals(valueA, valueB) {
    return objects.equals(valueA, valueB);
  }

  /**
   * Is called after a value is changed. May be implemented by subclasses. The default does nothing.
   */
  _valueChanged() {
    // NOP
  }

  /**
   * Validates the value by executing the validators. If a new value is the result, it will be set.
   */
  validate() {
    this._setValue(this.value);
  }

  /**
   * @param {function} validator the validator to be added.
   *     A validator is a function that accepts a raw value and either returns the validated value or
   *     throws an Error, a Status or an error message (string) if the value is invalid.
   * @param {boolean} [revalidate] True, to revalidate the value, false to just add the validator and do nothing else. Default is true.
   */
  addValidator(validator, revalidate) {
    let validators = this.validators.slice();
    validators.push(validator);
    this.setValidators(validators, revalidate);
  }

  /**
   * @param {function} validator the validator to be removed
   * @param {boolean} [revalidate] True, to revalidate the value, false to just remove the validator and do nothing else. Default is true.
   */
  removeValidator(validator, revalidate) {
    let validators = this.validators.slice();
    arrays.remove(validators, validator);
    this.setValidators(validators, revalidate);
  }

  /**
   * Replaces all existing validators with the given one. If you want to add multiple validators, use {@link #addValidator}.
   * <p>
   * Remember calling the default validator which is passed as parameter to the validate function, if needed.
   *
   * @param {function} validator the new validator which replaces every other. If null, the default validator is used.
   *     A validator is a function that accepts a raw value and either returns the validated value or
   *     throws an Error, a Status or an error message (string) if the value is invalid.
   */
  setValidator(validator, revalidate) {
    if (!validator) {
      validator = this._validateValue.bind(this);
    }
    let validators = [];
    if (validator) {
      validators = [validator];
    }
    this.setValidators(validators, revalidate);
  }

  setValidators(validators, revalidate) {
    this.setProperty('validators', validators);
    if (this.initialized && scout.nvl(revalidate, true)) {
      this.validate();
    }
  }

  /**
   * @param the value to be validated
   * @returns {*} the validated value
   * @throws a message, a Status or an error if the validation fails
   */
  validateValue(value) {
    let defaultValidator = this._validateValue.bind(this);
    this.validators.forEach(validator => {
      value = validator(value, defaultValidator);
    });
    value = scout.nvl(value, null); // Ensure value is never undefined (necessary for _updateTouched and should make it easier generally)
    return value;
  }

  /**
   * @returns {*} the validated value
   * @throws a message, a Status or an error if the validation fails
   */
  _validateValue(value) {
    if (typeof value === 'string' && value === '') {
      // Convert empty string to null.
      // Not using strings.nullIfEmpty is by purpose because it also removes white space characters which may not be desired here
      value = null;
    }
    return value;
  }

  _validationFailed(value, error) {
    $.log.isDebugEnabled() && $.log.debug('Validation failed for field with id ' + this.id, error);
    let status = this._createValidationFailedStatus(value, error);
    this.addErrorStatus(status);
    this._updateDisplayText(value);
  }

  _createValidationFailedStatus(value, error) {
    return this._createInvalidValueStatus('ValidationFailedStatus', value, error);
  }

  /**
   * @param {string} statusType
   * @returns {Status}
   */
  _createInvalidValueStatus(statusType, value, error) {
    let statusFunc = Status.classForName(statusType);
    // type of status is correct
    if (error instanceof statusFunc) {
      return error;
    }
    let message, severity = Status.Severity.ERROR;
    if (error instanceof Status) {
      // its a Status, but it has the wrong specific type
      message = error.message;
      severity = error.severity;
    } else if (typeof error === 'string') {
      // convert string to status
      message = error;
    } else {
      // create status with default message
      message = this.session.text(this.invalidValueMessageKey, value);
    }
    return scout.create(statusType, {
      message: message,
      severity: severity
    });
  }

  _updateDisplayText(value) {
    if (!this.initialized && !objects.isNullOrUndefined(this.displayText)) {
      // If a displayText is provided initially, use that text instead of using formatValue to generate a text based on the value
      return;
    }
    value = scout.nvl(value, this.value);
    let returned = this.formatValue(value);
    if (objects.isPromise(returned)) {
      this._updateDisplayTextPending = true;
      // Promise is returned -> set display text later
      returned
        .done(this.setDisplayText.bind(this))
        .fail(() => {
          // If display text was updated in the meantime, don't override the text with an empty string
          if (this._updateDisplayTextPending) {
            this.setDisplayText('');
          }
          $.log.isInfoEnabled() && $.log.info('Could not resolve display text for value: ' + value);
        })
        .always(() => {
          this._updateDisplayTextPending = false;
        });
    } else {
      this.setDisplayText(returned);
      this._updateDisplayTextPending = false;
    }
  }

  /**
   * Replaces the existing formatter. The formatter is called during {@link #formatValue(value)}.
   * <p>
   * Remember calling the default formatter which is passed as parameter to the format function, if needed.
   * @param {function} formatter the new formatter. If null, the default formatter is used.
   */
  setFormatter(formatter) {
    this.setProperty('formatter', formatter);
    if (this.initialized) {
      this.validate();
    }
  }

  _setFormatter(formatter) {
    if (!formatter) {
      formatter = this._formatValue.bind(this);
    }
    this._setProperty('formatter', formatter);
  }

  /**
   * @returns {string|Promise} the formatted display text
   */
  formatValue(value) {
    let defaultFormatter = this._formatValue.bind(this);
    return this.formatter(value, defaultFormatter);
  }

  /**
   * @returns {string|Promise} the formatted string or a promise
   */
  _formatValue(value) {
    return scout.nvl(value, '') + '';
  }

  _updateTouched() {
    this.touched = !this._valueEquals(this.value, this.initialValue);
  }

  addClearIcon($parent) {
    if (!$parent) {
      $parent = this.$container;
    }
    this.$clearIcon = $parent.appendSpan('clear-icon unfocusable text-field-icon action')
      .on('mousedown', this._onClearIconMouseDown.bind(this));
  }

  addContainer($parent, cssClass, layout) {
    super.addContainer($parent, cssClass, layout);
    this.$container.addClass('value-field');
  }

  addField($field) {
    super.addField($field);
    this.$field.data('valuefield', this);
  }

  markAsSaved() {
    super.markAsSaved();
    this.initialValue = this.value;
  }

  /**
   * @override
   */
  _updateEmpty() {
    this.empty = this.value === null || this.value === undefined;
  }

  // ==== static helper methods ==== //

  /**
   * Invokes 'ValueField.aboutToBlurByMouseDown' on the currently active value field.
   * This method has no effect if another element is the focus owner.
   */
  static invokeValueFieldAboutToBlurByMouseDown(target) {
    let activeValueField = this._getActiveValueField(target);
    if (activeValueField) {
      activeValueField.aboutToBlurByMouseDown(target);
    }
  }

  /**
   * Invokes 'ValueField.acceptInput' on the currently active value field.
   * This method has no effect if another element is the focus owner.
   */
  static invokeValueFieldAcceptInput(target) {
    let activeValueField = this._getActiveValueField(target);
    if (activeValueField) {
      activeValueField.acceptInput();
    }
  }

  /**
   * Returns the currently active value field, or null if another element is active.
   * Also, if no value field currently owns the focus, its parent is checked to be a value field and is returned accordingly.
   * That is used in DateField.js with multiple input elements.
   */
  static _getActiveValueField(target) {
    let $activeElement = $(target).activeElement(),
      activeWidget = scout.widget($activeElement);
    if (activeWidget instanceof ValueField && activeWidget.enabledComputed) {
      return activeWidget;
    }
    return null;
  }
}

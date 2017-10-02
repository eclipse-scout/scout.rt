/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * @abstract
 */
scout.ValueField = function() {
  scout.ValueField.parent.call(this);
  this.clearable = scout.ValueField.Clearable.FOCUSED;
  this.displayText = null;
  this.formatter = this._formatValue.bind(this);
  this.hasText = false;
  this.initialValue = null;
  this.invalidValueMessageKey = 'InvalidValueMessageX';
  this.parser = this._parseValue.bind(this);
  this.value = null;
  this.validators = [];
  this.validators.push(this._validateValue.bind(this));

  this.$clearIcon = null;

  this._addCloneProperties(['value', 'displayText']);
};
scout.inherits(scout.ValueField, scout.FormField);

scout.ValueField.Clearable = {
  /**
   * The clear icon is showed when the field has text.
   */
  ALWAYS: 'clearableAlways',
  /**
   * The clear icon will be showed when the field is focused and has text.
   */
  FOCUSED: 'clearableFocused',
  /**
   * Never show the clear icon.
   */
  NEVER: 'clearableNever'
};

scout.ValueField.prototype._init = function(model) {
  scout.ValueField.parent.prototype._init.call(this, model);
  if (this.validator) {
    // Validators are kept in a list, allow a single validator to be set in the model, similar to parser and formatter.
    // setValidator will add the new validator to this.validators and remove the other ones.
    this.setValidator(this.validator);
    delete this.validator;
  }
  this._initValue(this.value);
};

/**
 * Override this method if you need to influence the value initialization (e.g. do something before the value is initially set)
 */
scout.ValueField.prototype._initValue = function(value) {
  // Delete value first, value may be invalid and must not be set
  this.value = null;
  this._setValue(value);
  this._updateEmpty();
};

scout.ValueField.prototype._renderProperties = function() {
  scout.ValueField.parent.prototype._renderProperties.call(this);
  this._renderDisplayText();
  this._renderClearable();
  this._renderHasText();
};

/**
 * The default impl. is a NOP, because not every ValueField has a sensible display text.
 */
scout.ValueField.prototype._renderDisplayText = function() {
  this._updateHasText();
};


scout.ValueField.prototype._remove = function(){
  scout.ValueField.parent.prototype._remove.call(this);
  this.$clearIcon = null;
};

/**
 * The default impl. returns an empty string, because not every ValueField has a sensible display text.
 */
scout.ValueField.prototype._readDisplayText = function() {
  return '';
};

scout.ValueField.prototype._onClearIconMouseDown = function(event) {
  this.clear();
  event.preventDefault();
};

scout.ValueField.prototype._onFieldFocus = function(event) {
  this.setFocused(true);
};

scout.ValueField.prototype._onFieldBlur = function() {
  this.acceptInput(false);
  this.setFocused(false);
};

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
scout.ValueField.prototype.acceptInput = function(whileTyping) {
  whileTyping = !!whileTyping; // cast to boolean
  var displayText = scout.nvl(this._readDisplayText(), '');

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
};

scout.ValueField.prototype.parseAndSetValue = function(displayText) {
  this.clearErrorStatus();
  try {
    var event = new scout.Event({
      displayText: displayText
    });
    this.trigger('parse', event);
    if (!event.defaultPrevented) {
      var parsedValue = this.parseValue(displayText);
      this.setValue(parsedValue);
    }
  } catch (error) {
    this._parsingFailed(displayText, error);
  }
};

scout.ValueField.prototype._parsingFailed = function(displayText, error) {
  $.log.debug('Parsing failed for field with id ' + this.id, error);
  var event = new scout.Event({
    displayText: displayText,
    error: error
  });
  this.trigger('parseError', event);
  if (!event.defaultPrevented) {
    var status = this._createParsingFailedStatus(displayText, error);
    this.setErrorStatus(status);
  }
};

scout.ValueField.prototype._createParsingFailedStatus = function(displayText, error) {
  return this._createInvalidValueStatus(displayText, error);
};

/**
 * Replaces the existing parser. The parser is called during {@link #parseValue(displayText)}.
 * <p>
 * Remember calling the default parser passed as parameter to the parse function, if needed.
 * @param {function} parser the new parser. If null, the default parser is used.
 */
scout.ValueField.prototype.setParser = function(parser) {
  this.setProperty('parser', parser);
  if (this.initialized) {
    this.parseAndSetValue(this.displayText);
  }
};

scout.ValueField.prototype._setParser = function(parser) {
  if (!parser) {
    parser = this._parseValue.bind(this);
  }
  this._setProperty('parser', parser);
};

/**
 * @returns the parsed value
 * @throws a message, a scout.Status or an error if the parsing fails
 */
scout.ValueField.prototype.parseValue = function(displayText) {
  var defaultParser = this._parseValue.bind(this);
  return this.parser(displayText, defaultParser);
};

/**
 * @throws a message, a scout.Status or an error if the parsing fails
 */
scout.ValueField.prototype._parseValue = function(displayText) {
  return displayText;
};

scout.ValueField.prototype._checkDisplayTextChanged = function(displayText, whileTyping) {
  var oldDisplayText = scout.nvl(this.displayText, '');
  return displayText !== oldDisplayText;
};

/**
 * Method invoked upon a mousedown click with this field as the currently focused control, and is invoked just before the mousedown click will be interpreted.
 * However, the mousedown target must not be this control, but any other control instead.
 *
 * The default implementation checks, whether the click occurred outside this control, and if so invokes 'ValueField.acceptInput'.
 *
 * @param target
 *        the DOM target where the mouse down event occurred.
 */
scout.ValueField.prototype.aboutToBlurByMouseDown = function(target) {
  var eventOnField = this.isFocusOnField(target);
  if (!eventOnField) {
    this.acceptInput(); // event outside this value field.
  }
};

scout.ValueField.prototype.isFocusOnField = function(target) {
  return this.$field.isOrHas(target) || (this.$clearIcon && this.$clearIcon.isOrHas(target));
};

scout.ValueField.prototype._triggerAcceptInput = function(whileTyping) {
  var event = {
    displayText: this.displayText,
    whileTyping: !!whileTyping
  };
  this.trigger('acceptInput', event);
};

scout.ValueField.prototype.setDisplayText = function(displayText) {
  this.setProperty('displayText', displayText);
};

scout.ValueField.prototype._updateHasText = function() {
  this.setHasText(scout.strings.hasText(this._readDisplayText()));
};

scout.ValueField.prototype.setHasText = function(hasText) {
  this.setProperty('hasText', hasText);
};

scout.ValueField.prototype._renderHasText = function() {
  if (this.$field) {
    this.$field.toggleClass('has-text', this.hasText);
  }
  this.$container.toggleClass('has-text', this.hasText);
};

scout.ValueField.prototype.setClearable = function(clearableStyle) {
  this.setProperty('clearable', clearableStyle);
};

scout.ValueField.prototype._renderClearable = function() {
  if (this.isClearable()) {
    if (!this.$clearIcon) {
      this.addClearIcon();
    }
  } else {
    if (this.$clearIcon) {
      // Remove $dateField
      this.$clearIcon.remove();
      this.$clearIcon = null;
    }
  }
  this.invalidateLayoutTree(false);
  this._updateClearableStyles();
};

scout.ValueField.prototype._updateClearableStyles = function() {
  this.$container.removeClass('clearable-always clearable-focused');
  if (this.isClearable()) {
    if (this.clearable === scout.ValueField.Clearable.ALWAYS) {
      this.$container.addClass('clearable-always');
    } else if (this.clearable === scout.ValueField.Clearable.FOCUSED) {
      this.$container.addClass('clearable-focused');
    }
  }
};

scout.ValueField.prototype.isClearable = function() {
  return this.clearable === scout.ValueField.Clearable.ALWAYS || this.clearable === scout.ValueField.Clearable.FOCUSED;
};

/**
 * Clears the display text and the value to null.
 */
scout.ValueField.prototype.clear = function() {
  this._clear();
  this._updateHasText();
  this.acceptInput();
  this._triggerClear();
};

scout.ValueField.prototype._clear = function() {
  // to be implemented by sublcasses
};

scout.ValueField.prototype._triggerClear = function() {
  this.trigger('clear');
};

scout.ValueField.prototype.setValue = function(value) {
  // Same code as in Widget#setValue expect for the equals check
  // -> _setValue has to be called even if the value is equal so that update display text will be executed
  value = this._prepareProperty('value', value);
  if (this.rendered) {
    this._callRemoveProperty('value');
  }
  this._callSetProperty('value', value);
  if (this.rendered) {
    this._callRenderProperty('value');
  }
};

/**
 * Resets the value to its initial value.
 */
scout.ValueField.prototype.resetValue = function() {
  this.setValue(this.initialValue);
};

/**
 * Default does nothing because the value field does not know which type the concrete field uses.
 * May be overridden to cast the value to the required type.
 * @returns the value with the correct type.
 */
scout.ValueField.prototype._ensureValue = function(value) {
  return value;
};

scout.ValueField.prototype._setValue = function(value) {
  var oldValue = this.value;
  this._updateErrorStatus(null);

  try {
    var typedValue = this._ensureValue(value);
    this.value = this.validateValue(typedValue);
  } catch (error) {
    this._validationFailed(value, error);
    return;
  }

  this._updateDisplayText();
  if (scout.objects.equals(oldValue, this.value)) {
    return;
  }

  this._updateTouched();
  this._updateEmpty();
  this.triggerPropertyChange('value', oldValue, this.value);
};

/**
 * Validates the value by executing the validators. If a new value is the result, it will be set.
 */
scout.ValueField.prototype.validate = function() {
  this._setValue(this.value);
};

/**
 * @param {function} validator the validator to be added
 * @param {boolean} [revalidate] True, to revalidate the value, false to just add the validator and do nothing else. Default is true.
 */
scout.ValueField.prototype.addValidator = function(validator, revalidate) {
  var validators = this.validators.slice();
  validators.push(validator);
  this.setValidators(validators, revalidate);
};

/**
 * @param {function} validator the validator to be removed
 * @param {boolean} [revalidate] True, to revalidate the value, false to just remove the validator and do nothing else. Default is true.
 */
scout.ValueField.prototype.removeValidator = function(validator, revalidate) {
  var validators = this.validators.slice();
  scout.arrays.remove(validators, validator);
  this.setValidators(validators, revalidate);
};

/**
 * Replaces all existing validators with the given one. If you want to add multiple validators, use {@link #addValidator}.
 * <p>
 * Remember calling the default validator which is passed as parameter to the validate function, if needed.
 * @param {function} validator the new validator which replaces every other. If null, the default validator is used.
 */
scout.ValueField.prototype.setValidator = function(validator, revalidate) {
  if (!validator) {
    validator = this._validateValue.bind(this);
  }
  var validators = [];
  if (validator) {
    validators = [validator];
  }
  this.setValidators(validators, revalidate);
};

scout.ValueField.prototype.setValidators = function(validators, revalidate) {
  this.setProperty('validators', validators);
  if (this.initialized && scout.nvl(revalidate, true)) {
    this.validate();
  }
};

/**
 * @param the value to be validated
 * @returns the validated value
 * @throws a message, a scout.Status or an error if the validation fails
 */
scout.ValueField.prototype.validateValue = function(value) {
  var defaultValidator = this._validateValue.bind(this);
  this.validators.forEach(function(validator) {
    value = validator(value, defaultValidator);
  });
  value = scout.nvl(value, null); // Ensure value is never undefined (necessary for _updateTouched and should make it easier generally)
  return value;
};

/**
 * @returns the validated value
 * @throws a message, a scout.Status or an error if the validation fails
 */
scout.ValueField.prototype._validateValue = function(value) {
  return value;
};

scout.ValueField.prototype._validationFailed = function(value, error) {
  $.log.debug('Validation failed for field with id ' + this.id, error);
  var status = this._createValidationFailedStatus(value, error);
  this._updateErrorStatus(status);
  this._updateDisplayText(value);
};

scout.ValueField.prototype._createValidationFailedStatus = function(value, error) {
  return this._createInvalidValueStatus(value, error);
};

scout.ValueField.prototype._createInvalidValueStatus = function(value, error) {
  if (error instanceof scout.Status) {
    return error;
  }
  if (typeof error === 'string') {
    return scout.Status.error({
      message: error
    });
  }
  return scout.Status.error({
    message: this.session.text(this.invalidValueMessageKey, value)
  });
};

scout.ValueField.prototype._updateErrorStatus = function(status) {
  if (!this.initialized && this.errorStatus) {
    // Don't override the error status specified by the init model
    return;
  }
  if (!status) {
    this.clearErrorStatus();
  } else {
    this.setErrorStatus(status);
  }
};

scout.ValueField.prototype._updateDisplayText = function(value) {
  if (!this.initialized && !scout.objects.isNullOrUndefined(this.displayText)) {
    // If a displayText is provided initially, use that text instead of using formatValue to generate a text based on the value
    return;
  }
  value = scout.nvl(value, this.value);
  var returned = this.formatValue(value);
  if (returned && $.isFunction(returned.promise)) {
    // Promise is returned -> set display text later
    returned
      .done(this.setDisplayText.bind(this))
      .fail(function() {
        this.setDisplayText('');
        $.log.error('Could not resolve display text.');
      }.bind(this));
  } else {
    this.setDisplayText(returned);
  }
};

/**
 * Replaces the existing formatter. The formatter is called during {@link #formatValue(value)}.
 * <p>
 * Remember calling the default formatter which is passed as parameter to the format function, if needed.
 * @param {function} formatter the new formatter. If null, the default formatter is used.
 */
scout.ValueField.prototype.setFormatter = function(formatter) {
  this.setProperty('formatter', formatter);
  if (this.initialized) {
    this.validate();
  }
};

scout.ValueField.prototype._setFormatter = function(formatter) {
  if (!formatter) {
    formatter = this._formatValue.bind(this);
  }
  this._setProperty('formatter', formatter);
};

/**
 * @returns the formatted display text
 */
scout.ValueField.prototype.formatValue = function(value) {
  var defaultFormatter = this._formatValue.bind(this);
  return this.formatter(value, defaultFormatter);
};

/**
 * @returns the formatted string or a promise
 */
scout.ValueField.prototype._formatValue = function(value) {
  return scout.nvl(value, '') + '';
};

scout.ValueField.prototype._updateTouched = function() {
  this.touched = !scout.objects.equals(this.value, this.initialValue);
};

scout.ValueField.prototype.addClearIcon = function($parent) {
  if (!$parent) {
    $parent = this.$container;
  }
  this.$clearIcon = $parent.appendSpan('clear-icon needsclick unfocusable')
    .on('mousedown', this._onClearIconMouseDown.bind(this));
};

scout.ValueField.prototype.addContainer = function($parent, cssClass, layout) {
  scout.ValueField.parent.prototype.addContainer.call(this, $parent, cssClass, layout);
  this.$container.addClass('value-field');
};

scout.ValueField.prototype.addField = function($field) {
  scout.ValueField.parent.prototype.addField.call(this, $field);
  this.$field.data('valuefield', this);
};

scout.ValueField.prototype._onFieldInput = function() {
  this._updateHasText();
};

scout.ValueField.prototype._onStatusMouseDown = function(event) {
  if (this.menus && this.menus.length > 0) {
    var $activeElement = this.$container.activeElement();
    if ($activeElement.data('valuefield') === this ||
      $activeElement.parent().data('valuefield') === this) {
      this.acceptInput();
    }
  }

  scout.ValueField.parent.prototype._onStatusMouseDown.call(this, event);
};

scout.ValueField.prototype._getCurrentMenus = function() {
  if (this.currentMenuTypes) {
    var menuTypes = this.currentMenuTypes.map(function(elem) {
      return 'ValueField.' + elem;
    });
    return scout.menus.filter(this.menus, menuTypes);
  }
  return scout.ValueField.parent.prototype._getCurrentMenus.call(this);
};

scout.ValueField.prototype._renderCurrentMenuTypes = function() {
  // If a tooltip is shown, update it with the new menus
  if (this.tooltip) {
    this._showStatusMessage();
  }
};

// ==== static helper methods ==== //

/**
 * Invokes 'ValueField.aboutToBlurByMouseDown' on the currently active value field.
 * This method has no effect if another element is the focus owner.
 */
scout.ValueField.invokeValueFieldAboutToBlurByMouseDown = function(target) {
  var activeValueField = this._getActiveValueField(target);
  if (activeValueField) {
    activeValueField.aboutToBlurByMouseDown(target);
  }
};

/**
 * Invokes 'ValueField.acceptInput' on the currently active value field.
 * This method has no effect if another element is the focus owner.
 */
scout.ValueField.invokeValueFieldAcceptInput = function(target) {
  var activeValueField = this._getActiveValueField(target);
  if (activeValueField) {
    activeValueField.acceptInput();
  }
};

/**
 * Returns the currently active value field, or null if another element is active.
 * Also, if no value field currently owns the focus, its parent is checked to be a value field and is returned accordingly.
 * That is used in DateField.js with multiple input elements.
 */
scout.ValueField._getActiveValueField = function(target) {
  var $activeElement = $(target).activeElement(),
    valueField = $activeElement.data('valuefield') || $activeElement.parent().data('valuefield');
  return valueField && !(valueField.$field && valueField.$field.hasClass('disabled')) ? valueField : null;
};

scout.ValueField.prototype.markAsSaved = function() {
  scout.ValueField.parent.prototype.markAsSaved.call(this);
  this.initialValue = this.value;
};

/**
 * @override
 */
scout.ValueField.prototype._updateEmpty = function() {
  this.empty = this.value === null || this.value === undefined;
};

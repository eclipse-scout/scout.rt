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
scout.NumberField = function() {
  scout.NumberField.parent.call(this);
  this.calc = new scout.Calculator();
  this.invalidValueMessageKey = 'InvalidNumberMessageX';
};
scout.inherits(scout.NumberField, scout.BasicField);

/**
 * Initializes the decimal format before calling set value.
 * This cannot be done in _init because the value field would call _setValue first
 */
scout.NumberField.prototype._initValue = function(value) {
  this._setDecimalFormat(this.decimalFormat);
  scout.NumberField.parent.prototype._initValue.call(this, value);
};

/**
 * @override Widget.js
 */
scout.NumberField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.NumberField.prototype._render = function() {
  this.addContainer(this.$parent, 'number-field');
  this.addLabel();
  this.addMandatoryIndicator();
  var $field = scout.fields.makeTextField(this.$parent);
  this.addField($field);
  this.addDeletableIcon();
  this.addStatus();
};

scout.NumberField.prototype._renderGridData = function() {
  scout.NumberField.parent.prototype._renderGridData.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};

scout.NumberField.prototype._getDefaultFormat = function(locale) {
  return locale.decimalFormatPatternDefault;
};

scout.NumberField.prototype.setDecimalFormat = function(decimalFormat) {
  this.setProperty('decimalFormat', decimalFormat);
};

scout.NumberField.prototype._setDecimalFormat = function(decimalFormat) {
  if (!decimalFormat) {
    decimalFormat = this._getDefaultFormat(this.session.locale);
  }
  decimalFormat = scout.DecimalFormat.ensure(this.session.locale, decimalFormat);
  this._setProperty('decimalFormat', decimalFormat);

  if (this.initialized) {
    // if format changes on the fly, just update the display text
    this._updateDisplayText();
  }
};

/**
 * @override
 */
scout.NumberField.prototype._parseValue = function(displayText) {
  if (!displayText) {
    return null;
  }

  // Convert to JS number format (remove groupingChar, replace decimalSeparatorChar with '.')
  // Only needed for calculator
  var input = this.decimalFormat.normalize(displayText);

  // if only math symbols are in the input string...
  if (this.calc.isFormula(input)) {
    // ...evaluate and return. If the display text changed, ValueField.js will make sure, the new display text is sent to the model.
    return this.calc.evalFormula(input);
  }
  return this.decimalFormat.parse(displayText);
};

/**
 * @override
 */
scout.NumberField.prototype._ensureValue = function(value) {
  return scout.numbers.ensure(value);
};

/**
 * @param {number} the number to validate
 * @return {number} the validated number
 * @override
 */
scout.NumberField.prototype._validateValue = function(value) {
  if (scout.objects.isNullOrUndefined(value)) {
    return value;
  }
  if (!scout.numbers.isNumber(value)) {
    throw this.session.text(this.invalidValueMessageKey, value);
  }
  return value;
};

/**
 * @override
 */
scout.NumberField.prototype._formatValue = function(value) {
  if (scout.objects.isNullOrUndefined(value)) {
    return '';
  }
  if (typeof value !== 'number') {
    // if setValue() would be called with something other than a number don't try to format it
    return value + '';
  }
  return this.decimalFormat.format(value, false); // parse does not support multiplier yet -> disable it for the formatting
};

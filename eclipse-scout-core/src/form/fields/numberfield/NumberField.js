/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BasicField, Calculator, DecimalFormat, fields, InputFieldKeyStrokeContext, numbers, objects} from '../../../index';

export default class NumberField extends BasicField {

  constructor() {
    super();
    this.calc = new Calculator();
    this.minValue = null;
    this.maxValue = null;
    this.decimalFormat = null;
    this.invalidValueMessageKey = 'InvalidNumberMessageX';
    this.gridDataHints.horizontalAlignment = 1; // number fields are right aligned by default.
  }

  _init(model) {
    super._init(model);
    this._setMinValue(this.minValue);
    this._setMaxValue(this.maxValue);
    this._setDecimalFormat(this.decimalFormat);
  }

  /**
   * Initializes the decimal format before calling set value.
   * This cannot be done in _init because the value field would call _setValue first
   */
  _initValue(value) {
    this._setDecimalFormat(this.decimalFormat);
    super._initValue(value);
  }

  /**
   * @override Widget.js
   */
  _createKeyStrokeContext() {
    return new InputFieldKeyStrokeContext();
  }

  _render() {
    this.addContainer(this.$parent, 'number-field');
    this.addLabel();
    this.addMandatoryIndicator();
    let $field = fields.makeTextField(this.$parent);
    this.addField($field);
    this.addStatus();
  }

  _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  _getDefaultFormat(locale) {
    return locale.decimalFormatPatternDefault;
  }

  setDecimalFormat(decimalFormat) {
    this.setProperty('decimalFormat', decimalFormat);
  }

  _setDecimalFormat(decimalFormat) {
    if (!decimalFormat) {
      decimalFormat = this._getDefaultFormat(this.session.locale);
    }
    decimalFormat = DecimalFormat.ensure(this.session.locale, decimalFormat);
    this._setProperty('decimalFormat', decimalFormat);

    if (this.initialized) {
      // if format changes on the fly, just update the display text
      this._updateDisplayText();
    }
  }

  /**
   * @override
   */
  _parseValue(displayText) {
    if (!displayText) {
      return null;
    }

    return this.decimalFormat.parse(displayText, this._evaluateNumber.bind(this));
  }

  _evaluateNumber(normalizedNumberString) {
    // Convert to JS number format (remove groupingChar, replace decimalSeparatorChar with '.')
    // Only needed for calculator
    // if only math symbols are in the input string...
    if (this.calc.isFormula(normalizedNumberString)) {
      // ...evaluate and return. If the display text changed, ValueField.js will make sure, the new display text is sent to the model.
      let calculated = this.calc.evalFormula(normalizedNumberString);
      if (isNaN(calculated)) {
        // catch input that is not a valid expression (although it looks like one, e.g. "1.2.3")
        throw new Error(normalizedNumberString + ' is not a valid expression');
      }
      return calculated;
    }

    return Number(normalizedNumberString);
  }

  /**
   * @override
   */
  _ensureValue(value) {
    return numbers.ensure(value);
  }

  /**
   * @param {number} the number to validate
   * @return {number} the validated number
   * @override
   */
  _validateValue(value) {
    if (objects.isNullOrUndefined(value)) {
      return value;
    }
    if (!numbers.isNumber(value)) {
      throw this.session.text(this.invalidValueMessageKey, value);
    }
    if (!objects.isNullOrUndefined(this.minValue) && value < this.minValue) {
      this._onNumberTooSmall();
    }
    if (!objects.isNullOrUndefined(this.maxValue) && value > this.maxValue) {
      this._onNumberTooLarge();
    }
    return value;
  }

  _onNumberTooLarge() {
    if (objects.isNullOrUndefined(this.minValue)) {
      throw this.session.text('NumberTooLargeMessageX', this._formatValue(this.maxValue));
    }
    throw this.session.text('NumberTooLargeMessageXY', this._formatValue(this.minValue), this._formatValue(this.maxValue));
  }

  _onNumberTooSmall() {
    if (objects.isNullOrUndefined(this.maxValue)) {
      throw this.session.text('NumberTooSmallMessageX', this._formatValue(this.minValue));
    }
    throw this.session.text('NumberTooSmallMessageXY', this._formatValue(this.minValue), this._formatValue(this.maxValue));
  }

  /**
   * @override
   */
  _formatValue(value) {
    if (objects.isNullOrUndefined(value)) {
      return '';
    }
    if (typeof value !== 'number') {
      // if setValue() would be called with something other than a number don't try to format it
      return value + '';
    }
    return this.decimalFormat.format(value, true);
  }

  /**
   * Set the minimum value. Value <code>null</code> means no limitation.
   * <p>
   * If the new minimum value is bigger than the current maxValue, the current maximum value is changed to the new minimum value.
   * @param {number} the new minimum value
   */
  setMinValue(minValue) {
    if (this.minValue === minValue) {
      return;
    }
    this._setMinValue(minValue);
    this.validate();
  }

  _setMinValue(minValue) {
    this._setProperty('minValue', minValue);
    if (!objects.isNullOrUndefined(this.maxValue) && !objects.isNullOrUndefined(this.minValue) && minValue > this.maxValue) {
      this._setMaxValue(minValue);
    }
  }

  /**
   * Set the maximum value. Value <code>null</code> means no limitation.
   * <p>
   * If the new maximum value is smaller than the current minValue, the current minimum value is changed to the new maximum value.
   * @param {number} the new minimum value
   */
  setMaxValue(maxValue) {
    if (this.maxValue === maxValue) {
      return;
    }
    this._setMaxValue(maxValue);
    this.validate();
  }

  _setMaxValue(maxValue) {
    this._setProperty('maxValue', maxValue);
    if (!objects.isNullOrUndefined(this.maxValue) && !objects.isNullOrUndefined(this.minValue) && maxValue < this.minValue) {
      this._setMinValue(maxValue);
    }
  }
}

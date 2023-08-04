/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, BasicField, Calculator, DecimalFormat, DecimalFormatOptions, fields, InitModelOf, InputFieldKeyStrokeContext, KeyStrokeContext, Locale, NumberFieldEventMap, NumberFieldModel, numbers, objects} from '../../../index';

export class NumberField extends BasicField<number, number | string> implements NumberFieldModel {
  declare model: NumberFieldModel;
  declare eventMap: NumberFieldEventMap;
  declare self: NumberField;

  calc: Calculator;
  minValue: number;
  maxValue: number;
  decimalFormat: DecimalFormat;
  fractionDigits: number;

  constructor() {
    super();
    this.calc = new Calculator();
    this.minValue = null;
    this.maxValue = null;
    this.decimalFormat = null;
    this.fractionDigits = null;
    this.invalidValueMessageKey = 'InvalidNumberMessageX';
    this.gridDataHints.horizontalAlignment = 1; // number fields are right aligned by default.
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setMinValue(this.minValue);
    this._setMaxValue(this.maxValue);
  }

  /**
   * Initializes the decimal format and the fraction digits before calling set value.
   * This cannot be done in _init because the value field would call _setValue first
   */
  protected override _initValue(value: number) {
    this._setDecimalFormat(this.decimalFormat);
    this._setFractionDigits(this.fractionDigits);
    super._initValue(value);
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new InputFieldKeyStrokeContext();
  }

  protected override _render() {
    this.addContainer(this.$parent, 'number-field');
    this.addLabel();
    this.addMandatoryIndicator();
    let $field = fields.makeTextField(this.$parent);
    this.addField($field);
    this.addStatus();
    this._addAriaFieldDescription();
  }

  protected override _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  protected override _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  protected _getDefaultFormat(locale: Locale): string | DecimalFormat {
    return locale.decimalFormatPatternDefault;
  }

  setDecimalFormat(decimalFormat: string | DecimalFormat | DecimalFormatOptions) {
    this.setProperty('decimalFormat', decimalFormat);
  }

  protected _setDecimalFormat(decimalFormat: string | DecimalFormat | DecimalFormatOptions) {
    if (!decimalFormat) {
      decimalFormat = this._getDefaultFormat(this.session.locale);
    }
    decimalFormat = DecimalFormat.ensure(this.session.locale, decimalFormat);
    this._setProperty('decimalFormat', decimalFormat);

    if (this.initialized) {
      this.setValue(this.value);
    }
  }

  setFractionDigits(fractionDigits: number) {
    this.setProperty('fractionDigits', fractionDigits);
  }

  protected _setFractionDigits(fractionDigits: number) {
    this._setProperty('fractionDigits', fractionDigits);

    if (this.initialized) {
      this.setValue(this.value);
    }
  }

  protected override _parseValue(displayText: string): number {
    if (!displayText) {
      return null;
    }

    return this.decimalFormat.parse(displayText, this._evaluateNumber.bind(this));
  }

  protected _evaluateNumber(normalizedNumberString: string): number {
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

  protected override _ensureValue(value: number | string): number {
    let typedValue = numbers.ensure(value);
    if (objects.isNullOrUndefined(typedValue)) {
      return typedValue;
    }
    if (!numbers.isNumber(typedValue)) {
      // might be NaN if the string is no valid number
      throw this.session.text(this.invalidValueMessageKey, value);
    }
    if (numbers.isNumber(this.fractionDigits)) {
      typedValue = numbers.round(typedValue, this.decimalFormat.roundingMode, this.fractionDigits);
    }
    return typedValue;
  }

  protected override _validateValue(value: number): number {
    if (objects.isNullOrUndefined(value)) {
      return value;
    }
    if (!objects.isNullOrUndefined(this.minValue) && value < this.minValue) {
      this._onNumberTooSmall();
    }
    if (!objects.isNullOrUndefined(this.maxValue) && value > this.maxValue) {
      this._onNumberTooLarge();
    }
    return value;
  }

  protected _onNumberTooLarge() {
    if (objects.isNullOrUndefined(this.minValue)) {
      throw this.session.text('NumberTooLargeMessageX', this._formatValue(this.maxValue));
    }
    throw this.session.text('NumberTooLargeMessageXY', this._formatValue(this.minValue), this._formatValue(this.maxValue));
  }

  protected _onNumberTooSmall() {
    if (objects.isNullOrUndefined(this.maxValue)) {
      throw this.session.text('NumberTooSmallMessageX', this._formatValue(this.minValue));
    }
    throw this.session.text('NumberTooSmallMessageXY', this._formatValue(this.minValue), this._formatValue(this.maxValue));
  }

  protected override _formatValue(value: number): string | JQuery.Promise<string> {
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
   */
  setMinValue(minValue: number) {
    if (this.minValue === minValue) {
      return;
    }
    this._setMinValue(minValue);
    this.validate();
  }

  protected _setMinValue(minValue: number) {
    this._setProperty('minValue', minValue);
    if (!objects.isNullOrUndefined(this.maxValue) && !objects.isNullOrUndefined(this.minValue) && minValue > this.maxValue) {
      this._setMaxValue(minValue);
    }
  }

  /**
   * Set the maximum value. Value <code>null</code> means no limitation.
   * <p>
   * If the new maximum value is smaller than the current minValue, the current minimum value is changed to the new maximum value.
   */
  setMaxValue(maxValue: number) {
    if (this.maxValue === maxValue) {
      return;
    }
    this._setMaxValue(maxValue);
    this.validate();
  }

  protected _setMaxValue(maxValue: number) {
    this._setProperty('maxValue', maxValue);
    if (!objects.isNullOrUndefined(this.maxValue) && !objects.isNullOrUndefined(this.minValue) && maxValue < this.minValue) {
      this._setMinValue(maxValue);
    }
  }

  protected _addAriaFieldDescription() {
    aria.addHiddenDescriptionAndLinkToElement(this.$field, this.id + '-func-desc', this.session.text('ui.AriaNumberFieldDescription'));
  }
}

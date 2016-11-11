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
};
scout.inherits(scout.NumberField, scout.BasicField);

scout.NumberField.prototype._init = function(model) {
  scout.NumberField.parent.prototype._init.call(this, model);
  if (!(this.decimalFormat instanceof scout.DecimalFormat)) {
    this.decimalFormat = new scout.DecimalFormat(this.session.locale, this.decimalFormat);
  }
  this.calc = new scout.Calculator();
};

/**
 * @override Widget.js
 */
scout.NumberField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.NumberField.prototype._render = function($parent) {
  this.addContainer($parent, 'number-field');
  this.addLabel();
  this.addMandatoryIndicator();
  var $field = scout.fields.makeTextField($parent)
    .on('blur', this._onFieldBlur.bind(this));
  this.addField($field);
  this.addStatus();
};

scout.NumberField.prototype._renderGridData = function() {
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};

scout.NumberField.prototype._syncDecimalFormat = function(decimalFormat) {
  decimalFormat = scout.DecimalFormat.ensure(this.session.locale, decimalFormat);
  this._setProperty('decimalFormat', decimalFormat);
};

scout.NumberField.prototype._renderDecimalFormat = function() {
  this._parse();
};

scout.NumberField.prototype.acceptInput = function(whileTyping) {
  if (!whileTyping) {
    this._parse();
  }
  scout.NumberField.parent.prototype.acceptInput.call(this, whileTyping);
};

scout.NumberField.prototype.parse = function() {
  var number = null;
  try {
    number = this.decimalFormat.parse(this.displayText);
  } catch(e) {
    // catch Error thrown when number isNaN
  }
  return number;
};

scout.NumberField.prototype._parse = function() {
  var input = this.$field.val();
  if (input) {
    // Convert to JS number format (remove groupingChar, replace decimalSeparatorChar with '.')
    input = input
      .replace(new RegExp('[' + this.decimalFormat.groupingChar + ']', 'g'), '')
      .replace(new RegExp('[' + this.decimalFormat.decimalSeparatorChar + ']', 'g'), '.')
      .replace(/\s/g, '');

    // if only math symbols are in the input string...
    if (this.calc.isFormula(input)) {
      // ...evaluate, reformat the result and set is to the field. If the display text
      // changed, ValueField.js will make sure, the new value is sent to the model.
      try {
        input = this.calc.evalFormula(input);
        input = this.decimalFormat.format(input, false);
        this.$field.val(input);
      } catch (err) {
        // ignore errors, let the input be handled by scout model
      }
    }
  }
};

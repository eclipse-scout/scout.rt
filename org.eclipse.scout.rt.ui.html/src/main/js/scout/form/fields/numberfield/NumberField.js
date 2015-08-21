scout.NumberField = function() {
  scout.NumberField.parent.call(this);
};
scout.inherits(scout.NumberField, scout.ValueField);

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
  this.addField(
    scout.fields.new$TextField().
      blur(this._parse.bind(this)).
      blur(this._onFieldBlur.bind(this)));
  this.addStatus();

  this._renderDecimalFormat();
};

scout.NumberField.prototype._renderGridData = function() {
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};

scout.NumberField.prototype._renderDecimalFormat = function() {
  this._decimalFormat = this.decimalFormat ? new scout.DecimalFormat(this.session.locale, this.decimalFormat) : this.session.locale.decimalFormat;
};

scout.NumberField.prototype._parse = function () {
  var input = this.$field.val();
  if (input) {
    input = input
      .replace(new RegExp('[' + this._decimalFormat.groupingChar + ']', 'g'), '')
      .replace(new RegExp('[' + this._decimalFormat.pointChar + ']', 'g'), '.')
      .replace(/\s/g, '');

    // if only math symbols are in the input string...
    if (input.match(/^[\d\(\)\+\-\*\/\.]+$/)) {
      // ...evaluate, reformat the result and set is to the field. If the display text
      // changed, ValueField.js will make sure, the new value is sent to the model.
      try {
        input = eval(input);
        input = this._decimalFormat.format(input);
        this.$field.val(input);
      } catch (err) {
        // ignore errors, let the input be handled by scout model
      }
    }
  }
};

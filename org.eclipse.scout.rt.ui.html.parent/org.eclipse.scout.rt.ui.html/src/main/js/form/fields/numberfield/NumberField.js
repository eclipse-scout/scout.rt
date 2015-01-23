scout.NumberField = function() {
  scout.NumberField.parent.call(this);
};
scout.inherits(scout.NumberField, scout.ValueField);

scout.NumberField.prototype._render = function($parent) {
  this.addContainer($parent, 'number-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(
    scout.fields.new$TextField().
      blur(this._parse.bind(this)).
      blur(this._onFieldBlur.bind(this)));
  this.addStatus();
};

scout.NumberField.prototype._parse = function () {
  var input = this.$field.val();
  if (input) {
    // find simple format for value function
    var decimalFormat = this.session.locale.decimalFormat;
    input = input.replace(decimalFormat.groupChar, '').replace(decimalFormat.pointChar, '.').replace(/\s/g, '');

    // if only math symbols are in the input string...
    if (input.match(/^[\d\(\)\+\-\*\/\.]+$/)) {
      // ...evaluate, reformat the result and set is to the field. If the display text
      // changed, ValueField.js will make sure, the new value is sent to the model.
      try {
        input = eval(input);
        input = decimalFormat.format(input);
        this.$field.val(input);
      } catch (err) {
        // ignore errors, let the input handle by scout model
      }
    }
  }
};

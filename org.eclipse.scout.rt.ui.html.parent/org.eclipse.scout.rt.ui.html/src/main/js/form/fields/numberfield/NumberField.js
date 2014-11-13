// TODO AWE: gemeinsame basis-klasse für widgets mit text-feld
scout.NumberField = function() {
  scout.NumberField.parent.call(this);
};
scout.inherits(scout.NumberField, scout.ValueField);

scout.NumberField.prototype._render = function($parent) {
  this.addContainer($parent, 'number-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$field = $('<input>').
    attr('type', 'text').
    addClass('field').
    blur(this._parse.bind(this)).
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container);

  this.addStatus();
};

scout.NumberField.prototype._parse = function () {
  var value = this.$field.val();

  // find simple format for value function
  value = value.split(this.parent.session.locale.decimalFormat.groupChar).join('');
  value = value.split(this.parent.session.locale.decimalFormat.pointChar).join('.');

  // in case of non math symbols return false
  if (value.match(/[^\s\d\(\)\+\-\*\/\.]/g, '')) {
    return false;
  }

  // evaluate, in case of math errors return false
  try {
    value = String(eval(value));
    this.$field.val(value);
  } catch (err) {
    return false;
  }
};

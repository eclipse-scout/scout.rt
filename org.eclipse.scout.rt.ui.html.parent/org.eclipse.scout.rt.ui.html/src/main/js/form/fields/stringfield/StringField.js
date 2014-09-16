scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.prototype._render = function($parent) {
  this.addContainer($parent, 'StringField');
  this.addLabel();
  this.addStatus();

  if (this.multilineText) {
    this.$field = $('<textarea>');
  } else {
    this.$field = $('<input>').attr('type', 'text');
  }

  this.$field.
    addClass('field').
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container);
};


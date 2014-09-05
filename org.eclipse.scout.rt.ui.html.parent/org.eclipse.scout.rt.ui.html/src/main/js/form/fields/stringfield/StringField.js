scout.StringField = function() {
  scout.StringField.parent.call(this);
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.prototype._render = function($parent) {
  this.$container = $('<div>').
    appendTo($parent).
    addClass('form-field').
    attr('id', 'StringField-' + this.id);

  this.$label = $('<label>')
    .appendTo(this.$container);

  this.$status = $('<span>')
    .addClass('status')
    .appendTo(this.$container);

  this.$field = $('<input type="text">')
    .addClass('field')
    .blur(this._onFieldBlur.bind(this))
    .appendTo(this.$container);
};


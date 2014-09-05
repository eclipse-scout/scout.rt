scout.SmartField = function() {
  scout.SmartField.parent.call(this);
};
scout.inherits(scout.SmartField, scout.ValueField);

scout.SmartField.prototype._render = function($parent) {
  this.$container = $('<div>').
    appendTo($parent).
    addClass('form-field').
    attr('id', 'SmartField-' + this.id);

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

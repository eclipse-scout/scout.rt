scout.StringField = function() {
  scout.StringField.parent.call(this);
  this._$inputText;
};
scout.inherits(scout.StringField, scout.ValueField);

scout.StringField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.attr('id', 'StringField-' + this.id);

  this.$label = $('<label>')
    .appendTo(this.$container);

  this.$status = $('<span>')
    .addClass('status')
    .appendTo(this.$container);

  this.$field = $('<input type="text">')
    .addClass('field')
    .appendTo(this.$container);
};

// TODO AWE: gemeinsame basis-klasse für widgets mit text-feld
scout.NumberField = function() {
  scout.NumberField.parent.call(this);
};
scout.inherits(scout.NumberField, scout.ValueField);

scout.NumberField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.attr('id', 'NumberField-' + this.id);

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

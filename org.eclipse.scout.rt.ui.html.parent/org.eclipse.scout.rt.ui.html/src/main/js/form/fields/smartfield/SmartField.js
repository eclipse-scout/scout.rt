scout.SmartField = function() {
  scout.SmartField.parent.call(this);
};
scout.inherits(scout.SmartField, scout.ValueField);

scout.SmartField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$label = $('<label>')
    .appendTo(this.$container);

  this.$field = $('<input type="text">')
    .addClass('field')
    .appendTo(this.$container);
};

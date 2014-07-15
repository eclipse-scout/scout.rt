// TODO AWE: gemeinsame basis-klasse für widgets mit text-feld
scout.NumberField = function() {
  scout.NumberField.parent.call(this);
};
scout.inherits(scout.NumberField, scout.FormField);

scout.NumberField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.attr('id', 'NumberField-' + this.id);
  this.$label = $('<label>').text(this.label);
  this.$container.append(this.$label);

  this.$status = $('<span class="status"></span>');
  this.$container.append(this.$status);

  this.$field = $('<input type="text">').addClass('field');
  this.$container.append(this.field);
};

scout.NumberField.prototype._setValue = function(value) {
  this.$field.attr('value', value);
};


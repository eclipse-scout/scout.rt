scout.PlaceholderField = function() {
  scout.PlaceholderField.parent.call(this);
};
scout.inherits(scout.PlaceholderField, scout.FormField);

scout.PlaceholderField.prototype._render = function($parent) {
  this.addContainer($parent, 'placeholder-field');
};

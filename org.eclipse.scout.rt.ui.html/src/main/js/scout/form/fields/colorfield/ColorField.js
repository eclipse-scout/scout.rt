scout.ColorField = function() {
  scout.ColorField.parent.call(this);
};
scout.inherits(scout.ColorField, scout.ValueField);

scout.ColorField.prototype._render = function($parent) {
  this.addContainer($parent, 'color-field');
  this.addLabel();
  this.addField($('<div>')
    .text('not implemented yet')
    .addClass('not-implemented'));
  this.addMandatoryIndicator();
  this.addStatus();
};

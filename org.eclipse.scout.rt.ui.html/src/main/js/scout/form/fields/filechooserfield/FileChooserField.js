scout.FileChooserField = function() {
  scout.FileChooserField.parent.call(this);
};
scout.inherits(scout.FileChooserField, scout.ValueField);

scout.FileChooserField.prototype._render = function($parent) {
  this.addContainer($parent, 'file-chooser-field');
  this.addLabel();
  this.addField($('<div>')
    .text('not implemented yet')
    .addClass('not-implemented'));
  this.addMandatoryIndicator();
  this.addStatus();
};

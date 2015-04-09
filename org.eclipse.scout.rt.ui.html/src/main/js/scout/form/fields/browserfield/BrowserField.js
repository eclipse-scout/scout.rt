scout.BrowserField = function() {
  scout.BrowserField.parent.call(this);
};
scout.inherits(scout.BrowserField, scout.ValueField);

scout.BrowserField.prototype._render = function($parent) {
  this.addContainer($parent, 'browser-field');
  this.addLabel();
  this.addField($('<div>')
    .text('not implemented yet')
    .addClass('not-implemented'));
  this.addMandatoryIndicator();
  this.addStatus();
};

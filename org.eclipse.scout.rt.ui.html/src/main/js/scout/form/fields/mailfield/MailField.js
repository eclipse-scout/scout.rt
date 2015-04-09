scout.MailField = function() {
  scout.MailField.parent.call(this);
};
scout.inherits(scout.MailField, scout.ValueField);

scout.MailField.prototype._render = function($parent) {
  this.addContainer($parent, 'mail-field');
  this.addLabel();
  this.addField($('<div>')
    .text('not implemented yet')
    .addClass('not-implemented'));
  this.addMandatoryIndicator();
  this.addStatus();
};

scout.LabelField = function() {
  scout.LabelField.parent.call(this);
};
scout.inherits(scout.LabelField, scout.ValueField);

scout.LabelField.prototype._render = function($parent) {
  this.addContainer($parent, 'label-field');
  this.addLabel();

  this.$field = $('<div>')
    .addClass('field')
    .blur(this._onFieldBlur.bind(this))
    .appendTo(this.$container);

  this.addStatus();
};

/**
 * @Override
 */
scout.LabelField.prototype._renderDisplayText = function(displayText) {
  this.$field.html(displayText);
};

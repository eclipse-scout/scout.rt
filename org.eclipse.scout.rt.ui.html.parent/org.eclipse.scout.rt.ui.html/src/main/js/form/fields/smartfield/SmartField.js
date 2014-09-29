scout.SmartField = function() {
  scout.SmartField.parent.call(this);
};
scout.inherits(scout.SmartField, scout.ValueField);

scout.SmartField.prototype._render = function($parent) {
  this.addContainer($parent, 'SmartField');
  this.$container.addClass('smart-field');
  this.addLabel();
  this.addStatus();

  this.$field = $('<input>').
    attr('type', 'text').
    addClass('field').
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container);

  this.addIcon();
};

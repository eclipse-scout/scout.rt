// TODO AWE: gemeinsame basis-klasse für widgets mit text-feld
scout.NumberField = function() {
  scout.NumberField.parent.call(this);
};
scout.inherits(scout.NumberField, scout.ValueField);

scout.NumberField.prototype._render = function($parent) {
  this.addContainer($parent, 'NumberField');
  this.$container.addClass('number-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.$field = $('<input>').
    attr('type', 'text').
    addClass('field').
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container);

  this.addStatus();
};

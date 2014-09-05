scout.SmartField = function() {
  scout.SmartField.parent.call(this);
};
scout.inherits(scout.SmartField, scout.ValueField);

scout.SmartField.prototype._render = function($parent) {
  this.$container = $('<div>').
    appendTo($parent).
    addClass('form-field').
    attr('id', 'SmartField-' + this.id);

  scout.Layout.setLogicalGridData(this.$container, this.gridData);
  scout.Layout.setLayout(this.$container, new scout.FormFieldLayout());

  this.$label = $('<label>').
    appendTo(this.$container).
    attr('title', this.label); // TODO AWE: (form-fields) create base class for this form-field stuff

  this.$status = $('<span>').
    addClass('status').
    appendTo(this.$container);

  this.$field = $('<input type="text">').
    addClass('field').
    blur(this._onFieldBlur.bind(this)).
    appendTo(this.$container);
};

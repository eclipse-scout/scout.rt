scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._gridLayout;
  this._addAdapterProperties(['fields']);
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field sequence-box');
  this.$container.attr('id', 'Scout-' + this.id);
  this.$container.data('gridData', this.gridData);
  // the sequence box has as many columns as it has fields
  this.$container.data('columns', this.fields.length);
  this._gridLayout = new scout.GridLayout(this.$container);
  this.$container.data('gridLayout', this._gridLayout);

  var i, field;
  for (i = 0; i < this.fields.length; i++) {
    this.fields[i].attach(this.$container);
  }
};

scout.SequenceBox.prototype.dispose = function() {
  scout.SequenceBox.parent.prototype.dispose.call(this);
};

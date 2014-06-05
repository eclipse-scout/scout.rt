scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._gridLayout;
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype.init = function(model, session) {
  scout.SequenceBox.parent.prototype.init.call(this, model, session);

  this.fields = this.session.getOrCreateModelAdapters(this.model.fields, this);
};

scout.SequenceBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form-field sequence-box');
  this.$container.attr('id', 'Scout-' + this.model.id);
  this.$container.data('gridData', this.model.gridData);
  // the sequence box has as many columns as it has fields
  this.$container.data('columns', this.model.fields.length);
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

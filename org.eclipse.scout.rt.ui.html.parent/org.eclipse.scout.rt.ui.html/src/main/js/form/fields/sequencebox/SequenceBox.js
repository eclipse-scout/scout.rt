scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addAdapterProperties('fields');
  this.$sequenceBox;
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype._render = function($parent) {
  this.addContainer($parent, 'SequenceBox');
  this.addLabel();
  this.addStatus();

  this.$sequenceBox = $('<div>').
    addClass('field').
    addClass('sequence-box').
    appendTo(this.$container);
  scout.Layout.setLayout(this.$sequenceBox, new scout.LogicalGridLayout());

  var field, i;
  for (i = 0; i < this.fields.length; i++) {
    field = this.fields[i].render(this.$container);
  }
};

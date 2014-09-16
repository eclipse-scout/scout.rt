scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addAdapterProperties('fields');
  this.$_sequenceBox;
};

scout.inherits(scout.SequenceBox, scout.CompositeField);

scout.SequenceBox.prototype._render = function($parent) {
  this.addContainer($parent, 'SequenceBox');
  this.addLabel();
  this.addStatus();

  this.$_sequenceBox = $('<div>').
    addClass('field').
    addClass('sequence-box').
    appendTo(this.$container);
  scout.Layout.setLayout(this.$_sequenceBox, new scout.LogicalGridLayout(scout.HtmlEnvironment.formColumnGap, 0));

  var field, i;
  for (i = 0; i < this.fields.length; i++) {
    field = this.fields[i].render(this.$_sequenceBox);
  }
};

/**
 * @override CompositeField.js
 */
scout.SequenceBox.prototype.getFields = function() {
  return this.fields;
};

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
  var htmlSequenceBox = new scout.HtmlComponent(this.$_sequenceBox);
  htmlSequenceBox.setLayout(new scout.LogicalGridLayout(scout.HtmlEnvironment.formColumnGap, 0));

  var field, i;
  for (i = 0; i < this.fields.length; i++) {
    field = this.fields[i];
    field.render(this.$_sequenceBox);
    this._modifyLabel(field);
  }
};

// TODO AWE: (sequence-box) es braucht auch die sonderbehandlung vom status, siehe SwingScoutSequenceBox
scout.SequenceBox.prototype._modifyLabel = function(field) {
  if (field instanceof scout.CheckBoxField) {
    field.$label.setVisible(false);
  }
};

/**
 * @override CompositeField.js
 */
scout.SequenceBox.prototype.getFields = function() {
  return this.fields;
};

scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addAdapterProperties('fields');
  this.$sequenceBox;
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype._render = function($parent) {
  // TODO AWE: check if we can/should use AbstractSequenceBox#SequenceBoxGrid
  this.$container = $parent;
  this.$container.attr('id', 'SequenceBox-' + this.id);

  this.$label = $('<label>').
    appendTo(this.$container);

  this.$sequenceBox = $('<ul>').
    addClass('field').
    addClass('sequence-box').
    addClass('cols-' + this.fields.length).
    appendTo(this.$container);

  var i, $li;
  for (i = 0; i < this.fields.length; i++) {
    $li = $('<li>').
      addClass('form-field').
      appendTo(this.$sequenceBox);
    this.fields[i].render($li);
  }
};

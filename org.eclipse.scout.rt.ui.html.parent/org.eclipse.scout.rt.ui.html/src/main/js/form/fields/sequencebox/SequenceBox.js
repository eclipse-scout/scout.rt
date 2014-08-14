scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addAdapterProperties('fields');
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype._render = function($parent) {
  // TODO AWE: check if we should use AbstractSequenceBox#SequenceBoxGrid
  this.$container = $('<ul>').
  addClass('sequence-box').
  addClass('cols-' + this.fields.length).
  attr('id', 'SequenceBox-' + this.id);
  $parent.append(this.$container);

  var i, $li;
  for (i = 0; i < this.fields.length; i++) {
    $li = $('<li>').addClass('form-field');
    this.$container.append($li);
    this.fields[i].render($li);
  }
};

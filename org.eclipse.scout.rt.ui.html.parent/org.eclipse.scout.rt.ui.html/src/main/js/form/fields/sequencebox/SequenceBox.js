scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addAdapterProperties('fields');
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype._render = function($parent) {
  this.$container = $('<ul>').addClass('sequence-box');
  this.$container.attr('id', 'SequenceBox-' + this.id);
  // TODO AWE: check if we should use AbstractSequenceBox#SequenceBoxGrid
  this.$container.addClass('cols-' + this.fields.length);

  var i, field, $li;
  for (i = 0; i < this.fields.length; i++) {
    $li = $('<li>').addClass('form-field');
    this.fields[i].render($li);
    this.$container.append($li);
  }

  $parent.append(this.$container);
};


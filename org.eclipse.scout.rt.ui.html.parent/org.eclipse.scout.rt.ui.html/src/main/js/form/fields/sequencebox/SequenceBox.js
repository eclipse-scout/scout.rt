scout.SequenceBox = function() {
  scout.SequenceBox.parent.call(this);
  this._addAdapterProperties(['fields']);
};

scout.inherits(scout.SequenceBox, scout.FormField);

scout.SequenceBox.prototype._render = function($parent) {
  this.$container = $('<ul class="sequence-box"></ul>');
  this.$container.attr('id', 'SequenceBox-' + this.id);

  var i, field, $li;
  for (i = 0; i < this.fields.length; i++) {
    $li = $('<li></li>');
    this.fields[i].render($li);
    this.$container.append($li);
  }

  $parent.append(this.$container);
};


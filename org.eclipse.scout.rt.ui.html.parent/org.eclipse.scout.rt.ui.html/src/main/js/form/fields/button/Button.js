scout.Button = function() {
  scout.Button.parent.call(this);
};

scout.inherits(scout.Button, scout.FormField);

scout.Button.SYSTEM_TYPE = {
  NONE: 0,
  CANCEL: 1,
  CLOSE: 2,
  OK: 3
};

scout.Button.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.attr('id', 'Button-' + this.id);
  this.$field = $('<button>')
    .appendTo(this.$container)
    .on('click', function() {
      this.session.send('click', this.id);
    }.bind(this));
};

scout.Button.prototype._setLabel = function(label) {
  if (!label) {
    label = '';
  } else {
    label = label.replace('&', '');
  }
  this.$field.text(label);
};

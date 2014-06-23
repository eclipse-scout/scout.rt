scout.Button = function() {
  scout.Button.parent.call(this);
  this._$button;
};

scout.inherits(scout.Button, scout.FormField);

scout.Button.SYSTEM_TYPE = {
  NONE : 0,
  CANCEL : 1,
  CLOSE : 2,
  OK : 3,
};

scout.Button.prototype._render = function($parent) {
  var label = '';
  this.$container = $parent;
  this.$container.attr('id', 'Button-' + this.id);
  if (this.label) {
    label = this.label.replace('&', '');
  }
  this._$button = $('<button>' + label + '</button>');
  this._$button.appendTo(this.$container);
  this._$button.on('click', function() {
    this.session.send('click', this.id);
  }.bind(this));
};

scout.Button.prototype._setEnabled = function(enabled) {
  if (enabled) {
    this._$button.removeAttr('disabled');
  } else {
    this._$button.attr('disabled', 'disabled');
  }
};

scout.Button.prototype._setVisible = function(visible) {
  if (visible) {
    this._$button.css('display', 'inline');
  } else {
    this._$button.css('display', 'none');
  }
};


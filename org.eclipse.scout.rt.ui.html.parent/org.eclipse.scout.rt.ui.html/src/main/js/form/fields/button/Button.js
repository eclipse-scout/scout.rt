scout.Button = function(model, session) {
  scout.Button.parent.call(this, model, session);
  this._$button;
};
scout.inherits(scout.Button, scout.FormField);

scout.Button.prototype._render = function($parent) {
  scout.Button.parent.prototype._render.call(this, $parent);
  // TODO AWE: (button) remove mnemonic
  this._$button = $('<button>' + this.model.label + '</button>');
  this._$button.appendTo(this.$container);
  var that = this;
  this._$button.on('click', function() {
    that.session.send('click', that.model.id);
  });
};

scout.Button.prototype._setEnabled = function(enabled) {
  if (enabled) {
    this._$button.removeAttr('disabled');
  } else {
    this._$button.attr('disabled', 'disabled');
  }
};


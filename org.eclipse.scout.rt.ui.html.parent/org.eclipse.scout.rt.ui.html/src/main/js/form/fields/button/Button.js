scout.Button = function(model, session) {
  scout.Button.parent.call(this, model, session);
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
  this.$container = $parent.appendDiv(undefined, 'form-field');
  this.$container.data('gridData', this.model.gridData);
  // TODO AWE: (button) remove mnemonic
  this._$button = $('<button>' + this.model.label + '</button>');
  this._$button.appendTo(this.$container);
  var that = this;
  this._$button.on('click', function() {
    that.session.send('click', that.model.id);
  });
};

scout.Button.prototype.getSystemType = function() {
  return this.model.systemType;
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


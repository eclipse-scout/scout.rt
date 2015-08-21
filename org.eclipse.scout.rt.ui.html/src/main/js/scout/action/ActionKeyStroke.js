scout.ActionKeyStroke = function(action) {
  scout.ActionKeyStroke.parent.call(this);
  this.field = action;
  this.parseAndSetKeyStroke(action.keyStroke);
  this.stopPropagation = true; // TODO [dwi] Make this configurable in the Scout model. Use case: OK keystrokes should not bubble up, but other should.
  this.stopImmediatePropagation = false; // TODO [dwi] make this configurable in Scout model.
};
scout.inherits(scout.ActionKeyStroke, scout.KeyStroke);

scout.ActionKeyStroke.prototype._isEnabled = function() {
  if (!this.which.length) {
    return false; // actions without a keystroke are not enabled.
  } else {
    return scout.ActionKeyStroke.parent.prototype._isEnabled.call(this);
  }
};

scout.ActionKeyStroke.prototype.handle = function(event) {
  this.field.doAction(event);
};

scout.MenuKeyStroke = function(action) {
  scout.MenuKeyStroke.parent.call(this, action);
};
scout.inherits(scout.MenuKeyStroke, scout.ActionKeyStroke);

scout.MenuKeyStroke.prototype._isEnabled = function() {
  if (this.field.excludedByFilter) {
    return false;
  } else {
    return scout.MenuKeyStroke.parent.prototype._isEnabled.call(this);
  }
};

scout.Action = function() {
  scout.Action.parent.call(this);
};
scout.inherits(scout.Action, scout.ModelAdapter);

scout.Action.prototype._renderProperties = function() {
  scout.Action.parent.prototype._renderProperties.call(this);

  this._renderText(this.text);
  this._renderIconId(this.iconId);
  this._renderKeystroke(this.keystroke);
  this._renderEnabled(this.enabled);
  this._renderSelected(this.selected);
};

scout.Action.prototype._renderText = function(text) {
  text = text || '';
  this.$container.text(text);
};

scout.Action.prototype._renderIconId = function(iconId) {
  iconId = iconId || '';
  this.$container.icon(iconId);
};

scout.Action.prototype._renderEnabled = function(enabled) {
  if (enabled) {
    this.$container.on('mousedown', '', this._onMouseDown.bind(this));
  } else {
    this.$container.off('mousedown');
  }
  this.$container.setEnabled(enabled);
};

scout.Action.prototype._renderKeystroke = function(keystroke) {
  keystroke = keystroke || '';
  this.$container.attr('data-shortcut', keystroke);
};

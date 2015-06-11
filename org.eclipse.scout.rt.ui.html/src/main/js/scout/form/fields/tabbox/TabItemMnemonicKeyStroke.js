scout.TabItemMnemonicKeyStroke = function(keyStroke, field) {
  scout.TabItemMnemonicKeyStroke.parent.call(this);
  this.keyStroke = keyStroke;
  this.drawHint = true;
  this.ctrl = true;
  this.shift = true;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.TabItemMnemonicKeyStroke, scout.MnemonicKeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.TabItemMnemonicKeyStroke.prototype.handle = function(event) {
  // FIXME AWE/NBU: das kann mit der neuen TabBox nicht mehr funktionieren (data tabIndex gibt es nicht mehr)
  var index = this._field.$tabContainer.data('tabIndex');
  this._field.parent._selectTab(index);
  event.preventDefault();
};
/**
 * @Override scout.KeyStroke
 */
scout.TabItemMnemonicKeyStroke.prototype._drawKeyBox = function($container) {
  if (this._field.$tabContainer) {
    var keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
    scout.keyStrokeBox.drawSingleKeyBoxItem(16, keyBoxText, this._field.$tabContainer, this.ctrl, this.alt, this.shift, true);
  }
};

/**
 * @Override scout.KeyStroke
 */
scout.MnemonicKeyStroke.prototype.removeKeyBox = function($container) {
  $('.key-box', this._field.$tabContainer).remove();
};

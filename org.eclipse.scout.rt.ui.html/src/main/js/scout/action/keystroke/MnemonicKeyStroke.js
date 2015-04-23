scout.MnemonicKeyStroke = function(keyStroke, field) {
  scout.MnemonicKeyStroke.parent.call(this);
  this.keyStroke = keyStroke;
  this.drawHint = true;
  this.ctrl = true;
  this.shift = true;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.MnemonicKeyStroke, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.MnemonicKeyStroke.prototype.handle = function(event) {
  this._field.$label.trigger('click');
};
/**
 * @Override scout.KeyStroke
 */
scout.MnemonicKeyStroke.prototype._drawKeyBox = function($container) {
  if (this._field.$container) {
    var keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
    scout.keyStrokeBox.drawSingleKeyBoxItem(16, keyBoxText, this._field.$container, this.ctrl, this.alt, this.shift, true);
  }
};

scout.MnemonicKeyStroke.prototype._$containerForKeyBox = function() {
  return this._field.$label;
};

/**
 * @Override scout.KeyStroke
 */
scout.MnemonicKeyStroke.prototype.removeKeyBox = function($container) {
  $('.key-box', this._$containerForKeyBox()).remove();
  $('.key-box-additional', this._$containerForKeyBox()).remove();
};

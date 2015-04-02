scout.ButtonMnemonicKeyStroke = function(keyStroke, field) {
  scout.ButtonMnemonicKeyStroke.parent.call(this, keyStroke, field);
};
scout.inherits(scout.ButtonMnemonicKeyStroke, scout.MnemonicKeyStroke);

/**
 * @Override scout.MnemonicKeyStroke
 */
scout.ButtonMnemonicKeyStroke.prototype.handle = function(event) {
  this._field.$field.trigger('click');
};
/**
 * @Override scout.MnemonicKeyStroke
 */
scout.ButtonMnemonicKeyStroke.prototype._$containerForKeyBox = function(){
  return this._field.$field;
};

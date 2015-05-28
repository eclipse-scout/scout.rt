scout.ButtonMnemonicKeyStroke = function(keyStroke, field) {
  scout.ButtonMnemonicKeyStroke.parent.call(this, keyStroke, field);
};
scout.inherits(scout.ButtonMnemonicKeyStroke, scout.MnemonicKeyStroke);

/**
 * @Override scout.MnemonicKeyStroke
 */
scout.ButtonMnemonicKeyStroke.prototype.handle = function(event) {
  if (this._field.enabled && this._field.visible) {
    this._field.doAction($(event.target));
    if (this.preventDefaultOnEvent) {
      event.preventDefault();
    }
  }
};
/**
 * @Override scout.MnemonicKeyStroke
 */
scout.ButtonMnemonicKeyStroke.prototype._$containerForKeyBox = function(){
  return this._field.$field;
};

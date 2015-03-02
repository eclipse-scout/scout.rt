scout.ButtonMnemonicKeyStroke = function(keyStroke, field) {
  scout.ButtonMnemonicKeyStroke.parent.call(this, keyStroke, field);
  this.initKeyStrokeParts();
};
scout.inherits(scout.ButtonMnemonicKeyStroke, scout.MnemonicKeyStroke);

/**
 * @Override scout.MnemonicKeyStroke
 */
scout.ButtonMnemonicKeyStroke.prototype.handle = function(event) {
  this.field.$field.trigger('click');
};

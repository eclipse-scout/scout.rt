scout.ButtonMnemonicKeyStroke = function(keyStroke, field) {
  scout.ButtonMnemonicKeyStroke.parent.call(this, keyStroke, field);

  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$field;
  }.bind(this);
};
scout.inherits(scout.ButtonMnemonicKeyStroke, scout.MnemonicKeyStroke);

/**
 * @override MnemonicKeyStroke.js
 */
scout.ButtonMnemonicKeyStroke.prototype.handle = function(event) {
  this.field.doAction();
};

scout.MnemonicKeyStroke = function(keyStroke, field) {
  scout.MnemonicKeyStroke.parent.call(this);
  this.keyStroke = keyStroke;
  this.drawHint = false;
  this.alt = true;
  this.shift = true;
  this.field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.MnemonicKeyStroke, scout.KeyStroke);

scout.MnemonicKeyStroke.prototype.handle = function(event) {
  this.field.$field.trigger('click');
};

scout.MnemonicKeyStroke.prototype.addHintOnLabel = function() {
  //TODO nbu draw hint
};

scout.MnemonicKeyStroke.prototype.removeHintOnLabel = function() {
  //TODO nbu remove hint
};

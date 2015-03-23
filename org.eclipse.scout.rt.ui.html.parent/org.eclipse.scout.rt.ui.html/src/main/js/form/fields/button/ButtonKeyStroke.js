scout.ButtonKeyStroke = function(button, keyStroke) {
  scout.ButtonKeyStroke.parent.call(this);
  this.drawHint = true;
  this.keyStroke = keyStroke;
  this._button = button;
  this.initKeyStrokeParts();
  this.bubbleUp = false;
};
scout.inherits(scout.ButtonKeyStroke, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.ButtonKeyStroke.prototype.handle = function(event) {
  this._button.doAction();
};

scout.ButtonKeyStroke.prototype._drawKeyBox = function($container) {
  if (this._button.$container) {
    var keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
    scout.keyStrokeBox.drawSingleKeyBoxItem(4, keyBoxText, this._button.$container, this.ctrl, this.alt, this.shift);
  }
};

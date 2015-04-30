scout.PopupCloseKeyStroke = function(popup) {
  scout.PopupCloseKeyStroke.parent.call(this);
  this.drawHint = false;
  this.keyStroke = 'ESC';
  this._popup = popup;
  this.initKeyStrokeParts();
  this.bubbleUp = true;
};
scout.inherits(scout.PopupCloseKeyStroke, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.PopupCloseKeyStroke.prototype.handle = function(event) {
  this._popup.closePopup();
};

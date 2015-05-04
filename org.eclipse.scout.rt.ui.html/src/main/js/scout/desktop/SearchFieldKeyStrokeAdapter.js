scout.SearchFieldKeyStrokeAdapter = function(desktopNavigation) {
  scout.SearchFieldKeyStrokeAdapter.parent.call(this, desktopNavigation);

  this.keyStrokes.push(new scout.SearchFieldEnterKeyStroke());
  this.keyStrokes.push(new scout.SearchFieldBackspaceKeyStroke());
};
scout.inherits(scout.SearchFieldKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);


scout.SearchFieldKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeys) {
  //nop
};

scout.SearchFieldKeyStrokeAdapter.prototype.removeKeyBox = function() {
  //nop
};

scout.SearchFieldKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
 //nop
};
/**
 * It is possible that key strokes should only be accepted if a precondition is true.
 * @param event
 * @returns {Boolean}
 */
scout.SearchFieldKeyStrokeAdapter.prototype.accept = function(event) {
  return true;
};

scout.SearchFieldKeyStrokeAdapter.prototype.preventBubbleUp = function(event) {
  return false;
};


scout.SearchFieldEnterKeyStroke = function() {
  scout.SearchFieldEnterKeyStroke.parent.call(this);
  this.keyStroke = 'ENTER';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.SearchFieldEnterKeyStroke, scout.KeyStroke);

scout.SearchFieldEnterKeyStroke.prototype.handle = function(event) {};

scout.SearchFieldEnterKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  if (drawedKeys[this.keyStrokeName()]) {
    return;
  }
    drawedKeys[this.keyStrokeName()] = true;
};

scout.SearchFieldBackspaceKeyStroke = function() {
  scout.SearchFieldBackspaceKeyStroke.parent.call(this);
  this.keyStroke = 'Backspace';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.SearchFieldBackspaceKeyStroke, scout.KeyStroke);

scout.SearchFieldBackspaceKeyStroke.prototype.handle = function(event) {};

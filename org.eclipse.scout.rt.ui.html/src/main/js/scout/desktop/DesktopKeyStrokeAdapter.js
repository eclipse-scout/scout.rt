scout.DesktopKeyStrokeAdapter = function(desktop) {
  scout.DesktopKeyStrokeAdapter.parent.call(this, desktop);

  this.registerKeyStroke(new scout.ViewTabAutoKeyStroke(desktop));
  this.registerKeyStroke(new scout.DesktopBackspaceKeyStroke());
};
scout.inherits(scout.DesktopKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

/**
 * @override AbstractKeyStrokeAdapter.js
 */
scout.DesktopKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  scout.DesktopKeyStrokeAdapter.parent.prototype.installModelKeystrokes.call(this);

  scout.arrays.pushAll(this.keyStrokes, this._srcElement.viewButtons);
  scout.arrays.pushAll(this.keyStrokes, this._srcElement.actions);
};

// TODO [dwi] must always be active
scout.DesktopBackspaceKeyStroke = function() {
  scout.DesktopBackspaceKeyStroke.parent.call(this);
  this.keyStroke = 'Backspace';
  this.drawHint = false;
  this.initKeyStrokeParts();
  this.preventDefaultOnEvent = true;
};
scout.inherits(scout.DesktopBackspaceKeyStroke, scout.KeyStroke);

scout.DesktopBackspaceKeyStroke.prototype.handle = function(event) {};

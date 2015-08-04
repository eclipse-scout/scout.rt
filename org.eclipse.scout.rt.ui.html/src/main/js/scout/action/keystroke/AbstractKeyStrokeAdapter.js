scout.AbstractKeyStrokeAdapter = function(srcElement) {
  if (!srcElement) {
    throw new Error('\'srcElement\' must not be null');
  }
  this.keyStrokes = [];
  this._srcElement = srcElement;
  this.installModelKeystrokes();
  this.keyBoxDrawn = false;
  this.anchorKeyStrokeAdapter = false;
};

scout.AbstractKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeys) {
  if (this.keyBoxDrawn) {
    return;
  }
  this.keyBoxDrawn = true;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    this.keyStrokes[i].checkAndDrawKeyBox(this._srcElement.$container, drawedKeys);
  }
};

scout.AbstractKeyStrokeAdapter.prototype.removeKeyBox = function() {
  this.keyBoxDrawn = false;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    this.keyStrokes[i].removeKeyBox(this._srcElement.$container);
  }
};

scout.AbstractKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  if (this.keyStrokes.length > 0) {
    this.keyStrokes = this.keyStrokes.concat(this._srcElement.keyStrokes);
  } else if (this._srcElement.keyStrokes) {
    this.keyStrokes = this._srcElement.keyStrokes.slice(0);
  }
};
/**
 * It is possible that key strokes should only be accepted if a precondition is true.
 * @param event
 * @returns {Boolean}
 */
scout.AbstractKeyStrokeAdapter.prototype.accept = function(event) {
  return true;
};

scout.AbstractKeyStrokeAdapter.prototype.preventBubbleUp = function(event) {
  return false;
};

scout.AbstractKeyStrokeAdapter.prototype.registerKeyStroke = function(keyStroke) {

  var index = this.keyStrokes.indexOf(keyStroke);
  if (index === -1) {
    if (keyStroke === undefined) {
      throw new Error('KeyStroke is undefined');
    }
    this.keyStrokes.push(keyStroke);
    this.addDestroyListener(keyStroke);
  }
};

scout.AbstractKeyStrokeAdapter.prototype.addDestroyListener = function(keyStroke) {
  keyStroke.on('destroy', this._onKeyStrokeDestroy.bind(this, keyStroke));
};

scout.AbstractKeyStrokeAdapter.prototype._onKeyStrokeDestroy = function(keyStroke) {
  this.unregisterKeyStroke(keyStroke);
  keyStroke.off('destroy');
};

scout.AbstractKeyStrokeAdapter.prototype.unregisterKeyStroke = function(keyStroke) {
  var index = this.keyStrokes.indexOf(keyStroke);
  if (index > -1) {
    this.keyStrokes.splice(index, 1);
  }
};

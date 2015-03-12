scout.AbstractKeyStrokeAdapter = function(field) {
  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.keyStrokes = [];
  this._field = field;
  this.installModelKeystrokes();
  this.keyBoxDrawn = false;
};

scout.AbstractKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeys) {
  if (this.keyBoxDrawn) {
    return;
  }
  this.keyBoxDrawn = true;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    this.keyStrokes[i].checkAndDrawKeyBox(this._field.$container, drawedKeys);
  }
};

scout.AbstractKeyStrokeAdapter.prototype.removeKeyBox = function() {
  this.keyBoxDrawn = false;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    this.keyStrokes[i].removeKeyBox(this._field.$container);
  }

};

scout.AbstractKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  if (this.keyStrokes.length > 0) {
    this.keyStrokes = this.keyStrokes.concat(this._field.keyStrokes);
  } else if (this._field.keyStrokes) {
    this.keyStrokes = this._field.keyStrokes;
  }
};
/**
 * It is possible that key strokes should only be accepted if a precondition is true.
 * @param event
 * @returns {Boolean}
 */
scout.AbstractKeyStrokeAdapter.prototype.accept = function(event){
  return true;
};


scout.AbstractKeyStrokeAdapter.prototype.preventBubbleUp = function(event){
  return false;
};

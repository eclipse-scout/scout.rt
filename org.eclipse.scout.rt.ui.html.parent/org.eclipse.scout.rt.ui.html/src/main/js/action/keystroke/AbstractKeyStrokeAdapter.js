scout.AbstractKeyStrokeAdapter = function(field) {
  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.keyStrokes = [];
  this._field = field;
  this.installModelKeystrokes();
  this.keyBoxDrawn = false;
};

scout.AbstractKeyStrokeAdapter.prototype.drawKeyBox = function() {
  if (this.keyBoxDrawn) {
    return;
  }
  this.keyBoxDrawn = true;
  var offsetLeft = 4;
  for (var i = 0; i < this.keyStrokes.length; i++) {
    if (!this.keyStrokes[i].drawHint) {
      continue;
    }
    var keyBoxText = scout.codesToKeys[this.keyStrokes[i].keystrokeKeyPart];

    scout.KeyStrokeUtil.drawSingleKeyBoxItem(offsetLeft, '0', this._field.$container, this.ctrl, this.alt, this.shift);
  }
};

scout.AbstractKeyStrokeAdapter.prototype.removeKeyBox = function() {
  this.keyBoxDrawn = false;
  $('.key-box', this._field.$container).remove();
  $('.key-box-additional', this._field.$container).remove();
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

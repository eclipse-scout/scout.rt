scout.AbstractKeyStrokeAdapter = function(field) {
  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.keyStrokes = [];
  this._field = field;
  this.installModelKeystrokes();
  this.keyBoxDrawn = false;
  this._uiSessionId;
  if (this._field && this._field.session) {
    this._uiSessionId = this._field.session.uiSessionId;
  }
  this.anchorKeyStrokeAdapter = false;
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
    this.keyStrokes = this._field.keyStrokes.slice(0);
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
    this.keyStrokes.push(keyStroke);
  }
};

scout.AbstractKeyStrokeAdapter.prototype.unregisterKeyStroke = function(keyStroke) {
  var index = this.keyStrokes.indexOf(keyStroke);
  if (index > -1) {
    this.keyStrokes.splice(index, 1);
  }
};

scout.AbstractKeyStrokeAdapter.prototype.uiSessionId = function(uiSessionId){
  if (uiSessionId) {
    this._uiSessionId = uiSessionId;
  }
  return this._uiSessionId;
};

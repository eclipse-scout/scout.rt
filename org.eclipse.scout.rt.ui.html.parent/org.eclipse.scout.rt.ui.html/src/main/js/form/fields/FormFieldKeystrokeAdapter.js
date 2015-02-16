scout.FormFieldKeystrokeAdapter = function(field) {
  var that = this;

  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.keystrokes = [];
  this._field = field;
  this.installModelKeystrokes();
};

scout.FormFieldKeystrokeAdapter.prototype.drawKeyBox = function() {
  for (var i = 0; i < this.keystrokes.length; i++) {
    this.keystrokes[i].drawKeyBox();
  }
};

scout.FormFieldKeystrokeAdapter.prototype.installModelKeystrokes = function() {
  this.keystrokes = this.keystrokes.concat(this._field.keyStrokes);
};

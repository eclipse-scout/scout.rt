scout.Key = function(keyStroke, which) {
  this.keyStroke = keyStroke;
  this.which = which;

  this.ctrl = keyStroke.ctrl;
  this.alt = keyStroke.alt;
  this.shift = keyStroke.shift;
};

scout.Key.prototype.render = function($drawingArea, event) {
  this.$drawingArea = this.keyStroke.renderKeyBox($drawingArea, event);
  return !!this.$drawingArea;
};

scout.Key.prototype.remove = function() {
  this.keyStroke.removeKeyBox(this.$drawingArea);
  this.$drawingArea = null;
};

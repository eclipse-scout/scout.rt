scout.CloseKeyStroke = function(field, $drawingArea) {
  scout.CloseKeyStroke.parent.call(this);
  this.field = field;
  this.which = [scout.keys.ESC];
  this.renderingHints.render = true;
  this.stopPropagation = true;
  this.renderingHints = {
    render: !!$drawingArea,
    $drawingArea: $drawingArea
  };
};
scout.inherits(scout.CloseKeyStroke, scout.KeyStroke);

scout.CloseKeyStroke.prototype.handle = function(event) {
  this.field.close();
};

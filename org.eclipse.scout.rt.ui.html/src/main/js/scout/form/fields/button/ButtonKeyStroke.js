scout.ButtonKeyStroke = function(button, keyStroke) {
  scout.ButtonKeyStroke.parent.call(this);
  this.field = button;
  this.parseAndSetKeyStroke(keyStroke);
  this.stopPropagation = true;

  this.renderingHints.offset = 16;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$container;
  }.bind(this);
};
scout.inherits(scout.ButtonKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.ButtonKeyStroke.prototype._accept = function(event) {
  var accepted = scout.ButtonKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && jQuery.contains(document.documentElement, this.field.$field[0]);
};

/**
 * @override KeyStroke.js
 */
scout.ButtonKeyStroke.prototype.handle = function(event) {
  this.field.doAction();
};

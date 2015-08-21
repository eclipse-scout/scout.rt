scout.MnemonicKeyStroke = function(keyStroke, field) {
  scout.MnemonicKeyStroke.parent.call(this);
  this.field = field;
  this.parseAndSetKeyStroke(keyStroke);
  this.ctrl = true;
  this.shift = true;

  this.renderingHints.offset = 16;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$label;
  }.bind(this);
};
scout.inherits(scout.MnemonicKeyStroke, scout.KeyStroke);


/**
 * @override KeyStroke.js
 */
scout.MnemonicKeyStroke.prototype.handle = function(event) {
  this.field.$label.trigger('click');
};

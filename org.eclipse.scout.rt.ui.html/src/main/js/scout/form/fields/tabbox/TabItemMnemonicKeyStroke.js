scout.TabItemMnemonicKeyStroke = function(keyStroke, field) {
  scout.TabItemMnemonicKeyStroke.parent.call(this, keyStroke, field);

  this.ctrl = true;
  this.shift = true;

  this.renderingHints.offset = 16;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$tabContainer;
  }.bind(this);
};
scout.inherits(scout.TabItemMnemonicKeyStroke, scout.MnemonicKeyStroke);

/**
 * @override KeyStroke.js
 */
scout.TabItemMnemonicKeyStroke.prototype.handle = function(event) {
  this.field.parent._selectTab(this.field);
};
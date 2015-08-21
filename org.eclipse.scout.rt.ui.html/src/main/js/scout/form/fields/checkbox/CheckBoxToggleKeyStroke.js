scout.CheckBoxToggleKeyStroke = function(checkbox) {
  scout.CheckBoxToggleKeyStroke.parent.call(this);
  this.field = checkbox;
  this.which = [scout.keys.ENTER, scout.keys.SPACE];
  this.renderingHints.render = false;
};
scout.inherits(scout.CheckBoxToggleKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.CheckBoxToggleKeyStroke.prototype.handle = function(event) {
  this.field._toggleChecked();
};

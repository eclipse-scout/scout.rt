scout.CheckBoxToggleKeyStroke = function(checkbox) {
  scout.CheckBoxToggleKeyStroke.parent.call(this);
  this.field = checkbox;
  this.which = [scout.keys.SPACE];
  if (!(this.field.owner instanceof scout.Table)) {
    this.which.push(scout.keys.ENTER);
  }
  this.renderingHints.render = false;
  this.stopPropagation = true;
};
scout.inherits(scout.CheckBoxToggleKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.CheckBoxToggleKeyStroke.prototype.handle = function(event) {
  this.field._toggleChecked();
};

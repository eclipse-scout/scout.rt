scout.TableControlCloseKeyStroke = function(tableControl) {
  scout.TableControlCloseKeyStroke.parent.call(this);
  this.field = tableControl;
  this.which = [scout.keys.ESC];
  this.stopPropagation = true;
  this.renderingHints.render = false;
};
scout.inherits(scout.TableControlCloseKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.TableControlCloseKeyStroke.prototype.handle = function(event) {
  this.field.toggle();
};

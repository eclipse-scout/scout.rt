scout.ContextMenuKeyStroke = function(field, contextFunction, bindObject) {
  scout.ContextMenuKeyStroke.parent.call(this);
  this.keyStroke = 'select';
  this.drawHint = false;
  this.ctrl = false;
  this.shift = false;
  this._field = field;
  this._contextFunction = contextFunction;
  this._bindObject = bindObject || this;
  this.initKeyStrokeParts();
};
scout.inherits(scout.ContextMenuKeyStroke, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.ContextMenuKeyStroke.prototype.handle = function(event) {
  this._contextFunction.call(this._bindObject, event);
  event.preventDefault();
  event.stopPropagation();
};
/**
 * @Override scout.KeyStroke
 */
scout.ContextMenuKeyStroke.prototype._drawKeyBox = function($container) {
  //nop
};

/**
 * @Override scout.KeyStroke
 */
scout.ContextMenuKeyStroke.prototype.removeKeyBox = function($container) {
  //nope
};

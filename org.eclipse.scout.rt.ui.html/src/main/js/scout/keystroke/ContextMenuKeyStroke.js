scout.ContextMenuKeyStroke = function(field, contextFunction, bindObject) {
  scout.ContextMenuKeyStroke.parent.call(this);
  this._contextFunction = contextFunction;
  this._bindObject = bindObject || this;

  this.field = field;
  this.renderingHints.render = false;

  this.which = [scout.keys.SELECT];
  this.ctrl = false;
  this.shift = false;
  this.stopPropagation = true;
};
scout.inherits(scout.ContextMenuKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.ContextMenuKeyStroke.prototype.handle = function(event) {
  this._contextFunction.call(this._bindObject, event);
};

scout.MenuExecKeyStroke = function(menu) {
  scout.MenuExecKeyStroke.parent.call(this);
  this.field = menu;
  this.which = [scout.keys.SPACE, scout.keys.ENTER];
  this.stopPropagation = true;

  this.renderingHints.offset = 16;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$container;
  }.bind(this);
};
scout.inherits(scout.MenuExecKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.MenuExecKeyStroke.prototype.handle = function(event) {
  this.field.doAction(this.field.$container);
};

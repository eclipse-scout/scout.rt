scout.MenuKeyStrokeAdapter = function(menu) {
  scout.MenuKeyStrokeAdapter.parent.call(this, menu);

  this.registerKeyStroke(new scout.MenuExecKeyStroke(menu));
  this.registerKeyStroke(new scout.MenuSpaceExecKeyStroke(menu));
};
scout.inherits(scout.MenuKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.MenuExecKeyStroke = function(menu, keyStroke) {
  scout.MenuExecKeyStroke.parent.call(this);
  this.drawHint = true;
  this.keyStroke = 'ENTER';
  this.menu = menu;
  this.initKeyStrokeParts();
  this.bubbleUp = false;
};
scout.inherits(scout.MenuExecKeyStroke, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.MenuExecKeyStroke.prototype.handle = function(event) {
  if (this.menu.enabled && this.menu.visible) {
    this.menu.doAction(this.menu.$container);
    if (this.preventDefaultOnEvent) {
      event.preventDefault();
    }
  }
};

scout.MenuExecKeyStroke.prototype._drawKeyBox = function($container) {
  if (this.menu.$container && this.menu.enabled && this.menu.visible) {
    var keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
    scout.keyStrokeBox.drawSingleKeyBoxItem(16, keyBoxText, this.menu.$container, this.ctrl, this.alt, this.shift, true);
  }
};

scout.MenuSpaceExecKeyStroke = function(menu, keyStroke) {
  scout.MenuSpaceExecKeyStroke.parent.call(this, menu, keyStroke);
  this.keyStroke = 'SPACE';
  this.initKeyStrokeParts();
};
scout.inherits(scout.MenuSpaceExecKeyStroke, scout.MenuExecKeyStroke);

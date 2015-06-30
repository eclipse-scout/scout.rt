scout.DesktopNavigationKeyStroke = function(desktopNavigation, keyStroke) {
  scout.DesktopNavigationKeyStroke.parent.call(this);
  this._desktopNavigation = desktopNavigation;
  this.initKeyStrokeParts();
  this.keyBoxDrawed = false;
  this.drawHint = true;
};
scout.inherits(scout.DesktopNavigationKeyStroke, scout.KeyStroke);

/**
 * @Override scout.KeyStroke
 */
scout.DesktopNavigationKeyStroke.prototype.handle = function(event) {
  if (event && event.which === scout.keys.F3) {
    this._desktopNavigation.doViewMenuAction();
  }
  event.preventDefault();
};
/**
 * @Override scout.KeyStroke
 */
scout.DesktopNavigationKeyStroke.prototype.accept = function(event) {
  if (event && (event.which === scout.keys.F3) &&
    event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift) {
    return true;
  }
  return false;
};

/**
 * @Override scout.KeyStroke
 */
scout.DesktopNavigationKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  if (this.drawHint) {
    this._drawKeyBox($container, drawedKeys);
  }
};

scout.DesktopNavigationKeyStroke.prototype._drawKeyBox = function($container, drawedKeys) {
  if (this.keyBoxDrawed) {
    return;
  }
  if (!drawedKeys.F3) {
    // FIXME NBU/AWE: hier brauchen wir den ViewMenuButton (keyStroke hartcodiert?)
    scout.keyStrokeBox.drawSingleKeyBoxItem(10, 'F3', this._desktopNavigation.viewMenuTab.$container, this.ctrl, this.alt, this.shift);
    drawedKeys.F3 = true;
  }
  this.keyBoxDrawed = true;
};
/**
 * @Override scout.KeyStroke
 */
scout.DesktopNavigationKeyStroke.prototype.removeKeyBox = function() {
  if (!this.keyBoxDrawed) {
    return;
  }
  $('.key-box', this.$container).remove();
  $('.key-box-additional', this.$container).remove();
  this.keyBoxDrawed = false;
};

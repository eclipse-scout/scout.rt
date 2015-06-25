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
  if (event && event.which === scout.keys.F4 && this._desktopNavigation.activeTab !== this._desktopNavigation.searchTab) {
    this._desktopNavigation.searchTab.$tab.trigger('click'); // FIXME AWE/NBU: (desktop) use API
  }
  if (event && event.which === scout.keys.F2) {
    this._desktopNavigation.doViewMenuAction();
  }
  event.preventDefault();
};
/**
 * @Override scout.KeyStroke
 */
scout.DesktopNavigationKeyStroke.prototype.accept = function(event) {
  if (event && ((event.which === scout.keys.F4 && this._desktopNavigation.activeTab !== this._desktopNavigation.searchTab) || event.which === scout.keys.F2) &&
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
  if (!drawedKeys.F4 && this._desktopNavigation.activeTab !== this._desktopNavigation.searchTab) {
    scout.keyStrokeBox.drawSingleKeyBoxItem(10, 'F4', this._desktopNavigation.searchTab.$tab, this.ctrl, this.alt, this.shift);
    drawedKeys.F4 = true;
  }
  if (!drawedKeys.F2) {
    scout.keyStrokeBox.drawSingleKeyBoxItem(10, 'F2', this._desktopNavigation.outlineTab.$tab, this.ctrl, this.alt, this.shift);
    drawedKeys.F2 = true;
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

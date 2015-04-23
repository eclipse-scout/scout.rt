scout.ViewTabAutoKeyStroke = function(enabled, tabs, keyStroke) {
  scout.ViewTabAutoKeyStroke.parent.call(this);
  this._enabled = enabled;
  this._tabs = tabs;
  this.keyStroke = keyStroke;
  this.initKeyStrokeParts();
  this.keyBoxDrawed = false;
  this.drawHint = true;
};
scout.inherits(scout.ViewTabAutoKeyStroke, scout.KeyStroke);

/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype.handle = function(event) {
  if (this._tabs.length === 0 || (event.which !== 57 && event.which !== 49 && event.which - 49 > this._tabs.length)) {
    return;
  }
  this._tabs[event.which - 49].$container.trigger('click');
  event.preventDefault();
};

/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype.accept = function(event) {
  if (this._enabled && event && event.which >= 49 && event.which <= 57 && // 1-9
    event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift) {
    return true;
  }
  return false;
};
/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  if (scout.keyStrokeBox.keyStrokesAlreadyDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[1], scout.keys[9])) {
    return;
  }
  if (this.drawHint) {
    this._drawKeyBox($container);
    drawedKeys[this.keyStrokeName()] = true;
    scout.keyStrokeBox.keyStrokeRangeDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[1], scout.keys[9]);
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype._drawKeyBox = function($container) {
  if (this.keyBoxDrawed) {
    return;
  }
  if (this._enabled && this._tabs) {
    for (var i = 1; i < this._tabs.length + 1; i++) {
      var offsetLeft = 4;
      if (i <= 9) {
        scout.keyStrokeBox.drawSingleKeyBoxItem(offsetLeft, i, this._tabs[i - 1].$container, this.ctrl, this.alt, this.shift, true);
      }
    }
    this.keyBoxDrawed = true;
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.ViewTabAutoKeyStroke.prototype.removeKeyBox = function() {
  if (!this.keyBoxDrawed) {
    return;
  }
  for (var i = 0; i < this._tabs.length; i++) {
    $('.key-box', this._tabs[i].$container).remove();
    $('.key-box-additional', this._tabs[i].$container).remove();
  }
  this.keyBoxDrawed = false;
};

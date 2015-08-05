scout.ViewTabAutoKeyStroke = function(desktop) {
  scout.ViewTabAutoKeyStroke.parent.call(this);

  this._enabled = desktop.autoTabKeyStrokesEnabled;
  this._viewTabs = desktop.viewTabsController.viewTabs();

  this.keyStroke = desktop.autoTabKeyStrokeModifier;
  this.$container = desktop._$viewTabBar; // TODO [dwi] just temporary fix
  this.initKeyStrokeParts();
  this.keyBoxDrawed = false;
  this.drawHint = true;
};
scout.inherits(scout.ViewTabAutoKeyStroke, scout.KeyStroke);

/**
 * @override Action.js
 */
scout.ViewTabAutoKeyStroke.prototype.handle = function(event) {
  if (this._viewTabs.length === 0 || (event.which !== 57 && event.which !== 49 && event.which - 49 > this._viewTabs.length)) {
    return;
  }
  this._viewTabs[event.which - 49].$container.trigger('click');
  event.preventDefault();
};

/**
 * @override Action.js
 */
scout.ViewTabAutoKeyStroke.prototype.accept = function(event) {
  if (this._enabled && event && event.which >= 49 && event.which <= 57 && // 1-9
    event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift) {
    return true;
  }
  return false;
};
/**
 * @override Action.js
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
 * @override Action.js
 */
scout.ViewTabAutoKeyStroke.prototype._drawKeyBox = function($container) {
  if (this.keyBoxDrawed) {
    return;
  }
  if (this._enabled && this._viewTabs) {
    for (var i = 1; i < this._viewTabs.length + 1; i++) {
      var offsetLeft = 4;
      if (i <= 9) {
        scout.keyStrokeBox.drawSingleKeyBoxItem(offsetLeft, i, this._viewTabs[i - 1].$container, this.ctrl, this.alt, this.shift, true);
      }
    }
    this.keyBoxDrawed = true;
  }
};
/**
 * @override Action.js
 */
scout.ViewTabAutoKeyStroke.prototype.removeKeyBox = function($container) {
  if (!this.keyBoxDrawed) {
    return;
  }
  for (var i = 0; i < this._viewTabs.length; i++) {
    $('.key-box', this._viewTabs[i].$container).remove();
    $('.key-box-additional', this._viewTabs[i].$container).remove();
  }
  this.keyBoxDrawed = false;
};

/**
 * Keystroke to open the 'ViewMenuPopup' on 'F2'.
 */
scout.ViewMenuOpenKeyStroke = function(desktopNavigation, keyStroke) {
  scout.ViewMenuOpenKeyStroke.parent.call(this);
  this._desktopNavigation = desktopNavigation;
  this.initKeyStrokeParts();
  this.keyBoxDrawed = false;
  this.drawHint = true;
};
scout.inherits(scout.ViewMenuOpenKeyStroke, scout.KeyStroke);

/**
 * @override Action.js
 */
scout.ViewMenuOpenKeyStroke.prototype.handle = function(event) {
  if (event && event.which === scout.keys.F2) {
    this._desktopNavigation.doViewMenuAction(event);
  }
  event.preventDefault();
};

/**
 * @override Action.js
 */
scout.ViewMenuOpenKeyStroke.prototype.accept = function(event) {
  if (event && (event.which === scout.keys.F2) &&
    event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift) {
    return true;
  }
  return false;
};

/**
 * @override Action.js
 */
scout.ViewMenuOpenKeyStroke.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  if (this.drawHint) {
    this._drawKeyBox($container, drawedKeys);
  }
};

scout.ViewMenuOpenKeyStroke.prototype._drawKeyBox = function($container, drawedKeys) {
  if (this.keyBoxDrawed) {
    return;
  }
  if (!drawedKeys.F2) {
    scout.keyStrokeBox.drawSingleKeyBoxItem(10, 'F2', this._desktopNavigation.viewMenuTab.$container, this.ctrl, this.alt, this.shift);
    drawedKeys.F2 = true;
    var $icon = this._desktopNavigation.viewMenuTab.$container.find('.icon');
    if($icon.length){
      var wIcon = $icon.width();
      var wKeybox = this._desktopNavigation.viewMenuTab.$container.find('.key-box').outerWidth();
      var containerPadding = Number(this._desktopNavigation.viewMenuTab.$container.css('padding-left').replace('px', ''));
      var leftKeyBox = wIcon/2 - wKeybox/2 + containerPadding;
      this._desktopNavigation.viewMenuTab.$container.find('.key-box').css('left', leftKeyBox+'px');
    }
  }
  this.keyBoxDrawed = true;
};

/**
 * @override Action.js
 */
scout.ViewMenuOpenKeyStroke.prototype.removeKeyBox = function() {
  if (!this.keyBoxDrawed) {
    return;
  }
  $('.key-box', this.$container).remove();
  $('.key-box-additional', this.$container).remove();
  this.keyBoxDrawed = false;
};

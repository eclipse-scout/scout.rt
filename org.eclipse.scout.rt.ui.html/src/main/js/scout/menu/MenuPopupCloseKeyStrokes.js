scout.MenuPopupCloseKeyStrokes = function(popup) {
  scout.MenuPopupCloseKeyStrokes.parent.call(this);
  this.drawHint = false;
  this._popup = popup;
  this.initKeyStrokeParts();
  this.bubbleUp = true;
};
scout.inherits(scout.MenuPopupCloseKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.MenuPopupCloseKeyStrokes.prototype.handle = function(event) {
  this._popup.remove();
  event.preventDefault();
};

/**
 * @Override scout.KeyStroke
 */
scout.MenuPopupCloseKeyStrokes.prototype.accept = function(event) {
  return event &&
    $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.SPACE, scout.keys.ENTER, scout.keys.F1, scout.keys.ESC]) === -1 &&
    event.ctrlKey === this.ctrl &&
    event.altKey === this.alt &&
    event.shiftKey === this.shift;
};

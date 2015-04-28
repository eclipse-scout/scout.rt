scout.PopupCloseKeyStrokes = function(popup) {
  scout.PopupCloseKeyStrokes.parent.call(this);
  this.drawHint = false;
  this._popup = popup;
  this.initKeyStrokeParts();
  this.bubbleUp = true;
};
scout.inherits(scout.PopupCloseKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.PopupCloseKeyStrokes.prototype.handle = function(event) {
  this._popup.remove();
};

/**
 * @Override scout.KeyStroke
 */
scout.PopupCloseKeyStrokes.prototype.accept = function(event) {
  return event &&
    $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.SPACE, scout.keys.ENTER, scout.keys.ESC, scout.keys.F1]) === -1 &&
    event.ctrlKey === this.ctrl &&
    event.altKey === this.alt &&
    event.shiftKey === this.shift;
};

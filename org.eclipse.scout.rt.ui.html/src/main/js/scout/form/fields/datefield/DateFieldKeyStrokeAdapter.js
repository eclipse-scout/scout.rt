scout.DateFieldKeyStrokeAdapter = function(dateField) {
  scout.DateFieldKeyStrokeAdapter.parent.call(this, dateField);
};
scout.inherits(scout.DateFieldKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

scout.DateFieldKeyStrokeAdapter.prototype.drawKeyBox = function(drawedKeys) {
    //add swallowed keys to drawed keys;
    var i = 0;
    for (i = 0; i < this.preventBubbleUpKeys.length; i++) {
      scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, false, false, false, this.preventBubbleUpKeys[i]);
    }
    for (i = 0; i < this.ctrlShiftPreventBubbleUpKeys.length; i++) {
      scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, true, false, true, this.ctrlShiftPreventBubbleUpKeys[i]);
    }
    for (i = 0; i < this.ctrlPreventBubbleUpKeys.length; i++) {
      scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, true, false, false, this.ctrlPreventBubbleUpKeys[i]);
    }
};

scout.DateFieldKeyStrokeAdapter.prototype.preventBubbleUp = function(event) {
  return this._isInputKeyStroke(event) ||
    this._isCtrlPrevent(event) ||
    this._isCtrlShiftPrevent(event);
};

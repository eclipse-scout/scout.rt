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
    if (event.altKey || (event.altKey && event.shiftKey)) {
      return false;
    }
    if (event.ctrlKey && event.shiftKey && this.ctrlShiftPreventBubbleUpKeys.indexOf(event.which) > -1) {
      return true;
    }
    if (event.ctrlKey && this.ctrlPreventBubbleUpKeys.indexOf(event.which) > -1) {
      //copy, paste, mark all, etc.
      return true;
    }
    if (this.preventBubbleUpKeys.indexOf(event.which) > -1 || (event.which >= scout.keys.A && event.which <= scout.keys.Z) || (event.which >= scout.keys[0] && event.which <= scout.keys[9])) {
      //all alphabetical chars, numbers, etc. which are captured by input.
      return true;
    }
    return false;
};

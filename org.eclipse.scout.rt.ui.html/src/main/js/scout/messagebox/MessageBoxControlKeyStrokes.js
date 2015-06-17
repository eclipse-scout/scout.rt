scout.MessageBoxControlKeyStrokes = function(field) {
  scout.MessageBoxControlKeyStrokes.parent.call(this);
  this.drawHint = false;
  this._field = field;
};
scout.inherits(scout.MessageBoxControlKeyStrokes, scout.KeyStroke);

scout.MessageBoxControlKeyStrokes.prototype.handle = function(event) {
  if (event.which === scout.keys.ESCAPE) {
    this._field.close();
    return;
  }

  var $container = this._field.$container;
  var $focusableElements = $container.find(':focusable');
  var $firstFocusableElement = $focusableElements.first();
  var $lastFocusableElement = $focusableElements.last();
  var activeElement = document.activeElement;
  if (event.which === scout.keys.RIGHT) {
    if (activeElement === $lastFocusableElement[0]) {
      $.suppressEvent(event);
      $firstFocusableElement.focus();
    } else {
      $focusableElements[$focusableElements.index(activeElement) + 1].focus();
    }
  } else if (event.which === scout.keys.LEFT) {
    if (activeElement === $firstFocusableElement[0]) {
      $.suppressEvent(event);
      $lastFocusableElement.focus();
    } else {
      $focusableElements[$focusableElements.index(activeElement) - 1].focus();
    }
  }
};

scout.MessageBoxControlKeyStrokes.prototype.accept = function(event) {
  if (this.ignore(event)) {
    return false;
  }
  if (event && event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift &&
      scout.helpers.isOneOf(event.which, scout.keys.LEFT, scout.keys.RIGHT, scout.keys.ESCAPE)) {
    return true;
  }
  return false;
};

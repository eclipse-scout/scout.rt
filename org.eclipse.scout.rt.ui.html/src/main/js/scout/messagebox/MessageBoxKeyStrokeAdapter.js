scout.MessageBoxKeyStrokeAdapter = function(field) {
  scout.MessageBoxKeyStrokeAdapter.parent.call(this, field);
  this._$container = field.$container;
  this.uiSessionId(field._session.uiSessionId);
  this.keyStrokes.push(new scout.BoxNavKeyStrokes(this._$container));
  this.anchorKeyStrokeAdapter = true;
};
scout.inherits(scout.MessageBoxKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.MessageBoxKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
};

scout.BoxNavKeyStrokes = function($container) {
  scout.BoxNavKeyStrokes.parent.call(this);
  this.drawHint = false;
  this._$container = $container;
};
scout.inherits(scout.BoxNavKeyStrokes, scout.KeyStroke);

scout.BoxNavKeyStrokes.prototype.handle = function(event) {
  var $focusableElements = this._$container.find(':focusable');
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

scout.BoxNavKeyStrokes.prototype.accept = function(event) {
  if (this.ignore(event)) {
    return false;
  }
  if (event && event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift && (event.which === scout.keys.LEFT || event.which === scout.keys.RIGHT)) {
    return true;
  }
  return false;
};

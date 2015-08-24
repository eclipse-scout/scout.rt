scout.FocusAdjacentElementKeyStroke = function(session, field) {
  scout.FocusAdjacentElementKeyStroke.parent.call(this);
  this.session = session;
  this.field = field;
  this.which = [scout.keys.LEFT, scout.keys.RIGHT];
  this.renderingHints.render = false;
  this.stopPropagation = true;
  this.keyStrokeMode = scout.keyStrokeMode.DOWN;
};
scout.inherits(scout.FocusAdjacentElementKeyStroke, scout.KeyStroke);

scout.FocusAdjacentElementKeyStroke.prototype.handle = function(event) {
  var activeElement = document.activeElement,
    $focusableElements = this.field.$container.find(':focusable');

  switch (event.which) {
    case scout.keys.RIGHT:
      if (activeElement === $focusableElements.last()[0]) {
        this.session.focusManager.requestFocus($focusableElements.first());
      } else {
        this.session.focusManager.requestFocus($focusableElements[$focusableElements.index(activeElement) + 1]);
      }

      break;
    case scout.keys.LEFT:
      if (activeElement === $focusableElements.first()[0]) {
        this.session.focusManager.requestFocus($focusableElements.last());
      } else {
        this.session.focusManager.requestFocus($focusableElements[$focusableElements.index(activeElement) - 1]);
      }
      break;
  }
};

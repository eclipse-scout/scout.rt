scout.ClickActiveElementKeyStroke = function(field, which) {
  scout.ClickActiveElementKeyStroke.parent.call(this);
  this.field = field;
  this.which = which;
  this.stopPropagation = true;
  this.renderingHints.render = true;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event._$activeElement;
  };
};
scout.inherits(scout.ClickActiveElementKeyStroke, scout.KeyStroke);

scout.ClickActiveElementKeyStroke.prototype._accept = function(event) {
  var accepted = scout.ClickActiveElementKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  event._$activeElement = $(document.activeElement);
  return true;
};

/**
 * @override KeyStroke.js
 */
scout.ClickActiveElementKeyStroke.prototype.handle = function(event) {
  event._$activeElement.trigger({
    type: 'click',
    which: 1
  });
};

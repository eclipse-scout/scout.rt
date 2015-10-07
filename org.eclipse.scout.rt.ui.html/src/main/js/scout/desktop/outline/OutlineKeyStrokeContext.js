scout.OutlineKeyStrokeContext = function(outline) {
  scout.OutlineKeyStrokeContext.parent.call(this);
  this._outline = outline;
};

scout.inherits(scout.OutlineKeyStrokeContext, scout.KeyStrokeContext);

/**
 * Returns true if this event is handled by this context, and if so sets the propagation flags accordingly.
 */
scout.OutlineKeyStrokeContext.prototype.accept = function(event) {
  return !this._outline.inBackground && !this.isFormToolOpened() && scout.OutlineKeyStrokeContext.parent.prototype.accept.call(this, event);
};

scout.OutlineKeyStrokeContext.prototype.isFormToolOpened = function() {
  var popupButtons = this._outline.session.desktop.actions;
  return popupButtons.some(function(button) {
    return button.popup && button.popup.$container && button.popup.$container.isAttached();
  }, this);
};

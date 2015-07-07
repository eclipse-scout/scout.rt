/**
 * Controller with functionality to register and render message boxes.
 *
 * The message boxes are put into the list 'messageBoxes' contained in 'displayParent'.
 */
scout.MessageBoxController = function(displayParent, session) {
  this._displayParent = displayParent;
  this.session = session;
};

/**
 * Adds the given message box to this controller and renders it.
 */
scout.MessageBoxController.prototype.registerAndRender = function(messageBoxAdapterId) {
  var messageBox = this.session.getOrCreateModelAdapter(messageBoxAdapterId, this._displayParent);

  this._displayParent.messageBoxes.push(messageBox);
  this._render(messageBox);
};

/**
 * Removes the given message box from this controller and DOM. However, the message box's adapter is not destroyed. That only happens once the message box is closed.
 */
scout.MessageBoxController.prototype.unregisterAndRemove = function(messageBoxAdapterId) {
  var messageBox = this.session.getOrCreateModelAdapter(messageBoxAdapterId, this._displayParent);

  scout.arrays.remove(this._displayParent.messageBoxes, messageBox);
  this._remove(messageBox);
};

/**
 * Removes all message boxes registered with this controller from DOM.
 */
scout.MessageBoxController.prototype.remove = function() {
  this._displayParent.messageBoxes.forEach(this._remove.bind(this));
};

/**
 * Renders all message boxes registered with this controller.
 */
scout.MessageBoxController.prototype.render = function() {
  this._displayParent.messageBoxes.forEach(this._render.bind(this));
};

scout.MessageBoxController.prototype._render = function(messageBox) {
  messageBox.render(this.session.desktop.$container);
};

scout.MessageBoxController.prototype._remove = function(messageBox) {
  messageBox.remove();
};

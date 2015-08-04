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
  var messageBox = this.session.getModelAdapter(messageBoxAdapterId);
  if (messageBox) {
    scout.arrays.remove(this._displayParent.messageBoxes, messageBox);
    this._remove(messageBox);
  }
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
  // Only render message box if 'displayParent' is rendered yet; if not, the message box will be rendered once 'displayParent' is rendered.
  if (!this._displayParent.rendered) {
    return;
  }

  messageBox.render(this.session.desktop.$container);

  // Only display the message box if its 'displayParent' is visible to the user.
  if (!this._displayParent.inFront()) {
    messageBox.detach();
  }
};

scout.MessageBoxController.prototype._remove = function(messageBox) {
  messageBox.remove();
};

/**
 * Attaches all message boxes to their original DOM parents.
 * In contrast to 'render', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already attached.
 */
scout.MessageBoxController.prototype.attach = function() {
  this._displayParent.messageBoxes.forEach(function(messageBox) {
    messageBox.attach();
  }, this);
};

/**
 * Detaches all message boxes from their DOM parents. Thereby, modality glassPanes are not detached.
 * In contrast to 'remove', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already detached.
 */
scout.MessageBoxController.prototype.detach = function() {
  this._displayParent.messageBoxes.forEach(function(messageBox) {
    messageBox.detach();
  }, this);
};

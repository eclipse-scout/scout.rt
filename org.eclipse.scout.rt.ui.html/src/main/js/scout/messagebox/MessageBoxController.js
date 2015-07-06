/**
 * The MessageBoxController provides functionality to show message boxes.
 */
scout.MessageBoxController = function(parent, session, funcMessageBoxStore) {
  this._parent = parent;
  this.session = session;
  this._funcMessageBoxStore = funcMessageBoxStore;
};

/**
 * Adds the given message box to the 'messageBoxStore' and DOM.
 */
scout.MessageBoxController.prototype.addAndShow = function(messageBoxAdapterId) {
  var messageBox = this.session.getOrCreateModelAdapter(messageBoxAdapterId, this._parent);

  this._funcMessageBoxStore().push(messageBox);
  this._showMessageBox(messageBox);
};

/**
 * Removes the given message box from the 'messageBoxStore' and DOM. However, the message-box's adapter is not destroyed. That only happens once the message-box is closed.
 */
scout.MessageBoxController.prototype.removeAndHide = function(messageBoxAdapterId) {
  var messageBox = this.session.getOrCreateModelAdapter(messageBoxAdapterId, this._parent);

  scout.arrays.remove(this._funcMessageBoxStore(), messageBox);
  this._hideMessageBox(messageBox);
};

/**
 * Adds all message boxes contained in 'messageBoxStore' into DOM.
 */
scout.MessageBoxController.prototype.showAll = function() {
  this._funcMessageBoxStore().forEach(this._showMessageBox.bind(this));
};

scout.MessageBoxController.prototype._showMessageBox = function(messageBox) {
  messageBox.render(this.session.desktop.$container);
};

scout.MessageBoxController.prototype._hideMessageBox = function(messageBox) {
  messageBox.remove();
};
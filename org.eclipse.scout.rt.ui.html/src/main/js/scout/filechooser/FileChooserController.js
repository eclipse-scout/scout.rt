/**
 * The FileChooserController provides functionality to manage file choosers.
 */
scout.FileChooserController = function(parent, session, funcFileChooserStore) {
  this._parent = parent;
  this.session = session;
  this._funcFileChooserStore = funcFileChooserStore;
};

/**
 * Adds the given file chooser to the 'fileChooserStore' and DOM.
 */
scout.FileChooserController.prototype.addAndShow = function(fileChooserAdapterId) {
  var fileChooser = this.session.getOrCreateModelAdapter(fileChooserAdapterId, this._parent);

  this._funcFileChooserStore().push(fileChooser);
  this._showFileChooser(fileChooser);
};

/**
 * Removes the given file chooser from the 'fileChooserStore' and DOM. However, the file chooser's adapter is not destroyed. That only happens once the file chooser is closed.
 */
scout.FileChooserController.prototype.removeAndHide = function(fileChooserAdapterId) {
  var fileChooser = this.session.getOrCreateModelAdapter(fileChooserAdapterId, this._parent);

  scout.arrays.remove(this._funcFileChooserStore(), fileChooser);
  this._hideFileChooser(fileChooser);
};

/**
 * Adds all file choosers contained in 'fileChooserStore' into DOM.
 */
scout.FileChooserController.prototype.showAll = function() {
  this._funcFileChooserStore().forEach(this._showFileChooser.bind(this));
};

scout.FileChooserController.prototype._showFileChooser = function(fileChooser) {
  fileChooser.render(this.session.desktop.$container);
};

scout.FileChooserController.prototype._hideFileChooser = function(fileChooser) {
  fileChooser.remove();
};

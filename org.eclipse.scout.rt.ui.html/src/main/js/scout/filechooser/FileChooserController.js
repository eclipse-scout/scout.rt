/**
 * Controller with functionality to register and render file choosers.
 *
* The file choosers are put into the list fileChoosers contained in 'displayParent'.
 */
scout.FileChooserController = function(displayParent, session) {
  this._displayParent = displayParent;
  this.session = session;
};

/**
 * Adds the given file chooser to this controller and renders it.
 */
scout.FileChooserController.prototype.registerAndRender = function(fileChooserAdapterId) {
  var fileChooser = this.session.getOrCreateModelAdapter(fileChooserAdapterId, this._displayParent);

  this._displayParent.fileChoosers.push(fileChooser);
  this._render(fileChooser);
};

/**
 * Removes the given file chooser from this controller and DOM. However, the file chooser's adapter is not destroyed. That only happens once the file chooser is closed.
 */
scout.FileChooserController.prototype.unregisterAndRemove = function(fileChooserAdapterId) {
  var fileChooser = this.session.getOrCreateModelAdapter(fileChooserAdapterId, this._displayParent);

  scout.arrays.remove(this._displayParent.fileChoosers, fileChooser);
  this._remove(fileChooser);
};

/**
 * Removes all file choosers registered with this controller from DOM.
 */
scout.FileChooserController.prototype.remove = function() {
  this._displayParent.fileChoosers.forEach(this._remove.bind(this));
};

/**
 * Renders all file choosers registered with this controller.
 */
scout.FileChooserController.prototype.render = function() {
  this._displayParent.fileChoosers.forEach(this._render.bind(this));
};

scout.FileChooserController.prototype._render = function(fileChooser) {
  fileChooser.render(this.session.desktop.$container);
};

scout.FileChooserController.prototype._remove = function(fileChooser) {
  fileChooser.remove();
};

/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Controller with functionality to register and render file choosers.
 *
 * The file choosers are put into the list fileChoosers contained in 'displayParent'.
 */
scout.FileChooserController = function(displayParent, session) {
  this.displayParent = displayParent;
  this.session = session;
};

/**
 * Adds the given file chooser to this controller and renders it.
 */
scout.FileChooserController.prototype.registerAndRender = function(fileChooser) {
  fileChooser._setProperty('displayParent', this.displayParent);
  this.displayParent.fileChoosers.push(fileChooser);
  this._render(fileChooser);
};

/**
 * Removes the given file chooser from this controller and DOM. However, the file chooser's adapter is not destroyed. That only happens once the file chooser is closed.
 */
scout.FileChooserController.prototype.unregisterAndRemove = function(fileChooser) {
  if (fileChooser) {
    scout.arrays.remove(this.displayParent.fileChoosers, fileChooser);
    this._remove(fileChooser);
  }
};

/**
 * Removes all file choosers registered with this controller from DOM.
 */
scout.FileChooserController.prototype.remove = function() {
  this.displayParent.fileChoosers.forEach(this._remove.bind(this));
};

/**
 * Renders all file choosers registered with this controller.
 */
scout.FileChooserController.prototype.render = function() {
  this.displayParent.fileChoosers.forEach(this._render.bind(this));
};

scout.FileChooserController.prototype._render = function(fileChooser) {
  // Only render file chooser if 'displayParent' is rendered yet; if not, the file chooser will be rendered once 'displayParent' is rendered.
  if (!this.displayParent.rendered) {
    return;
  }
  // Prevent "Already rendered" errors / FIXME bsh, dwi: Remove this hack! Fix in on model if possible. See #162954.
  if (fileChooser.rendered) {
    return;
  }

  fileChooser.render(this.session.desktop.$container);

  // Only display the file chooser if its 'displayParent' is visible to the user.
  if (!this.displayParent.inFront()) {
    fileChooser.detach();
  }
};

scout.FileChooserController.prototype._remove = function(fileChooser) {
  fileChooser.remove();
};

/**
 * Attaches all file choosers to their original DOM parents.
 * In contrast to 'render', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already attached.
 */
scout.FileChooserController.prototype.attach = function() {
  this.displayParent.fileChoosers.forEach(function(fileChooser) {
    fileChooser.attach();
  }, this);
};

/**
 * Detaches all file choosers from their DOM parents. Thereby, modality glassPanes are not detached.
 * In contrast to 'remove', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already detached.
 */
scout.FileChooserController.prototype.detach = function() {
  this.displayParent.fileChoosers.forEach(function(fileChooser) {
    fileChooser.detach();
  }, this);
};

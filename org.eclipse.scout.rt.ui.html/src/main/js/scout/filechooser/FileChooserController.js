/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
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
  scout.assertProperty(fileChooser, 'displayParent');
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
  if (this.displayParent.inFront()) {
    this.displayParent.fileChoosers.forEach(function(chooser) {
      chooser.setDisplayParent(this.displayParent);
      this._render(chooser);
    }.bind(this));
  }
};

scout.FileChooserController.prototype._render = function(fileChooser) {
  // Use parent's function or (if not implemented) our own.
  if (this.displayParent.acceptView) {
    if (!this.displayParent.acceptView(fileChooser)) {
      return;
    }
  } else if (!this.acceptView(fileChooser)) {
    return;
  }
  // Prevent "Already rendered" errors --> TODO [7.0] bsh: Remove this hack! Fix it on model if possible. See #162954.
  if (fileChooser.rendered) {
    return;
  }
  // Open all file choosers in the center of the desktop, except the ones that belong to a popup-window
  // Since the file chooser doesn't have a DOM element as parent when render is called, we must find the
  // entryPoint by using the model.
  var $parent;
  if (this.displayParent instanceof scout.Form && this.displayParent.isPopupWindow()) {
    $parent = this.displayParent.popupWindow.$container;
  } else {
    $parent = this.session.desktop.$container;
  }
  fileChooser.render($parent);
};

scout.FileChooserController.prototype._remove = function(fileChooser) {
  fileChooser.remove();
};

scout.FileChooserController.prototype.acceptView = function(view) {
  return this.displayParent.rendered;
};

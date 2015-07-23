/**
 * Controller with functionality to register and render views and dialogs.
 *
 * The forms are put into the list 'views' and 'dialogs' contained in 'displayParent'.
 */
scout.FormController = function(displayParent, session) {
  this._displayParent = displayParent;
  this.session = session;
};

/**
 * Adds the given view or dialog to this controller and renders it.
 */
scout.FormController.prototype.registerAndRender = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._displayParent);

  if (form.displayHint === 'view') {
    this._renderView(form, true);
  } else {
    this._renderDialog(form, true);
  }
};

/**
 * Removes the given view or dialog from this controller and DOM. However, the form's adapter is not destroyed. That only happens once the Form is closed.
 */
scout.FormController.prototype.unregisterAndRemove = function(formAdapterId) {
  var form = this.session.getModelAdapter(formAdapterId);
  if (!form) {
    return;
  }

  if (form.displayHint === 'view') {
    this._removeView(form);
  } else {
    this._removeDialog(form);
  }
};

/**
 * Renders all dialogs and views registered with this controller.
 */
scout.FormController.prototype.render = function() {
  this._displayParent.dialogs.forEach(this._renderDialog.bind(this));
  this._displayParent.views.forEach(this._renderView.bind(this));
};

/**
 * Activates the given view or dialog.
 */
scout.FormController.prototype.activateForm = function(formAdapterId) {
  var form = this.session.getOrCreateModelAdapter(formAdapterId, this._displayParent);

  if (form.displayHint === 'view') {
    this._activateView(form);
  } else {
    this._activateDialog(form);
  }
};

scout.FormController.prototype._renderView = function(view, register) {
  if (register) {
    this._displayParent.views.push(view);
  }

  // Only render view if 'displayParent' is rendered yet; if not, the view will be rendered once 'displayParent' is rendered.
  if (!this._displayParent.rendered) {
    return;
  }

  var viewTabsController = this.session.desktop.viewTabsController;

  // Create the view-tab.
  var viewTab = viewTabsController.createAndRenderViewTab(view);
  viewTabsController.selectViewTab(viewTab);
};

scout.FormController.prototype._renderDialog = function(dialog, register) {
  if (register) {
    this._displayParent.dialogs.push(dialog);
  }

  // Only render dialog if 'displayParent' is rendered yet; if not, the dialog will be rendered once 'displayParent' is rendered.
  if (!this._displayParent.rendered) {
    return;
  }

  dialog.render(this.session.desktop.$container);

  this._layoutDialog(dialog);

  // Only display the dialog if its 'displayParent' is visible to the user.
  if (!this._displayParent.inFront()) {
    dialog.detach();
  }
};

scout.FormController.prototype._removeView = function(view) {
  scout.arrays.remove(this._displayParent.views, view);

  if (view.rendered) {
    view.remove();
  }
};

scout.FormController.prototype._removeDialog = function(dialog) {
  scout.arrays.remove(this._displayParent.dialogs, dialog);

  if (dialog.rendered) {
    dialog.remove();
  }
};

scout.FormController.prototype._activateView = function(view) {
  var viewTabsController = this.session.desktop.viewTabsController;

  var viewTab = viewTabsController.viewTab(view);
  viewTabsController.selectViewTab(viewTab);
};

scout.FormController.prototype._activateDialog = function(dialog) {
  // FIXME AWE: not implemented yet.
};

/**
 * Attaches all dialogs to their original DOM parents.
 * In contrast to 'render', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already attached.
 */
scout.FormController.prototype.attachDialogs = function() {
  this._displayParent.dialogs.forEach(function(dialog) {
    dialog.attach();
  }, this);
};

/**
 * Detaches all dialogs from their DOM parents. Thereby, modality glassPanes are not detached.
 * In contrast to 'remove', this method uses 'JQuery detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *
 * This method has no effect if already detached.
 */
scout.FormController.prototype.detachDialogs = function() {
  this._displayParent.dialogs.forEach(function(dialog) {
    dialog.detach();
  }, this);
};

scout.FormController.prototype._layoutDialog = function(dialog) {
  var left, top, opticalMiddleOffset, dialogSize,
    $document = $(document),
    documentSize = new scout.Dimension($document.width(), $document.height());

  dialog.htmlComp.pixelBasedSizing = true;
  dialog.htmlComp.validateLayout();

  dialogSize = dialog.htmlComp.getSize();
  left = (documentSize.width - dialogSize.width) / 2;
  top = (documentSize.height - dialogSize.height) / 2;

  // optical middle
  opticalMiddleOffset = Math.min(top / 5, 10);
  top -= opticalMiddleOffset;

  dialog.$container
    .cssLeft(left)
    .cssTop(top);
};
